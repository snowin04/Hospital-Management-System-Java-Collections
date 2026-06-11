package com.hms.level3;

import java.util.HashSet;
import java.util.Set;

/**
 * Level 3.2 &mdash; Tracks unique patient admissions using two {@link HashSet}s
 * (active and discharged). Sets give O(1) membership checks and naturally
 * prevent duplicate admissions.
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #admit}/{@link #discharge}/{@link #isAdmitted}</td><td>O(1) expected</td></tr>
 * </table>
 */
public class UniquePatientTracker {

    private final Set<String> active = new HashSet<>();
    private final Set<String> discharged = new HashSet<>();

    /** @return {@code true} if newly admitted, {@code false} if already active. */
    public boolean admit(String patientId) {
        discharged.remove(patientId); // re-admission clears the discharged flag
        return active.add(patientId);
    }

    /** @return {@code true} if the patient was active and is now discharged. */
    public boolean discharge(String patientId) {
        if (active.remove(patientId)) {
            discharged.add(patientId);
            return true;
        }
        return false;
    }

    public boolean isAdmitted(String patientId) {
        return active.contains(patientId);
    }

    public int activeCount() {
        return active.size();
    }

    public int dischargedCount() {
        return discharged.size();
    }
}
