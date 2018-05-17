package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.text.Collator;
import java.util.Comparator;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ShiftProfessorship;

public class ProfessorshipDegreeReport extends ProfessorshipReport {

    private final Degree degree;

    public ProfessorshipDegreeReport(final ShiftProfessorship shiftProfessorship, final Degree degree) {
        super(shiftProfessorship);
        this.degree = degree;
    }

    @Override
    public int compareTo(final ProfessorshipReport o) {

        if (!(o instanceof ProfessorshipDegreeReport)) {
            return super.compareTo(o);
        }

        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<ProfessorshipDegreeReport> byTeacher =
                (x, y) -> instance.compare(x.getTeacherName(), y.getTeacherName());
        final Comparator<ProfessorshipDegreeReport> bySemesterAndYear = (x,
                y) -> ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR.compare(x.getExecutionPeriod(), y.getExecutionPeriod());
        final Comparator<ProfessorshipDegreeReport> byDegreeName =
                (x, y) -> instance.compare(x.getDegreeName(), y.getDegreeName());
        final Comparator<ProfessorshipDegreeReport> byCourseName =
                (x, y) -> instance.compare(x.getExecutionCourseName(), y.getExecutionCourseName());

        return byTeacher.thenComparing(bySemesterAndYear).thenComparing(byDegreeName).thenComparing(byCourseName).compare(this,
                (ProfessorshipDegreeReport) o);
    }

    public String getDegreeName() {
        return this.degree.getNameI18N().getContent();
    }

    public String getDegreeCode() {
        return this.degree.getCode();
    }

}
