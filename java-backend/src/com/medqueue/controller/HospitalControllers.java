package com.medqueue.controller;

import com.medqueue.model.*;
import com.medqueue.repository.DataRegistry;
import com.medqueue.server.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// ════════════════════════════════════════════════════════════
// DOCTOR CONTROLLER
// ════════════════════════════════════════════════════════════
public class DoctorController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String id = pathId(ex, "/api/doctors");

        if (id == null || id.isEmpty()) {
            if (isMethod(ex, "GET"))  { getAll(ex); return; }
            if (isMethod(ex, "POST")) { create(ex); return; }
        } else {
            if (isMethod(ex, "GET"))    { getOne(ex, id); return; }
            if (isMethod(ex, "PATCH"))  { update(ex, id); return; }
            if (isMethod(ex, "DELETE")) { delete(ex, id); return; }
        }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        List<Doctor> docs = db.doctors.findAllSorted();
        sendResponse(ex, 200, JsonParser.listToJson(docs.stream().map(Doctor::toJson).collect(Collectors.toList())));
    }

    private void getOne(HttpExchange ex, String id) throws IOException {
        Optional<Doctor> doc = db.doctors.findById(id);
        if (doc.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        sendResponse(ex, 200, doc.get().toJson());
    }

    private void create(HttpExchange ex) throws IOException {
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        String name = JsonParser.str(body, "name");
        String specialty = JsonParser.str(body, "specialty");
        if (name == null || specialty == null) {
            sendResponse(ex, 400, JsonParser.error("name and specialty required")); return;
        }
        Doctor d = new Doctor(name.trim(), specialty.trim());
        if (body.get("phone") != null) d.setPhone(JsonParser.str(body, "phone"));
        if (body.get("email") != null) d.setEmail(JsonParser.str(body, "email"));
        if (body.get("bio") != null) d.setBio(JsonParser.str(body, "bio"));
        if (body.get("workingDays") != null) d.setWorkingDays(JsonParser.str(body, "workingDays"));
        if (body.get("maxDailyPatients") != null) d.setMaxDailyPatients(JsonParser.intVal(body, "maxDailyPatients", 20));
        db.doctors.save(d.getId(), d);
        sendResponse(ex, 201, d.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<Doctor> opt = db.doctors.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        Doctor d = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("name") != null) d.setName(JsonParser.str(body, "name"));
        if (body.get("specialty") != null) d.setSpecialty(JsonParser.str(body, "specialty"));
        if (body.get("phone") != null) d.setPhone(JsonParser.str(body, "phone"));
        if (body.get("email") != null) d.setEmail(JsonParser.str(body, "email"));
        if (body.get("bio") != null) d.setBio(JsonParser.str(body, "bio"));
        if (body.get("status") != null) d.setStatus(JsonParser.str(body, "status"));
        if (body.get("workingDays") != null) d.setWorkingDays(JsonParser.str(body, "workingDays"));
        if (body.get("maxDailyPatients") != null) d.setMaxDailyPatients(JsonParser.intVal(body, "maxDailyPatients", 20));
        db.doctors.save(id, d);
        sendResponse(ex, 200, d.toJson());
    }

    private void delete(HttpExchange ex, String id) throws IOException {
        if (!db.doctors.existsById(id)) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        db.doctors.deleteById(id);
        sendResponse(ex, 200, JsonParser.ok("Doctor removed"));
    }
}

// ════════════════════════════════════════════════════════════
// APPOINTMENT CONTROLLER
// ════════════════════════════════════════════════════════════
class AppointmentController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String id = pathId(ex, "/api/appointments");

        if (path.contains("/slots")) { getSlots(ex); return; }

        if (id == null || id.isEmpty()) {
            if (isMethod(ex, "GET"))  { getAll(ex); return; }
            if (isMethod(ex, "POST")) { create(ex); return; }
        } else {
            if (isMethod(ex, "PATCH"))  { update(ex, id); return; }
            if (isMethod(ex, "DELETE")) { cancel(ex, id); return; }
        }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String date = params.get("date");
        String doctorId = params.get("doctorId");
        String status = params.get("status");
        List<Appointment> list;
        if (date != null && doctorId != null) list = db.appointments.findByDoctorAndDate(doctorId, date);
        else if (date != null) list = db.appointments.findByDate(date);
        else list = db.appointments.findAll(status);
        sendResponse(ex, 200, JsonParser.listToJson(list.stream().map(Appointment::toJson).collect(Collectors.toList())));
    }

    private void getSlots(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String doctorId = params.get("doctorId");
        String date = params.get("date");
        if (doctorId == null || date == null) {
            sendResponse(ex, 400, JsonParser.error("doctorId and date required")); return;
        }
        List<String> slots = new ArrayList<>();
        for (int h = 8; h < 18; h++) {
            for (String m : new String[]{"00", "30"}) {
                String t = String.format("%02d:%s", h, m);
                boolean taken = db.appointments.isSlotTaken(doctorId, date, t);
                slots.add(String.format("{\"time\":\"%s\",\"available\":%b}", t, !taken));
            }
        }
        sendResponse(ex, 200, "[" + String.join(",", slots) + "]");
    }

    private void create(HttpExchange ex) throws IOException {
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        String patientName = JsonParser.str(body, "patientName");
        String doctorId    = JsonParser.str(body, "doctorId");
        String date        = JsonParser.str(body, "date");
        String time        = JsonParser.str(body, "time");
        if (patientName == null || doctorId == null || date == null || time == null) {
            sendResponse(ex, 400, JsonParser.error("patientName, doctorId, date, time required")); return;
        }
        Optional<Doctor> doc = db.doctors.findById(doctorId);
        if (doc.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Doctor not found")); return; }
        if (db.appointments.isSlotTaken(doctorId, date, time)) {
            sendResponse(ex, 409, JsonParser.error("Time slot already booked")); return;
        }
        Appointment a = new Appointment(patientName.trim(), doctorId, doc.get().getName(),
                                        doc.get().getSpecialty(), date, time);
        if (body.get("patientPhone") != null) a.setPatientPhone(JsonParser.str(body, "patientPhone"));
        if (body.get("reason") != null) a.setReason(JsonParser.str(body, "reason"));
        if (body.get("type") != null) a.setType(JsonParser.str(body, "type"));
        db.appointments.save(a.getId(), a);
        sendResponse(ex, 201, a.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<Appointment> opt = db.appointments.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        Appointment a = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("status") != null) a.setStatus(JsonParser.str(body, "status"));
        if (body.get("notes")  != null) a.setNotes(JsonParser.str(body, "notes"));
        if (body.get("reason") != null) a.setReason(JsonParser.str(body, "reason"));
        db.appointments.save(id, a);
        sendResponse(ex, 200, a.toJson());
    }

    private void cancel(HttpExchange ex, String id) throws IOException {
        Optional<Appointment> opt = db.appointments.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        opt.get().setStatus("cancelled");
        sendResponse(ex, 200, JsonParser.ok("Appointment cancelled"));
    }
}

// ════════════════════════════════════════════════════════════
// MEDICAL RECORDS CONTROLLER
// ════════════════════════════════════════════════════════════
class MedicalRecordController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String id = pathId(ex, "/api/records");
        if (id == null || id.isEmpty()) {
            if (isMethod(ex, "GET"))  { getAll(ex); return; }
            if (isMethod(ex, "POST")) { create(ex); return; }
        } else {
            if (isMethod(ex, "PATCH")) { update(ex, id); return; }
        }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        String q = queryParams(ex).get("patientName");
        List<MedicalRecord> list = (q != null && !q.isBlank())
            ? db.records.findByPatientName(q)
            : db.records.findAllSorted();
        sendResponse(ex, 200, JsonParser.listToJson(list.stream().map(MedicalRecord::toJson).collect(Collectors.toList())));
    }

    private void create(HttpExchange ex) throws IOException {
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        String patientName = JsonParser.str(body, "patientName");
        String doctorId    = JsonParser.str(body, "doctorId");
        String diagnosis   = JsonParser.str(body, "diagnosis");
        if (patientName == null || doctorId == null || diagnosis == null) {
            sendResponse(ex, 400, JsonParser.error("patientName, doctorId, diagnosis required")); return;
        }
        String doctorName = db.doctors.findById(doctorId).map(Doctor::getName).orElse("Unknown");
        MedicalRecord r = new MedicalRecord(patientName.trim(), doctorId, doctorName, diagnosis.trim());
        r.setPatientId(JsonParser.str(body, "patientId"));
        r.setSymptoms(JsonParser.str(body, "symptoms"));
        r.setTreatment(JsonParser.str(body, "treatment"));
        r.setPrescription(JsonParser.str(body, "prescription"));
        r.setNotes(JsonParser.str(body, "notes"));
        r.setFollowUpDate(JsonParser.str(body, "followUpDate"));
        r.setBloodPressure(JsonParser.str(body, "bloodPressure"));
        r.setTemperature(JsonParser.str(body, "temperature"));
        r.setHeartRate(JsonParser.str(body, "heartRate"));
        db.records.save(r.getId(), r);
        sendResponse(ex, 201, r.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<MedicalRecord> opt = db.records.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        MedicalRecord r = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("notes") != null) r.setNotes(JsonParser.str(body, "notes"));
        if (body.get("followUpDate") != null) r.setFollowUpDate(JsonParser.str(body, "followUpDate"));
        db.records.save(id, r);
        sendResponse(ex, 200, r.toJson());
    }
}

// ════════════════════════════════════════════════════════════
// INVOICE CONTROLLER
// ════════════════════════════════════════════════════════════
class InvoiceController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String id = pathId(ex, "/api/invoices");
        if (id == null || id.isEmpty()) {
            if (isMethod(ex, "GET"))  { getAll(ex); return; }
            if (isMethod(ex, "POST")) { create(ex); return; }
        } else {
            if (isMethod(ex, "PATCH")) { update(ex, id); return; }
        }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        String status = queryParams(ex).get("status");
        List<Invoice> list = (status != null) ? db.invoices.findByStatus(status) : db.invoices.findAllSorted();
        sendResponse(ex, 200, JsonParser.listToJson(list.stream().map(Invoice::toJson).collect(Collectors.toList())));
    }

    private void create(HttpExchange ex) throws IOException {
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        String patientName = JsonParser.str(body, "patientName");
        Object itemsRaw = body.get("items");
        if (patientName == null || itemsRaw == null) {
            sendResponse(ex, 400, JsonParser.error("patientName and items required")); return;
        }
        double subtotal = 0;
        String itemsJson = itemsRaw.toString();
        if (itemsRaw instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?,?> m) {
                    Object qtyObj = m.get("qty");
                    Object priceObj = m.get("unitPrice");
                    double qty = (qtyObj instanceof Number n) ? n.doubleValue() : 1.0;
                    double price = (priceObj instanceof Number n) ? n.doubleValue() : 0.0;
                    subtotal += qty * price;
                }
            }
            itemsJson = JsonParser.listToJson(items.stream().map(Object::toString).collect(Collectors.toList()));
        }
        double tax   = Math.round(subtotal * 0.15 * 100) / 100.0;
        double total = Math.round((subtotal + tax) * 100) / 100.0;
        String number = "INV-" + String.format("%04d", db.invoices.maxInvoiceNumber() + 1);
        Invoice inv = new Invoice(number, patientName.trim(),
                                  JsonParser.str(body, "doctorName"),
                                  itemsJson, subtotal, tax, total);
        if (body.get("patientPhone") != null) inv.setPatientPhone(JsonParser.str(body, "patientPhone"));
        if (body.get("notes") != null) inv.setNotes(JsonParser.str(body, "notes"));
        db.invoices.save(inv.getId(), inv);
        sendResponse(ex, 201, inv.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<Invoice> opt = db.invoices.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        Invoice inv = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("status") != null) inv.setStatus(JsonParser.str(body, "status"));
        if (body.get("notes") != null) inv.setNotes(JsonParser.str(body, "notes"));
        db.invoices.save(id, inv);
        sendResponse(ex, 200, inv.toJson());
    }
}

// ════════════════════════════════════════════════════════════
// STATS CONTROLLER
// ════════════════════════════════════════════════════════════
class StatsController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String today = LocalDate.now().toString();
        long totalDoctors     = db.doctors.count();
        long availDoctors     = db.doctors.findByStatus("available").size();
        long todayAppts       = db.appointments.findByDate(today).size();
        long totalRecords     = db.records.count();
        double totalRevenue   = db.invoices.sumPaidRevenue();
        long unpaidInvoices   = db.invoices.countUnpaid();

        String json = String.format(
            "{\"totalDoctors\":%d,\"availableDoctors\":%d,\"todayAppointments\":%d," +
            "\"totalRecords\":%d,\"totalRevenue\":%.2f,\"unpaidInvoices\":%d}",
            totalDoctors, availDoctors, todayAppts, totalRecords, totalRevenue, unpaidInvoices
        );
        sendResponse(ex, 200, json);
    }
}
