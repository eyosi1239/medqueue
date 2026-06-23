package com.medqueue.repository;

import com.medqueue.model.Invoice;
import java.util.*;
import java.util.stream.Collectors;

public class InvoiceRepository extends Repository<Invoice> {

    public List<Invoice> findByStatus(String status) {
        return store.values().stream()
            .filter(i -> status.equals(i.getStatus()))
            .sorted(Comparator.comparing(Invoice::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    public List<Invoice> findAllSorted() {
        return store.values().stream()
            .sorted(Comparator.comparing(Invoice::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    public double sumPaidRevenue() {
        return store.values().stream()
            .filter(i -> "paid".equals(i.getStatus()))
            .mapToDouble(Invoice::getTotal).sum();
    }

    public long countUnpaid() {
        return store.values().stream()
            .filter(i -> "unpaid".equals(i.getStatus())).count();
    }

    public int maxInvoiceNumber() {
        return store.values().stream()
            .mapToInt(i -> {
                try { return Integer.parseInt(i.getNumber().replace("INV-", "")); }
                catch (Exception e) { return 0; }
            }).max().orElse(0);
    }
}
