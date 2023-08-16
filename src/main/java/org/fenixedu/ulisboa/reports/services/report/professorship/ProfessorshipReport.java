package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.Optional;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.ShiftEnrolment;
import org.fenixedu.academic.domain.ShiftProfessorship;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.academic.domain.schedule.shiftCapacity.ShiftCapacity;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;

public class ProfessorshipReport implements Comparable<ProfessorshipReport> {

    private final ShiftProfessorship shiftProfessorship;

    public ProfessorshipReport(final ShiftProfessorship shiftProfessorship) {
        this.shiftProfessorship = shiftProfessorship;
    }

    @Override
    public int compareTo(final ProfessorshipReport o) {

        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<ProfessorshipReport> byTeacher = (x, y) -> instance.compare(x.getTeacherName(), y.getTeacherName());
        final Comparator<ProfessorshipReport> bySemesterAndYear =
                (x, y) -> ExecutionInterval.COMPARATOR_BY_BEGIN_DATE.compare(x.getExecutionPeriod(), y.getExecutionPeriod());
        final Comparator<ProfessorshipReport> byCourseName =
                (x, y) -> instance.compare(x.getExecutionCourseName(), y.getExecutionCourseName());

        return byTeacher.thenComparing(bySemesterAndYear).thenComparing(byCourseName).compare(this, o);
    }

    public String getTeacherName() {
        final Person person = getTeacherPerson();
        return person == null ? null : person.getName();
    }

    public String getCompetenceCourseCode() {
        return getCompetenceCourse().getCode();
    }

    private CompetenceCourse getCompetenceCourse() {
        // For some reason, an ExecutionCourse could be related to more than 1 CompetenceCourse.
        // this Method will return the first found CompetenceCourse.
        return getExecutionCourse().getCompetenceCourses().iterator().next();
    }

    protected ExecutionCourse getExecutionCourse() {
        final Shift shift = getShift();
        return shift == null ? null : shift.getExecutionCourse();
    }

    protected Shift getShift() {
        return shiftProfessorship.getShift();
    }

    public String getCompetenceCourseRegime() {
        return getCompetenceCourse().getRegime().getLocalizedName();
    }

    public String getCompetenceCourseDepartment() {
        return getCompetenceCourse().getDepartmentUnit().getName();
    }

    public String getTeacherUsername() {
        final Person person = getTeacherPerson();
        return person == null ? null : person.getUsername();
    }

    protected Person getTeacherPerson() {
        final Professorship professorship = getProfessorship();
        return professorship == null ? null : professorship.getPerson();
    }

    protected Professorship getProfessorship() {
        return shiftProfessorship.getProfessorship();
    }

    public String getTeacherDepartment() {
        return Optional.ofNullable(getProfessorship()).map(o -> o.getTeacher())
                .flatMap(o -> o.getTeacherAuthorization(getExecutionPeriod().getAcademicInterval())).map(o -> o.getUnit())
                .map(o -> o.getNameI18n().getContent()).orElse("");
    }

    public ExecutionInterval getExecutionPeriod() {
        final ExecutionCourse executionCourse = getExecutionCourse();
        return executionCourse == null ? null : executionCourse.getExecutionPeriod();
    }

    public String getIsResponsible() {
        return getProfessorship().isResponsibleFor() ? ULisboaReportsUtil.bundle("yes") : ULisboaReportsUtil.bundle("no");
    }

    public String getExecutionYearName() {
        final ExecutionYear executionYear = getExecutionYear();
        return executionYear == null ? null : executionYear.getQualifiedName();
    }

    public ExecutionYear getExecutionYear() {
        final ExecutionCourse executionCourse = getExecutionCourse();
        return executionCourse == null ? null : executionCourse.getExecutionYear();
    }

    public String getExecutionSemesterName() {
        final ExecutionInterval executionInterval = getExecutionPeriod();
        return executionInterval == null ? null : executionInterval.getName();
    }

    public String getExecutionCourseName() {
        return getExecutionCourse().getNameI18N().getContent();
    }

    public String getClassesName() {
        final Shift shift = getShift();
        return shift == null ? null : shift.getClassesPrettyPrint();
    }

    public String getShiftName() {
        final Shift shift = getShift();
        return shift == null ? null : shift.getName();
    }

    public String getShiftTypeName() {
        final Shift shift = getShift();
        return shift == null ? null : shift.getShiftTypesPrettyPrint();
    }

    public String getShiftOccupation() {
        return Integer.toString(ShiftEnrolment.getTotalEnrolments(getShift()));
    }

    public String getShiftCapacity() {
        return Integer.toString(ShiftCapacity.getTotalCapacity(getShift()));
    }

    public String getTotalHours() {
        return minutesToHours(getTotalMinutes());
    }

    private double getTotalMinutes() {

        for (final CompetenceCourse competenceCourse : getExecutionCourse().getCompetenceCourses()) {

            final CompetenceCourseInformation competenceCourseInformation =
                    competenceCourse.findInformationMostRecentUntil(getExecutionPeriod());

            return competenceCourseInformation.getCourseLoadDurationsSet().stream()
                    .filter(d -> d.getCourseLoadType().getAllowShifts()).map(d -> d.getHours().multiply(new BigDecimal(60)))
                    .mapToDouble(bd -> bd.doubleValue()).sum();
        }

        return 0.0;
    }

    private String minutesToHours(final double min) {
        final int t = (int) min;
        final int hours = t / 60;
        final int minutes = t % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public String getTeacherHours() {
        return getAllocationPercentage() == null ? null : minutesToHours(
                (long) (getAllocationPercentage() / 100 * getTotalMinutes()));
    }

    public Double getAllocationPercentage() {
        return getShiftProfessorship().getPercentage();
    }

    public ShiftProfessorship getShiftProfessorship() {
        return shiftProfessorship;
    }
}