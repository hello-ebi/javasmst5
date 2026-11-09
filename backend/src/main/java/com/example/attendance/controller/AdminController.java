package com.example.attendance.controller;

import com.example.attendance.dto.*;
import com.example.attendance.entity.User;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.AuthService;
import com.example.attendance.service.EmployeeService;
import com.example.attendance.service.LeaveService;
import com.example.attendance.service.OfficeLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final OfficeLocationService officeLocationService;
    private final AttendanceService attendanceService;
    private final AuthService authService;

    public AdminController(EmployeeService employeeService,
                           LeaveService leaveService,
                           OfficeLocationService officeLocationService,
                           AttendanceService attendanceService,
                           AuthService authService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.officeLocationService = officeLocationService;
        this.attendanceService = attendanceService;
        this.authService = authService;
    }

    // --- Employee Management ---

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(new ApiResponse(true, "Karyawan berhasil dinonaktifkan."));
    }

    // --- Leave Approval Management ---

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveResponse>> getLeaves(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveService.getAllLeavesForAdmin(status));
    }

    @PostMapping("/leaves/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable Long id) {
        User reviewer = authService.getCurrentUserEntity();
        return ResponseEntity.ok(leaveService.approveLeave(id, reviewer));
    }

    @PostMapping("/leaves/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable Long id,
                                                     @RequestBody LeaveReviewRequest request) {
        User reviewer = authService.getCurrentUserEntity();
        return ResponseEntity.ok(leaveService.rejectLeave(id, request, reviewer));
    }

    // --- Office Location Settings ---

    @GetMapping("/locations")
    public ResponseEntity<List<OfficeLocationResponse>> getLocations() {
        return ResponseEntity.ok(officeLocationService.getAllLocations());
    }

    @PostMapping("/locations")
    public ResponseEntity<OfficeLocationResponse> createLocation(@Valid @RequestBody OfficeLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officeLocationService.createLocation(request));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<OfficeLocationResponse> updateLocation(@PathVariable Long id,
                                                                 @Valid @RequestBody OfficeLocationRequest request) {
        return ResponseEntity.ok(officeLocationService.updateLocation(id, request));
    }

    @PutMapping("/locations/{id}/activate")
    public ResponseEntity<OfficeLocationResponse> activateLocation(@PathVariable Long id) {
        return ResponseEntity.ok(officeLocationService.activateLocation(id));
    }

    // --- Attendance Overview for Admin ---

    @GetMapping("/attendance/today")
    public ResponseEntity<List<AttendanceResponse>> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendanceForAdmin());
    }
}

