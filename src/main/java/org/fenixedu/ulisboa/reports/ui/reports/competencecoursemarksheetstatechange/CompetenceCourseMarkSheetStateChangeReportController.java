package org.fenixedu.ulisboa.reports.ui.reports.competencecoursemarksheetstatechange;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.EvaluationSeason;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetChangeRequestStateEnum;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetSnapshotEntry;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetStateEnum;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilderForXLSX;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.reports.dto.report.competencecoursemarksheetstatechange.CompetenceCourseMarkSheetStateChangeReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange.CompetenceCourseMarkSheetStateChangeReport;
import org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange.CompetenceCourseMarkSheetStateChangeReportService;
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

@SpringFunctionality(app = FenixeduULisboaReportsController.class,
        title = "label.org.fenixedu.ulisboa.reports.title.competenceCourseMarkSheetStateChange", accessGroup = "logged")
@RequestMapping(CompetenceCourseMarkSheetStateChangeReportController.CONTROLLER_URL)
public class CompetenceCourseMarkSheetStateChangeReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL =
            "/fenixedu-ulisboa-reports/reports/competencecoursemarksheetstatechange/competencecoursemarksheetstatechangereport";
    private static final String JSP_PATH = CONTROLLER_URL.substring(1);
    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;
    private static final String _SEARCH_CHANGE_REQUESTS_URI = "/searchchangerequests/";
    public static final String SEARCH_CHANGE_REQUESTS_URL = CONTROLLER_URL + _SEARCH_CHANGE_REQUESTS_URI;
    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;
    private static final String _SEARCHPOSTBACK_URI = "/searchpostback/";
    public static final String SEARCHPOSTBACK_URL = CONTROLLER_URL + _SEARCHPOSTBACK_URI;

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(@RequestParam(value = "executionsemester", required = false) ExecutionSemester executionSemester,
            @RequestParam(value = "competencecourse", required = false) CompetenceCourse competenceCourse, Model model,
            RedirectAttributes redirectAttributes) {
        final List<CompetenceCourseMarkSheet> searchResultsDataSet = filterSearch(executionSemester, competenceCourse);

        setParametersBean(new CompetenceCourseMarkSheetStateChangeReportParametersBean(), model);
        model.addAttribute("searchcompetencecoursemarksheetstatechangeResultsDataSet", searchResultsDataSet);

        return jspPage("competencecoursemarksheetstatechangereport");
    }

    private void setParametersBean(CompetenceCourseMarkSheetStateChangeReportParametersBean bean, Model model) {
        bean.update();

        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    private List<CompetenceCourseMarkSheet> filterSearch(final ExecutionSemester executionSemester,
            final CompetenceCourse competenceCourse) {

        return getSearchUniverseSearchDataSet(executionSemester, competenceCourse).collect(Collectors.toList());
    }

    private Stream<CompetenceCourseMarkSheet> getSearchUniverseSearchDataSet(final ExecutionSemester semester,
            final CompetenceCourse competence) {

        return CompetenceCourseMarkSheet.findBy(semester, competence, (ExecutionCourse) null, (EvaluationSeason) null,
                (DateTime) null, (Set<Shift>) null, (CompetenceCourseMarkSheetStateEnum) null,
                (CompetenceCourseMarkSheetChangeRequestStateEnum) null);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

    @RequestMapping(value = _SEARCH_CHANGE_REQUESTS_URI + "{oid}", method = RequestMethod.GET)
    public String searchChangeRequests(
            @PathVariable("oid") final CompetenceCourseMarkSheetStateChangeReportParametersBean competenceCourseMarkSheetStateChangeReportParametersBean,
            final Model model, final RedirectAttributes redirectAttributes) throws IOException {

        setParametersBean(competenceCourseMarkSheetStateChangeReportParametersBean, model);

        setResults(generateReport(competenceCourseMarkSheetStateChangeReportParametersBean), model);

        return jspPage("searchchangerequests");
    }

    static private Collection<CompetenceCourseMarkSheetStateChangeReport> generateReport(
            final CompetenceCourseMarkSheetStateChangeReportParametersBean bean) {
        final CompetenceCourseMarkSheetStateChangeReportService service = new CompetenceCourseMarkSheetStateChangeReportService();
        service.filterEnrolmentExecutionSemester(bean.getExecutionSemester());
        service.filterEnrolmentCompetenceCourse(bean.getCompetenceCourse());

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    private void setResults(Collection<CompetenceCourseMarkSheetStateChangeReport> results, Model model) {
        model.addAttribute("results", results);
    }

    @RequestMapping(value = _SEARCHPOSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> searchpostback(
            @RequestParam(value = "bean", required = false) final CompetenceCourseMarkSheetStateChangeReportParametersBean bean,
            final Model model) {

        bean.update();
        this.setParametersBean(bean, model);
        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") CompetenceCourseMarkSheetStateChangeReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {

        setParametersBean(bean, model);

        model.addAttribute("searchcompetencecoursemarksheetstatechangeResultsDataSet",
                filterSearch(bean.getExecutionSemester(), bean.getCompetenceCourse()));

        setResults(generateReport(bean), model);

        return jspPage("competencecoursemarksheetstatechangereport");
    }

    @RequestMapping(value = "/exportreport", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> exportReport(
            @RequestParam(value = "bean", required = false) final CompetenceCourseMarkSheetStateChangeReportParametersBean bean,
            final Model model) {
        final String reportId = getReportId("exportReport");
        new Thread(() -> processReport(this::exportToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    static private String getReportId(final String exportName) {
        return normalizeName(bundle("competenceCourseMarkSheetStateChange.event." + exportName), "_") + "_UUID_"
                + UUID.randomUUID().toString();
    }

    static private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }

    static public String normalizeName(final String input, final String replacement) {
        // ex [ ] * ? : / \
        String result = Normalizer.normalize(input, java.text.Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replace(" ", replacement).replace("[", replacement).replace("]", replacement).replace("*", replacement)
                .replace("?", replacement).replace(":", replacement).replace("/", replacement).replace("\\", replacement);

        while (result.contains(replacement + replacement)) {
            result = result.replace(replacement + replacement, replacement);
        }

        return result.trim();
    }

    @Atomic(mode = Atomic.TxMode.READ)
    protected void processReport(final Function<CompetenceCourseMarkSheetStateChangeReportParametersBean, byte[]> reportProcessor,
            final CompetenceCourseMarkSheetStateChangeReportParametersBean bean, final String reportId) {

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
            builder.addSheet(
                    ULisboaReportsUtil.bundle("competenceCourseMarkSheetStateChange.competenceCourseMarkSheetStateChange"),
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
    @ResponseBody
    public ResponseEntity<String> exportStatus(@PathVariable(value = "reportId") final String reportId, final Model model) {
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

    static private String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    private byte[] exportToXLS(final CompetenceCourseMarkSheetStateChangeReportParametersBean bean) {
        final Collection<CompetenceCourseMarkSheetStateChangeReport> toExport = generateReport(bean);

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();

        toExport.forEach(r -> {
            builder.addSheet(normalizeName(r.getEvaluationSeasonCode().concat(" ").concat(r.getCreationDateShort()), "_"),
                    new SheetData<CompetenceCourseMarkSheetSnapshotEntry>(
                            r.getCompetenceCourseMarkSheetLastSnapshot().getSortedEntries()) {

                        @Override
                        protected void makeLine(final CompetenceCourseMarkSheetSnapshotEntry report) {
                            addCompetenceCourseMarkSheetSnapshotEntryData(report);
                        }

                        private void addCompetenceCourseMarkSheetSnapshotEntryData(
                                final CompetenceCourseMarkSheetSnapshotEntry report) {
                            addData("competenceCourseMarkSheetStateChange.courseCode", r.getCompetenceCourseCode());
                            addData("competenceCourseMarkSheetStateChange.courseName", r.getCompetenceCourseName());
                            addData("competenceCourseMarkSheetStateChange.evaluationSeason", r.getEvaluationSeason());
                            addData("competenceCourseMarkSheetStateChange.executionSemester", r.getExecutionSemester());
                            addData("competenceCourseMarkSheetStateChange.studentNumber", report.getStudentNumber());
                            addData("competenceCourseMarkSheetStateChange.studentName", report.getStudentName());
                            addData("competenceCourseMarkSheetStateChange.studentGrade",
                                    report.getGrade() == null ? "NA" : report.getGrade().getValue());
                            addData("competenceCourseMarkSheetStateChange.studentGradeChanged", r.hasGradeChanged(report));
                            addData("competenceCourseMarkSheetStateChange.gradeAvailableDate", r.gradeAvailableDate(report));
                        }

                        private void addData(final String key, final Object value) {
                            addCell(bundle(key), value == null ? "" : value);
                        }
                    });
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
            @RequestParam(value = "bean", required = false) final CompetenceCourseMarkSheetStateChangeReportParametersBean bean,
            final Model model) {

        bean.updateData();
        bean.update();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }
}