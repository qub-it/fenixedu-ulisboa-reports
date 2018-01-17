package org.fenixedu.ulisboa.reports.dto.report.registrationhistory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.ProgramConclusion;
import org.fenixedu.academic.domain.student.RegistrationProtocol;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

public class RegistrationHistoryReportParametersBean implements IBean {

    private Set<ExecutionYear> executionYears = Sets.newHashSet();
    private Set<DegreeType> degreeTypes = Sets.newHashSet();
    private Set<Degree> degrees = Sets.newHashSet();
    private Set<RegistrationRegimeType> regimeTypes = Sets.newHashSet();
    private Set<RegistrationProtocol> registrationProtocols = Sets.newHashSet();
    private Set<IngressionType> ingressionTypes = Sets.newHashSet();
    private Set<RegistrationStateType> registrationStateTypes = Sets.newHashSet();
    private Set<StatuteType> statuteTypes = Sets.newHashSet();
    private Boolean firstTimeOnly;
    private Boolean withEnrolments = Boolean.FALSE;
    private Boolean dismissalsOnly;
    private Boolean improvementEnrolmentsOnly;
    private Boolean exportQualificationAndOriginInfo = Boolean.FALSE;
    private Boolean exportPersonalInfo = Boolean.FALSE;
    private Boolean exportContacts = Boolean.FALSE;
    private Boolean exportConclusionData = Boolean.FALSE;
    private Boolean exportExecutionYearData = Boolean.FALSE;
    private Integer studentNumber;
    private Set<ExecutionYear> graduatedExecutionYears = Sets.newHashSet();
    private LocalDate graduationPeriodStartDate;
    private LocalDate graduationPeriodEndDate;
    private Set<ProgramConclusion> programConclusions = Sets.newHashSet();

    private List<TupleDataSourceBean> executionYearsDataSource;
    private List<TupleDataSourceBean> degreeTypesDataSource;
    private List<TupleDataSourceBean> degreesDataSource;
    private List<TupleDataSourceBean> regimeTypesDataSource;
    private List<TupleDataSourceBean> registrationProtocolsDataSource;
    private List<TupleDataSourceBean> ingressionTypesDataSource;
    private List<TupleDataSourceBean> registrationStateTypesDataSource;
    private List<TupleDataSourceBean> statuteTypesDataSource;
    private List<TupleDataSourceBean> programConclusionsDataSource;

    public Set<ExecutionYear> getExecutionYears() {
        return executionYears;
    }

    public void setExecutionYears(Set<ExecutionYear> executionYears) {
        this.executionYears = executionYears;
    }

    public Set<DegreeType> getDegreeTypes() {
        return degreeTypes;
    }

    public Set<ExecutionYear> getGraduatedExecutionYears() {
        return graduatedExecutionYears;
    }

    public void setGraduatedExecutionYears(Set<ExecutionYear> graduatedExecutionYears) {
        this.graduatedExecutionYears = graduatedExecutionYears;
    }

    public LocalDate getGraduationPeriodStartDate() {
        return graduationPeriodStartDate;
    }

    public void setGraduationPeriodStartDate(LocalDate graduationPeriodStartDate) {
        this.graduationPeriodStartDate = graduationPeriodStartDate;
    }

    public LocalDate getGraduationPeriodEndDate() {
        return graduationPeriodEndDate;
    }

    public void setGraduationPeriodEndDate(LocalDate graduationPeriodEndDate) {
        this.graduationPeriodEndDate = graduationPeriodEndDate;
    }

    public void setDegreeTypes(Set<DegreeType> degreeTypes) {
        this.degreeTypes = degreeTypes;
    }

    public Set<RegistrationRegimeType> getRegimeTypes() {
        return regimeTypes;
    }

    public void setRegimeTypes(Set<RegistrationRegimeType> regimeTypes) {
        this.regimeTypes = regimeTypes;
    }

    public Set<RegistrationProtocol> getRegistrationProtocols() {
        return registrationProtocols;
    }

    public void setRegistrationProtocols(Set<RegistrationProtocol> registrationProtocols) {
        this.registrationProtocols = registrationProtocols;
    }

    public Set<IngressionType> getIngressionTypes() {
        return ingressionTypes;
    }

    public void setIngressionTypes(Set<IngressionType> ingressionTypes) {
        this.ingressionTypes = ingressionTypes;
    }

    public Set<RegistrationStateType> getRegistrationStateTypes() {
        return registrationStateTypes;
    }

    public void setRegistrationStateTypes(Set<RegistrationStateType> registrationStateTypes) {
        this.registrationStateTypes = registrationStateTypes;
    }

    public Set<StatuteType> getStatuteTypes() {
        return statuteTypes;
    }

    public void setStatuteTypes(Set<StatuteType> statuteTypes) {
        this.statuteTypes = statuteTypes;
    }

    public Set<Degree> getDegrees() {
        return degrees;
    }

    public void setDegrees(Set<Degree> degrees) {
        this.degrees = degrees;
    }

    public Set<ProgramConclusion> getProgramConclusions() {
        return programConclusions;
    }

    public void setProgramConclusions(Set<ProgramConclusion> programConclusions) {
        this.programConclusions = programConclusions;
    }

    public List<TupleDataSourceBean> getExecutionYearsDataSource() {
        return executionYearsDataSource;
    }

    public List<TupleDataSourceBean> getDegreeTypesDataSource() {
        return degreeTypesDataSource;
    }

    public List<TupleDataSourceBean> getDegreesDataSource() {
        return degreesDataSource;
    }

    public List<TupleDataSourceBean> getRegimeTypesDataSource() {
        return regimeTypesDataSource;
    }

    public List<TupleDataSourceBean> getIngressionTypesDataSource() {
        return ingressionTypesDataSource;
    }

    public List<TupleDataSourceBean> getRegistrationProtocolsDataSource() {
        return registrationProtocolsDataSource;
    }

    public List<TupleDataSourceBean> getRegistrationStateTypesDataSource() {
        return registrationStateTypesDataSource;
    }

    public List<TupleDataSourceBean> getStatuteTypesDataSource() {
        return statuteTypesDataSource;
    }

    public List<TupleDataSourceBean> getProgramConclusionsDataSource() {
        return programConclusionsDataSource;
    }

    public Boolean getFirstTimeOnly() {
        return firstTimeOnly;
    }

    public Boolean getFilterWithEnrolments() {
        return withEnrolments;
    }

    public Boolean getDismissalsOnly() {
        return dismissalsOnly;
    }

    public Boolean getImprovementEnrolmentsOnly() {
        return improvementEnrolmentsOnly;
    }

    public Boolean getExportQualificationAndOriginInfo() {
        return exportQualificationAndOriginInfo;
    }

    public Boolean getExportPersonalInfo() {
        return exportPersonalInfo;
    }

    public Boolean getExportContacts() {
        return exportContacts;
    }

    public Boolean getExportConclusionData() {
        return exportConclusionData;
    }

    public Boolean getExportExecutionYearData() {
        return exportExecutionYearData;
    }

    public RegistrationHistoryReportParametersBean() {

        updateData();

    }

    public void updateData() {

        this.executionYearsDataSource =
                ExecutionYear.readNotClosedExecutionYears().stream().sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed())
                        .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getQualifiedName())).collect(Collectors.toList());

        this.degreeTypesDataSource = Bennu.getInstance().getDegreeTypeSet().stream()
                .sorted((x, y) -> x.getName().getContent().compareTo(y.getName().getContent()))
                .map(x -> new TupleDataSourceBean(x.getExternalId(), x.getName().getContent())).collect(Collectors.toList());

        this.degreesDataSource =
                Bennu.getInstance().getDegreesSet().stream()
                        .filter(d -> getDegreeTypes() == null || getDegreeTypes().contains(d.getDegreeType()))
                        .sorted(Degree.COMPARATOR_BY_DEGREE_TYPE_AND_NAME_AND_ID)
                        .map(x -> new TupleDataSourceBean(x.getExternalId(),
                                "[" + x.getCode() + "] " + x.getPresentationNameI18N().getContent()))
                        .collect(Collectors.toList());

        this.regimeTypesDataSource = Arrays.asList(RegistrationRegimeType.values()).stream()
                .map(x -> new TupleDataSourceBean(x.getName(), x.getLocalizedName())).collect(Collectors.toList());

        this.registrationProtocolsDataSource = Bennu.getInstance().getRegistrationProtocolsSet().stream()
                .sorted((x, y) -> x.getDescription().getContent().compareTo(y.getDescription().getContent()))
                .map(x -> new TupleDataSourceBean(x.getExternalId(), "[" + x.getCode() + "] " + x.getDescription().getContent()))
                .collect(Collectors.toList());

        this.ingressionTypesDataSource = Bennu.getInstance().getIngressionTypesSet().stream()
                .sorted((x, y) -> x.getDescription().getContent().compareTo(y.getDescription().getContent()))
                .map(x -> new TupleDataSourceBean(x.getExternalId(), "[" + x.getCode() + "] " + x.getDescription().getContent()))
                .collect(Collectors.toList());

        this.registrationStateTypesDataSource = Arrays.asList(RegistrationStateType.values()).stream()
                .sorted((x, y) -> x.getDescription().compareTo(y.getDescription()))
                .map(x -> new TupleDataSourceBean(x.getName(), x.getDescription())).collect(Collectors.toList());

        this.statuteTypesDataSource = Bennu.getInstance().getStatuteTypesSet().stream().sorted(StatuteType.COMPARATOR_BY_NAME)
                .map(x -> new TupleDataSourceBean(x.getExternalId(), "[" + x.getCode() + "] " + x.getName().getContent()))
                .collect(Collectors.toList());

        if (getDegreeTypes() == null) {
            this.programConclusionsDataSource = Collections.emptyList();
        } else {
            this.programConclusionsDataSource =
                    getDegreeTypes().stream().flatMap(dt -> dt.getDegreeSet().stream())
                            .flatMap(d -> d.getDegreeCurricularPlansSet().stream())
                            .flatMap(dcp -> ProgramConclusion.conclusionsFor(dcp)).distinct()
                            .map(pc -> new TupleDataSourceBean(pc.getExternalId(),
                                    pc.getName().getContent() + " - " + pc.getDescription().getContent()))
                            .collect(Collectors.toList());
        }

    }

    public Integer getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(Integer studentNumber) {
        this.studentNumber = studentNumber;
    }

}
