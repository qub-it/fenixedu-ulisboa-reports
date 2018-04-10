package org.fenixedu.ulisboa.reports.dto.report.competencecoursemarksheet;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;

public class CompetenceCourseMarkSheetReportParametersBean implements IBean {

    private ExecutionSemester executionSemester;
    private List<TupleDataSourceBean> executionSemestersDataSource;

    public CompetenceCourseMarkSheetReportParametersBean() {
        updateData();
    }

    public List<TupleDataSourceBean> getExecutionSemestersDataSource() {
        return executionSemestersDataSource;
    }

    public void updateData() {
        this.executionSemestersDataSource = ExecutionSemester
                .readNotClosedExecutionPeriods().stream()
                .sorted(ExecutionSemester.COMPARATOR_BY_BEGIN_DATE.reversed())
                .map(x -> new TupleDataSourceBean(x.getExternalId(),
                        x.getQualifiedName()))
                .collect(Collectors.toList());
    }

    public ExecutionSemester getExecutionSemester() {
        return executionSemester;
    }

    public void setExecutionSemester(ExecutionSemester executionSemester) {
        this.executionSemester = executionSemester;
    }
}
