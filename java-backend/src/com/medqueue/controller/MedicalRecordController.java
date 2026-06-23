package com.medqueue.controller;

import com.medqueue.model.Doctor;
import com.medqueue.model.MedicalRecord;
import com.medqueue.repository.DataRegistry;
import com.medqueue.server.BaseHandler;
import com.medqueue.server.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MedicalRecordController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String id = pathId(ex, "/api/records");
        boolean hasId = id != null && !id.isEmpty();

        if (!hasId && isMethod(ex, "GET"))   { getAll(ex);       return; }
        if (!hasId && isMethod(ex, "POST"))  { create(ex);       return; }
        if (hasId  && isMethod(ex, "PATCH")) { update(ex, id);   return; }
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
        if (body.get("notes") != null)       r.setNotes(JsonParser.str(body, "notes"));
        if (body.get("followUpDate") != null) r.setFollowUpDate(JsonParser.str(body, "followUpDate"));
        db.records.save(id, r);
        sendResponse(ex, 200, r.toJson());
    }
}
