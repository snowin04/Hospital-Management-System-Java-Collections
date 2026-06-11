package com.hms.level6;

import com.hms.model.LabResult;
import com.hms.model.ResultCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 6 &mdash; Statistical analysis over a {@link LabResultRepository}.
 * Demonstrates custom {@link Comparator}s (top-N by value) and aggregation
 * (per-test means, category counts).
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #findAbnormalResults}/{@link #getCriticalResults}</td><td>O(n)</td></tr>
 *   <tr><td>{@link #getTopHighestResults}</td><td>O(n log n) sort</td></tr>
 *   <tr><td>{@link #getAverageValues}</td><td>O(n)</td></tr>
 * </table>
 */
public class LabAnalytics {

    private final LabResultRepository repository;

    public LabAnalytics(LabResultRepository repository) {
        this.repository = repository;
    }

    public List<LabResult> findAbnormalResults() {
        List<LabResult> out = new ArrayList<>();
        for (LabResult r : repository.getAll()) {
            if (r.isAbnormal()) out.add(r);
        }
        return out;
    }

    public List<LabResult> getCriticalResults() {
        List<LabResult> out = new ArrayList<>();
        for (LabResult r : repository.getAll()) {
            if (r.isCritical()) out.add(r);
        }
        return out;
    }

    /** @return the {@code n} highest results by value, descending. */
    public List<LabResult> getTopHighestResults(int n) {
        List<LabResult> all = repository.getAll();
        all.sort(Comparator.comparingDouble(LabResult::getValue).reversed());
        return all.subList(0, Math.min(n, all.size()));
    }

    /** @return count of results in each {@link ResultCategory}. */
    public Map<ResultCategory, Integer> categorizeResults() {
        EnumMap<ResultCategory, Integer> counts = new EnumMap<>(ResultCategory.class);
        for (ResultCategory c : ResultCategory.values()) counts.put(c, 0);
        for (LabResult r : repository.getAll()) {
            counts.merge(r.category(), 1, Integer::sum);
        }
        return counts;
    }

    /** @return mean value per test name. */
    public Map<String, Double> getAverageValues() {
        Map<String, double[]> acc = new HashMap<>(); // [sum, count]
        for (LabResult r : repository.getAll()) {
            double[] a = acc.computeIfAbsent(r.getTestName(), k -> new double[2]);
            a[0] += r.getValue();
            a[1] += 1;
        }
        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            averages.put(e.getKey(), e.getValue()[0] / e.getValue()[1]);
        }
        return averages;
    }
}
