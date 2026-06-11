package com.hms.level1;

import com.hms.model.Patient;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Level 1.2 &mdash; Emergency waiting queue backed by a {@link LinkedList}
 * used through the {@link Queue} interface (FIFO).
 *
 * <p>A {@code LinkedList} gives O(1) enqueue at the tail and O(1) dequeue at
 * the head, which is the textbook choice for a plain waiting line. (Clinical
 * urgency ordering is handled separately by the Level 3 triage
 * {@code PriorityQueue}; this queue is strictly first-come, first-served.)</p>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #addEmergencyPatient}</td><td>O(1)</td></tr>
 *   <tr><td>{@link #getNextPatient}</td><td>O(1)</td></tr>
 *   <tr><td>{@link #peekNextPatient}</td><td>O(1)</td></tr>
 *   <tr><td>{@link #getWaitingCount}</td><td>O(1)</td></tr>
 * </table>
 */
public class EmergencyQueue {

    private final Queue<Patient> queue = new LinkedList<>();

    /** Add a patient to the tail of the queue (FIFO). */
    public void addEmergencyPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("patient must not be null");
        }
        queue.offer(patient);
    }

    /** Remove and return the head, or {@code null} if the queue is empty. */
    public Patient getNextPatient() {
        return queue.poll();
    }

    /** Inspect the head without removing it, or {@code null} if empty. */
    public Patient peekNextPatient() {
        return queue.peek();
    }

    public int getWaitingCount() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
