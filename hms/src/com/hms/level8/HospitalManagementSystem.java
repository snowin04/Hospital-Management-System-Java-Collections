package com.hms.level8;

import com.hms.level1.EmergencyQueue;
import com.hms.level1.PatientRegistry;
import com.hms.level2.AppointmentScheduler;
import com.hms.level2.DoctorDirectory;
import com.hms.level3.PatientRecordSystem;
import com.hms.level3.TriageSystem;
import com.hms.level3.UniquePatientTracker;
import com.hms.level4.BedAllocationSystem;
import com.hms.level5.PharmacyInventory;
import com.hms.level5.PrescriptionManager;
import com.hms.level6.LabAnalytics;
import com.hms.level6.LabResultRepository;
import com.hms.level7.InsuranceClaimSystem;
import com.hms.model.Doctor;
import com.hms.model.LabResult;
import com.hms.model.Patient;
import com.hms.model.Prescription;
import com.hms.model.TriageLevel;
import com.hms.model.Ward;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 8 &mdash; The unified facade that wires every subsystem (Levels 1-7)
 * together, mirroring the multi-collection orchestration of a real EHR.
 *
 * <p>It exposes the two flagship integration flows from the brief:
 * {@link #handleEmergency} (the emergency protocol) and
 * {@link #getPatientCompleteView} (the patient-360 view), plus a dashboard
 * snapshot.</p>
 */
public class HospitalManagementSystem {

    private final PatientRegistry registry = new PatientRegistry();
    private final EmergencyQueue emergencyQueue = new EmergencyQueue();
    private final DoctorDirectory doctors = new DoctorDirectory();
    private final AppointmentScheduler scheduler = new AppointmentScheduler();
    private final TriageSystem triage = new TriageSystem();
    private final UniquePatientTracker tracker = new UniquePatientTracker();
    private final PatientRecordSystem records = new PatientRecordSystem();
    private final BedAllocationSystem beds = new BedAllocationSystem();
    private final PharmacyInventory pharmacy = new PharmacyInventory();
    private final PrescriptionManager prescriptions = new PrescriptionManager();
    private final LabResultRepository labRepo = new LabResultRepository();
    private final LabAnalytics labAnalytics = new LabAnalytics(labRepo);
    private final InsuranceClaimSystem claims = new InsuranceClaimSystem();

    /** Insertion-ordered audit log of significant events. */
    private final Map<Long, String> auditLog = new LinkedHashMap<>();
    private long auditSeq = 0;

    // ---- subsystem accessors (for demos / tests) -------------------------

    public PatientRegistry registry()        { return registry; }
    public EmergencyQueue emergencyQueue()   { return emergencyQueue; }
    public DoctorDirectory doctors()         { return doctors; }
    public AppointmentScheduler scheduler()  { return scheduler; }
    public TriageSystem triage()             { return triage; }
    public UniquePatientTracker tracker()    { return tracker; }
    public PatientRecordSystem records()     { return records; }
    public BedAllocationSystem beds()        { return beds; }
    public PharmacyInventory pharmacy()      { return pharmacy; }
    public PrescriptionManager prescriptions() { return prescriptions; }
    public LabResultRepository labRepo()     { return labRepo; }
    public LabAnalytics labAnalytics()       { return labAnalytics; }
    public InsuranceClaimSystem claims()     { return claims; }

    /** Register a patient and mirror them into the sorted record system. */
    public boolean admitPatient(Patient p) {
        boolean ok = registry.registerPatient(p);
        if (ok) {
            records.add(p);
            tracker.admit(p.getId());
        }
        return ok;
    }

    private void log(String event) {
        auditLog.put(auditSeq++, event);
    }

    /**
     * Emergency protocol. Queues the patient, triages them, reserves an ICU bed
     * for Level-1 cases, finds an available specialist, and writes an audit entry.
     *
     * @return a short human-readable summary of the actions taken.
     */
    public String handleEmergency(Patient patient, String requiredSpecialty) {
        StringBuilder sb = new StringBuilder();

        emergencyQueue.addEmergencyPatient(patient);          // 1. FIFO queue
        triage.addPatient(patient);                           // 2. priority triage
        tracker.admit(patient.getId());
        if (registry.findPatientById(patient.getId()) == null) {
            registry.registerPatient(patient);
            records.add(patient);
        }

        int reservedBed = -1;
        if (patient.getTriageLevel() == TriageLevel.CRITICAL) {  // 3. reserve ICU
            reservedBed = beds.assignToWard(patient.getId(), Ward.ICU);
        }

        Doctor specialist = doctors.findAvailableBySpecialty(requiredSpecialty); // 4. notify
        String docNote = specialist == null
                ? "no " + requiredSpecialty + " specialist available"
                : "Dr. " + specialist.getName() + " notified";

        String event = String.format(
                "EMERGENCY %s (%s) | bed=%s | %s",
                patient.getId(),
                patient.getTriageLevel().getLabel(),
                reservedBed > 0 ? "ICU #" + reservedBed : "none",
                docNote);
        log(event);                                            // 5. audit trail

        sb.append("\uD83D\uDEA8 ").append(event);
        return sb.toString();
    }

    /**
     * Patient-360 view: collects the patient's record, prescriptions, lab
     * results, bed assignment and triage state into one snapshot.
     */
    public String getPatientCompleteView(String patientId) {
        Patient p = registry.findPatientById(patientId);
        if (p == null) {
            return "Unknown patient: " + patientId;
        }
        List<Prescription> rx = prescriptions.getPrescriptionsForPatient(patientId);
        List<LabResult> labs = labRepo.getByPatient(patientId);
        Integer bed = beds.getBedOf(patientId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Patient 360: ").append(p).append(" ===\n");
        sb.append("  Admitted: ").append(tracker.isAdmitted(patientId)).append('\n');
        sb.append("  Bed: ").append(bed == null ? "none"
                : "#" + bed + " (" + Ward.forBed(bed) + ")").append('\n');
        sb.append("  Prescriptions: ").append(rx.size()).append('\n');
        for (Prescription r : rx) sb.append("    - ").append(r).append('\n');
        sb.append("  Lab results: ").append(labs.size()).append('\n');
        for (LabResult l : labs) sb.append("    - ").append(l).append('\n');
        return sb.toString();
    }

    /** @return a multi-section dashboard snapshot string. */
    public String dashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("================ HOSPITAL DASHBOARD ================\n");
        sb.append(String.format("Patients registered: %d | Active: %d | Emergency queue: %d%n",
                registry.getPatientCount(), tracker.activeCount(), emergencyQueue.getWaitingCount()));
        sb.append("Doctors on staff: ").append(doctors.getDoctorCount()).append('\n');
        sb.append("Appointments booked: ").append(scheduler.getAppointmentCount()).append('\n');
        sb.append("Bed occupancy:\n");
        for (Ward w : Ward.values()) {
            sb.append(String.format("   %-10s %3.0f%% (%d free)%n",
                    w, beds.occupancyRate(w) * 100, beds.availableBeds(w)));
        }
        sb.append("Most crowded ward: ").append(beds.getMostCrowdedWard()).append('\n');
        sb.append("Critical lab alerts: ").append(labAnalytics.getCriticalResults().size()).append('\n');
        sb.append("Duplicate claims flagged: ").append(claims.duplicateCount()).append('\n');
        sb.append("===================================================");
        return sb.toString();
    }

    /** @return a copy of the ordered audit log lines. */
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog.values());
    }
}
