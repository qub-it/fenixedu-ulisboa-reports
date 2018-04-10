package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheet;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;

public class CompetenceCourseMarkSheetReportService {

    private ExecutionSemester executionSemester;

    public void filterEnrolmentExecutionSemester(
            ExecutionSemester executionSemester) {
        this.executionSemester = executionSemester;
    }

    public Collection<CompetenceCourseMarkSheetReport> generateReport() {
        return process(executionSemester);
    }

    private Collection<CompetenceCourseMarkSheetReport> process(
            final ExecutionSemester executionSemester) {
        return buildSearchUniverse(executionSemester).stream()
                .map(this::buildReport).collect(Collectors.toSet());
    }

    private Set<CompetenceCourseMarkSheet> buildSearchUniverse(
            final ExecutionSemester executionSemester) {
        return executionSemester.getCompetenceCourseMarkSheetSet();
    }

    private CompetenceCourseMarkSheetReport buildReport(
            final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        return new CompetenceCourseMarkSheetReport(competenceCourseMarkSheet);
    }

}
