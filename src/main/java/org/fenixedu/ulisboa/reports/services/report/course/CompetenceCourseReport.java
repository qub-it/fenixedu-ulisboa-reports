package org.fenixedu.ulisboa.reports.services.report.course;

import java.text.Collator;
import java.util.Comparator;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.i18n.BundleUtil;

public class CompetenceCourseReport
        implements Comparable<CompetenceCourseReport> {

    private final CompetenceCourse competenceCourse;

    public CompetenceCourseReport(CompetenceCourse competenceCourse) {
        this.competenceCourse = competenceCourse;
    }

    @Override
    public int compareTo(final CompetenceCourseReport o) {

        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<CompetenceCourseReport> byCourseName = (x,
                y) -> instance.compare(x.getName(), y.getName());
        final Comparator<CompetenceCourseReport> byCode = (x, y) -> instance
                .compare(x.getCode(), y.getCode());

        return byCourseName.thenComparing(byCode).compare(this, o);
    }

    public String getCode() {
        return competenceCourse.getCode();
    }

    public String getName() {
        return competenceCourse.getNameI18N().getContent();
    }

    public String getNameEn() {
        return competenceCourse.getNameEn();
    }

    public String getBeginExecutionPeriod() {
        return competenceCourse.getBeginExecutionInterval().getQualifiedName();
    }

    public String getDepartment() {
        return competenceCourse.getDepartmentUnit().getNameI18n().getContent();
    }

    public String getScientificArea() {
        return competenceCourse.getScientificAreaUnit().getNameI18n()
                .getContent();
    }

    public String getAcronym() {
        return competenceCourse.getAcronym();
    }

    public String getType() {
        return BundleUtil.getLocalizedString(Bundle.ENUMERATION,
                competenceCourse.getType().name()).getContent();
    }

    public String getRegime() {
        return competenceCourse.getRegime().getLocalizedName();
    }

    public Double getTotalHours() {
        return 14 * (getTheoreticalHours() + getProblemsHours()
                + getFieldWorkHours() + getLaboratorialHours() + getOtherHours()
                + getSeminaryHours() + getTrainingPeriodHours()
                + getTutorialOrientationHours()) + getAutonomousWorkHours();
    }

    public Double getTheoreticalHours() {
        return competenceCourse.getTheoreticalHours();
    }

    public Double getProblemsHours() {
        return competenceCourse.getProblemsHours();
    }

    public Double getLaboratorialHours() {
        return competenceCourse.getLaboratorialHours();
    }

    public Double getSeminaryHours() {
        return competenceCourse.getSeminaryHours();
    }

    public Double getFieldWorkHours() {
        return competenceCourse.getFieldWorkHours();
    }

    public Double getTrainingPeriodHours() {
        return competenceCourse.getTrainingPeriodHours();
    }

    public Double getTutorialOrientationHours() {
        return competenceCourse.getTutorialOrientationHours();
    }

    public Double getOtherHours() {
        return competenceCourse.getOtherHours();
    }

    public Double getAutonomousWorkHours() {
        return competenceCourse.getAutonomousWorkHours();
    }

    public Double getECTS() {
        return competenceCourse.getEctsCredits();
    }
}