package com.medqueue.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicalRecord {
    private String id;
    private String patientName;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String prescription;
    private String notes;
    private String followUpDate;
    private String bloodPressure;
    private String temperature;
    private String heartRate;
    private String createdAt;

    public MedicalRecord(String patientName, String doctorId, String doctorName, String diagnosis) {
        this.id = UUID.randomUUID().toString();
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Full constructor
    public MedicalRecord(String id, String patientName, String patientId,
                         String doctorId, String doctorName, String diagnosis,
                         String symptoms, String treatment, String prescription,
                         String notes, String followUpDate, String bloodPressure,
                         String temperature, String heartRate, String createdAt) {
        this.id = id; this.patientName = patientName; this.patientId = patientId;
        this.doctorId = doctorId; this.doctorName = doctorName; this.diagnosis = diagnosis;
        this.symptoms = symptoms; this.treatment = treatment; this.prescription = prescription;
        this.notes = notes; this.followUpDate = followUpDate; this.bloodPressure = bloodPressure;
        this.temperature = temperature; this.heartRate = heartRate; this.createdAt = createdAt;
    }

    // Getters
    public String getId()            { return id; }
    public String getPatientName()   { return patientName; }
    public String getPatientId()     { return patientId; }
    public String getDoctorId()      { return doctorId; }
    public String getDoctorName()    { return doctorName; }
    public String getDiagnosis()     { return diagnosis; }
    public String getSymptoms()      { return symptoms; }
    public String getTreatment()     { return treatment; }
    public String getPrescription()  { return prescription; }
    public String getNotes()         { return notes; }
    public String getFollowUpDate()  { return followUpDate; }
    public String getBloodPressure() { return bloodPressure; }
    public String getTemperature()   { return temperature; }
    public String getHeartRate()     { return heartRate; }
    public String getCreatedAt()     { return createdAt; }

    // Setters
    public void setPatientId(String p)      { this.patientId = p; }
    public void setSymptoms(String s)       { this.symptoms = s; }
    public void setTreatment(String t)      { this.treatment = t; }
    public void setPrescription(String p)   { this.prescription = p; }
    public void setNotes(String n)          { this.notes = n; }
    public void setFollowUpDate(String f)   { this.followUpDate = f; }
    public void setBloodPressure(String b)  { this.bloodPressure = b; }
    public void setTemperature(String t)    { this.temperature = t; }
    public void setHeartRate(String h)      { this.heartRate = h; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"patientName\":\"%s\",\"patientId\":%s," +
            "\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"diagnosis\":\"%s\"," +
            "\"symptoms\":%s,\"treatment\":%s,\"prescription\":%s," +
            "\"notes\":%s,\"followUpDate\":%s," +
            "\"bloodPressure\":%s,\"temperature\":%s,\"heartRate\":%s," +
            "\"createdAt\":\"%s\"}",
            id, esc(patientName), jsonStr(patientId),
            doctorId, esc(doctorName), esc(diagnosis),
            jsonStr(symptoms), jsonStr(treatment), jsonStr(prescription),
            jsonStr(notes), jsonStr(followUpDate),
            jsonStr(bloodPressure), jsonStr(temperature), jsonStr(heartRate),
            createdAt
        );
    }

    private String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
    private String jsonStr(String s) { return s == null ? "null" : "\"" + esc(s) + "\""; }
}
