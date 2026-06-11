package com.hms.level5;

import com.hms.model.Medication;
import com.hms.model.Prescription;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Level 5 tests: HashMap inventory + nested-map prescription tracking. */
class Level5Test {

    private final LocalDate today = LocalDate.of(2026, 6, 8);

    @Test
    void addStockAndDispense() {
        PharmacyInventory inv = new PharmacyInventory();
        Medication para = new Medication("MED-1", "Paracetamol", today.plusYears(1));
        inv.register(para);
        inv.addStock(para, 500);
        assertEquals(500, inv.getStock("MED-1"));
        assertTrue(inv.dispense("MED-1", 10));
        assertEquals(490, inv.getStock("MED-1"));
    }

    @Test
    void dispenseRejectsInsufficientStock() {
        PharmacyInventory inv = new PharmacyInventory();
        Medication m = new Medication("MED-1", "Aspirin", today.plusMonths(6));
        inv.register(m);
        inv.addStock(m, 5);
        assertFalse(inv.dispense("MED-1", 10)); // not enough
        assertEquals(5, inv.getStock("MED-1")); // unchanged
    }

    @Test
    void lowStockAndExpiryFilters() {
        PharmacyInventory inv = new PharmacyInventory();
        Medication low = new Medication("MED-1", "Insulin", today.plusYears(1));
        Medication soon = new Medication("MED-2", "Aspirin", today.plusDays(15));
        inv.register(low);
        inv.register(soon);
        inv.addStock(low, 23);
        inv.addStock(soon, 200);

        List<Medication> lowStock = inv.getLowStockMedications(50);
        assertTrue(lowStock.contains(low));
        assertFalse(lowStock.contains(soon));

        List<Medication> expiring = inv.getExpiringMedications(today, 30);
        assertTrue(expiring.contains(soon));
        assertFalse(expiring.contains(low));
    }

    @Test
    void duplicatePrescriptionWindowBoundary() {
        PrescriptionManager mgr = new PrescriptionManager();
        mgr.addPrescription(new Prescription("RX-1", "PAT-001", "Amoxicillin", 30, today));
        // 29 days later -> still inside 30-day window -> duplicate
        assertTrue(mgr.isDuplicatePrescription("PAT-001", "Amoxicillin", today.plusDays(29)));
        // exactly 30 days later -> outside window -> allowed
        assertFalse(mgr.isDuplicatePrescription("PAT-001", "Amoxicillin", today.plusDays(30)));
    }

    @Test
    void mostPrescribedDrugFrequencyCounting() {
        PrescriptionManager mgr = new PrescriptionManager();
        mgr.addPrescription(new Prescription("RX-1", "PAT-001", "Amoxicillin", 10, today));
        mgr.addPrescription(new Prescription("RX-2", "PAT-002", "Amoxicillin", 10, today));
        mgr.addPrescription(new Prescription("RX-3", "PAT-003", "Paracetamol", 10, today));
        assertEquals("Amoxicillin", mgr.getMostPrescribedDrug());
        assertEquals(2, mgr.prescriptionCountFor("Amoxicillin"));
    }

    @Test
    void prescriptionLookupByPatientAndId() {
        PrescriptionManager mgr = new PrescriptionManager();
        Prescription rx = new Prescription("RX-1", "PAT-001", "Amoxicillin", 10, today);
        assertTrue(mgr.addPrescription(rx));
        assertSame(rx, mgr.getById("RX-1"));
        assertEquals(1, mgr.getPrescriptionsForPatient("PAT-001").size());
        assertTrue(mgr.getPrescriptionsForPatient("PAT-999").isEmpty());
    }
}
