package com.medqueue.controller;

import com.medqueue.model.Appointment;
import com.medqueue.model.Doctor;
import com.medqueue.repository.DataRegistry;
import com.medqueue.server.BaseHandler;
import com.medqueue.server.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AppointmentController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String id   = pathId(ex, "/api/appointments");
        boolean hasId = id != null && !id.isEmpty();

        if (path.endsWith("/slots"))            { getSlots(ex);      return; }
        if (!hasId && isMethod(ex, "GET"))      { getAll(ex);        return; }
        if (!hasId && isMethod(ex, "POST"))     { create(ex);        return; }
        if (hasId  && isMethod(ex, "PATCH"))    { update(ex, id);    return; }
        if (hasId  && isMethod(ex, "DELETE"))   { cancel(ex, id);    return; }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String date = params.get("date"), doctorId = params.get("doctorId"), status = params.get("status");
        List<Appointment> list;
        if (date != null && doctorId != null) list = db.appointments.findByDoctorAndDate(doctorId, date);
        else if (date != null)                list = db.appointments.findByDate(date);
        else                                  list = db.appointments.findAll(status);
        sendResponse(ex, 200, JsonParser.listToJson(list.stream().map(Appointment::toJson).collect(Collectors.toList())));
    }

    private void getSlots(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String doctorId = params.get("doctorId"), date = params.get("date");
        if (doctorId == null || date == null) { sendResponse(ex, 400, JsonParser.error("doctorId and date required")); return; }
        List<String> slots = new ArrayList<>();
        for (int h = 8; h < 18; h++) {
            for (String m : new String[]{"00","30"}) {
                String t = String.format("%02d:%s", h, m);
                slots.add(String.format("{\"time\":\"%s\",\"available\":%b}", t, !db.appointments.isSlotTaken(doctorId, date, t)));
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
        Appointment a = new Appointment(patientName.trim(), doctorId, doc.get().getName(), doc.get().getSpecialty(), date, time);
        if (body.get("patientPhone") != null) a.setPatientPhone(JsonParser.str(body, "patientPhone"));
        if (body.get("reason")      != null) a.setReason(JsonParser.str(body, "reason"));
        if (body.get("type")        != null) a.setType(JsonParser.str(body, "type"));
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
        db.appointments.save(id, opt.get());
        sendResponse(ex, 200, JsonParser.ok("Appointment cancelled"));
    }
}
