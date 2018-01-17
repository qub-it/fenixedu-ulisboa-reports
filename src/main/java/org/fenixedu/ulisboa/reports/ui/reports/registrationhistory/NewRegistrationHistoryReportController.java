package org.fenixedu.ulisboa.reports.ui.reports.registrationhistory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.academic.domain.DomainObjectUtil;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.candidacy.StudentCandidacy;
import org.fenixedu.academic.domain.degreeStructure.ProgramConclusion;
import org.fenixedu.academic.domain.degreeStructure.RegimeType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.StudentStatute;
import org.fenixedu.academic.domain.student.curriculum.Curriculum;
import org.fenixedu.academic.domain.student.curriculum.ICurriculum;
import org.fenixedu.academic.domain.student.curriculum.ICurriculumEntry;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.dto.student.RegistrationConclusionBean;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilderForXLSX;
import org.fenixedu.ulisboa.specifications.domain.CompetenceCourseServices;
import org.fenixedu.ulisboa.specifications.domain.evaluation.EvaluationComparator;
import org.fenixedu.ulisboa.specifications.domain.evaluation.season.EvaluationSeasonServices;
import org.fenixedu.ulisboa.reports.domain.exceptions.ULisboaReportsDomainException;
import org.fenixedu.ulisboa.specifications.domain.file.ULisboaSpecificationsTemporaryFile;
import org.fenixedu.ulisboa.specifications.domain.services.CurricularPeriodServices;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;
import org.fenixedu.ulisboa.specifications.domain.services.enrollment.EnrolmentServices;
import org.fenixedu.ulisboa.reports.dto.report.registrationhistory.RegistrationHistoryReportParametersBean;
import org.fenixedu.ulisboa.reports.services.report.registrationhistory.RegistrationHistoryReport;
import org.fenixedu.ulisboa.reports.services.report.registrationhistory.RegistrationHistoryReportService;
import org.fenixedu.ulisboa.reports.ui.FenixeduULisboaReportsBaseController;
import org.fenixedu.ulisboa.reports.ui.FenixeduULisboaReportsController;
import org.fenixedu.ulisboa.specifications.ui.registrationsdgesexport.RegistrationDGESStateBeanController;
import org.fenixedu.ulisboa.specifications.ui.registrationsdgesexport.RegistrationDGESStateBeanController.RegistrationDGESStateBean;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@SpringFunctionality(app = FenixeduULisboaReportsController.class, title = "label.title.reports.registrationHistory", accessGroup = "logged")
@RequestMapping(NewRegistrationHistoryReportController.CONTROLLER_URL)
public class NewRegistrationHistoryReportController extends FenixeduULisboaReportsBaseController {

    public static final String CONTROLLER_URL =
            "/fenixedu-ulisboa-reports/reports/registrationhistory/registrationhistoryreport";

    private static final String JSP_PATH = CONTROLLER_URL.substring(1);

    private void setParametersBean(RegistrationHistoryReportParametersBean bean, Model model) {
        model.addAttribute("beanJson", getBeanJson(bean));
        model.addAttribute("bean", bean);
    }

    @RequestMapping
    public String home(Model model, RedirectAttributes redirectAttributes) {
        return redirect(CONTROLLER_URL + "/search", model, redirectAttributes);
    }

    @RequestMapping(value = "/search")
    public String search(Model model, RedirectAttributes redirectAttributes) {
        setParametersBean(new RegistrationHistoryReportParametersBean(), model);
        return jspPage("registrationhistoryreport");
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("bean") RegistrationHistoryReportParametersBean bean, Model model,
            RedirectAttributes redirectAttributes) {
        setParametersBean(bean, model);

        setResults(generateReport(bean), model);

        return jspPage("registrationhistoryreport");
    }

    private void setResults(Collection<RegistrationHistoryReport> results, Model model) {
        model.addAttribute("results", results);
    }

    static private String getReportId(final String exportName) {
        return normalizeName(bundle("label.event.reports.registrationHistory." + exportName), "_") + "_UUID_"
                + UUID.randomUUID().toString();
    }

    static private String getFilename(final String reportId) {
        return reportId.substring(0, reportId.indexOf("_UUID_"));
    }

    static public String normalizeName(final String input, final String replacement) {
        // ex [ ] * ? : / \
        String result = Normalizer.normalize(input, java.text.Normalizer.Form.NFD)

                .replaceAll("[^\\p{ASCII}]", "")

                .replace(" ", replacement)

                .replace("[", replacement)

                .replace("]", replacement)

                .replace("*", replacement)

                .replace("?", replacement)

                .replace(":", replacement)

                .replace("/", replacement)

                .replace("\\", replacement);

        while (result.contains(replacement + replacement)) {
            result = result.replace(replacement + replacement, replacement);
        }

        return result.trim();
    }

    static private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }

    @RequestMapping(value = "/exportregistrations", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportRegistrations(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportRegistrations");
        new Thread(() -> processReport(this::exportRegistrationsToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    @Atomic(mode = TxMode.READ)
    protected void processReport(final Function<RegistrationHistoryReportParametersBean, byte[]> reportProcessor,
            final RegistrationHistoryReportParametersBean bean, final String reportId) {

        byte[] content = null;
        try {
            content = reportProcessor.apply(bean);
        } catch (Throwable e) {
            content = createXLSWithError(
                    e instanceof ULisboaReportsDomainException ? ((ULisboaReportsDomainException) e)
                            .getLocalizedMessage() : ExceptionUtils.getFullStackTrace(e));
        }

        ULisboaSpecificationsTemporaryFile.create(reportId, content, Authenticate.getUser());
    }

    @RequestMapping(value = "/exportstatus/{reportId}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportStatus(@PathVariable(value = "reportId") final String reportId,
            final Model model) {
        return new ResponseEntity<String>(
                String.valueOf(
                        ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate.getUser(), reportId).isPresent()),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/downloadreport/{reportId}", method = RequestMethod.GET)
    public void downloadReport(@PathVariable("reportId") String reportId, final Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) throws IOException {
        final Optional<ULisboaSpecificationsTemporaryFile> temporaryFile =
                ULisboaSpecificationsTemporaryFile.findByUserAndFilename(Authenticate.getUser(), reportId);
        writeFile(response, getFilename(reportId) + "_" + new DateTime().toString("yyyy-MM-dd_HH-mm-ss") + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", temporaryFile.get().getContent());
    }

    static private Collection<RegistrationHistoryReport> generateReport(final RegistrationHistoryReportParametersBean bean) {

        final RegistrationHistoryReportService service = new RegistrationHistoryReportService();
        service.filterEnrolmentExecutionYears(bean.getExecutionYears());
        service.filterDegrees(bean.getDegrees());
        service.filterDegreeTypes(bean.getDegreeTypes());
        service.filterIngressionTypes(bean.getIngressionTypes());
        service.filterRegimeTypes(bean.getRegimeTypes());
        service.filterRegistrationProtocols(bean.getRegistrationProtocols());
        service.filterRegistrationStateTypes(bean.getRegistrationStateTypes());
        service.filterStatuteTypes(bean.getStatuteTypes());
        service.filterFirstTimeOnly(bean.getFirstTimeOnly());
        service.filterWithEnrolments(bean.getFilterWithEnrolments());
        service.filterDismissalsOnly(bean.getDismissalsOnly());
        service.filterImprovementEnrolmentsOnly(bean.getImprovementEnrolmentsOnly());
        service.filterStudentNumber(bean.getStudentNumber());

        service.filterGraduatedExecutionYears(bean.getGraduatedExecutionYears());
        service.filterGraduationPeriodStartDate(bean.getGraduationPeriodStartDate());
        service.filterGraduationPeriodEndDate(bean.getGraduationPeriodEndDate());
        service.filterProgramConclusions(bean.getProgramConclusions());

        return service.generateReport().stream().sorted().collect(Collectors.toList());
    }

    private byte[] exportRegistrationsToXLS(final RegistrationHistoryReportParametersBean bean) {
        final Collection<RegistrationHistoryReport> toExport = generateReport(bean);

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.students"),
                new SheetData<RegistrationHistoryReport>(toExport) {

                    @Override
                    protected void makeLine(final RegistrationHistoryReport report) {
                        addPrimaryData(report);
                        addExecutionYearData(bean, report);
                        addConclusionData(bean, report);
                        addQualificationAndOriginInfo(bean, report);
                        addPersonalData(bean, report);
                        addContactsData(bean, report);
                    }

                    private void addPrimaryData(final RegistrationHistoryReport report) {
                        addData("RegistrationHistoryReport.executionYear", report.getExecutionYear().getQualifiedName());
                        addData("Student.number", report.getStudentNumber());
                        addData("Registration.number", report.getRegistrationNumber());
                        addData("Person.username", report.getUsername());
                        addData("Person.name", report.getPersonName());
                        addData("Degree.ministryCode", report.getDegreeCode());
                        addData("Degree.degreeType", report.getDegreeTypeName());
                        addData("Degree.presentationName", report.getDegreePresentationName());
                        addData("Registration.ingressionType", report.getIngressionTypeDescription());
                        addData("Registration.registrationProtocol", report.getRegistrationProtocolDescription());
                        addData("Registration.startDate", report.getStartDate());
                        addData("Registration.firstStateDate", report.getFirstRegistrationStateDate());
                        addData("Registration.registrationYear", report.getRegistrationYear());
                        addData("RegistrationHistoryReport.studentCurricularPlan", report.getStudentCurricularPlanName());
                        addData("RegistrationHistoryReport.studentCurricularPlan.count", report.getStudentCurricularPlanCount());
                        addData("RegistrationHistoryReport.schoolClasses", report.getSchoolClasses());
                        addData("RegistrationHistoryReport.isReingression", report.isReingression());
                        addData("RegistrationHistoryReport.hasPreviousReingression", report.hasPreviousReingression());
                        addData("RegistrationHistoryReport.curricularYear", report.getCurricularYear());
                        addData("RegistrationHistoryReport.previousYearCurricularYear", report.getPreviousYearCurricularYear());
                        addData("RegistrationHistoryReport.nextYearCurricularYear", report.getNextYearCurricularYear());
                        addData("RegistrationHistoryReport.ectsCredits", report.getEctsCredits());
                        addData("RegistrationHistoryReport.average", report.getAverage());
                        addData("RegistrationHistoryReport.currentAverage", report.getCurrentAverage());
                        addData("RegistrationHistoryReport.enrolmentYears", report.getEnrolmentYears());
                        addData("RegistrationHistoryReport.enrolmentYearsForPrescription",
                                report.isPrescriptionConfigured() ? report.getEnrolmentYearsForPrescription().toString() : "-");
                        addData("RegistrationHistoryReport.canPrescribe",
                                report.isPrescriptionConfigured() ? booleanString(report.canPrescribe()) : "-");
                        addData("RegistrationHistoryReport.enrolmentDate", report.getEnrolmentDate());
                        addData("Registration.lastEnrolmentExecutionYear", report.getLastEnrolmentExecutionYear());
                        addData("RegistrationHistoryReport.primaryBranch", report.getPrimaryBranchName());
                        addData("RegistrationHistoryReport.secondaryBranch", report.getSecondaryBranchName());
                        addData("RegistrationHistoryReport.statutes", report.getStudentStatutesNames());
                        addData("RegistrationHistoryReport.regimeType", report.getRegimeTypeName());
                        addData("RegistrationHistoryReport.enrolmentsWithoutShifts", report.hasEnrolmentsWithoutShifts());
                        addData("RegistrationHistoryReport.inactiveRegistrationStateForYear",
                                report.hasAnyInactiveRegistrationStateForYear());

                        addData("RegistrationHistoryReport.lastRegistrationState", report.getLastRegistrationStateType());
                        addData("RegistrationHistoryReport.lastRegistrationStateDate", report.getLastRegistrationStateDate());
                        addData("RegistrationHistoryReport.firstTime", report.isFirstTime());
                        addData("RegistrationHistoryReport.dismissals", report.hasDismissals());
                        addData("RegistrationHistoryReport.enroledInImprovement", report.hasImprovementEvaluations());
                        addData("RegistrationHistoryReport.annulledEnrolments", report.hasAnnulledEnrolments());
                        addData("RegistrationHistoryReport.enrolmentsCount", report.getEnrolmentsCount());
                        addData("RegistrationHistoryReport.enrolmentsCredits", report.getEnrolmentsCredits());
                        addData("RegistrationHistoryReport.extraCurricularEnrolmentsCount",
                                report.getExtraCurricularEnrolmentsCount());
                        addData("RegistrationHistoryReport.extraCurricularEnrolmentsCredits",
                                report.getExtraCurricularEnrolmentsCredits());
                        addData("RegistrationHistoryReport.standaloneEnrolmentsCount", report.getStandaloneEnrolmentsCount());
                        addData("RegistrationHistoryReport.standaloneEnrolmentsCredits", report.getStandaloneEnrolmentsCredits());
                        addData("RegistrationHistoryReport.tuitionCharged", report.isTuitionCharged());
                        addData("RegistrationHistoryReport.tuitionAmount", report.getTuitionAmount().toPlainString());
                        addData("Registration.researchArea",
                                report.getResearchArea() != null ? report.getResearchArea().getName().getContent() : null);
                        addData("Registration.registrationObservations", report.getRegistrationObservations());
                    }

                    private void addExecutionYearData(final RegistrationHistoryReportParametersBean bean,
                            final RegistrationHistoryReport report) {

                        if (bean.getExportExecutionYearData()) {

                            addData("RegistrationHistoryReport.executionYearSimpleAverage",
                                    report.getExecutionYearSimpleAverage());
                            addData("RegistrationHistoryReport.executionYearWeightedAverage",
                                    report.getExecutionYearWeightedAverage());
                            addData("RegistrationHistoryReport.executionYearEnroledMandatoryFlunked",
                                    report.getExecutionYearEnroledMandatoryFlunked());
                            addData("RegistrationHistoryReport.executionYearEnroledMandatoryInAdvance",
                                    report.getExecutionYearEnroledMandatoryInAdvance());
                            addData("RegistrationHistoryReport.executionYearCreditsMandatoryEnroled",
                                    report.getExecutionYearCreditsMandatoryEnroled());
                            addData("RegistrationHistoryReport.executionYearCreditsMandatoryApproved",
                                    report.getExecutionYearCreditsMandatoryApproved());
                            addData("RegistrationHistoryReport.executionYearConclusionDate",
                                    report.getExecutionYearConclusionDate());
                        }
                    }

                    private void addConclusionData(final RegistrationHistoryReportParametersBean parametersBean,
                            final RegistrationHistoryReport report) {

                        if (parametersBean.getExportConclusionData()) {

                            for (final ProgramConclusion programConclusion : report.getProgramConclusions()) {

                                final RegistrationConclusionBean bean = report.getConclusionReportFor(programConclusion);

                                final String concluded = bean == null ? null : booleanString(bean.isConcluded());
                                addCell(labelFor(programConclusion, "concluded"), concluded);

                                final String conclusionProcessed =
                                        bean == null ? null : booleanString(bean.isConclusionProcessed());
                                addCell(labelFor(programConclusion, "conclusionProcessed"), conclusionProcessed);

                                final String rawGrade =
                                        bean == null || bean.getRawGrade() == null ? null : bean.getRawGrade().getValue();
                                addCell(labelFor(programConclusion, "rawGrade"), rawGrade);

                                final String finalGrade =
                                        bean == null || bean.getFinalGrade() == null ? null : bean.getFinalGrade().getValue();
                                addCell(labelFor(programConclusion, "finalGrade"), finalGrade);

                                final String descriptiveGrade = bean == null || bean.getDescriptiveGrade() == null ? null : bean
                                        .getDescriptiveGradeExtendedValue() + " (" + bean.getDescriptiveGrade().getValue() + ")";
                                addCell(labelFor(programConclusion, "descriptiveGrade"), descriptiveGrade);

                                final YearMonthDay conclusionDate =
                                        bean == null || bean.getConclusionDate() == null ? null : bean.getConclusionDate();
                                addCell(labelFor(programConclusion, "conclusionDate"), conclusionDate);

                                final String conclusionYear = bean == null || bean.getConclusionYear() == null ? null : bean
                                        .getConclusionYear().getQualifiedName();
                                addCell(labelFor(programConclusion, "conclusionYear"), conclusionYear);

                                final String ectsCredits = bean == null ? null : String.valueOf(bean.getEctsCredits());
                                addCell(labelFor(programConclusion, "ectsCredits"), ectsCredits);

                            }

                            addData("RegistrationHistoryReport.otherConcludedRegistrationYears",
                                    report.getOtherConcludedRegistrationYears());
                        }
                    }

                    private String labelFor(ProgramConclusion programConclusion, String field) {
                        final String programConclusionPrefix = programConclusion.getName().getContent() + " - "
                                + programConclusion.getDescription().getContent() + ": ";

                        return programConclusionPrefix + bundle("label.RegistrationConclusionBean." + field);
                    }

                    private void addQualificationAndOriginInfo(final RegistrationHistoryReportParametersBean bean,
                            final RegistrationHistoryReport report) {

                        if (bean.getExportQualificationAndOriginInfo()) {

                            addData("PrecedentDegreeInformation.institutionUnit", report.getQualificationInstitutionName());
                            addData("PrecedentDegreeInformation.schoolLevel", report.getQualificationSchoolLevel());
                            addData("PrecedentDegreeInformation.degreeDesignation", report.getQualificationDegreeDesignation());
                            addData("PrecedentDegreeInformation.precedentInstitution", report.getOriginInstitutionName());
                            addData("PrecedentDegreeInformation.precedentSchoolLevel", report.getOriginSchoolLevel());
                            addData("PrecedentDegreeInformation.precedentDegreeDesignation", report.getOriginDegreeDesignation());
                        }
                    }

                    private void addPersonalData(final RegistrationHistoryReportParametersBean bean,
                            final RegistrationHistoryReport report) {

                        if (bean.getExportPersonalInfo()) {

                            addData("Person.idDocumentType", report.getIdDocumentType());
                            addData("Person.idDocumentNumber", report.getDocumentIdNumber());
                            addData("Person.gender", report.getGender());
                            addData("Person.dateOfBirth", report.getDateOfBirthYearMonthDay());
                            addData("Person.nameOfFather", report.getNameOfFather());
                            addData("Person.nameOfMother", report.getNameOfMother());
                            addData("Person.nationality", report.getNationality());
                            addData("Person.countryOfBirth", report.getCountryOfBirth());
                            addData("Person.socialSecurityNumber", report.getFiscalNumber());
                            addData("Person.districtOfBirth", report.getDistrictOfBirth());
                            addData("Person.districtSubdivisionOfBirth", report.getDistrictSubdivisionOfBirth());
                            addData("Person.parishOfBirth", report.getParishOfBirth());
                            addData("Student.studentPersonalDataAuthorizationChoice",
                                    report.getStudentPersonalDataAuthorizationChoice());
                        }
                    }

                    private void addContactsData(final RegistrationHistoryReportParametersBean bean,
                            final RegistrationHistoryReport report) {

                        if (bean.getExportContacts()) {

                            addData("Person.defaultEmailAddress", report.getDefaultEmailAddressValue());
                            addData("Person.institutionalEmailAddress", report.getInstitutionalEmailAddressValue());
                            addData("Person.otherEmailAddresses", report.getOtherEmailAddresses());
                            addData("Person.defaultPhone", report.getDefaultPhoneNumber());
                            addData("Person.defaultMobilePhone", report.getDefaultMobilePhoneNumber());

                            if (report.hasDefaultPhysicalAddress()) {
                                addData("PhysicalAddress.address", report.getDefaultPhysicalAddress());
                                addData("PhysicalAddress.districtOfResidence",
                                        report.getDefaultPhysicalAddressDistrictOfResidence());
                                addData("PhysicalAddress.districtSubdivisionOfResidence",
                                        report.getDefaultPhysicalAddressDistrictSubdivisionOfResidence());
                                addData("PhysicalAddress.parishOfResidence", report.getDefaultPhysicalAddressParishOfResidence());
                                addData("PhysicalAddress.area", report.getDefaultPhysicalAddressArea());
                                addData("PhysicalAddress.areaCode", report.getDefaultPhysicalAddressAreaCode());
                                addData("PhysicalAddress.areaOfAreaCode", report.getDefaultPhysicalAddressAreaOfAreaCode());
                                addData("PhysicalAddress.countryOfResidence",
                                        report.getDefaultPhysicalAddressCountryOfResidenceName());
                            }
                        }
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                    private void addData(final String key, final Boolean value) {
                        addCell(bundle("label." + key), value == null ? "" : booleanString(value));
                    }

                    private void addData(final String key, boolean value) {
                        addCell(bundle("label." + key), booleanString(value));
                    }

                    private String booleanString(final boolean value) {
                        return value ? bundle("label.yes") : bundle("label.no");
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    private byte[] createXLSWithError(String error) {

        try {

            final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
            builder.addSheet("Registrations", new SheetData<String>(Collections.singleton(error)) {
                @Override
                protected void makeLine(final String item) {
                    addCell(ULisboaReportsUtil.bundle("label.unexpected.error.occured"), item);
                }
            });

            final ByteArrayOutputStream result = new ByteArrayOutputStream();
            builder.build(result);

            return result.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String _POSTBACK_URI = "/postback";
    public static final String POSTBACK_URL = CONTROLLER_URL + _POSTBACK_URI;

    @RequestMapping(value = _POSTBACK_URI, method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> postback(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        bean.updateData();

        return new ResponseEntity<String>(getBeanJson(bean), HttpStatus.OK);
    }

    private String jspPage(final String page) {
        return JSP_PATH + "/" + page;
    }

    @RequestMapping(value = "/exportapprovals", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportApprovals(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportApprovals");
        new Thread(() -> processReport(this::exportApprovalsToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    private byte[] exportApprovalsToXLS(final RegistrationHistoryReportParametersBean bean) {
        final Collection<RegistrationHistoryReport> reports = generateReport(bean);

        // TODO extract all this logic to a future RegistrationHistoryApprovalReport

        final Collection<ICurriculum> curriculums =
                reports.stream().map(r -> RegistrationServices.getCurriculum(r.getRegistration(), (ExecutionYear) null))
                        .sorted((x, y) -> x.getStudentCurricularPlan().getRegistration().getNumber()
                                .compareTo(x.getStudentCurricularPlan().getRegistration().getNumber()))
                        .distinct().collect(Collectors.toList());

        final Multimap<ICurriculum, ICurriculumEntry> toExport = HashMultimap.create();
        curriculums.stream().forEach(c -> toExport.putAll(c, c.getCurriculumEntries()));

        final ExecutionYear executionYearForCurricularYear =
                bean.getExecutionYears().stream().max(ExecutionYear.COMPARATOR_BY_BEGIN_DATE).get();

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.approvals"),
                new SheetData<Map.Entry<ICurriculum, ICurriculumEntry>>(toExport.entries()) {

                    @Override
                    protected void makeLine(final Entry<ICurriculum, ICurriculumEntry> entry) {
                        final Registration registration = entry.getKey().getStudentCurricularPlan().getRegistration();
                        final ICurriculumEntry curriculumEntry = entry.getValue();

                        addData("Student.number", registration.getStudent().getNumber());
                        addData("Registration.number", registration.getNumber().toString());
                        addData("Person.name", registration.getStudent().getPerson().getName());
                        addData("Degree.code", registration.getDegree().getCode());
                        addData("Degree.presentationName", registration.getDegree().getPresentationNameI18N().getContent());
                        addData("RegistrationHistoryReport.curricularYear",
                                RegistrationServices.getCurricularYear(registration, executionYearForCurricularYear).getResult());
                        addData("ICurriculumEntry.code", curriculumEntry.getCode());
                        addData("ICurriculumEntry.name", curriculumEntry.getPresentationName().getContent());
                        addData("ICurriculumEntry.grade", curriculumEntry.getGradeValue());
                        addData("ICurriculumEntry.ectsCreditsForCurriculum", curriculumEntry.getEctsCreditsForCurriculum());
                        addData("ICurriculumEntry.executionPeriod", curriculumEntry.getExecutionPeriod().getQualifiedName());
                        addData("CurricularCourse.regime", getRegimeDescription(curriculumEntry));
                        addData("creationDate", curriculumEntry.getCreationDateDateTime().toString("yyyy-MM-dd HH:mm"));
                        addData("ICurriculumEntry.dismissal", ULisboaReportsUtil
                                .bundle(isDismissal(entry.getKey(), entry.getValue()) ? "label.yes" : "label.no"));
                        addData("ICurriculumEntry.curricularYear", getCurricularYear(entry.getKey(), curriculumEntry));
                        addData("ICurriculumEntry.curricularSemester", getCurricularSemester(entry.getKey(), curriculumEntry));
                        addData("ICurriculumEntry.groupPath", getGroupPath(entry.getKey(), curriculumEntry));
                        addData("Curriculum.totalApprovals", entry.getKey().getCurriculumEntries().size());
                        final OptionalDouble average =
                                entry.getKey().getCurriculumEntries().stream().filter(e -> e.getGrade().isNumeric())
                                        .map(e -> e.getGrade().getNumericValue()).mapToDouble(v -> v.doubleValue()).average();
                        addData("Curriculum.simpleAverage", average.isPresent() ? average.getAsDouble() : null);

                    }

                    private String getRegimeDescription(ICurriculumEntry curriculumEntry) {
                        if (curriculumEntry instanceof CurriculumLine) {
                            final CurriculumLine curriculumLine = (CurriculumLine) curriculumEntry;
                            return curriculumLine.getCurricularCourse() != null ? curriculumLine.getCurricularCourse()
                                    .getRegime(curriculumEntry.getExecutionYear()).getLocalizedName() : null;
                        }

                        return null;
                    }

                    private boolean isDismissal(ICurriculum curriculum, ICurriculumEntry entry) {
                        return ((Curriculum) curriculum).getDismissalRelatedEntries().contains(entry);
                    }

                    private Integer getCurricularSemester(ICurriculum curriculum, ICurriculumEntry entry) {
                        return belongsToStudentCurricularPlan(curriculum, entry) ? CurricularPeriodServices
                                .getCurricularSemester((CurriculumLine) entry) : null;
                    }

                    private Integer getCurricularYear(ICurriculum curriculum, ICurriculumEntry entry) {
                        return belongsToStudentCurricularPlan(curriculum, entry) ? CurricularPeriodServices
                                .getCurricularYear((CurriculumLine) entry) : null;
                    }

                    private String getGroupPath(ICurriculum curriculum, ICurriculumEntry entry) {
                        return belongsToStudentCurricularPlan(curriculum, entry) ? ((CurriculumLine) entry).getCurriculumGroup()
                                .getFullPath() : null;

                    }

                    protected boolean belongsToStudentCurricularPlan(ICurriculum curriculum, ICurriculumEntry entry) {
                        return entry instanceof Dismissal || entry instanceof Enrolment
                                && ((Enrolment) entry).getStudentCurricularPlan() == curriculum.getStudentCurricularPlan();
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    @RequestMapping(value = "/exportenrolments", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportEnrolments(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportEnrolments");
        new Thread(() -> processReport(this::exportEnrolmentsToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    private byte[] exportEnrolmentsToXLS(final RegistrationHistoryReportParametersBean bean) {
        final Collection<RegistrationHistoryReport> reports = generateReport(bean);

        // TODO extract all this logic to a future RegistrationHistoryEnrolmentReport

        final Multimap<RegistrationHistoryReport, Enrolment> toExportEnrolments = HashMultimap.create();
        reports.stream().forEach(r -> toExportEnrolments.putAll(r, r.getEnrolments()));

        final Map<Enrolment, ExecutionSemester> improvementsOnly = Maps.newHashMap();
        reports.stream().forEach(r -> {
            r.getImprovementEvaluations().forEach(ev -> {
                toExportEnrolments.put(r, ev.getEnrolment());

                if (ev.getExecutionPeriod() != ev.getEnrolment().getExecutionPeriod()) {
                    improvementsOnly.put(ev.getEnrolment(), ev.getExecutionPeriod());
                }
            });
        });

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.enrolments"),
                new SheetData<Map.Entry<RegistrationHistoryReport, Enrolment>>(toExportEnrolments.entries()) {

                    @Override
                    protected void makeLine(final Entry<RegistrationHistoryReport, Enrolment> entry) {
                        final RegistrationHistoryReport report = entry.getKey();
                        final Enrolment enrolment = entry.getValue();

                        final boolean improvementOnly = improvementsOnly.containsKey(enrolment);
                        final ExecutionSemester enrolmentPeriod =
                                improvementOnly ? improvementsOnly.get(enrolment) : enrolment.getExecutionPeriod();

                        final EnrolmentEvaluation finalEvaluation = enrolment.getFinalEnrolmentEvaluation();

                        addData("Student.number", report.getStudentNumber());
                        addData("Registration.number", report.getRegistrationNumber());
                        addData("Person.name", report.getPersonName());
                        addData("Degree.code", report.getDegreeCode());
                        addData("Degree.presentationName", report.getDegreePresentationName());
                        addData("RegistrationHistoryReport.curricularYear", report.getCurricularYear());
                        addData("Enrolment.code", enrolment.getCode());
                        addData("Enrolment.name", enrolment.getPresentationName().getContent());
                        addData("Enrolment.ectsCreditsForCurriculum", enrolment.getEctsCreditsForCurriculum());
                        addData("Enrolment.grade", finalEvaluation != null ? finalEvaluation.getGradeValue() : null);
                        addData("Enrolment.executionPeriod", enrolmentPeriod.getQualifiedName());
                        addData("CurricularCourse.regime",
                                enrolment.getCurricularCourse().getRegime(enrolment.getExecutionYear()).getLocalizedName());
                        addData("enrolmentDate", enrolment.getCreationDateDateTime().toString("yyyy-MM-dd HH:mm"));
                        addData("Enrolment.improvementOnly",
                                ULisboaReportsUtil.bundle(improvementOnly ? "label.yes" : "label.no"));
                        addData("Enrolment.shifts", EnrolmentServices.getShiftsDescription(enrolment, enrolmentPeriod));
                        addData("Enrolment.curriculumGroup", enrolment.getCurriculumGroup().getFullPath());
                        addData("Enrolment.numberOfEnrolments", CompetenceCourseServices.countEnrolmentsUntil(
                                report.getStudentCurricularPlan(), enrolment.getCurricularCourse(), report.getExecutionYear()));
                        addData("ICurriculumEntry.curricularYear", CurricularPeriodServices.getCurricularYear(enrolment));
                        addData("ICurriculumEntry.curricularSemester", CurricularPeriodServices.getCurricularSemester(enrolment));
                        addData("Person.defaultEmailAddress", enrolment.getStudent().getPerson().getDefaultEmailAddressValue());
                        addData("Person.institutionalEmailAddress",
                                enrolment.getStudent().getPerson().getInstitutionalEmailAddressValue());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                });

        final List<EnrolmentEvaluation> toExportEvaluations =
                toExportEnrolments.entries().stream().flatMap(e -> e.getValue().getEvaluationsSet().stream())
                        .filter(e -> EvaluationSeasonServices.isRequiredEnrolmentEvaluation(e.getEvaluationSeason())
                                && bean.getExecutionYears().contains(e.getExecutionPeriod().getExecutionYear()))
                        .sorted(EnrolmentEvaluation.SORT_BY_STUDENT_NUMBER.thenComparing(DomainObjectUtil.COMPARATOR_BY_ID))
                        .collect(Collectors.toList());

        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.evaluations"),
                new SheetData<EnrolmentEvaluation>(toExportEvaluations) {

                    @Override
                    protected void makeLine(final EnrolmentEvaluation evaluation) {

                        final Registration registration = evaluation.getRegistration();
                        final Enrolment enrolment = evaluation.getEnrolment();

                        addData("Student.number", registration.getStudent().getNumber());
                        addData("Registration.number", registration.getNumber().toString());
                        addData("Person.name", registration.getStudent().getPerson().getName());
                        addData("Degree.code", registration.getDegree().getCode());
                        addData("Degree.presentationName", registration.getDegree().getPresentationNameI18N().getContent());
                        addData("Enrolment.code", enrolment.getCode());
                        addData("Enrolment.name", enrolment.getPresentationName().getContent());
                        addData("Enrolment.executionPeriod", evaluation.getExecutionPeriod().getQualifiedName());
                        addData("CurricularCourse.regime",
                                enrolment.getCurricularCourse().getRegime(enrolment.getExecutionYear()).getLocalizedName());
                        addData("EnrolmentEvaluation.grade", evaluation.getGradeValue());

                        if (evaluation.getEvaluationSeason().isImprovement() && evaluation.isFinal()) {
                            addData("Enrolment.gradeImproved", gradeWasImproved(evaluation) ? ULisboaReportsUtil
                                    .bundle("label.yes") : ULisboaReportsUtil.bundle("label.no"));
                        } else {
                            addData("Enrolment.gradeImproved", "");
                        }

                        addData("EnrolmentEvaluation.season", evaluation.getEvaluationSeason().getName().getContent());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    //Report specific. Cannot be at service level (depends on instance logic to calculate final grade)
    private boolean gradeWasImproved(final EnrolmentEvaluation improvement) {
        final EnrolmentEvaluation previousEvaluation = improvement.getEnrolment().getEvaluationsSet().stream()
                .filter(ev -> ev.getEvaluationSeason() != improvement.getEvaluationSeason() && ev.isFinal() && ev.isApproved()
                        && !ev.getEvaluationSeason().isImprovement())
                .sorted(new EvaluationComparator().reversed()).findFirst().orElse(null);

        return previousEvaluation != null && improvement.getGrade().compareTo(previousEvaluation.getGrade()) > 0;
    }

    @RequestMapping(value = "/exportregistrationsbystatute", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportRegistrationsByStatute(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportRegistrationsByStatute");
        new Thread(() -> processReport(this::exportRegistrationsByStatuteToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    private byte[] exportRegistrationsByStatuteToXLS(final RegistrationHistoryReportParametersBean bean) {
        final Set<RegistrationHistoryReport> toExport = Sets.newHashSet();

        // TODO extract all this logic to RegistrationHistoryReport

        if (bean.getExecutionYears().size() != 1) {
            return createXLSWithError(ULisboaReportsUtil.bundle(
                    "error.reports.registrationHistory.to.export.registrations.by.statute.choose.a.single.execution.year"));
        }

        final ExecutionYear executionYear = bean.getExecutionYears().iterator().next();

        final Collection<StudentStatute> studentStatutes = Sets.newHashSet();
        for (final ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            studentStatutes.addAll(executionSemester.getBeginningStudentStatutesSet().stream()
                    .filter(x -> bean.getStatuteTypes().isEmpty() || bean.getStatuteTypes().contains(x.getType()))
                    .collect(Collectors.toSet()));
            studentStatutes.addAll(executionSemester.getEndingStudentStatutesSet().stream()
                    .filter(x -> bean.getStatuteTypes().isEmpty() || bean.getStatuteTypes().contains(x.getType()))
                    .collect(Collectors.toSet()));
        }

        for (final StudentStatute studentStatute : studentStatutes) {

            if (studentStatute.getRegistration() != null) {
                toExport.add(new RegistrationHistoryReport(studentStatute.getRegistration(), executionYear));
                continue;
            }

            for (final Registration registration : studentStatute.getStudent().getRegistrationsSet()) {
                if (registration.getRegistrationStatesTypes(executionYear).stream().anyMatch(x -> x.isActive())) {
                    toExport.add(new RegistrationHistoryReport(registration, executionYear));
                }
            }

        }

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.statutes"),
                new SheetData<RegistrationHistoryReport>(toExport) {

                    @Override
                    protected void makeLine(final RegistrationHistoryReport report) {
                        addData("RegistrationHistoryReport.executionYear", report.getExecutionYear().getQualifiedName());
                        addData("Student.number", report.getStudentNumber());
                        addData("Registration.number", report.getRegistrationNumber());
                        addData("Person.name", report.getPersonName());
                        addData("Degree.code", report.getDegreeCode());
                        addData("Degree.presentationName", report.getDegreePresentationName());
                        addData("RegistrationHistoryReport.statutes", report.getStudentStatutesNamesAndDates());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    @RequestMapping(value = "/exportbluerecord", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> exportBlueRecordInfo(
            @RequestParam(value = "bean", required = false) final RegistrationHistoryReportParametersBean bean,
            final Model model) {

        final String reportId = getReportId("exportBlueRecordData");
        new Thread(() -> processReport(this::exportRegistrationsBlueRecordInformationToXLS, bean, reportId)).start();

        return new ResponseEntity<String>(reportId, HttpStatus.OK);
    }

    private byte[] exportRegistrationsBlueRecordInformationToXLS(final RegistrationHistoryReportParametersBean bean) {
        final Collection<RegistrationHistoryReport> reports = generateReport(bean);

        final Collection<RegistrationDGESStateBean> toExport = new ArrayList<>();

        for (final RegistrationHistoryReport report : reports) {
            final StudentCandidacy studentCandidacy = report.getRegistration().getStudentCandidacy();
            if (studentCandidacy != null) {
                toExport.add(RegistrationDGESStateBeanController.populateBean(studentCandidacy));
            }
        }

        final SpreadsheetBuilderForXLSX builder = new SpreadsheetBuilderForXLSX();
        builder.addSheet(ULisboaReportsUtil.bundle("label.reports.registrationHistory.blueRecord"),
                new SheetData<RegistrationDGESStateBean>(toExport) {

                    @Override
                    protected void makeLine(final RegistrationDGESStateBean stateBean) {
                        addData("HouseholdInformationForm.executionYear", stateBean.getExecutionYear());
                        addData("Degree.degreeType", stateBean.getDegreeTypeName());
                        addData("studentsListByCurricularCourse.degree", stateBean.getDegreeCode());
                        addData("Degree.name", stateBean.getDegreeName());
                        addData("ServiceRequestSlot.label.cycleType", stateBean.getCycleName());
                        addData("RegistrationHistoryReport.curricularYear", stateBean.getCurricularYear());
                        addData("OriginInformationForm.schoolLevel", stateBean.getDegreeLevel());
                        addData("RegistrationHistoryReport.primaryBranch", stateBean.getDegreeBranch());
                        addData("RegistrationHistoryReport.regimeType", stateBean.getRegimeType());
                        addData("OriginInformationForm.institution", stateBean.getInstitutionName());
                        addData("identification.number", stateBean.getIdNumber());
                        addData("PersonalInformationForm.documentIdExpirationDate", stateBean.getExpirationDateOfIdDoc());
                        addData("PersonalInformationForm.documentIdEmissionLocation", stateBean.getEmissionLocationOfIdDoc());
                        addData("student", stateBean.getName());
                        addData("PersonalInformationForm.maritalStatus", stateBean.getMaritalStatus());
                        addData("is.registered", stateBean.getRegistrationState());
                        addData("candidacy", stateBean.getCandidacyState());
                        addData("FiliationForm.nationality", stateBean.getNationality());
                        addData("FiliationForm.secondNationality", stateBean.getSecondNationality());
                        addData("Person.birthYear", stateBean.getBirthYear());
                        addData("FiliationForm.countryOfBirth", stateBean.getCountryOfBirth());
                        addData("FiliationForm.districtOfBirth", stateBean.getDistrictOfBirth());
                        addData("FiliationForm.districtSubdivisionOfBirth", stateBean.getDistrictSubdivisionOfBirth());
                        addData("FiliationForm.parishOfBirth", stateBean.getParishOfBirth());
                        addData("Person.gender", stateBean.getGender());
                        addData("Registration.ingressionType", stateBean.getIngressionType());
                        addData("PersonalInformationForm.ingressionOption", stateBean.getPlacingOption());
                        addData("PersonalInformationForm.firstOptionDegreeDesignation.short", stateBean.getFirstOptionDegree());
                        addData("PersonalInformationForm.firstOptionInstitution.short", stateBean.getFirstOptionInstitution());
                        addData("ResidenceInformationForm.countryOfResidence", stateBean.getCountryOfResidence());
                        addData("ResidenceInformationForm.districtOfResidence", stateBean.getDistrictOfResidence());
                        addData("ResidenceInformationForm.districtSubdivisionOfResidence",
                                stateBean.getDistrictSubdivisionOfResidence());
                        addData("ResidenceInformationForm.parishOfResidence", stateBean.getParishOfResidence());
                        addData("ResidenceInformationForm.address", stateBean.getAddressOfResidence());
                        addData("ResidenceInformationForm.areaCode", stateBean.getAreaCodeOfResidence());
                        addData("ResidenceInformationForm.schoolTimeCountry", stateBean.getCountryOfDislocated());
                        addData("ResidenceInformationForm.schoolTimeDistrictOfResidence", stateBean.getDistrictOfDislocated());
                        addData("ResidenceInformationForm.schoolTimeDistrictSubdivisionOfResidence",
                                stateBean.getDistrictSubdivisionOfDislocated());
                        addData("ResidenceInformationForm.schoolTimeParishOfResidence", stateBean.getParishOfDislocated());
                        addData("ResidenceInformationForm.schoolTimeAddress", stateBean.getAddressOfDislocated());
                        addData("ResidenceInformationForm.schoolTimeAreaCode", stateBean.getAreaCodeOfDislocated());
                        addData("ResidenceInformationForm.dislocatedFromPermanentResidence", stateBean.getIsDislocated());
                        addData("firstTimeCandidacy.fillResidenceInformation", stateBean.getDislocatedResidenceType());
                        addData("PersonalInformationForm.profession", stateBean.getProfession());
                        addData("PersonalInformationForm.professionTimeType.short", stateBean.getProfessionTimeType());
                        addData("PersonalInformationForm.professionalCondition", stateBean.getProfessionalCondition());
                        addData("PersonalInformationForm.professionType", stateBean.getProfessionType());
                        addData("FiliationForm.fatherName", stateBean.getFatherName());
                        addData("HouseholdInformationForm.fatherSchoolLevel.short", stateBean.getFatherSchoolLevel());
                        addData("HouseholdInformationForm.fatherProfessionalCondition.short",
                                stateBean.getFatherProfessionalCondition());
                        addData("HouseholdInformationForm.fatherProfessionType.short", stateBean.getFatherProfessionType());
                        addData("FiliationForm.motherName", stateBean.getMotherName());
                        addData("HouseholdInformationForm.motherSchoolLevel.short", stateBean.getMotherSchoolLevel());
                        addData("HouseholdInformationForm.motherProfessionalCondition.short",
                                stateBean.getMotherProfessionalCondition());
                        addData("HouseholdInformationForm.motherProfessionType.short", stateBean.getMotherProfessionType());
                        addData("HouseholdInformationForm.householdSalarySpan.short", stateBean.getSalarySpan());
                        addData("firstTimeCandidacy.fillDisabilities", stateBean.getDisabilityType());
                        addData("DisabilitiesForm.needsDisabilitySupport.short", stateBean.getNeedsDisabilitySupport());
                        addData("MotivationsExpectationsForm.universityDiscoveryMeansAnswers.short",
                                stateBean.getUniversityDiscoveryString());
                        addData("MotivationsExpectationsForm.universityChoiceMotivationAnswers.short",
                                stateBean.getUniversityChoiceString());
                        addData("OriginInformationForm.countryWhereFinishedPreviousCompleteDegree",
                                stateBean.getPrecedentCountry());
                        addData("OriginInformationForm.districtWhereFinishedPreviousCompleteDegree",
                                stateBean.getPrecedentDistrict());
                        addData("OriginInformationForm.districtSubdivisionWhereFinishedPreviousCompleteDegree",
                                stateBean.getPrecedentDistrictSubdivision());
                        addData("OriginInformationForm.schoolLevel", stateBean.getPrecedentSchoolLevel());
                        addData("OriginInformationForm.institution", stateBean.getPrecedentInstitution());
                        addData("OriginInformationForm.degreeDesignation", stateBean.getPrecedentDegreeDesignation());
                        addData("OriginInformationForm.degree.cycle", stateBean.getPrecendentDegreeCycle());
                        addData("OriginInformationForm.conclusionGrade", stateBean.getPrecedentConclusionGrade());
                        addData("OriginInformationForm.conclusionYear", stateBean.getPrecedentConclusionYear());
                        addData("OriginInformationForm.highSchoolType", stateBean.getPrecedentHighSchoolType());
                        addData("ContactsForm.institutionalEmail", stateBean.getInstitutionalEmail());
                        addData("ContactsForm.personalEmail", stateBean.getDefaultEmail());
                        addData("ContactsForm.phoneNumber", stateBean.getPhone());
                        addData("ContactsForm.mobileNumber", stateBean.getTelephone());
                        addData("SchoolSpecificData.vaccinationValidity", stateBean.getVaccinationValidity());
                        addData("HouseholdInformationForm.grantOwnerType", stateBean.getGrantOwnerType());
                        addData("HouseholdInformationForm.grantOwnerProviderName", stateBean.getGrantOwnerProvider());
                    }

                    private void addData(final String key, final Object value) {
                        addCell(bundle("label." + key), value == null ? "" : value);
                    }

                });

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            builder.build(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

}
