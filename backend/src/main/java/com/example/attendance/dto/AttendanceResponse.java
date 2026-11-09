package com.example.attendance.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private OffsetDateTime checkInTime;
    private Double checkInDistance;
    private OffsetDateTime checkOutTime;
    private Double checkOutDistance;
    private String status;

    public AttendanceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public OffsetDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(OffsetDateTime checkInTime) { this.checkInTime = checkInTime; }

    public Double getCheckInDistance() { return checkInDistance; }
    public void setCheckInDistance(Double checkInDistance) { this.checkInDistance = checkInDistance; }

    public OffsetDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(OffsetDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Double getCheckOutDistance() { return checkOutDistance; }
    public void setCheckOutDistance(Double checkOutDistance) { this.checkOutDistance = checkOutDistance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

