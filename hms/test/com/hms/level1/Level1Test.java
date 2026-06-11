package com.hms.level1;

import com.hms.model.Patient;
import com.hms.model.TriageLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Level 1 tests: registry duplicate handling, ordering, discharge; queue FIFO. */
class Level1Test {

    private Patient pat(String id, int min) {
        return new Patient(id, "Name-" + id, 40, TriageLevel.URGENT,
                LocalDateTime.of(2026, 6, 8, 8, min));
    }

    @Test
    void registersNewPatient() {
        PatientRegistry r = new PatientRegistry();
        assertTrue(r.registerPatient(pat("PAT-001", 0)));
        assertEquals(1, r.getPatientCount());
    }

    @Test
    void rejectsDuplicateId() {
        PatientRegistry r = new PatientRegistry();
        r.registerPatient(pat("PAT-001", 0));
        assertFalse(r.registerPatient(pat("PAT-001", 5)));
    }

    @Test
    void findReturnsNullForMissing() {
        assertNull(new PatientRegistry().findPatientById("PAT-404"));
    }

    @Test
    void ordersByRegistrationNewestFirst() {
        PatientRegistry r = new PatientRegistry();
        r.registerPatient(pat("PAT-001", 0));
        r.registerPatient(pat("PAT-002", 10));
        assertEquals("PAT-002", r.getPatientsByRegistrationOrder().get(0).getId());
    }

    @Test
    void dischargeRemovesRecord() {
        PatientRegistry r = new PatientRegistry();
        r.registerPatient(pat("PAT-001", 0));
        assertTrue(r.dischargePatient("PAT-001"));
        assertFalse(r.dischargePatient("PAT-001"));
        assertEquals(0, r.getPatientCount());
    }

    @Test
    void queueIsFifoAndHandlesEmpty() {
        EmergencyQueue q = new EmergencyQueue();
        assertNull(q.getNextPatient());
        q.addEmergencyPatient(pat("PAT-001", 0));
        q.addEmergencyPatient(pat("PAT-002", 1));
        assertEquals("PAT-001", q.peekNextPatient().getId());
        assertEquals("PAT-001", q.getNextPatient().getId());
        assertEquals(1, q.getWaitingCount());
    }

    @Test
    void rejectsNullPatient() {
        assertThrows(IllegalArgumentException.class,
                () -> new PatientRegistry().registerPatient(null));
    }
}
