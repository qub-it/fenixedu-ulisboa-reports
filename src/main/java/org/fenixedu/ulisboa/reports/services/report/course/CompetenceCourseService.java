package org.fenixedu.ulisboa.reports.services.report.course;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.academic.domain.degreeStructure.Context;

import com.google.common.collect.Sets;

public class CompetenceCourseService {

    private ExecutionYear executionYear;

    private Boolean isActiveCompetenceCourses;

    public void filterExecutionYear(final ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public void filterIsActiveCompetenceCourses(final Boolean isActiveCompetenceCourses) {
        this.isActiveCompetenceCourses = isActiveCompetenceCourses;
    }

    public Collection<CompetenceCourse> process() {
        return buildSearchUniverse();
    }

    private Set<CompetenceCourse> buildSearchUniverse() {

        if (executionYear == null || isActiveCompetenceCourses == null) {
            return Sets.newHashSet();
        }

        if (isActiveCompetenceCourses) {
            return CompetenceCourse.findAll().stream().filter(cc -> cc.isApproved())
                    .filter(cc -> cc.getCurricularCourseContexts().stream().anyMatch(context -> context.isOpen(executionYear)))
                    .collect(Collectors.toSet());
        }

        return CompetenceCourse.findAll().stream().filter(cc -> cc.isApproved())
                .filter(cc -> cc.findInformationMostRecentUntil(executionYear.getFirstExecutionPeriod()) != null)
                .collect(Collectors.toSet());
    }
}