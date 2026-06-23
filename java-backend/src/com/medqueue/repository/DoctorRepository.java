package com.medqueue.repository;

import com.medqueue.model.Doctor;
import java.util.*;
import java.util.stream.Collectors;

public class DoctorRepository extends Repository<Doctor> {

    public List<Doctor> findByStatus(String status) {
        return store.values().stream()
            .filter(d -> status.equals(d.getStatus()))
            .sorted(Comparator.comparing(Doctor::getName))
            .collect(Collectors.toList());
    }

    public List<Doctor> findAllSorted() {
        return store.values().stream()
            .sorted(Comparator.comparing(Doctor::getName))
            .collect(Collectors.toList());
    }
}
