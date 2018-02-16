package org.fenixedu.ulisboa.reports.ui.reports.scholarshiptypology;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilderForXLSX;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.reports.dto.report.scholarshiptypology.ScholarshipTypologyReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.scholarshiptypology.ScholarshipTypologyReport;
import org.fenixedu.ulisboa.reports.services.report.scholarshiptypology.ScholarshipTypologyReportService;
import org.fenixedu.ulisboa.reports.ui.FenixeduULisboaReportsBaseController;
import org.fenixedu.ulisboa.reports.ui.FenixeduULisboaReportsController;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;
import org.fenixedu.ulisboa.specifications.domain.file.ULisboaSpecificationsTemporaryFile;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@SpringFunctionality(app = FenixeduULisboaReportsController.class,
        title = "label.org.fenixedu.ulisboa.reports.title.scholarshipTypology", accessGroup = "logged")
@RequestMapping(ScholarshipTypologyReportController.CONTROLLER_URL)
public class ScholarshipTypologyReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL = "/fenixedu-ulisboa-reports/reports/scholarshiptypology/scholarshiptypologyreport";

    private static final String JSP_PATH = CONTROLLER_URL.substring(1);

    private void setParametersBean(ScholarshipTypologyReportParametersBean bean, Model model) {
        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(Model model, RedirectAttributes redirectAttributes) {
        setParametersBean(new ScholarshipTypologyReportParametersBean(), model);
        return jspPage("scholarshiptypologyreport");
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") ScholarshipTypologyReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setParametersBean(bean, model);

        setResults(generateReport(bean), model);

        return jspPage("scholarshiptypologyreport");
    }

    private void setResults(Collection<ScholarshipTypologyReport> results, Model model) {
        model.addAttribute("results", results);
    }

    static private String getReportId(final String exportName) {
        return normalizeName(bundle("scholarshipTypology.event." + exportName), "_") + "_UUID_" + UUID.randomUUID().toString();
    }

    static private String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    static public String normalizeName(final String input, final String replacement) {
        // ex [ ] * ? : / \
        String result = Normalizer.normalize(input, java.text.Normalizer.Form.NFD)

                .replaceAll("[^\\p{ASCII}]", "")

                .replace(" ", replacement)

                .replace("[", replacement)

                .replace("]", replacement)

                .replace("*", replacement)

                .replace("?", replacement)

                .replace(":", replacement)

                .replace("/", replacement)

                .replace("\\", replacement);

        while (result.contains(replacement + replacement)) {
            result = result.replace(replacement + replacement, replacement);
        }

        return result.trim();
    }

    static private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }

    @RequestMapping(value = "/exportreport", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportReport(
            @RequestParam(value = "bean", required = false) final ScholarshipTypologyReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportReport");
        new Thread(() -> processReport(this::exportToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    @Atomic(mode = TxMode.READ)
    protected void processReport(final Function<ScholarshipTypologyReportParametersBean, byte[]> reportProcessor,
            final ScholarshipTypologyReportParametersBean bean, final String reportId) {

        byte[] content = null;
        try {
            content = reportProcessor.apply(bean);
        } catch (final Throwable e) {
            content = createXLSWithError(e instanceof ULisboaReportsDomainException ? ((ULisboaReportsDomainException) e)
                    .getLocalizedMessage() : ExceptionUtils.getFullStackTrace(e));
        }

        ULisboaSpecificationsTemporaryFile.create(reportId, content, Authenticate.getUser());
    }

    @RequestMapping(value = "/exportstatus/{reportId}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportStatus(@PathVariable(value = "reportId") final String reportId,
            final Model model) {
        return new ResponseEntity<String>(
                String.valueOf(
                        ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate.getUser(), reportId).isPresent()),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/downloadreport/{reportId}", method = RequestMethod.GET)
    public void downloadReport(@PathVariable("reportId") String reportId, final Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) throws IOException {
        final Optional<ULisboaSpecificationsTemporaryFile> temporaryFile =
                ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate.getUser(), reportId);
        writeFile(response, getFilename(reportId) + "_" + new DateTime().toString("yyyy-MM-dd_HH-mm-ss") + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", temporaryFile.get().getContent());
    }

    static private Collection<ScholarshipTypologyReport> generateReport(final ScholarshipTypologyReportParametersBean bean) {

        final ScholarshipTypologyReportService service = new ScholarshipTypologyReportService();
        service.filterEnrolmentExecutionYear(bean.getExecutionYear());

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    private byte[] exportToXLS(final ScholarshipTypologyReportParametersBean bean) {
        final Collection<ScholarshipTypologyReport> toExport = generateReport(bean);

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("scholarshipTypology.scholarshipTypology"),
                new SheetData<ScholarshipTypologyReport>(toExport) {

                    @Override
                    protected void makeLine(final ScholarshipTypologyReport report) {
                        addPrimaryData(report);
                        addComplementarPersonalData(bean, report);
                    }

                    private void addPrimaryData(final ScholarshipTypologyReport report) {
                        addData("ScholarshipTypologyReport.executionYear", report.getExecutionYear().getQualifiedName());
                        addData("ScholarshipTypologyReport.studentNumber", report.getStudentNumber());
                        addData("ScholarshipTypologyReport.personName", report.getPersonName());
                        addData("ScholarshipTypologyReport.degreeMinistryCode", report.getDegreeCode());
                    }

                    private void addComplementarPersonalData(final ScholarshipTypologyReportParametersBean bean,
                            final ScholarshipTypologyReport report) {
                        addData("ScholarshipTypologyReport.professionalCondition", getProfessionalCondition(report));
                        addData("ScholarshipTypologyReport.professionType", getProfessionalType(report));
                        addData("ScholarshipTypologyReport.professionTimeType", getProfessionTimeType(report));
                        addData("ScholarshipTypologyReport.grantOwnerType", getGrantOwnerType(report));
                        addData("ScholarshipTypologyReport.grantOwnerProvider", getGrantOwnerProvider(report));
                        addData("ScholarshipTypologyReport.motherSchoolLevel", getMotherSchoolLevel(report));
                        addData("ScholarshipTypologyReport.motherProfessionType", getMotherProfessionType(report));
                        addData("ScholarshipTypologyReport.motherProfessionalCondition", getMotherProfessionalCondition(report));
                        addData("ScholarshipTypologyReport.fatherSchoolLevel", getFatherSchoolLevel(report));
                        addData("ScholarshipTypologyReport.fatherProfessionType", getFatherProfessionType(report));
                        addData("ScholarshipTypologyReport.fatherProfessionalCondition", getFatherProfessionalCondition(report));
                        addData("ScholarshipTypologyReport.householdSalarySpan", getHouseholdSalarySpan(report));
                    }

                    private String getProfessionalCondition(ScholarshipTypologyReport report) {
                        return report.getProfessionalCondition() != null ? report.getProfessionalCondition()
                                .getLocalizedName() : null;
                    }

                    private String getProfessionalType(ScholarshipTypologyReport report) {
                        return report.getProfessionalType() != null ? report.getProfessionalType().getLocalizedName() : null;
                    }

                    private String getProfessionTimeType(ScholarshipTypologyReport report) {
                        return report.getProfessionTimeType() != null ? report.getProfessionTimeType().getLocalizedName() : null;
                    }

                    private String getGrantOwnerType(ScholarshipTypologyReport report) {
                        return report.getGrantOwnerType() != null ? ULisboaReportsUtil
                                .bundle("GrantOwnerType." + report.getGrantOwnerType()) : null;
                    }

                    private String getGrantOwnerProvider(ScholarshipTypologyReport report) {
                        return report.getGrantOwnerProvider() != null ? report.getGrantOwnerProvider().getNameI18n()
                                .getContent() : null;
                    }

                    private String getMotherSchoolLevel(ScholarshipTypologyReport report) {
                        return report.getMotherSchoolLevel() != null ? report.getMotherSchoolLevel().getLocalizedName() : null;
                    }

                    private String getMotherProfessionType(ScholarshipTypologyReport report) {
                        return report.getMotherProfessionType() != null ? report.getMotherProfessionType()
                                .getLocalizedName() : null;
                    }

                    private String getMotherProfessionalCondition(ScholarshipTypologyReport report) {
                        return report.getMotherProfessionalCondition() != null ? report.getMotherProfessionalCondition()
                                .getLocalizedName() : null;
                    }

                    private String getFatherSchoolLevel(ScholarshipTypologyReport report) {
                        return report.getFatherSchoolLevel() != null ? report.getFatherSchoolLevel().getLocalizedName() : null;
                    }

                    private String getFatherProfessionType(ScholarshipTypologyReport report) {
                        return report.getFatherProfessionType() != null ? report.getFatherProfessionType()
                                .getLocalizedName() : null;
                    }

                    private String getFatherProfessionalCondition(ScholarshipTypologyReport report) {
                        return report.getFatherProfessionalCondition() != null ? report.getFatherProfessionalCondition()
                                .getLocalizedName() : null;
                    }

                    private String getHouseholdSalarySpan(ScholarshipTypologyReport report) {
                        return report.getHouseholdSalarySpan() != null ? report.getHouseholdSalarySpan()
                                .getLocalizedName() : null;
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle(key), value == null ? "" : value);
                    }

                    private String booleanString(final boolean value) {
                        return value ? bundle("yes") : bundle("no");
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    private byte[] createXLSWithError(String error) {

        try {

            final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
            builder.addSheet(ULisboaReportsUtil.bundle("scholarshipTypology.scholarshipTypology"),
                    new SheetData<String>(Collections.singleton(error)) {
                        @Override
                        protected void makeLine(final String item) {
                            addCell(ULisboaReportsUtil.bundle("unexpected.error.occured"), item);
                        }
                    });

            final ByteArrayOutputStream result = new ByteArrayOutputStream();
            builder.build(result);

            return result.toByteArray();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;

    @RequestMapping(value = _POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> postback(
            @RequestParam(value = "bean", required = false) final ScholarshipTypologyReportParametersBean bean,
            final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
