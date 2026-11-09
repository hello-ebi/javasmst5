package com.example.attendance.service;

import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.CheckInRequest;
import com.example.attendance.dto.CheckOutRequest;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OfficeLocation;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.util.HaversineDistanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OfficeLocationService officeLocationService;

    // Use system or Asia/Jakarta local date
    private final ZoneId zoneId = ZoneId.of("Asia/Jakarta");

    public AttendanceService(AttendanceRepository attendanceRepository,
                             OfficeLocationService officeLocationService) {
        this.attendanceRepository = attendanceRepository;
        this.officeLocationService = officeLocationService;
    }

    public LocalDate getTodayDate() {
        return LocalDate.now(zoneId);
    }

    public Optional<AttendanceResponse> getTodayAttendance(Employee employee) {
        return attendanceRepository.findByEmployeeAndAttendanceDate(employee, getTodayDate())
                .map(this::mapToResponse);
    }

    public List<AttendanceResponse> getEmployeeHistory(Employee employee) {
        return attendanceRepository.findByEmployeeOrderByAttendanceDateDesc(employee)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getTodayAttendanceForAdmin() {
        return attendanceRepository.findByAttendanceDateOrderByCheckInTimeDesc(getTodayDate())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse checkIn(Employee employee, CheckInRequest request) {
        LocalDate today = getTodayDate();

        Optional<Attendance> existing = attendanceRepository.findByEmployeeAndAttendanceDate(employee, today);
        if (existing.isPresent()) {
            throw new ConflictException("Anda sudah melakukan presensi masuk untuk hari ini (" + today + ").");
        }

        OfficeLocation office = officeLocationService.getActiveOffice();
        double distance = HaversineDistanceCalculator.calculateDistanceMeters(
                request.getLatitude(), request.getLongitude(),
                office.getLatitude(), office.getLongitude()
        );

        if (distance > office.getRadiusMeters()) {
            throw new BadRequestException(String.format(
                    "Presensi ditolak: Jarak Anda adalah %.1f meter dari %s (Maksimal diperbolehkan: %d meter).",
                    distance, office.getName(), office.getRadiusMeters()
            ));
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(OffsetDateTime.now());
        attendance.setCheckInLat(request.getLatitude());
        attendance.setCheckInLng(request.getLongitude());
        attendance.setCheckInAccuracy(request.getAccuracy());
        attendance.setCheckInDistance(distance);
        attendance.setStatus("PRESENT");

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(Employee employee, CheckOutRequest request) {
        LocalDate today = getTodayDate();

        Attendance attendance = attendanceRepository.findByEmployeeAndAttendanceDate(employee, today)
                .orElseThrow(() -> new BadRequestException("Anda belum melakukan presensi masuk hari ini. Presensi pulang tidak diizinkan."));

        if (attendance.getCheckOutTime() != null) {
            throw new ConflictException("Anda sudah melakukan presensi pulang untuk hari ini.");
        }

        OfficeLocation office = officeLocationService.getActiveOffice();
        double distance = HaversineDistanceCalculator.calculateDistanceMeters(
                request.getLatitude(), request.getLongitude(),
                office.getLatitude(), office.getLongitude()
        );

        if (distance > office.getRadiusMeters()) {
            throw new BadRequestException(String.format(
                    "Presensi pulang ditolak: Jarak Anda adalah %.1f meter dari %s (Maksimal diperbolehkan: %d meter).",
                    distance, office.getName(), office.getRadiusMeters()
            ));
        }

        attendance.setCheckOutTime(OffsetDateTime.now());
        attendance.setCheckOutLat(request.getLatitude());
        attendance.setCheckOutLng(request.getLongitude());
        attendance.setCheckOutAccuracy(request.getAccuracy());
        attendance.setCheckOutDistance(distance);

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public AttendanceResponse mapToResponse(Attendance att) {
        AttendanceResponse res = new AttendanceResponse();
        res.setId(att.getId());
        res.setEmployeeId(att.getEmployee().getId());
        res.setEmployeeName(att.getEmployee().getFullName());
        res.setEmployeeCode(att.getEmployee().getEmployeeId());
        res.setAttendanceDate(att.getAttendanceDate());
        res.setCheckInTime(att.getCheckInTime());
        res.setCheckInDistance(att.getCheckInDistance());
        res.setCheckOutTime(att.getCheckOutTime());
        res.setCheckOutDistance(att.getCheckOutDistance());
        res.setStatus(att.getStatus());
        return res;
    }
}

