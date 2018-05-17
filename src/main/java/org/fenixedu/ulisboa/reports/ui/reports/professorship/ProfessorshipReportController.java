package org.fenixedu.ulisboa.reports.ui.reports.professorship;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.academic.domain.ShiftProfessorship;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilderForXLSX;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.reports.dto.report.professorship.ProfessorshipReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.professorship.ProfessorshipDegreeReport;
import org.fenixedu.ulisboa.reports.services.report.professorship.ProfessorshipDegreeReportService;
import org.fenixedu.ulisboa.reports.services.report.professorship.ProfessorshipReport;
import org.fenixedu.ulisboa.reports.services.report.professorship.ProfessorshipReportService;
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
        title = "label.org.fenixedu.ulisboa.reports.title.professorship", accessGroup = "logged")
@RequestMapping(ProfessorshipReportController.CONTROLLER_URL)
public class ProfessorshipReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL = "/fenixedu-ulisboa-reports/reports/professorship/professorshipreport";
    private static final String JSP_PATH = CONTROLLER_URL.substring(1);
    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(Model model, RedirectAttributes redirectAttributes) {
        setParametersBean(new ProfessorshipReportParametersBean(), model);
        return jspPage("professorshipreport");
    }

    private void setParametersBean(ProfessorshipReportParametersBean bean, Model model) {
        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") ProfessorshipReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setParametersBean(bean, model);

        setResults(generateProfessorshipReport(bean), model);

        return jspPage("professorshipreport");
    }

    private void setResults(Collection<? extends ProfessorshipReport> results, Model model) {
        model.addAttribute("results", results);
    }

    private static Collection<ProfessorshipReport> generateProfessorshipReport(final ProfessorshipReportParametersBean bean) {

        final ProfessorshipReportService service = new ProfessorshipReportService();
        service.filterEnrolmentExecutionYear(bean.getExecutionYear());

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    @RequestMapping(value = "/exportreport", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportReport(
            @RequestParam(value = "bean", required = false) final ProfessorshipReportParametersBean bean, final Model model) {

        final String reportId = getReportId("exportReport");
        new Thread(() -> processReport(this::exportToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    private static String getReportId(final String exportName) {
        return normalizeName(bundle("professorship.event." + exportName), "_") + "_UUID_" + UUID.randomUUID().toString();
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

    private static String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }

    @Atomic(mode = TxMode.READ)
    protected void processReport(final Function<ProfessorshipReportParametersBean, byte[]> reportProcessor,
            final ProfessorshipReportParametersBean bean, final String reportId) {

        byte[] content = null;
        try {
            content = reportProcessor.apply(bean);
        } catch (final Throwable e) {
            content = createXLSWithError(e instanceof ULisboaReportsDomainException ? ((ULisboaReportsDomainException) e)
                    .getLocalizedMessage() : ExceptionUtils.getFullStackTrace(e));
        }

        ULisboaSpecificationsTemporaryFile.create(reportId, content, Authenticate.getUser());
    }

    private byte[] createXLSWithError(String error) {

        try {

            final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
            builder.addSheet(ULisboaReportsUtil.bundle("professorship.professorship"),
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

    private static String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    private byte[] exportToXLS(final ProfessorshipReportParametersBean bean) {
        final Collection<ProfessorshipReport> toExport = generateProfessorshipReport(bean);

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("professorship.professorship"), new SheetData<ProfessorshipReport>(toExport) {

            @Override
            protected void makeLine(final ProfessorshipReport report) {
                addPrimaryData(report);
                addProfessorshipData(report);
            }

            private void addPrimaryData(final ProfessorshipReport report) {
                addData("ProfessorshipReport.teacher", report.getTeacherName());
                addData("ProfessorshipReport.teacherUsername", report.getTeacherUsername());
                addData("ProfessorshipReport.teacherDepartment", report.getTeacherDepartment());
                addData("ProfessorshipReport.responsible", report.getIsResponsible());
                addData("ProfessorshipReport.executionYear", report.getExecutionYearName());
                addData("ProfessorshipReport.executionSemester", report.getExecutionSemesterName());
            }

            private void addData(final String key, final Object value) {
                addCell(bundle(key), value == null ? "" : value);
            }

            private void addProfessorshipData(final ProfessorshipReport report) {
                addData("ProfessorshipReport.executionCourse", report.getExecutionCourseName());
                addData("ProfessorshipReport.courseCode", report.getCompetenceCourseCode());
                addData("ProfessorshipReport.courseRegime", report.getCompetenceCourseRegime());
                addData("ProfessorshipReport.courseDepartment", report.getCompetenceCourseDepartment());
                addData("ProfessorshipReport.classes", report.getClassesName());
                addData("ProfessorshipReport.shift", report.getShiftName());
                addData("ProfessorshipReport.shiftType", report.getShiftTypeName());
                addData("ProfessorshipReport.shiftOcupation", report.getShiftOccupation());
                addData("ProfessorshipReport.shiftCapacity", report.getShiftCapacity());
                addData("ProfessorshipReport.totalHours", report.getTotalHours());
                addData("ProfessorshipReport.allocationPercentage", report.getAllocationPercentage());
                addData("ProfessorshipReport.workload", report.getTeacherHours());
            }

        });

        final Collection<ProfessorshipDegreeReport> newReports = generateProfessorshipDegreeReport(toExport);

        builder.addSheet(ULisboaReportsUtil.bundle("professorship.professorshipDegree"),
                new SheetData<ProfessorshipDegreeReport>(newReports) {

                    @Override
                    protected void makeLine(final ProfessorshipDegreeReport report) {
                        addPrimaryData(report);
                        addDegreeData(report);
                        addProfessorshipData(report);
                    }

                    private void addPrimaryData(final ProfessorshipDegreeReport report) {
                        addData("ProfessorshipReport.teacher", report.getTeacherName());
                        addData("ProfessorshipReport.teacherUsername", report.getTeacherUsername());
                        addData("ProfessorshipReport.teacherDepartment", report.getTeacherDepartment());
                        addData("ProfessorshipReport.responsible", report.getIsResponsible());
                        addData("ProfessorshipReport.executionYear", report.getExecutionYearName());
                        addData("ProfessorshipReport.executionSemester", report.getExecutionSemesterName());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle(key), value == null ? "" : value);
                    }

                    private void addDegreeData(final ProfessorshipDegreeReport report) {
                        addData("ProfessorshipDegreeReport.degree", report.getDegreeName());
                        addData("ProfessorshipDegreeReport.degreeCode", report.getDegreeCode());
                    }

                    private void addProfessorshipData(final ProfessorshipDegreeReport report) {
                        addData("ProfessorshipReport.executionCourse", report.getExecutionCourseName());
                        addData("ProfessorshipReport.courseCode", report.getCompetenceCourseCode());
                        addData("ProfessorshipReport.courseRegime", report.getCompetenceCourseRegime());
                        addData("ProfessorshipReport.courseDepartment", report.getCompetenceCourseDepartment());
                        addData("ProfessorshipReport.classes", report.getClassesName());
                        addData("ProfessorshipReport.shift", report.getShiftName());
                        addData("ProfessorshipReport.shiftType", report.getShiftTypeName());
                        addData("ProfessorshipReport.shiftOcupation", report.getShiftOccupation());
                        addData("ProfessorshipReport.shiftCapacity", report.getShiftCapacity());
                        addData("ProfessorshipReport.totalHours", report.getTotalHours());
                        addData("ProfessorshipReport.allocationPercentage", report.getAllocationPercentage());
                        addData("ProfessorshipReport.workload", report.getTeacherHours());
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

    private static Collection<ProfessorshipDegreeReport> generateProfessorshipDegreeReport(
            final Collection<ProfessorshipReport> professorshipReports) {

        final Set<ShiftProfessorship> shiftProfessorships =
                professorshipReports.stream().map(pr -> pr.getShiftProfessorship()).collect(Collectors.toSet());

        final ProfessorshipDegreeReportService service = new ProfessorshipDegreeReportService(shiftProfessorships);

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    @RequestMapping(value = _POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> postback(
            @RequestParam(value = "bean", required = false) final ProfessorshipReportParametersBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

}
