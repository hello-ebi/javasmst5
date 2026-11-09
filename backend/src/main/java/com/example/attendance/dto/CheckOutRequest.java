package com.example.attendance.dto;

import jakarta.validation.constraints.NotNull;

public class CheckOutRequest {

    @NotNull(message = "Latitude wajib diisi.")
    private Double latitude;

    @NotNull(message = "Longitude wajib diisi.")
    private Double longitude;

    private Double accuracy;

    public CheckOutRequest() {}

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}

