package com.example.attendance;

import com.example.attendance.dto.CheckInRequest;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OfficeLocation;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.OfficeLocationService;
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
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private OfficeLocationService officeLocationService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private OfficeLocation office;

    @BeforeEach
    void setUp() {
        User user = new User("employee@simponi.local", "hashedpass", "EMPLOYEE");
        user.setId(1L);

        employee = new Employee(user, "EMP-001", "Jane Doe", "08123456", "IT", "Engineer");
        employee.setId(10L);

        office = new OfficeLocation("HQ", -6.2088, 106.8456, 100, true);
        office.setId(5L);
    }

    @Test
    @DisplayName("Check-in inside office radius succeeds")
    void testCheckInInsideRadiusSuccess() {
        when(attendanceRepository.findByEmployeeAndAttendanceDate(eq(employee), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(officeLocationService.getActiveOffice()).thenReturn(office);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        CheckInRequest req = new CheckInRequest();
        req.setLatitude(-6.2088); // exact office coordinate
        req.setLongitude(106.8456);
        req.setAccuracy(10.0);

        AttendanceResponse res = attendanceService.checkIn(employee, req);
        assertNotNull(res);
        assertEquals(0.0, res.getCheckInDistance());
        assertEquals("PRESENT", res.getStatus());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Check-in outside office radius is rejected with BadRequestException")
    void testCheckInOutsideRadiusRejected() {
        when(attendanceRepository.findByEmployeeAndAttendanceDate(eq(employee), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(officeLocationService.getActiveOffice()).thenReturn(office);

        CheckInRequest req = new CheckInRequest();
        // Shift latitude by ~1.1km away
        req.setLatitude(-6.2188);
        req.setLongitude(106.8456);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            attendanceService.checkIn(employee, req);
        });

        assertTrue(ex.getMessage().contains("Presensi ditolak: Jarak Anda adalah"));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Check-in duplicate on same day is rejected with ConflictException")
    void testCheckInDuplicateRejected() {
        Attendance existing = new Attendance();
        existing.setId(99L);
        when(attendanceRepository.findByEmployeeAndAttendanceDate(eq(employee), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));

        CheckInRequest req = new CheckInRequest();
        req.setLatitude(-6.2088);
        req.setLongitude(106.8456);

        assertThrows(ConflictException.class, () -> {
            attendanceService.checkIn(employee, req);
        });

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }
}

