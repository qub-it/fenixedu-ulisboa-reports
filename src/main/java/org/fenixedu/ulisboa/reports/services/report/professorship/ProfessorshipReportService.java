package org.fenixedu.ulisboa.reports.services.report.professorship;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.ShiftProfessorship;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.collect.Sets;

public class ProfessorshipReportService {

    private ExecutionYear executionYear;

    public void filterEnrolmentExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public Collection<ProfessorshipReport> generateReport() {
        return process(executionYear);
    }

    private Predicate<ProfessorshipReport> filterPredicate() {
        Predicate<ProfessorshipReport> result = r -> true;
        
        Predicate<ProfessorshipReport> executionYearFilter =
                r -> r.getExecutionYear().equals(executionYear);
        result = result.and(executionYearFilter);

        return result;
    }

    private Collection<ProfessorshipReport> process(final ExecutionYear executionYear) {

        final Predicate<ProfessorshipReport> filterPredicate = filterPredicate();

        return buildSearchUniverse().stream()

                .map(r -> buildReport(r))

                .filter(filterPredicate)

                .collect(Collectors.toSet());
    }

    private Set<ShiftProfessorship> buildSearchUniverse() {

        final Set<ShiftProfessorship> result = Sets.newHashSet();
        
        result.addAll(Bennu.getInstance().getShiftProfessorshipsSet());
     
        return result;
    }

    private ProfessorshipReport buildReport(final ShiftProfessorship shiftProfessorship) {
        final ProfessorshipReport result = new ProfessorshipReport(shiftProfessorship);
        return result;
    }
    
}