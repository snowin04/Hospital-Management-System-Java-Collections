package com.hms.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A medication in the pharmacy catalog (Level 5). Stock and expiry are tracked
 * separately by the {@code PharmacyInventory}; this object holds catalog data.
 */
public class Medication {

    private final String id;     // e.g. "MED-001"
    private final String name;   // e.g. "Paracetamol"
    private final LocalDate expiryDate;

    public Medication(String id, String name, LocalDate expiryDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Medication id must not be blank");
        }
        this.id = id;
        this.name = name;
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /** @return whole days until expiry from {@code today} (negative if expired). */
    public long daysUntilExpiry(LocalDate today) {
        return java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medication)) return false;
        return id.equals(((Medication) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s [%s] exp %s", name, id, expiryDate);
    }
}
