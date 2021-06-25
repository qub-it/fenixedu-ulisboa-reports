package org.fenixedu.ulisboa.reports.dto.report.course;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;

public class CourseReportParametersBean implements IBean {

    private ExecutionYear executionYear;
    private List<TupleDataSourceBean> executionYearsDataSource;
    private Boolean isActiveCompetenceCourses;

    public CourseReportParametersBean() {
        updateData();
    }

    public void updateData() {
        this.executionYearsDataSource =
                ExecutionYear.readNotClosedExecutionYears().stream().sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed())
                        .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getQualifiedName())).collect(Collectors.toList());
    }

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(final ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Boolean getIsActiveCompetenceCourses() {
        return this.isActiveCompetenceCourses;
    }

    public void setIsActiveCompetenceCourses(final Boolean isActiveCompetenceCourses) {
        this.isActiveCompetenceCourses = isActiveCompetenceCourses;
    }

    public List<TupleDataSourceBean> getExecutionYearsDataSource() {
        return executionYearsDataSource;
    }

}
