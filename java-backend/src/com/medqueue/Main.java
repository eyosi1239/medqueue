package com.medqueue;

import com.medqueue.controller.*;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * MedQueue Java Backend
 * Pure Java HTTP server — no Maven, no Spring Boot, no external libraries.
 * Uses com.sun.net.httpserver (built into JDK 8+)
 *
 * Run: javac -d out src\com\medqueue\**\*.java  (compile)
 *      java -cp out com.medqueue.Main            (run)
 */
public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register all routes
        server.createContext("/api/doctors",      new DoctorController());
        server.createContext("/api/appointments", new AppointmentController());
        server.createContext("/api/records",      new MedicalRecordController());
        server.createContext("/api/invoices",     new InvoiceController());
        server.createContext("/api/java-stats",   new StatsController());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   MedQueue Java Backend — Port " + port + "      ║");
        System.out.println("║   Pure OOP Java — No Maven required      ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  Doctors:      /api/doctors              ║");
        System.out.println("║  Appointments: /api/appointments         ║");
        System.out.println("║  Records:      /api/records              ║");
        System.out.println("║  Invoices:     /api/invoices             ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
