package org.fenixedu.ulisboa.reports.services.report.scholarshiptypology;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.ulisboa.specifications.domain.services.student.RegistrationDataServices;

import com.google.common.collect.Sets;

public class ScholarshipTypologyReportService {

    private ExecutionYear enrolmentExecutionYear;

    public void filterEnrolmentExecutionYear(ExecutionYear executionYear) {
        this.enrolmentExecutionYear = executionYear;
    }

    public Collection<ScholarshipTypologyReport> generateReport() {
        return process(enrolmentExecutionYear);
    }

    private Predicate<ScholarshipTypologyReport> filterPredicate() {
        Predicate<ScholarshipTypologyReport> result = r -> true;

        Predicate<ScholarshipTypologyReport> hasPersonalIngressionDataFilter = r -> r.getPersonalIngressionData() != null;
        result = result.and(hasPersonalIngressionDataFilter);

        return result;
    }

    private Collection<ScholarshipTypologyReport> process(final ExecutionYear executionYear) {
        if (executionYear == null) {
            return Sets.newHashSet();
        }

        final Predicate<ScholarshipTypologyReport> filterPredicate = filterPredicate();

        return buildSearchUniverse(executionYear).stream()

                .map(r -> buildReport(r, executionYear))

                .filter(filterPredicate)

                .collect(Collectors.toSet());
    }

    private Set<Registration> buildSearchUniverse(final ExecutionYear executionYear) {

        final Set<Registration> result = Sets.newHashSet();

        // registration/start execution year relation
        result.addAll(executionYear.getStudentsSet());

        result.addAll(executionYear.getExecutionPeriodsSet().stream()
                .flatMap(semester -> semester.getEnrolmentsSet().stream().map(enrolment -> enrolment.getRegistration()).filter(
                        registration -> RegistrationDataServices.getRegistrationData(registration, executionYear) != null))
                .collect(Collectors.toSet()));

        return result;
    }

    private ScholarshipTypologyReport buildReport(final Registration registration, final ExecutionYear executionYear) {
        final ScholarshipTypologyReport result = new ScholarshipTypologyReport(registration, executionYear);
        return result;
    }

}
