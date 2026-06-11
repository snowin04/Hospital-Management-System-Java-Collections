package com.hms.level7;

import com.hms.model.ClaimStatus;
import com.hms.model.InsuranceClaim;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Level 7 tests: business-keyed equality, identity-based dedup, nested reporting. */
class Level7Test {

    private final LocalDate date = LocalDate.of(2026, 6, 8);

    private InsuranceClaim claim(String id, String provider, String svc) {
        return new InsuranceClaim(id, provider, "PAT-001", svc, date, 100_000);
    }

    @Test
    void equalityIgnoresClaimId() {
        assertEquals(claim("C1", "Apollo", "SVC"), claim("C2", "Apollo", "SVC"));
        assertNotEquals(claim("C1", "Apollo", "SVC-A"), claim("C2", "Apollo", "SVC-B"));
    }

    @Test
    void duplicateInstanceIsFlaggedOriginalIsNot() {
        InsuranceClaimSystem sys = new InsuranceClaimSystem();
        InsuranceClaim original = claim("C1", "Apollo", "SVC");
        InsuranceClaim dup = claim("C2", "Apollo", "SVC");
        assertTrue(sys.submitClaim(original));
        assertFalse(sys.submitClaim(dup));
        assertTrue(sys.isDuplicate(dup));
        assertFalse(sys.isDuplicate(original));
        assertSame(original, sys.getOriginalOf(dup));
        assertEquals(1, sys.duplicateCount());
    }

    @Test
    void nestedDrillDownReturnsProviderMonthClaims() {
        InsuranceClaimSystem sys = new InsuranceClaimSystem();
        sys.submitClaim(claim("C1", "Apollo", "SVC-A"));
        sys.submitClaim(claim("C2", "Apollo", "SVC-B"));
        sys.submitClaim(claim("C3", "Fortis", "SVC-C"));
        assertEquals(2, sys.getProviderClaims("Apollo", 2026, 6).size());
        assertEquals(1, sys.getProviderClaims("Fortis", 2026, 6).size());
        assertTrue(sys.getProviderClaims("Apollo", 2025, 6).isEmpty());
    }

    @Test
    void statusGroupingAndTransitions() {
        InsuranceClaimSystem sys = new InsuranceClaimSystem();
        InsuranceClaim c = claim("C1", "Apollo", "SVC");
        sys.submitClaim(c);
        assertEquals(1, sys.getClaimsByStatus(ClaimStatus.SUBMITTED).size());
        sys.updateStatus(c, ClaimStatus.APPROVED);
        assertEquals(0, sys.getClaimsByStatus(ClaimStatus.SUBMITTED).size());
        assertEquals(1, sys.getClaimsByStatus(ClaimStatus.APPROVED).size());
        assertEquals(100_000, sys.approvedTotal("Apollo"));
    }

    @Test
    void unknownProviderReturnsEmpty() {
        assertTrue(new InsuranceClaimSystem().getProviderClaims("Nobody", 2026, 6).isEmpty());
    }
}
