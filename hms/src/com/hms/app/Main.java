package com.hms.app;

import com.hms.level8.HospitalManagementSystem;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.InsuranceClaim;
import com.hms.model.LabResult;
import com.hms.model.Medication;
import com.hms.model.Patient;
import com.hms.model.Prescription;
import com.hms.model.TriageLevel;
import com.hms.model.Ward;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * End-to-end demonstration of the Hospital Management System. Run this class to
 * see every level exercised against sample data and a final dashboard.
 */
public class Main {

    public static void main(String[] args) {
        HospitalManagementSystem hms = new HospitalManagementSystem();
        LocalDate today = LocalDate.of(2026, 6, 8);

        banner("LEVEL 1 \u2014 Registration & Emergency Queue");
        Patient p1 = new Patient("PAT-001", "John Smith", 45, TriageLevel.EMERGENT,
                LocalDateTime.of(2026, 6, 8, 8, 0));
        Patient p2 = new Patient("PAT-002", "Mary Johnson", 70, TriageLevel.CRITICAL,
                LocalDateTime.of(2026, 6, 8, 8, 5));
        Patient p3 = new Patient("PAT-003", "Robert Brown", 30, TriageLevel.URGENT,
                LocalDateTime.of(2026, 6, 8, 8, 10));
        System.out.println("register PAT-001: " + hms.admitPatient(p1));
        System.out.println("register PAT-002: " + hms.admitPatient(p2));
        System.out.println("register PAT-003: " + hms.admitPatient(p3));
        System.out.println("duplicate PAT-001: " + hms.admitPatient(
                new Patient("PAT-001", "Imposter", 99, TriageLevel.ROUTINE)));
        hms.emergencyQueue().addEmergencyPatient(p1);
        hms.emergencyQueue().addEmergencyPatient(p3);
        System.out.println("queue size: " + hms.emergencyQueue().getWaitingCount());
        System.out.println("next (FIFO): " + hms.emergencyQueue().getNextPatient().getName());

        banner("LEVEL 2 \u2014 Doctor Directory & Appointments");
        Doctor dSmith = new Doctor("DOC-001", "Smith", "Cardiology");
        Doctor dJones = new Doctor("DOC-002", "Jones", "Neurology");
        hms.doctors().addDoctor(dSmith);
        hms.doctors().addDoctor(dJones);
        System.out.println("cardiologists: " + hms.doctors().getDoctorsBySpecialty("Cardiology"));
        LocalDateTime slot10 = AppointmentSchedulerSlot(today, 10, 0);
        System.out.println("book 10:00 Smith: " + hms.scheduler().bookAppointment(
                new Appointment(p1, dSmith, slot10)));
        System.out.println("conflict 10:00 Smith: " + !hms.scheduler().bookAppointment(
                new Appointment(p2, dSmith, slot10)));
        hms.scheduler().bookAppointment(new Appointment(p2, dJones,
                AppointmentSchedulerSlot(today, 9, 0)));
        System.out.println("next appointment: " + hms.scheduler().getNextAppointment());

        banner("LEVEL 3 \u2014 Triage Priority");
        hms.triage().addPatient(p1);
        hms.triage().addPatient(p2);
        hms.triage().addPatient(p3);
        System.out.println("triage processing order:");
        while (!hms.triage().isEmpty()) {
            Patient next = hms.triage().getNextPatient();
            System.out.println("   " + next);
        }
        System.out.println("patients aged 60+: " + hms.records().getByAgeAtLeast(60));

        banner("LEVEL 4 \u2014 Bed Allocation");
        System.out.println("PAT-002 -> ICU bed #" + hms.beds().assignToWard("PAT-002", Ward.ICU));
        System.out.println("PAT-001 -> General bed #" + hms.beds().assignToWard("PAT-001", Ward.GENERAL));
        System.out.println("ICU available: " + hms.beds().availableBeds(Ward.ICU) + "/" + Ward.ICU.capacity());
        System.out.println("most crowded ward: " + hms.beds().getMostCrowdedWard());

        banner("LEVEL 5 \u2014 Pharmacy & Prescriptions");
        Medication para = new Medication("MED-001", "Paracetamol", today.plusDays(120));
        Medication aspirin = new Medication("MED-002", "Aspirin", today.plusDays(15));
        hms.pharmacy().addStock(para, 500);
        hms.pharmacy().addStock(aspirin, 30);
        System.out.println("dispense 10 Paracetamol: " + hms.pharmacy().dispense("MED-001", 10)
                + " (stock " + hms.pharmacy().getStock("MED-001") + ")");
        System.out.println("expiring within 30d: " + hms.pharmacy().getExpiringMedications(today, 30));
        System.out.println("add Rx Amoxicillin: " + hms.prescriptions().addPrescription(
                new Prescription("RX-001", "PAT-001", "Amoxicillin", 1, today)));
        System.out.println("duplicate Rx (5 days later): " + !hms.prescriptions().addPrescription(
                new Prescription("RX-002", "PAT-001", "Amoxicillin", 1, today.plusDays(5))));
        System.out.println("most prescribed: " + hms.prescriptions().getMostPrescribedDrug());

        banner("LEVEL 6 \u2014 Lab Results & Analytics");
        hms.labRepo().add(new LabResult("PAT-002", "Blood Glucose", 250, "mg/dL",
                LocalDateTime.of(2026, 6, 4, 9, 0), 70, 100, 50, 240));
        hms.labRepo().add(new LabResult("PAT-003", "Hemoglobin", 8.2, "g/dL",
                LocalDateTime.of(2026, 6, 5, 9, 0), 12, 17, 9, 99));
        hms.labRepo().add(new LabResult("PAT-001", "Blood Glucose", 95, "mg/dL",
                LocalDateTime.of(2026, 6, 6, 9, 0), 70, 100, 50, 240));
        System.out.println("critical results: " + hms.labAnalytics().getCriticalResults());
        System.out.println("categories: " + hms.labAnalytics().categorizeResults());
        System.out.println("avg per test: " + hms.labAnalytics().getAverageValues());

        banner("LEVEL 7 \u2014 Insurance Claims & Dedup");
        InsuranceClaim c1 = new InsuranceClaim("C001", "Apollo", "PAT-001", "SVC-CARDIO",
                today, 850_000);
        InsuranceClaim c2 = new InsuranceClaim("C002", "Fortis", "PAT-002", "SVC-NEURO",
                today, 620_000);
        InsuranceClaim c3 = new InsuranceClaim("C003", "Apollo", "PAT-001", "SVC-CARDIO",
                today, 850_000); // same patient+service+date as C001 -> duplicate
        System.out.println("submit C001: " + hms.claims().submitClaim(c1));
        System.out.println("submit C002: " + hms.claims().submitClaim(c2));
        System.out.println("submit C003 (dup): " + hms.claims().submitClaim(c3));
        System.out.println("C003 is duplicate of: "
                + hms.claims().getOriginalOf(c3).getClaimId());

        banner("LEVEL 8 \u2014 Integration");
        Patient emergency = new Patient("PAT-999", "Emergency Case", 60, TriageLevel.CRITICAL);
        System.out.println(hms.handleEmergency(emergency, "Cardiology"));
        System.out.println();
        System.out.println(hms.getPatientCompleteView("PAT-002"));
        System.out.println(hms.dashboard());
    }

    private static LocalDateTime AppointmentSchedulerSlot(LocalDate d, int h, int m) {
        return LocalDateTime.of(d, java.time.LocalTime.of(h, m));
    }

    private static void banner(String title) {
        System.out.println("\n--------------------------------------------------");
        System.out.println(title);
        System.out.println("--------------------------------------------------");
    }
}
