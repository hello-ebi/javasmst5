package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.CheckInRequest;
import com.example.attendance.dto.CheckOutRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OfficeLocation;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.AuthService;
import com.example.attendance.service.OfficeLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;
    private final OfficeLocationService officeLocationService;

    public AttendanceController(AttendanceService attendanceService,
                                AuthService authService,
                                OfficeLocationService officeLocationService) {
        this.attendanceService = attendanceService;
        this.authService = authService;
        this.officeLocationService = officeLocationService;
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayStatus() {
        Employee employee = authService.getCurrentEmployeeEntity();
        Optional<AttendanceResponse> attendanceOpt = attendanceService.getTodayAttendance(employee);
        OfficeLocation office = officeLocationService.getActiveOffice();

        Map<String, Object> response = new HashMap<>();
        response.put("todayDate", attendanceService.getTodayDate());
        response.put("hasCheckedIn", attendanceOpt.isPresent());
        response.put("hasCheckedOut", attendanceOpt.isPresent() && attendanceOpt.get().getCheckOutTime() != null);
        response.put("attendance", attendanceOpt.orElse(null));
        response.put("officeName", office.getName());
        response.put("officeLatitude", office.getLatitude());
        response.put("officeLongitude", office.getLongitude());
        response.put("officeRadiusMeters", office.getRadiusMeters());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceResponse>> getHistory() {
        Employee employee = authService.getCurrentEmployeeEntity();
        return ResponseEntity.ok(attendanceService.getEmployeeHistory(employee));
    }

    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@Valid @RequestBody CheckInRequest request) {
        Employee employee = authService.getCurrentEmployeeEntity();
        AttendanceResponse attendance = attendanceService.checkIn(employee, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Presensi masuk berhasil dicatat.");
        response.put("distanceMeters", attendance.getCheckInDistance());
        response.put("attendance", attendance);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        Employee employee = authService.getCurrentEmployeeEntity();
        AttendanceResponse attendance = attendanceService.checkOut(employee, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Presensi pulang berhasil dicatat.");
        response.put("distanceMeters", attendance.getCheckOutDistance());
        response.put("attendance", attendance);

        return ResponseEntity.ok(response);
    }
}

