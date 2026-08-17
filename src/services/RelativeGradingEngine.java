package services;

import models.GradeRecord;
import java.util.*;

public class RelativeGradingEngine {
    private double topAPlusPct = 10.0; // Top 10%
    private double nextAPct = 20.0;     // Next 20% (Cumulative 30%)
    private double nextBPct = 30.0;     // Next 30% (Cumulative 60%)
    private double nextCPct = 25.0;     // Next 25% (Cumulative 85%)

    public RelativeGradingEngine() {}

    public RelativeGradingEngine(double topAPlusPct, double nextAPct, double nextBPct, double nextCPct) {
        setThresholds(topAPlusPct, nextAPct, nextBPct, nextCPct);
    }

    public void setThresholds(double topAPlusPct, double nextAPct, double nextBPct, double nextCPct) {
        if (topAPlusPct + nextAPct + nextBPct + nextCPct > 100.0) {
            throw new IllegalArgumentException("Sum of grade percentages cannot exceed 100%");
        }
        this.topAPlusPct = topAPlusPct;
        this.nextAPct = nextAPct;
        this.nextBPct = nextBPct;
        this.nextCPct = nextCPct;
    }

    public double getTopAPlusPct() { return topAPlusPct; }
    public double getNextAPct() { return nextAPct; }
    public double getNextBPct() { return nextBPct; }
    public double getNextCPct() { return nextCPct; }
    public double getFPct() { return Math.max(0, 100.0 - (topAPlusPct + nextAPct + nextBPct + nextCPct)); }

    /**
     * Calculates relative grades for a list of GradeRecords.
     * Records are sorted by marks descending and grades assigned based on percentile thresholds.
     */
    public void calculateGrades(List<GradeRecord> records) {
        if (records == null || records.isEmpty()) return;

        // Group by course code first
        Map<String, List<GradeRecord>> courseGroups = new HashMap<>();
        for (GradeRecord record : records) {
            courseGroups.computeIfAbsent(record.getCourseCode(), k -> new ArrayList<>()).add(record);
        }

        for (List<GradeRecord> group : courseGroups.values()) {
            // Sort by marks descending
            group.sort((r1, r2) -> Double.compare(r2.getMarks(), r1.getMarks()));

            int n = group.size();
            int limitAPlus = (int) Math.ceil((topAPlusPct / 100.0) * n);
            int limitA = (int) Math.ceil(((topAPlusPct + nextAPct) / 100.0) * n);
            int limitB = (int) Math.ceil(((topAPlusPct + nextAPct + nextBPct) / 100.0) * n);
            int limitC = (int) Math.ceil(((topAPlusPct + nextAPct + nextBPct + nextCPct) / 100.0) * n);

            for (int i = 0; i < n; i++) {
                GradeRecord rec = group.get(i);
                
                // Tie handling: if marks equal previous student, assign same grade
                if (i > 0 && Double.compare(rec.getMarks(), group.get(i - 1).getMarks()) == 0) {
                    rec.setGrade(group.get(i - 1).getGrade());
                } else if (i < limitAPlus) {
                    rec.setGrade("A+");
                } else if (i < limitA) {
                    rec.setGrade("A");
                } else if (i < limitB) {
                    rec.setGrade("B");
                } else if (i < limitC) {
                    rec.setGrade("C");
                } else {
                    rec.setGrade("F");
                }
            }
        }
    }
}
