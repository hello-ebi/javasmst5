package com.example.attendance.controller;

import com.example.attendance.dto.LeaveCreateRequest;
import com.example.attendance.dto.LeaveResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.service.AuthService;
import com.example.attendance.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/leaves")
public class LeaveController {

    private final LeaveService leaveService;
    private final AuthService authService;

    public LeaveController(LeaveService leaveService, AuthService authService) {
        this.leaveService = leaveService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getMyLeaves() {
        Employee employee = authService.getCurrentEmployeeEntity();
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(employee));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitLeave(@Valid @RequestBody LeaveCreateRequest request) {
        Employee employee = authService.getCurrentEmployeeEntity();
        LeaveResponse response = leaveService.createLeaveRequest(employee, request);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Pengajuan cuti/izin berhasil dikirim.");
        body.put("leave", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}

