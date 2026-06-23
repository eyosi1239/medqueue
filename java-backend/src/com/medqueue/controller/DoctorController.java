package com.medqueue.controller;

import com.medqueue.model.Doctor;
import com.medqueue.repository.DataRegistry;
import com.medqueue.server.BaseHandler;
import com.medqueue.server.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DoctorController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String id = pathId(ex, "/api/doctors");
        boolean hasId = id != null && !id.isEmpty();

        if (!hasId && isMethod(ex, "GET"))    { getAll(ex);        return; }
        if (!hasId && isMethod(ex, "POST"))   { create(ex);        return; }
        if (hasId  && isMethod(ex, "GET"))    { getOne(ex, id);    return; }
        if (hasId  && isMethod(ex, "PATCH"))  { update(ex, id);    return; }
        if (hasId  && isMethod(ex, "DELETE")) { delete(ex, id);    return; }
        sendResponse(ex, 405, JsonParser.error("Method not allowed"));
    }

    private void getAll(HttpExchange ex) throws IOException {
        sendResponse(ex, 200, JsonParser.listToJson(
            db.doctors.findAllSorted().stream().map(Doctor::toJson).collect(Collectors.toList())));
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
        if (body.get("phone") != null)           d.setPhone(JsonParser.str(body, "phone"));
        if (body.get("email") != null)           d.setEmail(JsonParser.str(body, "email"));
        if (body.get("bio") != null)             d.setBio(JsonParser.str(body, "bio"));
        if (body.get("workingDays") != null)     d.setWorkingDays(JsonParser.str(body, "workingDays"));
        if (body.get("maxDailyPatients") != null) d.setMaxDailyPatients(JsonParser.intVal(body, "maxDailyPatients", 20));
        db.doctors.save(d.getId(), d);
        sendResponse(ex, 201, d.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<Doctor> opt = db.doctors.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        Doctor d = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("name") != null)            d.setName(JsonParser.str(body, "name"));
        if (body.get("specialty") != null)       d.setSpecialty(JsonParser.str(body, "specialty"));
        if (body.get("phone") != null)           d.setPhone(JsonParser.str(body, "phone"));
        if (body.get("email") != null)           d.setEmail(JsonParser.str(body, "email"));
        if (body.get("bio") != null)             d.setBio(JsonParser.str(body, "bio"));
        if (body.get("status") != null)          d.setStatus(JsonParser.str(body, "status"));
        if (body.get("workingDays") != null)     d.setWorkingDays(JsonParser.str(body, "workingDays"));
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
