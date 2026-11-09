package com.example.attendance.dto;

public class LeaveReviewRequest {
    private String rejectionReason;

    public LeaveReviewRequest() {}

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}

