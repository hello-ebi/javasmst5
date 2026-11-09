package com.example.attendance.service;

import com.example.attendance.dto.LeaveCreateRequest;
import com.example.attendance.dto.LeaveResponse;
import com.example.attendance.dto.LeaveReviewRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<LeaveResponse> getEmployeeLeaves(Employee employee) {
        return leaveRequestRepository.findByEmployeeOrderByCreatedAtDesc(employee)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveResponse> getAllLeavesForAdmin(String status) {
        List<LeaveRequest> list;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        } else {
            list = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponse createLeaveRequest(Employee employee, LeaveCreateRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Tanggal selesai tidak boleh sebelum tanggal mulai.");
        }

        Employee replacement = null;
        if (Boolean.TRUE.equals(request.getNeedsReplacement())) {
            if (request.getReplacementEmployeeId() == null) {
                throw new BadRequestException("Karyawan pengganti wajib dipilih jika pengganti diperlukan.");
            }
            if (request.getReplacementEmployeeId().equals(employee.getId())) {
                throw new BadRequestException("Anda tidak dapat memilih diri sendiri sebagai karyawan pengganti.");
            }
            replacement = employeeRepository.findById(request.getReplacementEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Karyawan pengganti yang dipilih tidak ditemukan."));
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(request.getLeaveType().toUpperCase());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason().trim());
        leave.setNeedsReplacement(Boolean.TRUE.equals(request.getNeedsReplacement()));
        leave.setReplacementEmployee(replacement);
        leave.setStatus("PENDING");

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse approveLeave(Long id, User reviewer) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan cuti/izin tidak ditemukan."));

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            throw new BadRequestException("Pengajuan ini sudah berstatus " + leave.getStatus() + ".");
        }

        leave.setStatus("APPROVED");
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(OffsetDateTime.now());
        leave.setRejectionReason(null);

        return mapToResponse(leaveRequestRepository.save(leave));
    }

    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveReviewRequest request, User reviewer) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan cuti/izin tidak ditemukan."));

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            throw new BadRequestException("Pengajuan ini sudah berstatus " + leave.getStatus() + ".");
        }

        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BadRequestException("Alasan penolakan cuti wajib diisi.");
        }

        leave.setStatus("REJECTED");
        leave.setRejectionReason(request.getRejectionReason().trim());
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(OffsetDateTime.now());

        return mapToResponse(leaveRequestRepository.save(leave));
    }

    public LeaveResponse mapToResponse(LeaveRequest l) {
        LeaveResponse res = new LeaveResponse();
        res.setId(l.getId());
        res.setEmployeeId(l.getEmployee().getId());
        res.setEmployeeName(l.getEmployee().getFullName());
        res.setEmployeeCode(l.getEmployee().getEmployeeId());
        res.setLeaveType(l.getLeaveType());
        res.setStartDate(l.getStartDate());
        res.setEndDate(l.getEndDate());
        res.setReason(l.getReason());
        res.setNeedsReplacement(l.getNeedsReplacement());

        if (l.getReplacementEmployee() != null) {
            res.setReplacementEmployeeId(l.getReplacementEmployee().getId());
            res.setReplacementEmployeeName(l.getReplacementEmployee().getFullName());
        }

        res.setStatus(l.getStatus());
        res.setRejectionReason(l.getRejectionReason());

        if (l.getReviewedBy() != null) {
            res.setReviewedByName(l.getReviewedBy().getEmail());
        }

        res.setReviewedAt(l.getReviewedAt());
        res.setCreatedAt(l.getCreatedAt());
        return res;
    }
}

