package com.example.attendance;

import com.example.attendance.dto.LeaveCreateRequest;
import com.example.attendance.dto.LeaveResponse;
import com.example.attendance.dto.LeaveReviewRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import com.example.attendance.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private Employee colleague;
    private User admin;

    @BeforeEach
    void setUp() {
        User u1 = new User("emp1@simponi.local", "hash1", "EMPLOYEE");
        u1.setId(1L);
        employee = new Employee(u1, "EMP-001", "Alice", "08111", "Finance", "Officer");
        employee.setId(10L);

        User u2 = new User("emp2@simponi.local", "hash2", "EMPLOYEE");
        u2.setId(2L);
        colleague = new Employee(u2, "EMP-002", "Bob", "08222", "Finance", "Officer");
        colleague.setId(20L);

        admin = new User("admin@simponi.local", "hash3", "ADMIN");
        admin.setId(99L);
    }

    @Test
    @DisplayName("Create leave request with valid replacement succeeds")
    void testCreateLeaveRequestSuccess() {
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(colleague));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> {
            LeaveRequest lr = i.getArgument(0);
            lr.setId(101L);
            return lr;
        });

        LeaveCreateRequest req = new LeaveCreateRequest();
        req.setLeaveType("ANNUAL");
        req.setStartDate(LocalDate.now().plusDays(2));
        req.setEndDate(LocalDate.now().plusDays(4));
        req.setReason("Liburan keluarga");
        req.setNeedsReplacement(true);
        req.setReplacementEmployeeId(20L);

        LeaveResponse res = leaveService.createLeaveRequest(employee, req);
        assertNotNull(res);
        assertEquals("PENDING", res.getStatus());
        assertEquals("Bob", res.getReplacementEmployeeName());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Create leave request with end date earlier than start date throws BadRequestException")
    void testInvalidDatesThrowsException() {
        LeaveCreateRequest req = new LeaveCreateRequest();
        req.setLeaveType("SICK");
        req.setStartDate(LocalDate.now().plusDays(5));
        req.setEndDate(LocalDate.now().plusDays(2)); // earlier!
        req.setReason("Sakit demam");

        assertThrows(BadRequestException.class, () -> {
            leaveService.createLeaveRequest(employee, req);
        });

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Approve leave request sets status to APPROVED and records reviewer")
    void testApproveLeave() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(50L);
        lr.setEmployee(employee);
        lr.setStatus("PENDING");
        lr.setLeaveType("ANNUAL");
        lr.setStartDate(LocalDate.now());
        lr.setEndDate(LocalDate.now().plusDays(1));

        when(leaveRequestRepository.findById(50L)).thenReturn(Optional.of(lr));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveResponse res = leaveService.approveLeave(50L, admin);
        assertEquals("APPROVED", res.getStatus());
        assertEquals(admin.getEmail(), res.getReviewedByName());
    }

    @Test
    @DisplayName("Reject leave request without reason throws BadRequestException")
    void testRejectLeaveWithoutReasonThrows() {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(50L);
        lr.setStatus("PENDING");
        when(leaveRequestRepository.findById(50L)).thenReturn(Optional.of(lr));

        LeaveReviewRequest req = new LeaveReviewRequest();
        req.setRejectionReason("");

        assertThrows(BadRequestException.class, () -> {
            leaveService.rejectLeave(50L, req, admin);
        });
    }
}

