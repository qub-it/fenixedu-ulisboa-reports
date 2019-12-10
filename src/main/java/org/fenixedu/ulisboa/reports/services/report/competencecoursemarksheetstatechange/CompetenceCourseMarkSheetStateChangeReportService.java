package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;

public class CompetenceCourseMarkSheetStateChangeReportService {

    private ExecutionInterval executionInterval;
    private CompetenceCourse competenceCourse;

    public void filterEnrolmentExecutionSemester(ExecutionInterval executionInterval) {
        this.executionInterval = executionInterval;
    }

    public void filterEnrolmentCompetenceCourse(CompetenceCourse competenceCourse) {
        this.competenceCourse = competenceCourse;
    }

    public Collection<CompetenceCourseMarkSheetStateChangeReport> generateReport() {
        return process(executionInterval, competenceCourse);
    }

    private Collection<CompetenceCourseMarkSheetStateChangeReport> process(final ExecutionInterval executionInterval,
            final CompetenceCourse competenceCourse) {
        return buildSearchUniverse(executionInterval, competenceCourse).stream().map(this::buildReport)
                .collect(Collectors.toSet());
    }

    private Set<CompetenceCourseMarkSheet> buildSearchUniverse(final ExecutionInterval executionInterval,
            final CompetenceCourse competenceCourse) {
        return competenceCourse.getCompetenceCourseMarkSheetSet().stream()
                .filter(ccms -> ccms.getExecutionSemester().getOid().equals(executionInterval.getOid()) && ccms.isConfirmed())
                .filter(ccms -> ccms.getCompetenceCourse().getOid().equals(competenceCourse.getOid()))
                .collect(Collectors.toSet());
    }

    private CompetenceCourseMarkSheetStateChangeReport buildReport(final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        return new CompetenceCourseMarkSheetStateChangeReport(competenceCourseMarkSheet);
    }

}
