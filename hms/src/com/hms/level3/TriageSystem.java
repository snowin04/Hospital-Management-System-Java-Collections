package com.hms.level3;

import com.hms.model.Patient;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Level 3.1 &mdash; Triage system backed by a {@link PriorityQueue} with a
 * custom {@link Comparator} that faithfully models ESI prioritization.
 *
 * <p>Ordering rules (highest priority dequeued first):
 * <ol>
 *   <li>lower triage code first (Level 1 = Critical wins);</li>
 *   <li>tie &rarr; older patient first;</li>
 *   <li>tie &rarr; earlier registration time first.</li>
 * </ol>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #addPatient}</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #getNextPatient}</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #peekNextPatient}</td><td>O(1)</td></tr>
 * </table>
 */
public class TriageSystem {

    /** Shared so it can be unit-tested directly. */
    public static final Comparator<Patient> TRIAGE_ORDER =
            Comparator.comparingInt((Patient p) -> p.getTriageLevel().getCode())
                      .thenComparing(Comparator.comparingInt(Patient::getAge).reversed())
                      .thenComparing(Patient::getRegisteredAt);

    private final PriorityQueue<Patient> queue = new PriorityQueue<>(TRIAGE_ORDER);

    /** Insert a patient according to triage priority. */
    public void addPatient(Patient patient) {
        if (patient == null || patient.getTriageLevel() == null) {
            throw new IllegalArgumentException("patient and triage level are required");
        }
        queue.offer(patient);
    }

    /** Remove and return the highest-priority patient, or {@code null}. */
    public Patient getNextPatient() {
        return queue.poll();
    }

    /** Inspect the highest-priority patient without removing, or {@code null}. */
    public Patient peekNextPatient() {
        return queue.peek();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
