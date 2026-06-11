package com.hms.level2;

import com.hms.model.Doctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 2 &mdash; Doctor directory.
 *
 * <p>Two maps are kept in sync:
 * <ul>
 *   <li>{@code byId}: doctor id &rarr; {@link Doctor}, for O(1) lookup;</li>
 *   <li>{@code bySpecialty}: specialty &rarr; list of doctors, an inverted index
 *       so specialty queries are O(1) to reach the bucket instead of scanning
 *       every doctor.</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #addDoctor}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #findDoctor}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #removeDoctor}</td><td>O(k) (k = doctors in that specialty)</td></tr>
 *   <tr><td>{@link #getDoctorsBySpecialty}</td><td>O(1) to reach bucket</td></tr>
 * </table>
 */
public class DoctorDirectory {

    private final Map<String, Doctor> byId = new HashMap<>();
    private final Map<String, List<Doctor>> bySpecialty = new HashMap<>();

    /** @return {@code true} if added, {@code false} if the id already exists. */
    public boolean addDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("doctor must not be null");
        }
        if (byId.containsKey(doctor.getId())) {
            return false;
        }
        byId.put(doctor.getId(), doctor);
        bySpecialty.computeIfAbsent(doctor.getSpecialty(), k -> new ArrayList<>())
                   .add(doctor);
        return true;
    }

    /** @return the doctor with this id, or {@code null}. */
    public Doctor findDoctor(String id) {
        return byId.get(id);
    }

    /** @return {@code true} if a doctor was removed. */
    public boolean removeDoctor(String id) {
        Doctor removed = byId.remove(id);
        if (removed == null) {
            return false;
        }
        List<Doctor> bucket = bySpecialty.get(removed.getSpecialty());
        if (bucket != null) {
            bucket.remove(removed);
            if (bucket.isEmpty()) {
                bySpecialty.remove(removed.getSpecialty());
            }
        }
        return true;
    }

    /** @return an unmodifiable view of doctors in a specialty (never null). */
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return Collections.unmodifiableList(
                bySpecialty.getOrDefault(specialty, Collections.emptyList()));
    }

    /** @return any available doctor in a specialty, or {@code null}. */
    public Doctor findAvailableBySpecialty(String specialty) {
        for (Doctor d : bySpecialty.getOrDefault(specialty, Collections.emptyList())) {
            if (d.isAvailable()) {
                return d;
            }
        }
        return null;
    }

    public int getDoctorCount() {
        return byId.size();
    }
}
