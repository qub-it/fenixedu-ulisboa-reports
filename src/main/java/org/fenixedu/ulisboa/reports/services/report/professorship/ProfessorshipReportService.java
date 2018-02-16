package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.ShiftProfessorship;

import com.google.common.collect.Sets;

public class ProfessorshipReportService {

    private ExecutionYear executionYear;

    public void filterEnrolmentExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Collection<ProfessorshipReport> generateReport() {
        return process(executionYear);
    }

    private Collection<ProfessorshipReport> process(final ExecutionYear executionYear) {
        return buildSearchUniverse().stream().map(r -> buildReport(r)).collect(Collectors.toSet());
    }

    private Set<ShiftProfessorship> buildSearchUniverse() {
        if (executionYear == null) {
            return Sets.newHashSet();
        }

        return executionYear.getExecutionPeriodsSet().stream()

                .flatMap(ep -> ep.getAssociatedExecutionCoursesSet().stream())

                .flatMap(ec -> ec.getAssociatedShifts().stream())

                .flatMap(s -> s.getAssociatedShiftProfessorshipSet().stream())

                .collect(Collectors.toSet());

    }

    private ProfessorshipReport buildReport(final ShiftProfessorship shiftProfessorship) {
        return new ProfessorshipReport(shiftProfessorship);
    }

}