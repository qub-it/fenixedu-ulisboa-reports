package org.fenixedu.ulisboa.reports.dto.report.scholarshiptypology;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;

public class ScholarshipTypologyReportParametersBean implements IBean {

    private ExecutionYear executionYear;

    private List<TupleDataSourceBean> executionYearsDataSource;

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public List<TupleDataSourceBean> getExecutionYearsDataSource() {
        return executionYearsDataSource;
    }

    public ScholarshipTypologyReportParametersBean() {

        updateData();

    }

    public void updateData() {

        this.executionYearsDataSource =
                ExecutionYear.readNotClosedExecutionYears().stream().sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed())
                        .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getQualifiedName())).collect(Collectors.toList());

    }

}
