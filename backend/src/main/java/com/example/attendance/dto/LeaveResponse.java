package com.example.attendance.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean needsReplacement;
    private Long replacementEmployeeId;
    private String replacementEmployeeName;
    private String status;
    private String rejectionReason;
    private String reviewedByName;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;

    public LeaveResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Boolean getNeedsReplacement() { return needsReplacement; }
    public void setNeedsReplacement(Boolean needsReplacement) { this.needsReplacement = needsReplacement; }

    public Long getReplacementEmployeeId() { return replacementEmployeeId; }
    public void setReplacementEmployeeId(Long replacementEmployeeId) { this.replacementEmployeeId = replacementEmployeeId; }

    public String getReplacementEmployeeName() { return replacementEmployeeName; }
    public void setReplacementEmployeeName(String replacementEmployeeName) { this.replacementEmployeeName = replacementEmployeeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

