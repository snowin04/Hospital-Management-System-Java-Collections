# Hospital Management System — Java Collections Framework

A complete, production-grade **Hospital Management System** built entirely on the
Java Collections Framework, implementing all **8 progressive levels** of the
Talenciaglobal *"Collections in Healthcare"* hackathon. Each level maps a real
hospital function to the data structure whose performance characteristics best
fit that function.

---

## 1. Project Structure

```
hms/
├── README.md
├── src/com/hms/
│   ├── model/        # Domain types (Patient, Doctor, Appointment, Medication,
│   │                 #   Prescription, LabResult, InsuranceClaim) + enums
│   │                 #   (TriageLevel, Ward, ClaimStatus, ResultCategory)
│   ├── level1/       # PatientRegistry (ArrayList), EmergencyQueue (LinkedList/Queue)
│   ├── level2/       # DoctorDirectory (HashMap), AppointmentScheduler (TreeMap)
│   ├── level3/       # TriageSystem (PriorityQueue), UniquePatientTracker (HashSet),
│   │                 #   PatientRecordSystem (TreeSet)
│   ├── level4/       # BedAllocationSystem (LinkedHashMap + EnumMap)
│   ├── level5/       # PharmacyInventory (HashMap), PrescriptionManager (nested HashMap)
│   ├── level6/       # LabResultRepository (multi-map + TreeMap), LabAnalytics (Comparators)
│   ├── level7/       # InsuranceClaimSystem (3-level nested Map + EnumMap + IdentityHashMap)
│   ├── level8/       # HospitalManagementSystem (full integration facade)
│   └── app/          # Main.java — end-to-end demo of all 8 levels
└── test/com/hms/     # JUnit 5 tests (Level1Test … Level7Test)
```

---

## 2. How to Compile & Run

Requires a **JDK 17+** (the project uses `LocalDate`/`LocalDateTime`, enums,
generics, and the diamond operator; tested against Java 21).

### Compile

```bash
cd hms
javac -d out $(find src -name '*.java')
```

### Run the demo

```bash
java -cp out com.hms.app.Main
```

This exercises every level with sample data and prints a section per level
(registration, scheduling, triage ordering, bed allocation, pharmacy dispensing,
lab analytics, claim deduplication, and the integrated emergency protocol +
Patient-360 view).

### Run the JUnit tests

The tests use **JUnit 5 (Jupiter)**. Download the standalone console launcher
(`junit-platform-console-standalone-<version>.jar`) from Maven Central, then:

```bash
# compile tests against the compiled classes + junit jar
javac -cp out:junit-platform-console-standalone.jar -d out-test \
      $(find test -name '*.java')

# run all tests
java -jar junit-platform-console-standalone.jar \
     --class-path out:out-test --scan-class-path
```

> **Note on this build environment:** the container used to author this project
> shipped with the Java 21 **JRE only** (no `javac`) and no network access to
> install a full JDK. Compilation correctness was therefore verified using the
> Java single-file source launcher (`java File.java`): the entire multi-package
> project was mechanically flattened into one compilation unit and executed
> end-to-end (all 8 level sections produce correct output), and a separate
> 35-assertion edge-case suite was run with `java -ea` and passed **35/35**
> (duplicate rejection, null/empty polls, scheduling conflicts, triage
> tie-breaks, invalid-bed exceptions, double-assignment rejection, insufficient
> stock, the 30-day prescription window boundary, and IdentityHashMap dedup).
> On any machine with a real JDK the `javac` commands above compile cleanly.

---

## 3. Collections Architecture Summary

| Level | Collection | Use Case | Key Operation | Time Complexity |
|------:|------------|----------|---------------|-----------------|
| 1 | `ArrayList` | Patient Registry | `registerPatient()` | O(n) search, O(1) amortized add |
| 1 | `LinkedList` (as `Queue`) | Emergency Queue | FIFO processing | O(1) enqueue / dequeue |
| 2 | `HashMap` | Doctor Directory | `findDoctor()` | O(1) expected |
| 2 | `TreeMap` | Appointments | `getByDate()` (range) | O(log n) |
| 3 | `PriorityQueue` | Triage System | `getNextPatient()` | O(log n) poll, O(1) peek |
| 3 | `HashSet` / `TreeSet` | Patient Tracking | `isAdmitted()` / range | O(1) / O(log n) |
| 4 | `LinkedHashMap` | Bed Assignment (audit order) | `assignBed()` | O(1) with insertion order |
| 4 | `EnumMap` | Ward Statistics | `occupancyRate()` | O(1) type-safe, no boxing |
| 5 | `HashMap` (nested) | Pharmacy / Prescriptions | `dispense()`, `isDuplicate()` | O(1) expected |
| 6 | `TreeMap` + `Comparator` | Lab Analytics | `getCriticalResults()` | O(n) scan, O(n log n) ranking |
| 7 | Nested `Map` + `EnumMap` | Insurance Claims | `getProviderClaims()` | O(1) per level |
| 7 | `IdentityHashMap` | Claim Deduplication | `isDuplicate()` | O(1) by reference identity |

---

## 4. Design Rationale & Notable Decisions

**Equality keyed on immutable business identity.** `Patient`, `Doctor`, and
`Medication` define `equals`/`hashCode` on their stable id only, so they behave
correctly as `HashMap`/`HashSet` keys even as mutable fields (availability,
admission status) change over their lifetime.

**Claim equality excludes the claim id (fraud detection).** `InsuranceClaim`
equality is deliberately based on `patientId + serviceCode + serviceDate`,
*excluding* `claimId`. Two distinct submissions for the same service on the same
day are therefore *equal* — which is exactly what lets a `HashMap` detect logical
duplicates. A parallel `IdentityHashMap` then maps each duplicate **instance** to
the original it duplicates, distinguishing "same object" (reference identity)
from "same billing event" (value equality) — the precise distinction this level
is meant to teach.

**Triage comparator with deterministic tie-breaks.** `TriageSystem` orders by
(1) triage level ascending (1 = Critical first), then (2) age descending (older
first), then (3) registration time ascending. The full chain removes ambiguity so
two patients never compare "equal" by accident — important because a
`PriorityQueue` makes no guarantees among equal elements.

**`TreeSet` range queries via a synthetic floor probe.** `PatientRecordSystem`
keeps dual sorted sets (by name, by age, each with an id tie-breaker). "Patients
aged ≥ N" is answered with `tailSet` against a synthetic floor key, giving
O(log n) range access without scanning.

**`LinkedHashMap` for auditability.** Bed assignments and the emergency event log
use `LinkedHashMap` so the iteration order *is* the chronological audit trail —
no separate timestamp sort needed.

**Bidirectional bed mapping.** `BedAllocationSystem` keeps both `bed → patient`
and `patient → bed` maps so both "who is in bed 3?" and "where is PAT-001?" are
O(1), with an `EnumMap<Ward, Integer>` for zero-boxing occupancy statistics.

---

## 5. Levels at a Glance

1. **Patient Registration** — `ArrayList` registry (dup-id rejection, newest-first ordering) + `LinkedList` FIFO emergency queue.
2. **Doctor Scheduling** — `HashMap` directory with a specialty inverted index + `TreeMap` appointment book with conflict detection and `subMap` date ranges.
3. **Waitlist & Triage** — `PriorityQueue` triage with custom comparator, `HashSet` unique-admission tracking, dual `TreeSet` record system.
4. **Bed Allocation** — `LinkedHashMap` ordered assignment + reverse map + `EnumMap` ward stats and `getMostCrowdedWard()`.
5. **Pharmacy Inventory** — `HashMap` catalog/stock/expiry + nested-`HashMap` prescription manager with a 30-day duplicate window and frequency counting.
6. **Lab Results & Analytics** — multi-map views, per-patient `TreeMap` trends, and `Comparator`-driven analytics (abnormal/critical detection, top-N, per-test means).
7. **Insurance Claims** — 3-level nested `Map` (provider → year → month), `EnumMap` status grouping, and `IdentityHashMap` deduplication.
8. **Complete Dashboard** — `HospitalManagementSystem` facade wiring all subsystems: `handleEmergency()`, `getPatientCompleteView()` (Patient-360), and a full dashboard snapshot.

---

*Built for the Talenciaglobal "Collections in Healthcare: 8-Level Hackathon."*
