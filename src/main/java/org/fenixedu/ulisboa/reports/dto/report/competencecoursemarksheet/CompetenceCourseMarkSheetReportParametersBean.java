package org.fenixedu.ulisboa.reports.dto.report.competencecoursemarksheet;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;

public class CompetenceCourseMarkSheetReportParametersBean implements IBean {

    private ExecutionInterval executionInterval;
    private List<TupleDataSourceBean> executionSemestersDataSource;

    public CompetenceCourseMarkSheetReportParametersBean() {
        updateData();
    }

    public List<TupleDataSourceBean> getExecutionSemestersDataSource() {
        return executionSemestersDataSource;
    }

    public void updateData() {
        this.executionSemestersDataSource =
                ExecutionInterval.findActiveChilds().stream().sorted(ExecutionInterval.COMPARATOR_BY_BEGIN_DATE.reversed())
                        .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getQualifiedName())).collect(Collectors.toList());
    }

    public ExecutionInterval getExecutionSemester() {
        return executionInterval;
    }

    public void setExecutionSemester(ExecutionInterval executionInterval) {
        this.executionInterval = executionInterval;
    }
}
