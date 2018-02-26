package org.fenixedu.ulisboa.reports.services.report.course;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionYear;

import com.google.common.collect.Sets;

public class CompetenceCourseService {

    private ExecutionYear executionYear;

    public void filterExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Collection<CompetenceCourse> process() {
        return buildSearchUniverse();
    }

    private Set<CompetenceCourse> buildSearchUniverse() {

        if (executionYear == null) {
            return Sets.newHashSet();
        }

        return CompetenceCourse.readApprovedBolonhaCompetenceCourses().stream()
                .filter(cc -> cc.getCurricularCourseContexts().stream().anyMatch(context -> context.isOpen(executionYear)))
                .collect(Collectors.toSet());
    }
}