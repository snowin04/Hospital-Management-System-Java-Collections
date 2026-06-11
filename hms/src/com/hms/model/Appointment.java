package com.hms.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A booked appointment that ties a patient to a doctor at a point in time.
 * Stored in a {@link java.util.TreeMap} keyed by {@link #getTime()} so the
 * scheduler stays chronologically ordered without manual sorting (Level 2).
 */
public class Appointment {

    private final Patient patient;
    private final Doctor doctor;
    private final LocalDateTime time;

    public Appointment(Patient patient, Doctor doctor, LocalDateTime time) {
        this.patient = Objects.requireNonNull(patient, "patient");
        this.doctor = Objects.requireNonNull(doctor, "doctor");
        this.time = Objects.requireNonNull(time, "time");
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("%s \u2014 %s with %s",
                time.toLocalTime(), patient.getName(), doctor);
    }
}
