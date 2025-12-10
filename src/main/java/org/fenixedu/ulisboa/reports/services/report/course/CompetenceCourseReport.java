package org.fenixedu.ulisboa.reports.services.report.course;

import java.text.Collator;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseLoadType;
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
        return competenceCourse.getCompetenceCourseType().getName().getContent();
    }

    public String getRegime() {
        return getMostRecentCompetenceCourseInformation().getRegime().getLocalizedName();
    }

    public Double getTotalHours() {
        return getTheoreticalHours() + getProblemsHours() + getFieldWorkHours() + getLaboratorialHours() + getOtherHours()
                + getSeminaryHours() + getTrainingPeriodHours() + getTutorialOrientationHours() + getAutonomousWorkHours();
    }

    public Double getTheoreticalHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.THEORETICAL))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getProblemsHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.THEORETICAL_PRACTICAL))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getLaboratorialHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.PRACTICAL_LABORATORY))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getSeminaryHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.SEMINAR))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getFieldWorkHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.FIELD_WORK))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getTrainingPeriodHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.INTERNSHIP))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getTutorialOrientationHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.TUTORIAL_ORIENTATION))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getOtherHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.OTHER))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getAutonomousWorkHours() {
        return getMostRecentCompetenceCourseInformation().getLoadHours(CourseLoadType.of(CourseLoadType.AUTONOMOUS_WORK))
                .map(bd -> Double.valueOf(bd.doubleValue())).orElse(Double.valueOf(0));
    }

    public Double getECTS() {
        return getMostRecentCompetenceCourseInformation().getCredits().doubleValue();
    }
}