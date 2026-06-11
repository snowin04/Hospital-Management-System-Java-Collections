package com.hms.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * An insurance claim (Level 7).
 *
 * <p><b>Equality is intentionally business-keyed</b>: two claims are considered
 * equal when they share the same {@code patientId}, {@code serviceCode} and
 * {@code serviceDate}. This is what makes hash-based duplicate detection
 * possible &mdash; a freshly submitted claim that {@code equals()} an existing
 * one is a likely duplicate/fraud even though it is a different object and has a
 * different {@code claimId}.</p>
 *
 * <p>The {@code claimId} deliberately does <i>not</i> participate in equality;
 * the {@code InsuranceClaimSystem} uses an {@link java.util.IdentityHashMap} to
 * track distinct <i>object instances</i> alongside this logical equality.</p>
 */
public class InsuranceClaim {

    private final String claimId;     // e.g. "C001" (NOT part of equality)
    private final String providerId;  // e.g. "Apollo"
    private final String patientId;
    private final String serviceCode; // e.g. "SVC-CARDIO"
    private final LocalDate serviceDate;
    private final double amount;
    private ClaimStatus status;

    public InsuranceClaim(String claimId, String providerId, String patientId,
                          String serviceCode, LocalDate serviceDate, double amount) {
        this.claimId = Objects.requireNonNull(claimId, "claimId");
        this.providerId = Objects.requireNonNull(providerId, "providerId");
        this.patientId = Objects.requireNonNull(patientId, "patientId");
        this.serviceCode = Objects.requireNonNull(serviceCode, "serviceCode");
        this.serviceDate = Objects.requireNonNull(serviceDate, "serviceDate");
        this.amount = amount;
        this.status = ClaimStatus.SUBMITTED;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public int getYear() {
        return serviceDate.getYear();
    }

    public int getMonth() {
        return serviceDate.getMonthValue();
    }

    public double getAmount() {
        return amount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    /** Business equality: patient + service + date (claimId excluded). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsuranceClaim)) return false;
        InsuranceClaim other = (InsuranceClaim) o;
        return patientId.equals(other.patientId)
                && serviceCode.equals(other.serviceCode)
                && serviceDate.equals(other.serviceDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, serviceCode, serviceDate);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s/%s/%s \u20b9%,.0f (%s)",
                claimId, providerId, patientId, serviceCode, serviceDate, amount, status);
    }
}
