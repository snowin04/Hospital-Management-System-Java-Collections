package com.hms.level2;

import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.TriageLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Level 2 tests: O(1) directory, specialty index, TreeMap conflict + range queries. */
class Level2Test {

    private final LocalDate day = LocalDate.of(2026, 6, 8);
    private final Patient patient = new Patient("PAT-001", "P", 40, TriageLevel.URGENT);

    @Test
    void directoryLookupAndSpecialtyIndex() {
        DoctorDirectory dir = new DoctorDirectory();
        Doctor d = new Doctor("DOC-1", "Smith", "Cardiology");
        assertTrue(dir.addDoctor(d));
        assertFalse(dir.addDoctor(new Doctor("DOC-1", "Other", "Neuro")));
        assertSame(d, dir.findDoctor("DOC-1"));
        assertEquals(1, dir.getDoctorsBySpecialty("Cardiology").size());
        assertTrue(dir.getDoctorsBySpecialty("ENT").isEmpty());
    }

    @Test
    void removeDoctorClearsSpecialtyBucket() {
        DoctorDirectory dir = new DoctorDirectory();
        dir.addDoctor(new Doctor("DOC-1", "Smith", "Cardiology"));
        assertTrue(dir.removeDoctor("DOC-1"));
        assertTrue(dir.getDoctorsBySpecialty("Cardiology").isEmpty());
        assertFalse(dir.removeDoctor("DOC-1"));
    }

    @Test
    void rejectsDoubleBooking() {
        AppointmentScheduler s = new AppointmentScheduler();
        Doctor d = new Doctor("DOC-1", "Smith", "Cardiology");
        assertTrue(s.bookAppointment(new Appointment(patient, d, AppointmentScheduler.at(day, 10, 0))));
        assertFalse(s.bookAppointment(new Appointment(patient, d, AppointmentScheduler.at(day, 10, 0))));
    }

    @Test
    void differentDoctorsSameSlotAllowed() {
        AppointmentScheduler s = new AppointmentScheduler();
        assertTrue(s.bookAppointment(new Appointment(patient,
                new Doctor("DOC-1", "A", "Cardiology"), AppointmentScheduler.at(day, 10, 0))));
        assertTrue(s.bookAppointment(new Appointment(patient,
                new Doctor("DOC-2", "B", "Neuro"), AppointmentScheduler.at(day, 10, 0))));
    }

    @Test
    void nextAppointmentIsEarliestAndRangeQueryWorks() {
        AppointmentScheduler s = new AppointmentScheduler();
        Doctor d = new Doctor("DOC-1", "Smith", "Cardiology");
        s.bookAppointment(new Appointment(patient, d, AppointmentScheduler.at(day, 14, 0)));
        s.bookAppointment(new Appointment(patient, d, AppointmentScheduler.at(day, 9, 0)));
        assertEquals(9, s.getNextAppointment().getTime().getHour());
        assertEquals(2, s.getAppointmentsByDate(day).size());
        assertTrue(s.getAppointmentsByDate(day.plusDays(1)).isEmpty());
    }
}
