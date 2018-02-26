package org.fenixedu.ulisboa.reports.services.report.course;

import java.util.Collection;
import java.util.Set;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degreeStructure.Context;

import com.google.common.collect.Sets;

public class CurricularCourseContextReportService {

    private final Collection<CompetenceCourse> competenceCourses;
    private final ExecutionYear executionYear;

    public CurricularCourseContextReportService(Collection<CompetenceCourse> competenceCourses, ExecutionYear executionYear) {
        this.competenceCourses = competenceCourses;
        this.executionYear = executionYear;
    }

    public Collection<CurricularCourseContextReport> generateReport() {
        return process();
    }

    private Collection<CurricularCourseContextReport> process() {

        Set<CurricularCourseContextReport> result = Sets.newHashSet();

        competenceCourses.stream().map(cc -> cc.getAssociatedCurricularCoursesSet()).flatMap(Collection::stream)
                .forEach(course -> {
                    course.getParentContextsSet().stream().filter(context -> context.isOpen(executionYear))
                            .forEach(context -> result.add(buildReport(course, context)));
                });

        return result;
    }

    private CurricularCourseContextReport buildReport(CurricularCourse curricularCourse, Context context) {
        return new CurricularCourseContextReport(curricularCourse, context);
    }
}