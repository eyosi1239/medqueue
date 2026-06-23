package com.medqueue.controller;

import com.medqueue.repository.DataRegistry;
import com.medqueue.server.BaseHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;

public class StatsController extends BaseHandler {
    private final DataRegistry db = DataRegistry.getInstance();

    @Override
    protected void route(HttpExchange ex) throws IOException {
        String today          = LocalDate.now().toString();
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
