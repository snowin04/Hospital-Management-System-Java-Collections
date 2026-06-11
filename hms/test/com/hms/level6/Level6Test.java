package com.hms.level6;

import com.hms.model.LabResult;
import com.hms.model.ResultCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** Level 6 tests: multi-map views, trends, and analytics with custom comparators. */
class Level6Test {

    private final LocalDateTime t0 = LocalDateTime.of(2026, 6, 1, 9, 0);

    // Glucose: normal 70-100, abnormal 101-240, panic >240 or <50.
    private LabResult glucose(String pid, double value, LocalDateTime when) {
        return new LabResult(pid, "Glucose", value, "mg/dL", when, 70, 100, 50, 240);
    }

    @Test
    void repositoryViewsByPatientAndTest() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-001", 95, t0));
        repo.add(glucose("PAT-002", 180, t0.plusHours(1)));
        assertEquals(1, repo.getByPatient("PAT-001").size());
        assertEquals(2, repo.getByTest("Glucose").size());
        assertEquals(2, repo.getAll().size());
        assertTrue(repo.getByPatient("PAT-999").isEmpty());
    }

    @Test
    void patientTrendIsChronological() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-002", 250, t0.plusDays(2)));
        repo.add(glucose("PAT-002", 95, t0));
        repo.add(glucose("PAT-002", 180, t0.plusDays(1)));
        TreeMap<LocalDateTime, Double> trend = repo.getTrend("PAT-002");
        // TreeMap keeps ascending date order: 95 -> 180 -> 250
        assertArrayEquals(new Double[]{95.0, 180.0, 250.0},
                trend.values().toArray(new Double[0]));
    }

    @Test
    void findsAbnormalResults() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-001", 95, t0));    // normal
        repo.add(glucose("PAT-002", 180, t0));   // abnormal (>100, not panic)
        LabAnalytics an = new LabAnalytics(repo);
        List<LabResult> abnormal = an.findAbnormalResults();
        assertEquals(1, abnormal.size());
        assertEquals(180, abnormal.get(0).getValue(), 1e-9);
    }

    @Test
    void getsCriticalResults() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-001", 95, t0));    // normal
        repo.add(glucose("PAT-003", 250, t0));   // panic (>240)
        repo.add(glucose("PAT-004", 40, t0));    // panic (<50)
        LabAnalytics an = new LabAnalytics(repo);
        assertEquals(2, an.getCriticalResults().size());
    }

    @Test
    void topHighestResultsSortedDescending() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-001", 95, t0));
        repo.add(glucose("PAT-002", 250, t0));
        repo.add(glucose("PAT-003", 180, t0));
        LabAnalytics an = new LabAnalytics(repo);
        List<LabResult> top2 = an.getTopHighestResults(2);
        assertEquals(2, top2.size());
        assertEquals(250, top2.get(0).getValue(), 1e-9);
        assertEquals(180, top2.get(1).getValue(), 1e-9);
    }

    @Test
    void categorizeAndAverage() {
        LabResultRepository repo = new LabResultRepository();
        repo.add(glucose("PAT-001", 90, t0));    // normal
        repo.add(glucose("PAT-002", 200, t0));   // abnormal
        repo.add(glucose("PAT-003", 250, t0));   // critical
        LabAnalytics an = new LabAnalytics(repo);

        Map<ResultCategory, Integer> cats = an.categorizeResults();
        assertEquals(1, cats.get(ResultCategory.NORMAL));
        assertEquals(1, cats.get(ResultCategory.ABNORMAL));
        assertEquals(1, cats.get(ResultCategory.CRITICAL));

        // mean of 90, 200, 250 = 180.0
        assertEquals(180.0, an.getAverageValues().get("Glucose"), 1e-9);
    }
}
