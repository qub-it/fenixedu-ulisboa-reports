package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;

public class CompetenceCourseMarkSheetStateChangeReportService {

    private ExecutionSemester executionSemester;
    private CompetenceCourse competenceCourse;

    public void filterEnrolmentExecutionSemester(ExecutionSemester executionSemester) {
        this.executionSemester = executionSemester;
    }

    public void filterEnrolmentCompetenceCourse(CompetenceCourse competenceCourse) {
        this.competenceCourse = competenceCourse;
    }

    public Collection<CompetenceCourseMarkSheetStateChangeReport> generateReport() {
        return process(executionSemester, competenceCourse);
    }

    private Collection<CompetenceCourseMarkSheetStateChangeReport> process(final ExecutionSemester executionSemester,
            final CompetenceCourse competenceCourse) {
        return buildSearchUniverse(executionSemester, competenceCourse).stream().map(this::buildReport)
                .collect(Collectors.toSet());
    }

    private Set<CompetenceCourseMarkSheet> buildSearchUniverse(final ExecutionSemester executionSemester,
            final CompetenceCourse competenceCourse) {
        return competenceCourse.getCompetenceCourseMarkSheetSet().stream()
                .filter(ccms -> ccms.getExecutionSemester().getOid().equals(executionSemester.getOid()) && ccms.isConfirmed())
                .filter(ccms -> ccms.getCompetenceCourse().getOid().equals(competenceCourse.getOid()))
                .collect(Collectors.toSet());
    }

    private CompetenceCourseMarkSheetStateChangeReport buildReport(final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        return new CompetenceCourseMarkSheetStateChangeReport(competenceCourseMarkSheet);
    }

}
