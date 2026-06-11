package com.hms.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A registered patient. This is the central entity of the system and is
 * referenced by nearly every level (queues, triage, beds, prescriptions, etc.).
 *
 * <p>Equality and hash code are based solely on the immutable patient {@code id},
 * which lets a {@code Patient} be used safely as a key in hash-based collections
 * and as a member of a {@link java.util.HashSet}.</p>
 */
public class Patient {

    private final String id;          // e.g. "PAT-001"
    private final String name;
    private final int age;
    private final LocalDateTime registeredAt;
    private TriageLevel triageLevel;  // may change while the patient is treated
    private boolean admitted;

    public Patient(String id, String name, int age, TriageLevel triageLevel) {
        this(id, name, age, triageLevel, LocalDateTime.now());
    }

    /** Full constructor; an explicit timestamp keeps tests deterministic. */
    public Patient(String id, String name, int age, TriageLevel triageLevel,
                   LocalDateTime registeredAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Patient id must not be blank");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age must not be negative");
        }
        this.id = id;
        this.name = name;
        this.age = age;
        this.triageLevel = triageLevel;
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public TriageLevel getTriageLevel() {
        return triageLevel;
    }

    public void setTriageLevel(TriageLevel triageLevel) {
        this.triageLevel = triageLevel;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        return id.equals(((Patient) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        String level = triageLevel == null ? "n/a"
                : "Level " + triageLevel.getCode() + " - " + triageLevel.getLabel();
        return String.format("%s: %s (%d) [%s]", id, name, age, level);
    }
}
