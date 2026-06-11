package com.hms.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single lab measurement for a patient (Level 6). Each result carries the
 * reference (normal) range and the panic (critical) thresholds for its test,
 * so analytics can classify it without an external lookup table.
 *
 * <p>A value is:
 * <ul>
 *   <li><b>CRITICAL</b> if below {@code panicLow} or above {@code panicHigh};</li>
 *   <li><b>ABNORMAL</b> if outside [{@code refLow}, {@code refHigh}] but not critical;</li>
 *   <li><b>NORMAL</b> otherwise.</li>
 * </ul>
 */
public class LabResult {

    private final String patientId;
    private final String testName;   // e.g. "Blood Glucose"
    private final double value;
    private final String unit;       // e.g. "mg/dL"
    private final LocalDateTime takenAt;
    private final double refLow;
    private final double refHigh;
    private final double panicLow;
    private final double panicHigh;

    public LabResult(String patientId, String testName, double value, String unit,
                     LocalDateTime takenAt,
                     double refLow, double refHigh,
                     double panicLow, double panicHigh) {
        this.patientId = Objects.requireNonNull(patientId, "patientId");
        this.testName = Objects.requireNonNull(testName, "testName");
        this.value = value;
        this.unit = unit;
        this.takenAt = Objects.requireNonNull(takenAt, "takenAt");
        this.refLow = refLow;
        this.refHigh = refHigh;
        this.panicLow = panicLow;
        this.panicHigh = panicHigh;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getTestName() {
        return testName;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public boolean isCritical() {
        return value < panicLow || value > panicHigh;
    }

    public boolean isAbnormal() {
        return value < refLow || value > refHigh;
    }

    /** @return the classification bucket for this measurement. */
    public ResultCategory category() {
        if (isCritical()) return ResultCategory.CRITICAL;
        if (isAbnormal()) return ResultCategory.ABNORMAL;
        return ResultCategory.NORMAL;
    }

    @Override
    public String toString() {
        return String.format("%s: %s %.1f %s (%s)",
                patientId, testName, value, unit, category());
    }
}
