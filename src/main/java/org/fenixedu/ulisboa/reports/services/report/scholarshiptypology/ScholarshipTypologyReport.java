package org.fenixedu.ulisboa.reports.services.report.scholarshiptypology;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.GrantOwnerType;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.ProfessionType;
import org.fenixedu.academic.domain.ProfessionalSituationConditionType;
import org.fenixedu.academic.domain.SchoolLevelType;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.PersonalIngressionData;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.specifications.domain.PersonUlisboaSpecifications;
import org.fenixedu.ulisboa.specifications.domain.ProfessionTimeType;
import org.fenixedu.ulisboa.specifications.domain.SalarySpan;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;

import com.google.common.collect.Sets;

public class ScholarshipTypologyReport implements Comparable<ScholarshipTypologyReport> {

    private final ExecutionYear executionYear;

    private final Registration registration;

    public ScholarshipTypologyReport(final Registration registration, final ExecutionYear executionYear) {
        this.executionYear = executionYear;
        this.registration = registration;

        if (getStudentCurricularPlan() == null) {

            throw new ULisboaReportsDomainException(
                    "error.RegistrationHistoryReport.found.registration.without.student.curricular.plan",
                    getStudent().getNumber().toString(), getDegree().getCode(), executionYear.getQualifiedName());
        }
    }

    @Override
    public int compareTo(final ScholarshipTypologyReport o) {

        final Comparator<ScholarshipTypologyReport> byYear =
                (x, y) -> ExecutionYear.COMPARATOR_BY_BEGIN_DATE.compare(x.getExecutionYear(), y.getExecutionYear());
        final Comparator<ScholarshipTypologyReport> byRegistrationNumber =
                (x, y) -> x.getRegistrationNumber().compareTo(y.getRegistrationNumber());

        return byYear.thenComparing(byRegistrationNumber).compare(this, o);
    }

    public ExecutionYear getExecutionYear() {
        return this.executionYear;
    }

    public Registration getRegistration() {
        return registration;
    }

    protected Degree getDegree() {
        final Registration registration = getRegistration();
        return registration == null ? null : registration.getDegree();
    }

    private Student getStudent() {
        final Registration registration = getRegistration();
        return registration == null ? null : registration.getStudent();
    }

    private Person getPerson() {
        final Registration registration = getRegistration();
        return registration == null ? null : registration.getPerson();
    }

    public PersonalIngressionData getPersonalIngressionData() {
        final Student student = getStudent();
        return student == null ? null : student.getPersonalIngressionDataByExecutionYear(executionYear);
    }

    private PersonUlisboaSpecifications getPersonUlisboaSpecifications() {
        final Person person = getPerson();
        return person == null ? null : person.getPersonUlisboaSpecifications();
    }

    public StudentCurricularPlan getStudentCurricularPlan() {
        if (registration.getStudentCurricularPlansSet().size() == 1) {
            return registration.getLastStudentCurricularPlan();
        }

        StudentCurricularPlan studentCurricularPlan = registration.getStudentCurricularPlan(getExecutionYear());

        if (studentCurricularPlan != null) {
            return studentCurricularPlan;
        }

        studentCurricularPlan = registration.getFirstStudentCurricularPlan();

        if (studentCurricularPlan.getStartExecutionYear().isAfterOrEquals(getExecutionYear())) {
            return studentCurricularPlan;
        }

        return null;
    }

    public String getStudentNumber() {
        final Student student = getStudent();
        return student == null ? null : student.getNumber().toString();
    }

    public String getRegistrationNumber() {
        final Registration registration = getRegistration();
        return registration == null ? null : registration.getNumber().toString();
    }

    public String getDegreeCode() {
        String result = null;

        final Degree degree = getDegree();
        final String ministryCode = degree == null ? null : degree.getMinistryCode();
        final String code = degree == null ? null : degree.getCode();

        if (ministryCode != null) {
            result = ministryCode;
        }

        if (code != null && !code.equals(ministryCode)) {
            result = result.isEmpty() ? code : (result + " [" + code + "]");
        }

        return result;
    }

    public String getPersonName() {
        final Person person = getPerson();
        return person == null ? null : person.getName();
    }

    public String getOtherConcludedRegistrationYears() {
        final StringBuilder result = new StringBuilder();

        getStudent().getRegistrationsSet().stream()

                .filter(r -> r != registration && r.isConcluded() && r.getLastStudentCurricularPlan() != null)

                .forEach(r -> {

                    final SortedSet<ExecutionYear> executionYears =
                            Sets.newTreeSet(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed());
                    executionYears.addAll(RegistrationServices.getEnrolmentYears(r));

                    if (!executionYears.isEmpty()) {
                        result.append(executionYears.first().getQualifiedName()).append("|");
                    }

                });

        return result.toString().endsWith("|") ? result.delete(result.length() - 1, result.length()).toString() : result
                .toString();
    }

    public ProfessionalSituationConditionType getFatherProfessionalCondition() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getFatherProfessionalCondition();
    }

    public ProfessionType getFatherProfessionType() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getFatherProfessionType();
    }

    public SchoolLevelType getFatherSchoolLevel() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getFatherSchoolLevel();
    }

    public Unit getGrantOwnerProvider() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getGrantOwnerProvider();
    }

    public GrantOwnerType getGrantOwnerType() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getGrantOwnerType();
    }

    public SalarySpan getHouseholdSalarySpan() {
        return Optional.ofNullable(getPersonUlisboaSpecifications())
                .map(p -> p.getPersonUlisboaSpecificationsByExcutionYear(this.executionYear)).map(p -> p.getHouseholdSalarySpan())
                .orElse(null);
    }

    public ProfessionalSituationConditionType getMotherProfessionalCondition() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getMotherProfessionalCondition();
    }

    public ProfessionType getMotherProfessionType() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getMotherProfessionType();
    }

    public SchoolLevelType getMotherSchoolLevel() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getMotherSchoolLevel();
    }

    public ProfessionalSituationConditionType getProfessionalCondition() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getProfessionalCondition();
    }

    public ProfessionType getProfessionalType() {
        final PersonalIngressionData personalIngressionData = getPersonalIngressionData();
        return personalIngressionData == null ? null : personalIngressionData.getProfessionType();
    }

    public ProfessionTimeType getProfessionTimeType() {
        return Optional.ofNullable(getPersonUlisboaSpecifications())
                .map(p -> p.getPersonUlisboaSpecificationsByExcutionYear(this.executionYear)).map(p -> p.getProfessionTimeType())
                .orElse(null);
    }
}