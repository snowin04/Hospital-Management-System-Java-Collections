package com.hms.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A prescription linking a patient to a drug on a given date (Level 5).
 * The {@code PrescriptionManager} uses the drug name + date to enforce the
 * "no duplicate of the same drug within 30 days" rule.
 */
public class Prescription {

    private final String id;        // e.g. "RX-001"
    private final String patientId;
    private final String drugName;
    private final int quantity;
    private final LocalDate prescribedOn;

    public Prescription(String id, String patientId, String drugName,
                        int quantity, LocalDate prescribedOn) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Prescription id must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.id = id;
        this.patientId = Objects.requireNonNull(patientId, "patientId");
        this.drugName = Objects.requireNonNull(drugName, "drugName");
        this.quantity = quantity;
        this.prescribedOn = Objects.requireNonNull(prescribedOn, "prescribedOn");
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDrugName() {
        return drugName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getPrescribedOn() {
        return prescribedOn;
    }

    @Override
    public String toString() {
        return String.format("%s: %s x%d for %s on %s",
                id, drugName, quantity, patientId, prescribedOn);
    }
}
