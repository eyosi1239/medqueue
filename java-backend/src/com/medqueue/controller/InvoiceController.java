package com.medqueue.controller;

import com.medqueue.model.Invoice;
import com.medqueue.repository.DataRegistry;
import com.medqueue.server.BaseHandler;
import com.medqueue.server.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InvoiceController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String id = pathId(ex, "/api/invoices");
        boolean hasId = id != null && !id.isEmpty();

        if (!hasId && isMethod(ex, "GET"))   { getAll(ex);      return; }
        if (!hasId && isMethod(ex, "POST"))  { create(ex);      return; }
        if (hasId  && isMethod(ex, "PATCH")) { update(ex, id);  return; }
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
        Object itemsRaw    = body.get("items");
        if (patientName == null || itemsRaw == null) {
            sendResponse(ex, 400, JsonParser.error("patientName and items required")); return;
        }
        double subtotal = 0;
        StringBuilder itemsJson = new StringBuilder("[");
        if (itemsRaw instanceof List<?> items) {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item instanceof Map<?,?> m) {
                    Object qtyObj   = m.get("qty");
                    Object priceObj = m.get("unitPrice");
                    Object descObj  = m.get("desc");
                    double qty   = (qtyObj   instanceof Number n) ? n.doubleValue() : 1.0;
                    double price = (priceObj instanceof Number n) ? n.doubleValue() : 0.0;
                    String desc  = descObj != null ? descObj.toString() : "";
                    subtotal += qty * price;
                    if (i > 0) itemsJson.append(",");
                    itemsJson.append(String.format("{\"desc\":\"%s\",\"qty\":%.0f,\"unitPrice\":%.2f}", desc, qty, price));
                }
            }
        }
        itemsJson.append("]");
        double tax   = Math.round(subtotal * 0.15 * 100) / 100.0;
        double total = Math.round((subtotal + tax) * 100) / 100.0;
        String number = "INV-" + String.format("%04d", db.invoices.maxInvoiceNumber() + 1);
        Invoice inv = new Invoice(number, patientName.trim(), JsonParser.str(body, "doctorName"),
                                  itemsJson.toString(), subtotal, tax, total);
        if (body.get("patientPhone") != null) inv.setPatientPhone(JsonParser.str(body, "patientPhone"));
        if (body.get("notes")        != null) inv.setNotes(JsonParser.str(body, "notes"));
        db.invoices.save(inv.getId(), inv);
        sendResponse(ex, 201, inv.toJson());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        Optional<Invoice> opt = db.invoices.findById(id);
        if (opt.isEmpty()) { sendResponse(ex, 404, JsonParser.error("Not found")); return; }
        Invoice inv = opt.get();
        Map<String, Object> body = JsonParser.parseObject(readBody(ex));
        if (body.get("status") != null) inv.setStatus(JsonParser.str(body, "status"));
        if (body.get("notes")  != null) inv.setNotes(JsonParser.str(body, "notes"));
        db.invoices.save(id, inv);
        sendResponse(ex, 200, inv.toJson());
    }
}
