package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class OfficeLocationRequest {

    @NotBlank(message = "Nama lokasi kantor wajib diisi.")
    private String name;

    @NotNull(message = "Latitude wajib diisi.")
    private Double latitude;

    @NotNull(message = "Longitude wajib diisi.")
    private Double longitude;

    @NotNull(message = "Radius presensi (meter) wajib diisi.")
    private Integer radiusMeters;

    private Boolean isActive = true;

    public OfficeLocationRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

