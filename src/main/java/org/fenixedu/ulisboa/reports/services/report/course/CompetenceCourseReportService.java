package org.fenixedu.ulisboa.reports.services.report.course;

import java.util.Collection;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionYear;

public class CompetenceCourseReportService {

    private final Collection<CompetenceCourse> competenceCourses;
    private final ExecutionYear executionYear;

    public CompetenceCourseReportService(final Collection<CompetenceCourse> competenceCourses,
            final ExecutionYear executionYear) {
        this.competenceCourses = competenceCourses;
        this.executionYear = executionYear;
    }

    public Collection<CompetenceCourseReport> generateReport() {
        return process();
    }

    private Collection<CompetenceCourseReport> process() {
        return competenceCourses.stream().map(cc -> buildReport(cc, executionYear)).collect(Collectors.toSet());
    }

    private CompetenceCourseReport buildReport(final CompetenceCourse competenceCourse, final ExecutionYear executionYear) {
        return new CompetenceCourseReport(competenceCourse, executionYear);
    }

}
