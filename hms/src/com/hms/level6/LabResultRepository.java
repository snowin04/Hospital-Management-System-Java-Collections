package com.hms.level6;

import com.hms.model.LabResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Level 6 &mdash; Lab result repository offering several access views over the
 * same results:
 * <ul>
 *   <li>{@code byPatient}: patient id &rarr; results;</li>
 *   <li>{@code byTest}: test name &rarr; results;</li>
 *   <li>{@code byDate}: a {@link TreeMap} keyed by timestamp for chronological
 *       and range queries;</li>
 *   <li>{@code trends}: patient id &rarr; (timestamp &rarr; value) for a single
 *       test's progression over time.</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #add}</td><td>O(log n) (date tree insert)</td></tr>
 *   <tr><td>{@link #getByPatient}/{@link #getByTest}</td><td>O(1) to reach bucket</td></tr>
 *   <tr><td>{@link #getTrend}</td><td>O(log m) to reach a patient's series</td></tr>
 * </table>
 */
public class LabResultRepository {

    private final Map<String, List<LabResult>> byPatient = new HashMap<>();
    private final Map<String, List<LabResult>> byTest = new HashMap<>();
    private final TreeMap<LocalDateTime, LabResult> byDate = new TreeMap<>();
    private final Map<String, TreeMap<LocalDateTime, Double>> trends = new HashMap<>();

    public void add(LabResult r) {
        byPatient.computeIfAbsent(r.getPatientId(), k -> new ArrayList<>()).add(r);
        byTest.computeIfAbsent(r.getTestName(), k -> new ArrayList<>()).add(r);
        byDate.put(r.getTakenAt(), r);
        trends.computeIfAbsent(r.getPatientId(), k -> new TreeMap<>())
              .put(r.getTakenAt(), r.getValue());
    }

    public List<LabResult> getByPatient(String patientId) {
        return List.copyOf(byPatient.getOrDefault(patientId, List.of()));
    }

    public List<LabResult> getByTest(String testName) {
        return List.copyOf(byTest.getOrDefault(testName, List.of()));
    }

    public List<LabResult> getAll() {
        return new ArrayList<>(byDate.values());
    }

    /** @return chronological (time &rarr; value) series for a patient. */
    public TreeMap<LocalDateTime, Double> getTrend(String patientId) {
        return trends.getOrDefault(patientId, new TreeMap<>());
    }
}
