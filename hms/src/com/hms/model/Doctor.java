package com.hms.model;

import java.util.Objects;

/**
 * A physician. Identified by an immutable {@code id} and grouped by
 * {@code specialty} for filtered lookups in the doctor directory (Level 2).
 */
public class Doctor {

    private final String id;        // e.g. "DOC-001"
    private final String name;
    private final String specialty; // e.g. "Cardiology"
    private boolean available;

    public Doctor(String id, String name, String specialty) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Doctor id must not be blank");
        }
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        return id.equals(((Doctor) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Dr. %s (%s)", name, specialty);
    }
}
