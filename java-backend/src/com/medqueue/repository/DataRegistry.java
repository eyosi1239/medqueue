package com.medqueue.repository;

import com.medqueue.model.Doctor;

public class DataRegistry {
    private static DataRegistry instance;

    public final DoctorRepository      doctors      = new DoctorRepository();
    public final AppointmentRepository appointments = new AppointmentRepository();
    public final MedicalRecordRepository records    = new MedicalRecordRepository();
    public final InvoiceRepository     invoices     = new InvoiceRepository();

    private DataRegistry() { seedDoctors(); }

    public static DataRegistry getInstance() {
        if (instance == null) instance = new DataRegistry();
        return instance;
    }

    private void seedDoctors() {
        Doctor d1 = new Doctor("Dr. Abebe Girma", "General Medicine");
        d1.setPhone("+251911000001"); d1.setEmail("abebe@medqueue.et");
        d1.setBio("Senior general practitioner with 15 years experience.");
        d1.setWorkingDays("Mon,Tue,Wed,Thu,Fri");
        doctors.save(d1.getId(), d1);

        Doctor d2 = new Doctor("Dr. Sara Tesfaye", "Cardiology");
        d2.setPhone("+251911000002"); d2.setEmail("sara@medqueue.et");
        d2.setBio("Specialist in cardiovascular diseases."); d2.setStatus("busy");
        d2.setWorkingDays("Mon,Wed,Fri"); d2.setMaxDailyPatients(15);
        doctors.save(d2.getId(), d2);

        Doctor d3 = new Doctor("Dr. Yonas Kebede", "Pediatrics");
        d3.setPhone("+251911000003"); d3.setEmail("yonas@medqueue.et");
        d3.setBio("Dedicated pediatrician specializing in child care.");
        d3.setWorkingDays("Mon,Tue,Thu,Fri"); d3.setMaxDailyPatients(25);
        doctors.save(d3.getId(), d3);

        Doctor d4 = new Doctor("Dr. Eyossias Tesfaye", "Neurologist");
        d4.setPhone("+251911000004"); d4.setEmail("yonas@medqueue.et");
        d4.setBio("Specializes in diagnosing and treating diseases of the brain, spinal cord and nerves..");
        d4.setWorkingDays("Mon,Tue,Thu,Fri"); d4.setMaxDailyPatients(25);
        doctors.save(d4.getId(), d4);
    }
}
