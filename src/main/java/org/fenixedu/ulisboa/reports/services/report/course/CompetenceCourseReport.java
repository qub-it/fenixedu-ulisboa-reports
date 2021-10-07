package org.fenixedu.ulisboa.reports.services.report.course;

import java.text.Collator;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseLoad;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;

public class CompetenceCourseReport implements Comparable<CompetenceCourseReport> {

    private final CompetenceCourse competenceCourse;
    private final ExecutionYear executionYear;

    public CompetenceCourseReport(final CompetenceCourse competenceCourse, final ExecutionYear executionYear) {

        this.competenceCourse = competenceCourse;
        this.executionYear = executionYear;
    }

    @Override
    public int compareTo(final CompetenceCourseReport o) {

        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<CompetenceCourseReport> byCourseName = (x, y) -> instance.compare(x.getName(), y.getName());
        final Comparator<CompetenceCourseReport> byCode = (x, y) -> instance.compare(x.getCode(), y.getCode());

        return byCourseName.thenComparing(byCode).compare(this, o);
    }

    private CompetenceCourseInformation getMostRecentCompetenceCourseInformation() {
        return competenceCourse.findInformationMostRecentUntil(executionYear.getFirstExecutionPeriod());
    }

    public String getCode() {
        return competenceCourse.getCode();
    }

    public String getName() {
        return getMostRecentCompetenceCourseInformation().getNameI18N().getContent();
    }

    public String getNameEn() {
        return getMostRecentCompetenceCourseInformation().getNameEn();
    }

    public String getIsActive() {
        for (final Context context : competenceCourse.getCurricularCourseContexts()) {
            if (context.isOpen(executionYear)) {
                return ULisboaReportsUtil.bundle("yes");
            }
        }

        return ULisboaReportsUtil.bundle("no");
    }

    public String getBeginExecutionPeriod() {
        return getMostRecentCompetenceCourseInformation().getExecutionPeriod().getQualifiedName();
    }

    public String getFirstVersionExecutionPeriod() {
        return competenceCourse.getBeginExecutionInterval().getQualifiedName();
    }

    public String getDepartment() {
        return competenceCourse.getDepartmentUnit().getNameI18n().getContent();
    }

    public String getScientificArea() {

        return getMostRecentCompetenceCourseInformation().getCompetenceCourseGroupUnit().getParentUnits().stream()
                .filter(u -> u.isScientificAreaUnit()).map(u -> u.getName()).collect(Collectors.joining(", "));
    }

    public String getAcronym() {
        return getMostRecentCompetenceCourseInformation().getAcronym();
    }

    public String getType() {
        return BundleUtil.getLocalizedString(Bundle.ENUMERATION, competenceCourse.getType().name()).getContent();
    }

    public String getRegime() {
        return getMostRecentCompetenceCourseInformation().getRegime().getLocalizedName();
    }

    public Double getTotalHours() {
        return getTheoreticalHours() + getProblemsHours() + getFieldWorkHours() + getLaboratorialHours() + getOtherHours()
                + getSeminaryHours() + getTrainingPeriodHours() + getTutorialOrientationHours() + getAutonomousWorkHours();
    }

    public Double getTheoreticalHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getTheoreticalHours();
        }

        return result;
    }

    public Double getProblemsHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getProblemsHours();
        }

        return result;
    }

    public Double getLaboratorialHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getLaboratorialHours();
        }

        return result;
    }

    public Double getSeminaryHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getSeminaryHours();
        }

        return result;
    }

    public Double getFieldWorkHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getFieldWorkHours();
        }

        return result;
    }

    public Double getTrainingPeriodHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getTrainingPeriodHours();
        }

        return result;
    }

    public Double getTutorialOrientationHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getTutorialOrientationHours();
        }

        return result;
    }

    public Double getOtherHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getOtherHours();
        }

        return result;
    }

    public Double getAutonomousWorkHours() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getAutonomousWorkHours();
        }

        return result;
    }

    public Double getECTS() {
        Double result = 0.0;

        for (final CompetenceCourseLoad competenceCourseLoad : getMostRecentCompetenceCourseInformation()
                .getCompetenceCourseLoadsSet()) {
            result += competenceCourseLoad.getEctsCredits();
        }

        return result;
    }
}