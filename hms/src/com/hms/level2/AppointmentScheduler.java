package com.hms.level2;

import com.hms.model.Appointment;
import com.hms.model.Doctor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Level 2 &mdash; Appointment scheduler backed by a {@link TreeMap} keyed by
 * {@link LocalDateTime}. The tree's natural ordering keeps appointments sorted
 * by time for free, so "next appointment" and date-range queries are cheap and
 * no manual sorting is ever needed.
 *
 * <p>Double-booking is rejected: a slot is taken if any existing appointment at
 * that exact time involves the same doctor.</p>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #bookAppointment}</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #getNextAppointment}</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #getAppointmentsByDate}</td><td>O(log n + k)</td></tr>
 * </table>
 */
public class AppointmentScheduler {

    /** time slot -> appointments booked in that slot (different doctors allowed). */
    private final TreeMap<LocalDateTime, List<Appointment>> schedule = new TreeMap<>();

    /**
     * Book an appointment unless the doctor is already booked at that time.
     *
     * @return {@code true} on success, {@code false} on a doctor conflict.
     */
    public boolean bookAppointment(Appointment appt) {
        if (appt == null) {
            throw new IllegalArgumentException("appointment must not be null");
        }
        List<Appointment> slot = schedule.get(appt.getTime());
        if (slot != null) {
            for (Appointment existing : slot) {
                if (existing.getDoctor().equals(appt.getDoctor())) {
                    return false; // double-booking the same doctor -> conflict
                }
            }
        }
        schedule.computeIfAbsent(appt.getTime(), t -> new ArrayList<>()).add(appt);
        return true;
    }

    /** @return {@code true} if the doctor already has an appointment at {@code time}. */
    public boolean hasConflict(Doctor doctor, LocalDateTime time) {
        List<Appointment> slot = schedule.get(time);
        if (slot == null) {
            return false;
        }
        return slot.stream().anyMatch(a -> a.getDoctor().equals(doctor));
    }

    /** @return the earliest upcoming appointment, or {@code null} if none. */
    public Appointment getNextAppointment() {
        Map.Entry<LocalDateTime, List<Appointment>> first = schedule.firstEntry();
        return (first == null || first.getValue().isEmpty()) ? null
                : first.getValue().get(0);
    }

    /**
     * All appointments on a calendar date, in chronological order, using a
     * half-open {@link TreeMap#subMap} over [startOfDay, startOfNextDay).
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        List<Appointment> result = new ArrayList<>();
        for (List<Appointment> slot : schedule.subMap(from, true, to, false).values()) {
            result.addAll(slot);
        }
        return result;
    }

    public int getAppointmentCount() {
        return schedule.values().stream().mapToInt(List::size).sum();
    }

    /** Convenience builder for a same-day slot. */
    public static LocalDateTime at(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
}
