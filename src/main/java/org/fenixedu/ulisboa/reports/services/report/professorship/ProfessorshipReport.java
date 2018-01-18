package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.text.Collator;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.ShiftProfessorship;

public class ProfessorshipReport implements Comparable<ProfessorshipReport> {

    private ShiftProfessorship shiftProfessorship;

    public ProfessorshipReport(final ShiftProfessorship shiftProfessorship) {
        this.shiftProfessorship = shiftProfessorship;
    }

    @Override
    public int compareTo(final ProfessorshipReport o) {

        final Comparator<ProfessorshipReport> byTeacher =
                (x, y) -> Collator.getInstance().compare(x.getTeacherName(), y.getTeacherName());

        final Comparator<ProfessorshipReport> bySemesterAndYear =
                (x, y) -> ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR.compare(x.getExecutionPeriod(), y.getExecutionPeriod());
   

        return byTeacher.thenComparing(bySemesterAndYear).compare(this, o);
    }

    public ShiftProfessorship getShiftProfessorship() {
        return shiftProfessorship;
    }

    protected Professorship getProfessorship(){
        return shiftProfessorship.getProfessorship();
    }
    
    protected Shift getShift(){
        return shiftProfessorship.getShift();
    }
    
    protected ExecutionCourse getExecutionCourse(){
        Shift shift = getShift();
        return shift == null ? null : shift.getExecutionCourse(); 
    }
    
    protected Person getTeacherPerson(){
        Professorship professorship = getProfessorship();
        return professorship == null ? null : professorship.getPerson();
    }
    
    public String getTeacherName() {
        final Person person = getTeacherPerson();
        return person == null ? null : person.getName();
    }
    
    public String getTeacherUsername() {
        final Person person = getTeacherPerson();
        return person == null ? null : person.getUsername();
    }

    public ExecutionYear getExecutionYear() {
        final ExecutionCourse executionCourse = getExecutionCourse();
        return executionCourse == null ? null : executionCourse.getExecutionYear();
    }
    
    public String getExecutionYearName() {
        final ExecutionYear executionYear = getExecutionYear();
        return executionYear == null ? null : executionYear.getQualifiedName();
    }
    
    public ExecutionSemester getExecutionPeriod() {
        final ExecutionCourse executionCourse = getExecutionCourse();
        return executionCourse == null ? null : executionCourse.getExecutionPeriod();
    }
    
    public String getExecutionSemesterName() {
        final ExecutionSemester executionSemester = getExecutionPeriod();
        return executionSemester == null ? null : executionSemester.getName();
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
        return shift == null ? null : shift.getNome();
    }
    
    public String getShiftTypeName() {
        final Shift shift = getShift();
        return shift == null ? null : shift.getShiftTypesPrettyPrint();
    }
    
    public String getTotalHours() {
        return minutesToHours(getTotalMinutes());
    }
    
    public Double getAllocationPercentage() {
        return getShiftProfessorship().getPercentage();
    }
    
    public String getTeacherHours() {
        return getAllocationPercentage() == null ? null : minutesToHours((long) (getAllocationPercentage() / 100 * getTotalMinutes()));
    }
    
    private String minutesToHours(long min) {
        int t = (int) min;
        int hours = t / 60;
        int minutes = t % 60;
        return String.format("%d:%02d", hours, minutes);
    }
    
    private long getTotalMinutes() {
        return getShift().getAssociatedLessonsSet().stream()
                .map(l -> (l.getAllLessonDatesWithoutInstanceDates().size() + l.getLessonInstancesSet().size())
                        * l.getTotalDuration().getStandardMinutes())
                .collect(Collectors.summingLong(i -> i));
    }
    
}