package com.medqueue.repository;

import com.medqueue.model.Appointment;
import java.util.*;
import java.util.stream.Collectors;

public class AppointmentRepository extends Repository<Appointment> {

    public List<Appointment> findByDate(String date) {
        return store.values().stream()
            .filter(a -> date.equals(a.getDate()))
            .sorted(Comparator.comparing(Appointment::getTime))
            .collect(Collectors.toList());
    }

    public List<Appointment> findByDoctorAndDate(String doctorId, String date) {
        return store.values().stream()
            .filter(a -> doctorId.equals(a.getDoctorId()) && date.equals(a.getDate()))
            .sorted(Comparator.comparing(Appointment::getTime))
            .collect(Collectors.toList());
    }

    public boolean isSlotTaken(String doctorId, String date, String time) {
        return store.values().stream().anyMatch(a ->
            doctorId.equals(a.getDoctorId()) &&
            date.equals(a.getDate()) &&
            time.equals(a.getTime()) &&
            !"cancelled".equals(a.getStatus())
        );
    }

    public List<Appointment> findAll(String statusFilter) {
        return store.values().stream()
            .filter(a -> statusFilter == null || statusFilter.equals(a.getStatus()))
            .sorted(Comparator.comparing(a -> a.getDate() + a.getTime()))
            .collect(Collectors.toList());
    }
}
