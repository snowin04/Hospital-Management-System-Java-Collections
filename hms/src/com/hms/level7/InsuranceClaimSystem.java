package com.hms.level7;

import com.hms.model.ClaimStatus;
import com.hms.model.InsuranceClaim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 7 &mdash; Insurance claim processing, the most structurally complex
 * level. It combines three distinct map strategies:
 *
 * <ul>
 *   <li><b>3-level nested {@link HashMap}</b>: Provider &rarr; Year &rarr; Month
 *       &rarr; list of claims, for drill-down reporting in O(1) per level.</li>
 *   <li><b>{@link EnumMap}</b> keyed by {@link ClaimStatus}: type-safe grouping
 *       of claims by workflow state.</li>
 *   <li><b>{@link IdentityHashMap}</b>: tracks distinct object <i>instances</i>
 *       by reference. Combined with the business-keyed {@code equals} on
 *       {@link InsuranceClaim}, this lets us flag a freshly submitted claim that
 *       is logically equal to one already seen (same patient + service + date)
 *       as a duplicate, mapping the duplicate instance &rarr; the original.</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #submitClaim}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #getProviderClaims}</td><td>O(1) per nesting level</td></tr>
 *   <tr><td>{@link #getClaimsByStatus}</td><td>O(1) to reach bucket</td></tr>
 *   <tr><td>{@link #isDuplicate}</td><td>O(1) expected</td></tr>
 * </table>
 */
public class InsuranceClaimSystem {

    // provider -> year -> month -> claims
    private final Map<String, Map<Integer, Map<Integer, List<InsuranceClaim>>>> nested =
            new HashMap<>();

    private final EnumMap<ClaimStatus, List<InsuranceClaim>> byStatus =
            new EnumMap<>(ClaimStatus.class);

    /** First logically-equal claim seen, keyed by its business identity. */
    private final Map<InsuranceClaim, InsuranceClaim> canonical = new HashMap<>();

    /** duplicate instance -> the original instance it duplicates. */
    private final IdentityHashMap<InsuranceClaim, InsuranceClaim> duplicates =
            new IdentityHashMap<>();

    public InsuranceClaimSystem() {
        for (ClaimStatus s : ClaimStatus.values()) {
            byStatus.put(s, new ArrayList<>());
        }
    }

    /**
     * Submit a claim. If a logically-equal claim was already submitted, this
     * instance is recorded as a duplicate of the original and is NOT filed into
     * the nested structure.
     *
     * @return {@code true} if filed as new, {@code false} if it was a duplicate.
     */
    public boolean submitClaim(InsuranceClaim claim) {
        InsuranceClaim original = canonical.get(claim);
        if (original != null && original != claim) {
            duplicates.put(claim, original); // identity-keyed: this exact object
            return false;
        }
        canonical.put(claim, claim);

        nested.computeIfAbsent(claim.getProviderId(), p -> new HashMap<>())
              .computeIfAbsent(claim.getYear(), y -> new HashMap<>())
              .computeIfAbsent(claim.getMonth(), m -> new ArrayList<>())
              .add(claim);

        byStatus.get(claim.getStatus()).add(claim);
        return true;
    }

    /** @return {@code true} if this exact instance was flagged as a duplicate. */
    public boolean isDuplicate(InsuranceClaim claim) {
        return duplicates.containsKey(claim);
    }

    /** @return the original claim that {@code duplicate} duplicates, or {@code null}. */
    public InsuranceClaim getOriginalOf(InsuranceClaim duplicate) {
        return duplicates.get(duplicate);
    }

    /** Drill-down report: all claims for a provider in a given year and month. */
    public List<InsuranceClaim> getProviderClaims(String providerId, int year, int month) {
        Map<Integer, Map<Integer, List<InsuranceClaim>>> byYear = nested.get(providerId);
        if (byYear == null) return List.of();
        Map<Integer, List<InsuranceClaim>> byMonth = byYear.get(year);
        if (byMonth == null) return List.of();
        return List.copyOf(byMonth.getOrDefault(month, List.of()));
    }

    /** Total approved amount for a provider across all years/months. */
    public double approvedTotal(String providerId) {
        double total = 0;
        Map<Integer, Map<Integer, List<InsuranceClaim>>> byYear = nested.get(providerId);
        if (byYear == null) return 0;
        for (Map<Integer, List<InsuranceClaim>> byMonth : byYear.values()) {
            for (List<InsuranceClaim> claims : byMonth.values()) {
                for (InsuranceClaim c : claims) {
                    if (c.getStatus() == ClaimStatus.APPROVED) {
                        total += c.getAmount();
                    }
                }
            }
        }
        return total;
    }

    public List<InsuranceClaim> getClaimsByStatus(ClaimStatus status) {
        return List.copyOf(byStatus.getOrDefault(status, List.of()));
    }

    /**
     * Move a claim to a new status, keeping the status index consistent.
     * (Only affects filed, non-duplicate claims.)
     */
    public void updateStatus(InsuranceClaim claim, ClaimStatus newStatus) {
        byStatus.get(claim.getStatus()).remove(claim);
        claim.setStatus(newStatus);
        byStatus.get(newStatus).add(claim);
    }

    public int duplicateCount() {
        return duplicates.size();
    }
}
