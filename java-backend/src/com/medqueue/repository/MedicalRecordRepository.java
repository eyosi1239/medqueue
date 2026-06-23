package com.medqueue.repository;

import com.medqueue.model.MedicalRecord;
import java.util.*;
import java.util.stream.Collectors;

public class MedicalRecordRepository extends Repository<MedicalRecord> {

    public List<MedicalRecord> findByPatientName(String query) {
        String q = query.toLowerCase();
        return store.values().stream()
            .filter(r -> r.getPatientName().toLowerCase().contains(q))
            .sorted(Comparator.comparing(MedicalRecord::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    public List<MedicalRecord> findAllSorted() {
        return store.values().stream()
            .sorted(Comparator.comparing(MedicalRecord::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }
}
