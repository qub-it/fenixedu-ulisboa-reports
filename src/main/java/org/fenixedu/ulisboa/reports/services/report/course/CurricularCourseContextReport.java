package org.fenixedu.ulisboa.reports.services.report.course;

import java.text.Collator;
import java.util.Comparator;

import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;

public class CurricularCourseContextReport implements Comparable<CurricularCourseContextReport> {

    private final CurricularCourse curricularCourse;
    private final Context context;

    public CurricularCourseContextReport(CurricularCourse curricularCourse, Context context) {
        this.curricularCourse = curricularCourse;
        this.context = context;
    }

    @Override
    public int compareTo(final CurricularCourseContextReport o) {

        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<CurricularCourseContextReport> byCourseName =
                (x, y) -> instance.compare(x.getCompetenceCourseName(), y.getCompetenceCourseName());
        final Comparator<CurricularCourseContextReport> byCode =
                (x, y) -> instance.compare(x.getCompetenceCourseCode(), y.getCompetenceCourseCode());
        final Comparator<CurricularCourseContextReport> byDegreeName =
                (x, y) -> instance.compare(x.getDegreeName(), y.getDegreeName());
        final Comparator<CurricularCourseContextReport> byDegreeCode =
                (x, y) -> instance.compare(x.getDegreeCode(), y.getDegreeCode());
        final Comparator<CurricularCourseContextReport> byGroupName =
                (x, y) -> instance.compare(x.getContextGroupName(), y.getContextGroupName());

        return byCourseName.thenComparing(byCode).thenComparing(byDegreeName).thenComparing(byDegreeCode)
                .thenComparing(byGroupName).compare(this, o);
    }

    public String getCompetenceCourseCode() {
        return curricularCourse.getCode();
    }

    public String getCompetenceCourseName() {
        return curricularCourse.getNameI18N().getContent();
    }

    public String getDegreeCode() {
        return curricularCourse.getDegree().getCode();
    }

    public String getDegreeName() {
        return curricularCourse.getDegree().getName();
    }

    public String getContextGroupName() {
        return context.getParentCourseGroup().getOneFullName();
    }

    public String getDegreeType() {
        return curricularCourse.getDegreeType().getName().getContent();
    }

    public String getOfficialDegreeCode() {
        return curricularCourse.getDegree().getMinistryCode();
    }

    public String getDegreeCurricularPlanName() {
        return context.getParentCourseGroup().getParentDegreeCurricularPlan().getName();
    }

    public String getTypology() {
        return context.getParentCourseGroup().isOptionalCourseGroup() ? ULisboaReportsUtil
                .bundle("curricularCourseContextReport.typology.optional") : ULisboaReportsUtil
                        .bundle("curricularCourseContextReport.typology.mandatory");
    }

    public int getCurricularYear() {
        return context.getCurricularYear();
    }

    public String getCurricularPeriod() {
        return context.getCurricularPeriod().getLabel();
    }

    public String getContextBeginDate() {
        return context.getBeginExecutionPeriod().getQualifiedName();
    }

    public String getContextEndDate() {
        return context.getEndExecutionPeriod() == null ? ULisboaReportsUtil
                .bundle("curricularCourseContextReport.context.open") : context.getEndExecutionPeriod().getQualifiedName();
    }
}