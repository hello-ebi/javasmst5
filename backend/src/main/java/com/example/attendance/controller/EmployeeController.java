package com.example.attendance.controller;

import com.example.attendance.dto.ApiResponse;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.ChangeEmailRequest;
import com.example.attendance.dto.ChangePasswordRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LeaveResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.AuthService;
import com.example.attendance.service.EmployeeService;
import com.example.attendance.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;

    public EmployeeController(AuthService authService,
                              EmployeeService employeeService,
                              AttendanceService attendanceService,
                              LeaveService leaveService) {
        this.authService = authService;
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Employee employee = authService.getCurrentEmployeeEntity();
        Optional<AttendanceResponse> todayAttendance = attendanceService.getTodayAttendance(employee);
        List<LeaveResponse> recentLeaves = leaveService.getEmployeeLeaves(employee);

        Map<String, Object> data = new HashMap<>();
        data.put("employee", employeeService.mapToResponse(employee));
        data.put("todayAttendance", todayAttendance.orElse(null));
        data.put("recentLeaves", recentLeaves.stream().limit(5).toList());

        return ResponseEntity.ok(data);
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeResponse> getProfile() {
        Employee employee = authService.getCurrentEmployeeEntity();
        return ResponseEntity.ok(employeeService.mapToResponse(employee));
    }

    @PutMapping("/profile/email")
    public ResponseEntity<ApiResponse> updateEmail(@Valid @RequestBody ChangeEmailRequest request) {
        return ResponseEntity.ok(authService.changeEmail(request));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<ApiResponse> updatePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @GetMapping("/colleagues")
    public ResponseEntity<List<EmployeeResponse>> getColleagues() {
        Employee employee = authService.getCurrentEmployeeEntity();
        return ResponseEntity.ok(employeeService.getActiveColleagues(employee.getId()));
    }
}

