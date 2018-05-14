package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetSnapshot;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetSnapshotEntry;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetStateChange;
import org.fenixedu.academic.domain.evaluation.season.EvaluationSeasonServices;
import org.fenixedu.academic.dto.evaluation.markSheet.MarkBean;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;
import org.fenixedu.ulisboa.specifications.dto.evaluation.markSheet.CompetenceCourseMarkSheetBean;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import edu.emory.mathcs.backport.java.util.Collections;

public class CompetenceCourseMarkSheetStateChangeReport implements Comparable<CompetenceCourseMarkSheetStateChangeReport> {

    private final CompetenceCourseMarkSheet competenceCourseMarkSheet;
    private final List<MarkBean> gradeDifferences;
    private final List<MarkBean> gradeAvailableDates;
    private final CompetenceCourseMarkSheetBean competenceCourseMarkSheetBean;
    private org.fenixedu.academic.domain.EvaluationSeason evaluationSeason;
    private org.fenixedu.academic.domain.Evaluation evaluation;

    public CompetenceCourseMarkSheetStateChangeReport(final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        this.competenceCourseMarkSheet = competenceCourseMarkSheet;
        this.gradeDifferences = getCompetenceCourseMarkSheetPreviousSnapshot() == null ? Collections
                .emptyList() : getCompetenceCourseMarkSheetPreviousSnapshot().getDifferencesToNextGradeValues();

        this.competenceCourseMarkSheetBean = new CompetenceCourseMarkSheetBean(this.competenceCourseMarkSheet);
        this.gradeAvailableDates = competenceCourseMarkSheetBean.getUpdateGradeAvailableDateBeans();

    }

    @Override
    public int compareTo(final CompetenceCourseMarkSheetStateChangeReport o) {
        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<CompetenceCourseMarkSheetStateChangeReport> byCourseName =
                (x, y) -> instance.compare(x.getCompetenceCourseName(), y.getCompetenceCourseName());
        final Comparator<CompetenceCourseMarkSheetStateChangeReport> byCode =
                (x, y) -> instance.compare(x.getCompetenceCourseCode(), y.getCompetenceCourseCode());
        final Comparator<CompetenceCourseMarkSheetStateChangeReport> byCreationDate =
                Comparator.comparing(x -> x.getCompetenceCourseMarkSheet().getCreationDate());
        final Comparator<CompetenceCourseMarkSheetStateChangeReport> byCheckSum =
                (x, y) -> instance.compare(x.getCheckSum(), y.getCheckSum());

        return byCourseName.thenComparing(byCode).thenComparing(byCreationDate).thenComparing(byCheckSum).compare(this, o);
    }

    public static pt.ist.fenixframework.dml.runtime.DirectRelation<org.fenixedu.academic.domain.EvaluationSeason, org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet> getRelationCompetenceCourseMarkSheetEvaluationSeason() {
        return org.fenixedu.academic.domain.EvaluationSeason.getRelationCompetenceCourseMarkSheetEvaluationSeason();
    }

    public String getCompetenceCourseCode() {
        return competenceCourseMarkSheet.getCompetenceCourse().getCode();
    }

    public String getCompetenceCourseName() {
        return getCompetenceCourseMarkSheet().getCompetenceCourse().getNameI18N().getContent();
    }

    public String getExecutionPresentation() {
        return CompetenceCourseMarkSheetBean.getExecutionCoursePresentation(competenceCourseMarkSheet.getExecutionCourse());
    }

    public String getEvaluationSeasonCode() {
        return competenceCourseMarkSheet.getEvaluationSeason().getAcronym().getContent();
    }

    public String getEvaluationSeason() {
        return EvaluationSeasonServices.getDescriptionI18N(competenceCourseMarkSheet.getEvaluationSeason()).getContent();
    }

    public String getCreationDateShort() {
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(competenceCourseMarkSheet.getCreationDate());
    }

    public String getCreationDate() {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(competenceCourseMarkSheet.getCreationDate());
    }

    public String getExecutionSemester() {
        return competenceCourseMarkSheet.getExecutionSemester().getQualifiedName();
    }

    public String getCheckSum() {
        return competenceCourseMarkSheet.getFormattedCheckSum();
    }

    public String getEvaluationDate() {
        return competenceCourseMarkSheet.getEvaluationDatePresentation();
    }

    public String getLastSubmissionDate() {
        final CompetenceCourseMarkSheetStateChange lastSubmissionStateChange = competenceCourseMarkSheet.getStateChangeSet()
                .stream().filter(sc -> sc.isSubmitted()).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
        return lastSubmissionStateChange == null ? bundle(
                "CompetenceCourseMarkSheetReport.submissionDate.notfound") : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                        .print(lastSubmissionStateChange.getDate());
    }

    public String getCertifier() {
        return CompetenceCourseMarkSheetBean.getPersonDescription(competenceCourseMarkSheet.getCertifier());
    }

    public String getLastState() {
        return competenceCourseMarkSheet.getStateChangeSet().stream().sorted(Comparator.reverseOrder())
                .map(sc -> sc.getState().getDescriptionI18N().getContent()).findFirst().orElse(null);
    }

    public String getPrintStatus() {
        return competenceCourseMarkSheet.getPrinted() ? bundle("yes") : bundle("no");
    }

    public String getLastPendingChange() {
        return competenceCourseMarkSheet.getLastPendingChangeRequest() != null ? bundle("yes") : bundle("no");
    }

    private CompetenceCourseMarkSheet getCompetenceCourseMarkSheet() {
        return this.competenceCourseMarkSheet;
    }

    public CompetenceCourseMarkSheetSnapshot getCompetenceCourseMarkSheetLastSnapshot() {
        return this.competenceCourseMarkSheet.getLastSnapshot().get();
    }

    public CompetenceCourseMarkSheetSnapshot getCompetenceCourseMarkSheetPreviousSnapshot() {
        Comparator<CompetenceCourseMarkSheetSnapshot> comp = (ss1, ss2) -> ss1.getStateChange().compareTo(ss2.getStateChange());

        return this.competenceCourseMarkSheet.getPreviousSnapshots().stream().sorted(comp.reversed()).findFirst().orElse(null);

    }

    public Collection<CompetenceCourseMarkSheetSnapshotEntry> getEntries(
            CompetenceCourseMarkSheetSnapshot competenceCourseMarkSheetSnapshot) {
        return competenceCourseMarkSheetSnapshot.getSortedEntries();
    }

    public String hasGradeChanged(CompetenceCourseMarkSheetSnapshotEntry competenceCourseMarkSheetSnapshotEntry) {
        return this.gradeDifferences.stream()
                .filter(gd -> gd.getStudentNumber().equals(competenceCourseMarkSheetSnapshotEntry.getStudentNumber())).findFirst()
                .isPresent() ? bundle("yes") : " ";
    }

    public LocalDate gradeAvailableDate(CompetenceCourseMarkSheetSnapshotEntry competenceCourseMarkSheetSnapshotEntry) {
        return this.gradeAvailableDates.stream()
                .filter(gad -> gad.getStudentNumber().equals(competenceCourseMarkSheetSnapshotEntry.getStudentNumber()))
                .findFirst().map(gad -> gad.getGradeAvailableDate()).orElse(null);
    }

    private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }
}
