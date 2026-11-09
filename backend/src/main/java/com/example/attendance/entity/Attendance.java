package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(name = "uq_attendance_emp_date", columnNames = {"employee_id", "attendance_date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time", nullable = false)
    private OffsetDateTime checkInTime;

    @Column(name = "check_in_lat", nullable = false)
    private Double checkInLat;

    @Column(name = "check_in_lng", nullable = false)
    private Double checkInLng;

    @Column(name = "check_in_accuracy")
    private Double checkInAccuracy;

    @Column(name = "check_in_distance", nullable = false)
    private Double checkInDistance;

    @Column(name = "check_out_time")
    private OffsetDateTime checkOutTime;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "check_out_accuracy")
    private Double checkOutAccuracy;

    @Column(name = "check_out_distance")
    private Double checkOutDistance;

    @Column(nullable = false, length = 50)
    private String status = "PRESENT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Attendance() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public OffsetDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(OffsetDateTime checkInTime) { this.checkInTime = checkInTime; }

    public Double getCheckInLat() { return checkInLat; }
    public void setCheckInLat(Double checkInLat) { this.checkInLat = checkInLat; }

    public Double getCheckInLng() { return checkInLng; }
    public void setCheckInLng(Double checkInLng) { this.checkInLng = checkInLng; }

    public Double getCheckInAccuracy() { return checkInAccuracy; }
    public void setCheckInAccuracy(Double checkInAccuracy) { this.checkInAccuracy = checkInAccuracy; }

    public Double getCheckInDistance() { return checkInDistance; }
    public void setCheckInDistance(Double checkInDistance) { this.checkInDistance = checkInDistance; }

    public OffsetDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(OffsetDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Double getCheckOutLat() { return checkOutLat; }
    public void setCheckOutLat(Double checkOutLat) { this.checkOutLat = checkOutLat; }

    public Double getCheckOutLng() { return checkOutLng; }
    public void setCheckOutLng(Double checkOutLng) { this.checkOutLng = checkOutLng; }

    public Double getCheckOutAccuracy() { return checkOutAccuracy; }
    public void setCheckOutAccuracy(Double checkOutAccuracy) { this.checkOutAccuracy = checkOutAccuracy; }

    public Double getCheckOutDistance() { return checkOutDistance; }
    public void setCheckOutDistance(Double checkOutDistance) { this.checkOutDistance = checkOutDistance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

