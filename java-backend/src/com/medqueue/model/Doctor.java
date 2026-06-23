package com.medqueue.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doctor entity — OOP model with encapsulation
 */
public class Doctor {
    private String id;
    private String name;
    private String specialty;
    private String phone;
    private String email;
    private String bio;
    private String status; // available, busy, off
    private int maxDailyPatients;
    private String workingDays;
    private String createdAt;

    // Constructor
    public Doctor(String name, String specialty) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.specialty = specialty;
        this.status = "available";
        this.maxDailyPatients = 20;
        this.workingDays = "Mon,Tue,Wed,Thu,Fri";
        this.createdAt = LocalDateTime.now().toString();
    }

    // Full constructor for loading from file
    public Doctor(String id, String name, String specialty, String phone,
                  String email, String bio, String status,
                  int maxDailyPatients, String workingDays, String createdAt) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.bio = bio;
        this.status = status;
        this.maxDailyPatients = maxDailyPatients;
        this.workingDays = workingDays;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId()             { return id; }
    public String getName()           { return name; }
    public String getSpecialty()      { return specialty; }
    public String getPhone()          { return phone; }
    public String getEmail()          { return email; }
    public String getBio()            { return bio; }
    public String getStatus()         { return status; }
    public int getMaxDailyPatients()  { return maxDailyPatients; }
    public String getWorkingDays()    { return workingDays; }
    public String getCreatedAt()      { return createdAt; }

    // Setters
    public void setName(String name)                      { this.name = name; }
    public void setSpecialty(String specialty)            { this.specialty = specialty; }
    public void setPhone(String phone)                    { this.phone = phone; }
    public void setEmail(String email)                    { this.email = email; }
    public void setBio(String bio)                        { this.bio = bio; }
    public void setStatus(String status)                  { this.status = status; }
    public void setMaxDailyPatients(int maxDailyPatients) { this.maxDailyPatients = maxDailyPatients; }
    public void setWorkingDays(String workingDays)        { this.workingDays = workingDays; }

    // Convert to JSON string manually
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\"," +
            "\"phone\":%s,\"email\":%s,\"bio\":%s," +
            "\"status\":\"%s\",\"maxDailyPatients\":%d," +
            "\"workingDays\":\"%s\",\"createdAt\":\"%s\"}",
            id, esc(name), esc(specialty),
            jsonStr(phone), jsonStr(email), jsonStr(bio),
            status, maxDailyPatients,
            workingDays != null ? workingDays : "", createdAt
        );
    }

    private String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
    private String jsonStr(String s) { return s == null ? "null" : "\"" + esc(s) + "\""; }

    @Override
    public String toString() { return toJson(); }
}
