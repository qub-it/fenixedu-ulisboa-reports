package org.fenixedu.ulisboa.reports.ui.reports.competencecoursemarksheet;

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
import org.fenixedu.ulisboa.reports.dto.report.competencecoursemarksheet.CompetenceCourseMarkSheetReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheet.CompetenceCourseMarkSheetReport;
import org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheet.CompetenceCourseMarkSheetReportService;
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

@SpringFunctionality(app = FenixeduULisboaReportsController.class, title = "label.org.fenixedu.ulisboa.reports.title.competenceCourseMarkSheet", accessGroup = "logged")
@RequestMapping(CompetenceCourseMarkSheetReportController.CONTROLLER_URL)
public class CompetenceCourseMarkSheetReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL =
            "/fenixedu-ulisboa-reports/reports/competencecoursemarksheet/competencecoursemarksheetreport";
    private static final String JSP_PATH = CONTROLLER_URL.substring(1);
    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;

    static private String getReportId(final String exportName) {
        return normalizeName(bundle("competenceCourseMarkSheet.event." + exportName), "_") + "_UUID_" + UUID.randomUUID()
                .toString();
    }

    static private String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    static private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
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

    static private Collection<CompetenceCourseMarkSheetReport> generateReport(
            final CompetenceCourseMarkSheetReportParametersBean bean) {
        final CompetenceCourseMarkSheetReportService service = new CompetenceCourseMarkSheetReportService();
        service.filterEnrolmentExecutionSemester(bean.getExecutionSemester());

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    private void setParametersBean(CompetenceCourseMarkSheetReportParametersBean bean, Model model) {
        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(Model model, RedirectAttributes redirectAttributes) {
        setParametersBean(new CompetenceCourseMarkSheetReportParametersBean(), model);
        return jspPage("competencecoursemarksheetreport");
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") CompetenceCourseMarkSheetReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setParametersBean(bean, model);
        return jspPage("competencecoursemarksheetreport");
    }

    @RequestMapping(value = "/exportreport", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> exportReport(
            @RequestParam(value = "bean", required = false) final CompetenceCourseMarkSheetReportParametersBean bean,
            final Model model) {
        final String reportId = getReportId("exportReport");
        new Thread(() -> processReport(this::exportToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    @Atomic(mode = Atomic.TxMode.READ)
    protected void processReport(final Function<CompetenceCourseMarkSheetReportParametersBean, byte[]> reportProcessor,
            final CompetenceCourseMarkSheetReportParametersBean bean, final String reportId) {

        byte[] content = null;
        try {
            content = reportProcessor.apply(bean);
        } catch (final Throwable e) {
            content = createXLSWithError(e instanceof ULisboaReportsDomainException ?
                                         ((ULisboaReportsDomainException) e).getLocalizedMessage() :
                                         ExceptionUtils.getFullStackTrace(e));
        }

        ULisboaSpecificationsTemporaryFile.create(reportId, content, Authenticate.getUser());
    }

    private byte[] createXLSWithError(String error) {
        try {

            final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
            builder.addSheet(ULisboaReportsUtil.bundle("competenceCourseMarkSheet.competenceCourseMarkSheet"), new SheetData<String>(Collections
                    .singleton(error)) {
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

    @RequestMapping(value = "/exportstatus/{reportId}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> exportStatus(@PathVariable(value = "reportId") final String reportId,
            final Model model) {
        return new ResponseEntity<String>(String.valueOf(ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate
                .getUser(), reportId).isPresent()), HttpStatus.OK);
    }

    @RequestMapping(value = "/downloadreport/{reportId}", method = RequestMethod.GET)
    public void downloadReport(@PathVariable("reportId") String reportId, final Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) throws IOException {
        final Optional<ULisboaSpecificationsTemporaryFile> temporaryFile =
                ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate.getUser(), reportId);
        writeFile(response, getFilename(reportId) + "_" + new DateTime().toString("yyyy-MM-dd_HH-mm-ss") + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", temporaryFile
                .get()
                .getContent());
    }

    private byte[] exportToXLS(final CompetenceCourseMarkSheetReportParametersBean bean) {
        final Collection<CompetenceCourseMarkSheetReport> toExport = generateReport(bean);

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(bundle("competenceCourseMarkSheet.competenceCourseMarkSheet"), new SheetData<CompetenceCourseMarkSheetReport>(toExport) {

            @Override
            protected void makeLine(final CompetenceCourseMarkSheetReport report) {
                addCompetenceCourseMarkSheetData(report);
            }

            private void addCompetenceCourseMarkSheetData(final CompetenceCourseMarkSheetReport report) {
                addData("CompetenceCourseMarkSheetReport.courseName", report.getCompetenceCourseName());
                addData("CompetenceCourseMarkSheetReport.courseCode", report.getCompetenceCourseCode());
                addData("CompetenceCourseMarkSheetReport.executionPresentation", report.getExecutionPresentation());
                addData("CompetenceCourseMarkSheetReport.evaluationSeason", report.getEvaluationSeason());
                addData("CompetenceCourseMarkSheetReport.creationDate", report.getCreationDate());
                addData("CompetenceCourseMarkSheetReport.executionSemester", report.getExecutionSemester());
                addData("CompetenceCourseMarkSheetReport.checksum", report.getCheckSum());
                addData("CompetenceCourseMarkSheetReport.evaluationDate", report.getEvaluationDate());
                addData("CompetenceCourseMarkSheetReport.submissionDate", report.getLastSubmissionDate());
                addData("CompetenceCourseMarkSheetReport.certifier", report.getCertifier());
                addData("CompetenceCourseMarkSheetReport.state", report.getLastState());
                addData("CompetenceCourseMarkSheetReport.printed", report.getPrintStatus());
                addData("CompetenceCourseMarkSheetReport.pendingChange", report.getLastPendingChange());
            }

            private void addData(final String key, final Object value) {
                addCell(bundle(key), value == null ? "" : value);
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

    @RequestMapping(value = _POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> postback(
            @RequestParam(value = "bean", required = false) final CompetenceCourseMarkSheetReportParametersBean bean,
            final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }
}