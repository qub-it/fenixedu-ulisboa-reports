package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheet;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;

public class CompetenceCourseMarkSheetReportService {

    private ExecutionInterval executionInterval;

    public void filterEnrolmentExecutionSemester(ExecutionInterval executionInterval) {
        this.executionInterval = executionInterval;
    }

    public Collection<CompetenceCourseMarkSheetReport> generateReport() {
        return process(executionInterval);
    }

    private Collection<CompetenceCourseMarkSheetReport> process(final ExecutionInterval executionInterval) {
        return buildSearchUniverse(executionInterval).stream().map(this::buildReport).collect(Collectors.toSet());
    }

    private Set<CompetenceCourseMarkSheet> buildSearchUniverse(final ExecutionInterval executionInterval) {
        return executionInterval.getCompetenceCourseMarkSheetSet();
    }

    private CompetenceCourseMarkSheetReport buildReport(final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        return new CompetenceCourseMarkSheetReport(competenceCourseMarkSheet);
    }

}
