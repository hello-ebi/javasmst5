package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveCreateRequest {

    @NotBlank(message = "Jenis cuti/izin wajib dipilih.")
    private String leaveType; // 'ANNUAL', 'SICK', 'EMERGENCY', 'UNPAID'

    @NotNull(message = "Tanggal mulai wajib diisi.")
    private LocalDate startDate;

    @NotNull(message = "Tanggal selesai wajib diisi.")
    private LocalDate endDate;

    @NotBlank(message = "Alasan cuti wajib diisi.")
    private String reason;

    private Boolean needsReplacement = false;
    private Long replacementEmployeeId;

    public LeaveCreateRequest() {}

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
}

