package com.hms.level4;

import com.hms.model.Ward;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Level 4 &mdash; Bed allocation system.
 *
 * <p>Three collections cooperate:
 * <ul>
 *   <li>{@code bedToPatient} &mdash; a {@link LinkedHashMap} so the order in
 *       which beds were assigned is preserved for audit trails;</li>
 *   <li>{@code patientToBed} &mdash; a {@link HashMap} giving the reverse
 *       mapping for O(1) "which bed is this patient in?" lookups;</li>
 *   <li>{@code occupancy} &mdash; an {@link EnumMap} keyed by {@link Ward}
 *       for type-safe, zero-boxing per-ward counts.</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #assignBed}/{@link #releaseBed}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #getBedOf}/{@link #getPatientInBed}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #getMostCrowdedWard}</td><td>O(w), w = number of wards</td></tr>
 * </table>
 */
public class BedAllocationSystem {

    private final Map<Integer, String> bedToPatient = new LinkedHashMap<>();
    private final Map<String, Integer> patientToBed = new HashMap<>();
    private final EnumMap<Ward, Integer> occupancy = new EnumMap<>(Ward.class);

    public BedAllocationSystem() {
        for (Ward w : Ward.values()) {
            occupancy.put(w, 0);
        }
    }

    /**
     * Assign a specific bed to a patient.
     *
     * @return {@code true} on success; {@code false} if the bed is taken or the
     *         patient already holds a bed.
     * @throws IllegalArgumentException if {@code bedNumber} is out of range.
     */
    public boolean assignBed(String patientId, int bedNumber) {
        Ward ward = Ward.forBed(bedNumber); // throws if invalid bed number
        if (bedToPatient.containsKey(bedNumber) || patientToBed.containsKey(patientId)) {
            return false;
        }
        bedToPatient.put(bedNumber, patientId);
        patientToBed.put(patientId, bedNumber);
        occupancy.merge(ward, 1, Integer::sum);
        return true;
    }

    /** Auto-assign the first free bed in a ward. @return bed number or -1. */
    public int assignToWard(String patientId, Ward ward) {
        if (patientToBed.containsKey(patientId)) {
            return -1;
        }
        for (int bed = ward.getFirstBed(); bed <= ward.getLastBed(); bed++) {
            if (!bedToPatient.containsKey(bed)) {
                assignBed(patientId, bed);
                return bed;
            }
        }
        return -1; // ward full
    }

    /** @return {@code true} if the patient was occupying a bed and is released. */
    public boolean releaseBed(String patientId) {
        Integer bed = patientToBed.remove(patientId);
        if (bed == null) {
            return false;
        }
        bedToPatient.remove(bed);
        occupancy.merge(Ward.forBed(bed), -1, Integer::sum);
        return true;
    }

    /** @return the bed number a patient occupies, or {@code null}. */
    public Integer getBedOf(String patientId) {
        return patientToBed.get(patientId);
    }

    /** @return the patient occupying a bed, or {@code null}. */
    public String getPatientInBed(int bedNumber) {
        return bedToPatient.get(bedNumber);
    }

    public int availableBeds(Ward ward) {
        return ward.capacity() - occupancy.getOrDefault(ward, 0);
    }

    /** @return occupancy fraction in [0,1] for a ward. */
    public double occupancyRate(Ward ward) {
        return (double) occupancy.getOrDefault(ward, 0) / ward.capacity();
    }

    /** @return the ward with the highest occupancy rate. */
    public Ward getMostCrowdedWard() {
        Ward worst = null;
        double max = -1;
        for (Ward w : Ward.values()) {
            double rate = occupancyRate(w);
            if (rate > max) {
                max = rate;
                worst = w;
            }
        }
        return worst;
    }

    /** @return an unmodifiable view of the assignment-ordered bed map (audit trail). */
    public Map<Integer, String> getAuditTrail() {
        return Collections.unmodifiableMap(bedToPatient);
    }
}
