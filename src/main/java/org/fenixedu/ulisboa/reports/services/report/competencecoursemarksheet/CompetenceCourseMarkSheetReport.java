package org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheet;

import java.text.Collator;
import java.util.Comparator;

import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheet;
import org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetStateChange;
import org.fenixedu.academic.domain.evaluation.season.EvaluationSeasonServices;
import org.fenixedu.ulisboa.reports.util.ULisboaReportsUtil;
import org.fenixedu.ulisboa.specifications.dto.evaluation.markSheet.CompetenceCourseMarkSheetBean;
import org.joda.time.format.DateTimeFormat;

public class CompetenceCourseMarkSheetReport
        implements Comparable<CompetenceCourseMarkSheetReport> {

    private final CompetenceCourseMarkSheet competenceCourseMarkSheet;

    public CompetenceCourseMarkSheetReport(
            final CompetenceCourseMarkSheet competenceCourseMarkSheet) {
        this.competenceCourseMarkSheet = competenceCourseMarkSheet;
    }

    public int compareTo(final CompetenceCourseMarkSheetReport o) {
        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        final Comparator<CompetenceCourseMarkSheetReport> byCourseName = (x,
                y) -> instance.compare(x.getComptenceCourseName(),
                        y.getComptenceCourseName());
        final Comparator<CompetenceCourseMarkSheetReport> byCode = (x,
                y) -> instance.compare(x.getCompetenceCourseCode(),
                        y.getCompetenceCourseCode());
        final Comparator<CompetenceCourseMarkSheetReport> byCreationDate = Comparator
                .comparing(x -> x.getCompetenceCourseMarkSheet()
                        .getCreationDate());
        final Comparator<CompetenceCourseMarkSheetReport> byCheckSum = (x,
                y) -> instance.compare(x.getCheckSum(), y.getCheckSum());

        return byCourseName.thenComparing(byCode).thenComparing(byCreationDate)
                .thenComparing(byCheckSum).compare(this, o);
    }

    public String getCompetenceCourseCode() {
        return competenceCourseMarkSheet.getCompetenceCourse().getCode();
    }

    public String getComptenceCourseName() {
        return getCompetenceCourseMarkSheet().getCompetenceCourse()
                .getNameI18N().getContent();
    }

    public String getExecutionPresentation() {
        return CompetenceCourseMarkSheetBean.getExecutionCoursePresentation(
                competenceCourseMarkSheet.getExecutionCourse());
    }

    public String getEvaluationSeason() {
        return EvaluationSeasonServices
                .getDescriptionI18N(
                        competenceCourseMarkSheet.getEvaluationSeason())
                .getContent();
    }

    public String getCreationDate() {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .print(competenceCourseMarkSheet.getCreationDate());
    }

    public String getExecutionSemester() {
        return competenceCourseMarkSheet.getExecutionSemester()
                .getQualifiedName();
    }

    public String getCheckSum() {
        return competenceCourseMarkSheet.getFormattedCheckSum();
    }

    public String getEvaluationDate() {
        return competenceCourseMarkSheet.getEvaluationDatePresentation();
    }

    public String getLastSubmissionDate() {
        final CompetenceCourseMarkSheetStateChange lastSubmissionStateChange = competenceCourseMarkSheet
                .getStateChangeSet().stream().filter(sc -> sc.isSubmitted())
                .sorted(Comparator.reverseOrder()).findFirst().orElse(null);
        return lastSubmissionStateChange == null ? bundle(
                "CompetenceCourseMarkSheetReport.submissionDate.notfound")
                : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                        .print(lastSubmissionStateChange.getDate());
    }

    public String getCertifier() {
        return CompetenceCourseMarkSheetBean
                .getPersonDescription(competenceCourseMarkSheet.getCertifier());
    }

    public String getLastState() {
        return competenceCourseMarkSheet.getStateChangeSet().stream()
                .sorted(Comparator.reverseOrder())
                .map(sc -> sc.getState().getDescriptionI18N().getContent())
                .findFirst().orElse(null);
    }

    public String getPrintStatus() {
        return competenceCourseMarkSheet.getPrinted() ? bundle("yes")
                : bundle("no");
    }

    public String getLastPendingChange() {
        return competenceCourseMarkSheet.getLastPendingChangeRequest() != null
                ? bundle("yes")
                : bundle("no");
    }

    private CompetenceCourseMarkSheet getCompetenceCourseMarkSheet() {
        return competenceCourseMarkSheet;
    }

    private String bundle(final String key) {
        return ULisboaReportsUtil.bundle(key);
    }
}
