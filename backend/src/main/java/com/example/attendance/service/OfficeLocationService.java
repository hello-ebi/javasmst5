package com.example.attendance.service;

import com.example.attendance.dto.OfficeLocationRequest;
import com.example.attendance.dto.OfficeLocationResponse;
import com.example.attendance.entity.OfficeLocation;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.OfficeLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfficeLocationService {

    private final OfficeLocationRepository officeLocationRepository;

    public OfficeLocationService(OfficeLocationRepository officeLocationRepository) {
        this.officeLocationRepository = officeLocationRepository;
    }

    public List<OfficeLocationResponse> getAllLocations() {
        return officeLocationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OfficeLocation getActiveOffice() {
        return officeLocationRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                .orElseThrow(() -> new BadRequestException("Lokasi kantor aktif belum dikonfigurasi oleh Admin."));
    }

    public OfficeLocationResponse getActiveOfficeResponse() {
        return mapToResponse(getActiveOffice());
    }

    @Transactional
    public OfficeLocationResponse createLocation(OfficeLocationRequest request) {
        if (Boolean.TRUE.equals(request.getIsActive())) {
            deactivateAll();
        }
        OfficeLocation loc = new OfficeLocation(
                request.getName().trim(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters(),
                request.getIsActive() != null ? request.getIsActive() : true
        );
        return mapToResponse(officeLocationRepository.save(loc));
    }

    @Transactional
    public OfficeLocationResponse updateLocation(Long id, OfficeLocationRequest request) {
        OfficeLocation loc = officeLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lokasi kantor tidak ditemukan."));

        if (Boolean.TRUE.equals(request.getIsActive())) {
            deactivateAll();
        }

        loc.setName(request.getName().trim());
        loc.setLatitude(request.getLatitude());
        loc.setLongitude(request.getLongitude());
        loc.setRadiusMeters(request.getRadiusMeters());
        if (request.getIsActive() != null) {
            loc.setIsActive(request.getIsActive());
        }

        return mapToResponse(officeLocationRepository.save(loc));
    }

    @Transactional
    public OfficeLocationResponse activateLocation(Long id) {
        OfficeLocation loc = officeLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lokasi kantor tidak ditemukan."));

        deactivateAll();
        loc.setIsActive(true);
        return mapToResponse(officeLocationRepository.save(loc));
    }

    private void deactivateAll() {
        List<OfficeLocation> all = officeLocationRepository.findAll();
        for (OfficeLocation loc : all) {
            loc.setIsActive(false);
        }
        officeLocationRepository.saveAll(all);
    }

    public OfficeLocationResponse mapToResponse(OfficeLocation loc) {
        OfficeLocationResponse res = new OfficeLocationResponse();
        res.setId(loc.getId());
        res.setName(loc.getName());
        res.setLatitude(loc.getLatitude());
        res.setLongitude(loc.getLongitude());
        res.setRadiusMeters(loc.getRadiusMeters());
        res.setIsActive(loc.getIsActive());
        res.setCreatedAt(loc.getCreatedAt());
        return res;
    }
}

