package org.fenixedu.ulisboa.reports.services.report.course;

import java.util.Collection;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;

public class CompetenceCourseReportService {

    private final Collection<CompetenceCourse> competenceCourses;

    public CompetenceCourseReportService(Collection<CompetenceCourse> competenceCourses) {
        this.competenceCourses = competenceCourses;
    }

    public Collection<CompetenceCourseReport> generateReport() {
        return process();
    }

    private Collection<CompetenceCourseReport> process() {
        return competenceCourses.stream().map(cc -> buildReport(cc)).collect(Collectors.toSet());
    }

    private CompetenceCourseReport buildReport(final CompetenceCourse competenceCourse) {
        return new CompetenceCourseReport(competenceCourse);
    }

}
