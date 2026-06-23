package com.medqueue.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private String id;
    private String patientName;
    private String patientPhone;
    private String doctorId;
    private String doctorName;
    private String specialty;
    private String date;
    private String time;
    private String reason;
    private String type;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public Appointment(String patientName, String doctorId, String doctorName,
                       String specialty, String date, String time) {
        this.id = UUID.randomUUID().toString();
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.date = date;
        this.time = time;
        this.type = "consultation";
        this.status = "scheduled";
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = this.createdAt;
    }

    // Full constructor for loading from file
    public Appointment(String id, String patientName, String patientPhone,
                       String doctorId, String doctorName, String specialty,
                       String date, String time, String reason, String type,
                       String status, String notes, String createdAt, String updatedAt) {
        this.id = id; this.patientName = patientName; this.patientPhone = patientPhone;
        this.doctorId = doctorId; this.doctorName = doctorName; this.specialty = specialty;
        this.date = date; this.time = time; this.reason = reason; this.type = type;
        this.status = status; this.notes = notes;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    // Getters
    public String getId()           { return id; }
    public String getPatientName()  { return patientName; }
    public String getPatientPhone() { return patientPhone; }
    public String getDoctorId()     { return doctorId; }
    public String getDoctorName()   { return doctorName; }
    public String getSpecialty()    { return specialty; }
    public String getDate()         { return date; }
    public String getTime()         { return time; }
    public String getReason()       { return reason; }
    public String getType()         { return type; }
    public String getStatus()       { return status; }
    public String getNotes()        { return notes; }
    public String getCreatedAt()    { return createdAt; }
    public String getUpdatedAt()    { return updatedAt; }

    // Setters
    public void setPatientPhone(String p) { this.patientPhone = p; }
    public void setReason(String r)       { this.reason = r; }
    public void setType(String t)         { this.type = t; }
    public void setStatus(String s)       { this.status = s; this.updatedAt = LocalDateTime.now().toString(); }
    public void setNotes(String n)        { this.notes = n; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"patientName\":\"%s\",\"patientPhone\":%s," +
            "\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"specialty\":\"%s\"," +
            "\"date\":\"%s\",\"time\":\"%s\",\"reason\":%s,\"type\":\"%s\"," +
            "\"status\":\"%s\",\"notes\":%s,\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
            id, esc(patientName), jsonStr(patientPhone),
            doctorId, esc(doctorName), esc(specialty),
            date, time, jsonStr(reason), type,
            status, jsonStr(notes), createdAt, updatedAt
        );
    }

    private String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
    private String jsonStr(String s) { return s == null ? "null" : "\"" + esc(s) + "\""; }
}
