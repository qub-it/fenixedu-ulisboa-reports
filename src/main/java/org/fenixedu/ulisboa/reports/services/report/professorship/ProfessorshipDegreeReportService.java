package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.util.Collection;
import java.util.Set;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ShiftProfessorship;

import com.google.common.collect.Sets;

public class ProfessorshipDegreeReportService {

    final Collection<ShiftProfessorship> shiftProfessorships;

    public ProfessorshipDegreeReportService(Collection<ShiftProfessorship> shiftProfessorships) {
        this.shiftProfessorships = shiftProfessorships;
    }

    public Collection<ProfessorshipDegreeReport> generateReport() {
        return process();
    }

    private Collection<ProfessorshipDegreeReport> process() {

        Set<ProfessorshipDegreeReport> result = Sets.newHashSet();
        this.shiftProfessorships.stream().forEach(sp -> sp.getShift().getExecutionCourse().getExecutionDegrees().stream()
                .forEach(ed -> result.add(buildReport(sp, ed.getDegree()))));

        return result;
    }

    private ProfessorshipDegreeReport buildReport(final ShiftProfessorship shiftProfessorship, final Degree degree) {
        return new ProfessorshipDegreeReport(shiftProfessorship, degree);
    }

}
