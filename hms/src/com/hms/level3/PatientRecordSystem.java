package com.hms.level3;

import com.hms.model.Patient;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Level 3.3 &mdash; Maintains two {@link TreeSet} views of the same patients,
 * one ordered by name and one ordered by age, enabling sorted iteration and
 * efficient range queries (e.g. "patients aged 60+").
 *
 * <p>Both comparators end with patient id as a tie-breaker so that patients who
 * share a name (or an age) are not silently collapsed by the set.</p>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #add}</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #getByAgeAtLeast}</td><td>O(log n + k)</td></tr>
 *   <tr><td>{@link #getByName}</td><td>O(n) iteration in name order</td></tr>
 * </table>
 */
public class PatientRecordSystem {

    private final TreeSet<Patient> byName = new TreeSet<>(
            Comparator.comparing(Patient::getName).thenComparing(Patient::getId));

    private final TreeSet<Patient> byAge = new TreeSet<>(
            Comparator.comparingInt(Patient::getAge).thenComparing(Patient::getId));

    public boolean add(Patient patient) {
        boolean added = byName.add(patient);
        byAge.add(patient);
        return added;
    }

    public boolean remove(Patient patient) {
        byAge.remove(patient);
        return byName.remove(patient);
    }

    /** @return all patients in ascending name order. */
    public List<Patient> getByName() {
        return List.copyOf(byName);
    }

    /** @return all patients in ascending age order. */
    public List<Patient> getByAge() {
        return List.copyOf(byAge);
    }

    /**
     * Range query: patients at least {@code minAge} years old, age-ascending.
     * Uses a synthetic floor probe and {@link TreeSet#tailSet}.
     */
    public List<Patient> getByAgeAtLeast(int minAge) {
        // id "" sorts before any real id, so the probe is a true lower bound.
        Patient probe = new Patient("\u0000probe", "", minAge, null,
                java.time.LocalDateTime.MIN);
        return List.copyOf(byAge.tailSet(probe, true));
    }

    public int size() {
        return byName.size();
    }
}
