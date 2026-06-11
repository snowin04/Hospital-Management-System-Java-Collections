package com.hms.level3;

import com.hms.model.Patient;
import com.hms.model.TriageLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Level 3 tests: triage comparator correctness, unique tracking, range queries. */
class Level3Test {

    private Patient p(String id, int age, TriageLevel level, int min) {
        return new Patient(id, id, age, level, LocalDateTime.of(2026, 6, 8, 8, min));
    }

    @Test
    void higherSeverityServedFirst() {
        TriageSystem t = new TriageSystem();
        t.addPatient(p("ROUTINE", 30, TriageLevel.ROUTINE, 0));
        t.addPatient(p("CRIT", 30, TriageLevel.CRITICAL, 0));
        assertEquals("CRIT", t.getNextPatient().getId());
    }

    @Test
    void sameLevelOlderFirst() {
        TriageSystem t = new TriageSystem();
        t.addPatient(p("YOUNG", 30, TriageLevel.CRITICAL, 5));
        t.addPatient(p("OLD", 80, TriageLevel.CRITICAL, 5));
        assertEquals("OLD", t.getNextPatient().getId());
    }

    @Test
    void sameLevelAndAgeEarlierRegistrationFirst() {
        TriageSystem t = new TriageSystem();
        t.addPatient(p("LATE", 50, TriageLevel.URGENT, 30));
        t.addPatient(p("EARLY", 50, TriageLevel.URGENT, 0));
        assertEquals("EARLY", t.getNextPatient().getId());
    }

    @Test
    void emptyQueueReturnsNull() {
        assertNull(new TriageSystem().getNextPatient());
    }

    @Test
    void trackerPreventsDoubleAdmission() {
        UniquePatientTracker tr = new UniquePatientTracker();
        assertTrue(tr.admit("PAT-001"));
        assertFalse(tr.admit("PAT-001"));
        assertTrue(tr.isAdmitted("PAT-001"));
        assertTrue(tr.discharge("PAT-001"));
        assertFalse(tr.isAdmitted("PAT-001"));
        assertEquals(1, tr.dischargedCount());
    }

    @Test
    void recordSystemAgeRangeQuery() {
        PatientRecordSystem rs = new PatientRecordSystem();
        rs.add(p("A", 30, TriageLevel.ROUTINE, 0));
        rs.add(p("B", 65, TriageLevel.ROUTINE, 1));
        rs.add(p("C", 80, TriageLevel.ROUTINE, 2));
        assertEquals(2, rs.getByAgeAtLeast(60).size());
    }
}
