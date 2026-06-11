package com.hms.level5;

import com.hms.model.Medication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 5 &mdash; Pharmacy inventory.
 *
 * <p>Parallel {@link HashMap}s keep catalog, stock and expiry indexed for O(1)
 * access:
 * <ul>
 *   <li>{@code catalog}: medication id &rarr; {@link Medication};</li>
 *   <li>{@code stock}: medication id &rarr; quantity on hand;</li>
 *   <li>{@code byExpiry}: expiry date &rarr; medications expiring that day.</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Complexity</caption>
 *   <tr><th>Operation</th><th>Time</th></tr>
 *   <tr><td>{@link #addStock}/{@link #dispense}</td><td>O(1) expected</td></tr>
 *   <tr><td>{@link #getLowStockMedications}</td><td>O(n)</td></tr>
 *   <tr><td>{@link #getExpiringMedications}</td><td>O(n)</td></tr>
 * </table>
 */
public class PharmacyInventory {

    private final Map<String, Medication> catalog = new HashMap<>();
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<LocalDate, List<Medication>> byExpiry = new HashMap<>();

    /** Register a medication in the catalog (idempotent on id). */
    public void register(Medication med) {
        catalog.putIfAbsent(med.getId(), med);
        byExpiry.computeIfAbsent(med.getExpiryDate(), d -> new ArrayList<>()).add(med);
        stock.putIfAbsent(med.getId(), 0);
    }

    /** Add {@code qty} units of stock; registers the medication if new. */
    public void addStock(Medication med, int qty) {
        if (qty < 0) throw new IllegalArgumentException("qty must be >= 0");
        register(med);
        stock.merge(med.getId(), qty, Integer::sum);
    }

    /**
     * Dispense units from stock.
     *
     * @return {@code true} if there was enough stock and it was deducted.
     */
    public boolean dispense(String medicationId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive");
        int have = stock.getOrDefault(medicationId, 0);
        if (have < qty) {
            return false;
        }
        stock.put(medicationId, have - qty);
        return true;
    }

    public int getStock(String medicationId) {
        return stock.getOrDefault(medicationId, 0);
    }

    /** @return medications whose stock is strictly below {@code threshold}. */
    public List<Medication> getLowStockMedications(int threshold) {
        List<Medication> low = new ArrayList<>();
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            if (e.getValue() < threshold) {
                Medication m = catalog.get(e.getKey());
                if (m != null) low.add(m);
            }
        }
        return low;
    }

    /** @return medications expiring within {@code days} of {@code today} (inclusive). */
    public List<Medication> getExpiringMedications(LocalDate today, int days) {
        List<Medication> expiring = new ArrayList<>();
        for (Medication m : catalog.values()) {
            long left = m.daysUntilExpiry(today);
            if (left >= 0 && left <= days) {
                expiring.add(m);
            }
        }
        return expiring;
    }
}
