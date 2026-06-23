package com.medqueue.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Invoice {
    private String id;
    private String number;
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String items; // JSON string
    private double subtotal;
    private double tax;
    private double total;
    private String status;
    private String notes;
    private String paidAt;
    private String createdAt;

    public Invoice(String number, String patientName, String doctorName,
                   String items, double subtotal, double tax, double total) {
        this.id = UUID.randomUUID().toString();
        this.number = number;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.status = "unpaid";
        this.createdAt = LocalDateTime.now().toString();
    }

    // Full constructor
    public Invoice(String id, String number, String patientName, String patientPhone,
                   String doctorName, String items, double subtotal, double tax,
                   double total, String status, String notes, String paidAt, String createdAt) {
        this.id = id; this.number = number; this.patientName = patientName;
        this.patientPhone = patientPhone; this.doctorName = doctorName;
        this.items = items; this.subtotal = subtotal; this.tax = tax; this.total = total;
        this.status = status; this.notes = notes; this.paidAt = paidAt; this.createdAt = createdAt;
    }

    // Getters
    public String getId()           { return id; }
    public String getNumber()       { return number; }
    public String getPatientName()  { return patientName; }
    public String getPatientPhone() { return patientPhone; }
    public String getDoctorName()   { return doctorName; }
    public String getItems()        { return items; }
    public double getSubtotal()     { return subtotal; }
    public double getTax()          { return tax; }
    public double getTotal()        { return total; }
    public String getStatus()       { return status; }
    public String getNotes()        { return notes; }
    public String getPaidAt()       { return paidAt; }
    public String getCreatedAt()    { return createdAt; }

    // Setters
    public void setPatientPhone(String p) { this.patientPhone = p; }
    public void setStatus(String s) {
        this.status = s;
        if ("paid".equals(s)) this.paidAt = LocalDateTime.now().toString();
    }
    public void setNotes(String n) { this.notes = n; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"number\":\"%s\",\"patientName\":\"%s\"," +
            "\"patientPhone\":%s,\"doctorName\":%s," +
            "\"items\":%s,\"subtotal\":%.2f,\"tax\":%.2f,\"total\":%.2f," +
            "\"status\":\"%s\",\"notes\":%s,\"paidAt\":%s,\"createdAt\":\"%s\"}",
            id, number, esc(patientName),
            jsonStr(patientPhone), jsonStr(doctorName),
            items != null ? items : "[]", subtotal, tax, total,
            status, jsonStr(notes), jsonStr(paidAt), createdAt
        );
    }

    private String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
    private String jsonStr(String s) { return s == null ? "null" : "\"" + esc(s) + "\""; }
}
