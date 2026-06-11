package com.hms.level4;

import com.hms.model.Ward;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Level 4 tests: LinkedHashMap audit order + EnumMap ward statistics. */
class Level4Test {

    @Test
    void assignAndReverseLookup() {
        BedAllocationSystem beds = new BedAllocationSystem();
        assertTrue(beds.assignBed("PAT-001", 3)); // ICU bed
        assertEquals(Integer.valueOf(3), beds.getBedOf("PAT-001"));
        assertEquals("PAT-001", beds.getPatientInBed(3));
    }

    @Test
    void rejectsTakenBedAndDoubleAssign() {
        BedAllocationSystem beds = new BedAllocationSystem();
        beds.assignBed("PAT-001", 3);
        assertFalse(beds.assignBed("PAT-002", 3));   // bed already occupied
        assertFalse(beds.assignBed("PAT-001", 4));   // patient already has a bed
    }

    @Test
    void invalidBedNumberThrows() {
        BedAllocationSystem beds = new BedAllocationSystem();
        assertThrows(IllegalArgumentException.class, () -> beds.assignBed("PAT-001", 0));
        assertThrows(IllegalArgumentException.class, () -> beds.assignBed("PAT-001", 101));
    }

    @Test
    void assignToWardPicksFirstFreeBed() {
        BedAllocationSystem beds = new BedAllocationSystem();
        int b1 = beds.assignToWard("PAT-001", Ward.ICU);
        int b2 = beds.assignToWard("PAT-002", Ward.ICU);
        assertEquals(1, b1);
        assertEquals(2, b2);
        assertEquals(8, beds.availableBeds(Ward.ICU)); // 10 capacity - 2 used
    }

    @Test
    void occupancyRateAndMostCrowdedWard() {
        BedAllocationSystem beds = new BedAllocationSystem();
        beds.assignToWard("PAT-001", Ward.ICU);   // 1/10  = 10%
        beds.assignToWard("PAT-002", Ward.ICU);   // 2/10  = 20%
        beds.assignToWard("PAT-003", Ward.EMERGENCY); // 1/10 = 10%
        assertEquals(0.20, beds.occupancyRate(Ward.ICU), 1e-9);
        assertEquals(Ward.ICU, beds.getMostCrowdedWard());
    }

    @Test
    void releaseFreesBedAndUpdatesStats() {
        BedAllocationSystem beds = new BedAllocationSystem();
        beds.assignBed("PAT-001", 3);
        assertTrue(beds.releaseBed("PAT-001"));
        assertNull(beds.getBedOf("PAT-001"));
        assertNull(beds.getPatientInBed(3));
        assertFalse(beds.releaseBed("PAT-001")); // already released
    }

    @Test
    void auditTrailPreservesAssignmentOrder() {
        BedAllocationSystem beds = new BedAllocationSystem();
        beds.assignBed("PAT-A", 5);
        beds.assignBed("PAT-B", 2);
        beds.assignBed("PAT-C", 9);
        // LinkedHashMap keeps insertion order: 5, 2, 9
        assertArrayEquals(new Integer[]{5, 2, 9},
                beds.getAuditTrail().keySet().toArray(new Integer[0]));
    }
}
