package org.fenixedu.ulisboa.reports.ui.reports.course;

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
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilderForXLSX;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.reports.dto.report.course.CourseReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.course.CompetenceCourseReport;
import org.fenixedu.ulisboa.reports.services.report.course.CompetenceCourseReportService;
import org.fenixedu.ulisboa.reports.services.report.course.CompetenceCourseService;
import org.fenixedu.ulisboa.reports.services.report.course.CurricularCourseContextReport;
import org.fenixedu.ulisboa.reports.services.report.course.CurricularCourseContextReportService;
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

@SpringFunctionality(app = FenixeduULisboaReportsController.class, title = "label.org.fenixedu.ulisboa.reports.title.course",
        accessGroup = "logged")
@RequestMapping(CourseReportController.CONTROLLER_URL)
public class CourseReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL = "/fenixedu-ulisboa-reports/reports/course/coursereport";
    private static final String JSP_PATH = CONTROLLER_URL.substring(1);

    private void setParametersBean(CourseReportParametersBean bean, Model model) {
        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(Model model, RedirectAttributes redirectAttributes) {
        setParametersBean(new CourseReportParametersBean(), model);
        return jspPage("coursereport");
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") CourseReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setParametersBean(bean, model);
        return jspPage("coursereport");
    }

    static private String getReportId(final String exportName) {
        return normalizeName(bundle("course.event." + exportName), "_") + "_UUID_" + UUID.randomUUID().toString();
    }

    static private String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    static public String normalizeName(final String input, final String replacement) {
        // ex [ ] * ? : / \
        String result = Normalizer.normalize(input, java.text.Normalizer.Form.NFD)

                .replaceAll("[^\\p{ASCII}]", "").replace(" ", replacement).replace("[", replacement).replace("]", replacement)
                .replace("*", replacement).replace("?", replacement).replace(":", replacement).replace("/", replacement)
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
            @RequestParam(value = "bean", required = false) final CourseReportParametersBean bean, final Model model) {

        final String reportId = getReportId("exportReport");
        new Thread(() -> processReport(this::exportToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    @Atomic(mode = TxMode.READ)
    protected void processReport(final Function<CourseReportParametersBean, byte[]> reportProcessor,
            final CourseReportParametersBean bean, final String reportId) {

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

    private byte[] exportToXLS(final CourseReportParametersBean bean) {

        final CompetenceCourseService service = new CompetenceCourseService();
        service.filterExecutionYear(bean.getExecutionYear());
        final Collection<CompetenceCourse> competenceCourses = service.process();

        final CompetenceCourseReportService competenceCourseReportService = new CompetenceCourseReportService(competenceCourses);
        final Collection<CompetenceCourseReport> competenceCourseReports =
                competenceCourseReportService.generateReport().stream().sorted().collect(Collectors.toList());

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();

        builder.addSheet(ULisboaReportsUtil.bundle("course.competenceCourse"),
                new SheetData<CompetenceCourseReport>(competenceCourseReports) {

                    @Override
                    protected void makeLine(final CompetenceCourseReport report) {
                        addCompetenceCourseData(report);
                    }

                    private void addCompetenceCourseData(CompetenceCourseReport report) {
                        addData("CompetenceCourseReport.code", report.getCode());
                        addData("CompetenceCourseReport.name", report.getName());
                        addData("CompetenceCourseReport.nameEN", report.getNameEn());
                        addData("CompetenceCourseReport.beginDate", report.getBeginExecutionPeriod());
                        addData("CompetenceCourseReport.department", report.getDepartment());
                        addData("CompetenceCourseReport.area", report.getScientificArea());
                        addData("CompetenceCourseReport.acronym", report.getAcronym());
                        addData("CompetenceCourseReport.regime", report.getRegime());
                        addData("CompetenceCourseReport.load.theoretical", report.getTheoreticalHours());
                        addData("CompetenceCourseReport.load.problems", report.getProblemsHours());
                        addData("CompetenceCourseReport.load.laboratorial", report.getLaboratorialHours());
                        addData("CompetenceCourseReport.load.seminary", report.getSeminaryHours());
                        addData("CompetenceCourseReport.load.tutorialOrientation", report.getTutorialOrientationHours());
                        addData("competenceCourseReport.load.other", report.getOtherHours());
                        addData("competenceCourseReport.load.autonomous", report.getAutonomousWorkHours());
                        addData("competenceCourseReport.load.total", report.getTotalHours());
                        addData("competenceCourseReport.ects", report.getECTS());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle(key), value == null ? "" : value);
                    }

                });

        final CurricularCourseContextReportService curricularContextService =
                new CurricularCourseContextReportService(competenceCourses, bean.getExecutionYear());
        final Collection<CurricularCourseContextReport> curricularContextReports =
                curricularContextService.generateReport().stream().sorted().collect(Collectors.toList());

        builder.addSheet(ULisboaReportsUtil.bundle("course.curricularCourse"),
                new SheetData<CurricularCourseContextReport>(curricularContextReports) {

                    @Override
                    protected void makeLine(final CurricularCourseContextReport report) {
                        addCurricularCourseContextData(report);
                    }

                    private void addCurricularCourseContextData(CurricularCourseContextReport report) {
                        addData("curricularCourseContextReport.courseCode", report.getCompetenceCourseCode());
                        addData("curricularCourseContextReport.courseName", report.getCompetenceCourseName());
                        addData("curricularCourseContextReport.degreeCode", report.getDegreeCode());
                        addData("curricularCourseContextReport.degreeName", report.getDegreeName());
                        addData("curricularCourseContextReport.curricularPlan", report.getDegreeCurricularPlanName());
                        addData("curricularCourseContextReport.group", report.getContextGroupName());
                        addData("curricularCourseContextReport.typology", report.getTypology());
                        addData("curricularCourseContextReport.curricularYear", report.getCurricularYear());
                        addData("curricularCourseContextReport.curricularPeriod", report.getCurricularPeriod());
                        addData("curricularCourseContextReport.beginDate", report.getContextBeginDate());
                        addData("curricularCourseContextReport.endDate", report.getContextEndDate());
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

    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;

    @RequestMapping(value = _POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> postback(
            @RequestParam(value = "bean", required = false) final CourseReportParametersBean bean, final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private byte[] createXLSWithError(String error) {

        try {
            final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
            builder.addSheet(ULisboaReportsUtil.bundle("competenceCourse.competenceCourse"),
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

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

}
