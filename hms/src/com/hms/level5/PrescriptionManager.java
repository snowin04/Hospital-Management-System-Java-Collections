package com.hms.level5;

import com.hms.model.Prescription;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 5 &mdash; Prescription manager built from several indexes:
 * <ul>
 *   <li>{@code byPatient}: patient id &rarr; their prescriptions;</li>
 *   <li>{@code byId}: prescription id &rarr; the prescription;</li>
 *   <li>{@code drugToPatients}: drug name &rarr; patient ids who received it;</li>
 *   <li>{@code drugCounts}: drug name &rarr; total times prescribed.</li>
 * </ul>
 *
 * <p>The duplicate rule blocks the same drug for the same patient within a
 * 30-day window.</p>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #addPrescription}</td><td>O(p) where p = patient's prior scripts (dup scan)</td></tr>
 *   <tr><td>{@link #isDuplicatePrescription}</td><td>O(p)</td></tr>
 *   <tr><td>{@link #getMostPrescribedDrug}</td><td>O(d), d = distinct drugs</td></tr>
 * </table>
 */
public class PrescriptionManager {

    private static final int DUPLICATE_WINDOW_DAYS = 30;

    private final Map<String, List<Prescription>> byPatient = new HashMap<>();
    private final Map<String, Prescription> byId = new HashMap<>();
    private final Map<String, List<String>> drugToPatients = new HashMap<>();
    private final Map<String, Integer> drugCounts = new HashMap<>();

    /**
     * Add a prescription unless it duplicates the same drug for the same patient
     * within the 30-day window.
     *
     * @return {@code true} if added, {@code false} if rejected as a duplicate.
     */
    public boolean addPrescription(Prescription rx) {
        if (isDuplicatePrescription(rx.getPatientId(), rx.getDrugName(), rx.getPrescribedOn())) {
            return false;
        }
        byPatient.computeIfAbsent(rx.getPatientId(), k -> new ArrayList<>()).add(rx);
        byId.put(rx.getId(), rx);
        drugToPatients.computeIfAbsent(rx.getDrugName(), k -> new ArrayList<>())
                      .add(rx.getPatientId());
        drugCounts.merge(rx.getDrugName(), 1, Integer::sum);
        return true;
    }

    /** @return {@code true} if an equivalent recent prescription already exists. */
    public boolean isDuplicatePrescription(String patientId, String drugName, LocalDate on) {
        for (Prescription existing : byPatient.getOrDefault(patientId, List.of())) {
            if (existing.getDrugName().equalsIgnoreCase(drugName)) {
                long gap = Math.abs(ChronoUnit.DAYS.between(existing.getPrescribedOn(), on));
                if (gap < DUPLICATE_WINDOW_DAYS) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Prescription> getPrescriptionsForPatient(String patientId) {
        return List.copyOf(byPatient.getOrDefault(patientId, List.of()));
    }

    public Prescription getById(String prescriptionId) {
        return byId.get(prescriptionId);
    }

    /** @return the most frequently prescribed drug, or {@code null} if none. */
    public String getMostPrescribedDrug() {
        String top = null;
        int max = 0;
        for (Map.Entry<String, Integer> e : drugCounts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }
        return top;
    }

    public int prescriptionCountFor(String drugName) {
        return drugCounts.getOrDefault(drugName, 0);
    }
}
