package com.hms.level1;

import com.hms.model.Patient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Level 1.1 &mdash; Patient registry backed by an {@link ArrayList}.
 *
 * <p>An {@code ArrayList} gives O(1) amortized append and O(1) indexed access,
 * which suits an append-mostly registry. The trade-off is O(n) lookup/removal
 * by id, which is acceptable at the registration layer but is exactly why
 * Level 2 switches to a {@code HashMap} for the doctor directory.</p>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #registerPatient}</td><td>O(n) (duplicate scan) + O(1) add</td></tr>
 *   <tr><td>{@link #findPatientById}</td><td>O(n)</td></tr>
 *   <tr><td>{@link #dischargePatient}</td><td>O(n)</td></tr>
 *   <tr><td>{@link #getPatientsByRegistrationOrder}</td><td>O(n log n)</td></tr>
 * </table>
 */
public class PatientRegistry {

    private final List<Patient> patients = new ArrayList<>();

    /**
     * Register a patient, rejecting duplicate ids.
     *
     * @return {@code true} if registered, {@code false} if the id already exists.
     */
    public boolean registerPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("patient must not be null");
        }
        if (findPatientById(patient.getId()) != null) {
            return false; // duplicate id -> reject
        }
        patients.add(patient);
        return true;
    }

    /** Linear search by id. @return the patient, or {@code null} if absent. */
    public Patient findPatientById(String id) {
        for (Patient p : patients) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** @return patients sorted by registration time, newest first. */
    public List<Patient> getPatientsByRegistrationOrder() {
        List<Patient> sorted = new ArrayList<>(patients);
        sorted.sort(Comparator.comparing(Patient::getRegisteredAt).reversed());
        return sorted;
    }

    /**
     * Remove a patient by id.
     *
     * @return {@code true} if a record was removed.
     */
    public boolean dischargePatient(String id) {
        return patients.removeIf(p -> p.getId().equals(id));
    }

    public int getPatientCount() {
        return patients.size();
    }

    /** @return an unmodifiable snapshot of all registered patients. */
    public List<Patient> getAllPatients() {
        return List.copyOf(patients);
    }
}
