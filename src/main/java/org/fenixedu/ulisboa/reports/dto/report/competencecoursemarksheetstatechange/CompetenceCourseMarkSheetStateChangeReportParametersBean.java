package org.fenixedu.ulisboa.reports.dto.report.competencecoursemarksheetstatechange;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;

import com.google.common.collect.Sets;

public class CompetenceCourseMarkSheetStateChangeReportParametersBean implements IBean {

    private ExecutionInterval executionInterval;
    private List<TupleDataSourceBean> executionSemesterDataSource;

    private CompetenceCourse competenceCourse;
    private List<TupleDataSourceBean> competenceCourseDataSource;

    private ExecutionCourse executionCourse;
    private List<TupleDataSourceBean> executionCourseDataSource;

    public CompetenceCourseMarkSheetStateChangeReportParametersBean() {
        update();
    }

    public List<TupleDataSourceBean> getExecutionSemestersDataSource() {
        return executionSemesterDataSource;
    }

    public void updateData() {
        this.executionSemesterDataSource =
                ExecutionInterval.findActiveChilds().stream().sorted(ExecutionInterval.COMPARATOR_BY_BEGIN_DATE.reversed())
                        .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getQualifiedName())).collect(Collectors.toList());
    }

    public void update() {
        setExecutionSemesterDataSource(ExecutionInterval.findActiveChilds());

        updateCompetenceCourseDataSource();

//        setExecutionCourseDataSource(getFilteredExecutionCourses(null).collect(Collectors.toSet()));

    }

    public void setExecutionSemesterDataSource(final Collection<ExecutionInterval> value) {
        this.executionSemesterDataSource = value.stream().sorted(ExecutionInterval.COMPARATOR_BY_BEGIN_DATE.reversed()).map(x -> {
            TupleDataSourceBean tuple = new TupleDataSourceBean();
            tuple.setId(x.getExternalId());
            tuple.setText(x.getQualifiedName());

            return tuple;

        }).collect(Collectors.toList());
    }

    private void updateCompetenceCourseDataSource() {
        final Set<CompetenceCourse> value;
        if (getExecutionCourse() != null) {
            value = getExecutionCourse().getAssociatedCurricularCoursesSet().stream().map(e -> e.getCompetenceCourse())
                    .collect(Collectors.toSet());
        } else if (getExecutionSemester() != null) {
            value = getExecutionSemester().getAssociatedExecutionCoursesSet().stream()
                    .flatMap(e -> e.getCompetenceCourses().stream()).collect(Collectors.toSet());
        } else {
            value = Sets.newHashSet();
        }

        this.competenceCourseDataSource = value.stream().sorted(CompetenceCourse.COMPETENCE_COURSE_COMPARATOR_BY_NAME).map(x -> {
            TupleDataSourceBean tuple = new TupleDataSourceBean();
            tuple.setId(x.getExternalId());
            tuple.setText(x.getCode() + " - " + (x.getName().replace("'", " ").replace("\"", " ")));

            return tuple;

        }).collect(Collectors.toList());
    }

    public ExecutionCourse getExecutionCourse() {
        return executionCourse;
    }

    public CompetenceCourse getCompetenceCourse() {
        return competenceCourse;
    }

    public ExecutionInterval getExecutionSemester() {
        return executionInterval;
    }

    public List<TupleDataSourceBean> getExecutionSemesterDataSource() {
        return executionSemesterDataSource;
    }

    public List<TupleDataSourceBean> getCompetenceCourseDataSource() {
        return competenceCourseDataSource;
    }

    public void setExecutionSemester(ExecutionInterval executionInterval) {
        this.executionInterval = executionInterval;
    }

    public void setCompetenceCourse(CompetenceCourse competenceCourse) {
        this.competenceCourse = competenceCourse;
    }

}