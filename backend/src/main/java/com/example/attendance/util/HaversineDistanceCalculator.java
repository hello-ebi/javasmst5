package com.example.attendance.util;

/**
 * Spherical Haversine Distance Calculator.
 * Computes great-circle distance between two GPS coordinates in meters.
 * SIMPONI — Created by Deddy Harisman
 */
public final class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private HaversineDistanceCalculator() {}

    /**
     * Calculates distance in meters between two latitude/longitude points.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in meters rounded to 2 decimal places
     */
    public static double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(phi1) * Math.cos(phi2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_METERS * c;
        return Math.round(distance * 100.0) / 100.0;
    }
}

