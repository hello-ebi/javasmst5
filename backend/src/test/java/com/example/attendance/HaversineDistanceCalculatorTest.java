package com.example.attendance;

import com.example.attendance.util.HaversineDistanceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineDistanceCalculatorTest {

    @Test
    @DisplayName("Calculate distance between identical coordinates returns 0")
    void testZeroDistance() {
        double lat = -6.2088;
        double lon = 106.8456;
        double distance = HaversineDistanceCalculator.calculateDistanceMeters(lat, lon, lat, lon);
        assertEquals(0.0, distance, 0.01);
    }

    @Test
    @DisplayName("Calculate distance for known coordinates (Jakarta Monas to Bundaran HI approx ~1.9km)")
    void testKnownCoordinatesDistance() {
        // Monas
        double monasLat = -6.175392;
        double monasLon = 106.827153;
        // Bundaran HI
        double bundaranLat = -6.192534;
        double bundaranLon = 106.823056;

        double distance = HaversineDistanceCalculator.calculateDistanceMeters(monasLat, monasLon, bundaranLat, bundaranLon);
        // Expected distance is approximately 1950 - 2000 meters
        assertTrue(distance > 1850 && distance < 2050, "Distance should be ~1950m, got: " + distance);
    }

    @Test
    @DisplayName("Calculate small distance (10-50 meters shift)")
    void testSmallDistanceShift() {
        double baseLat = -6.208800;
        double baseLon = 106.845600;
        // approx 0.0001 deg lat is ~11.1 meters
        double shiftedLat = -6.208900;
        double shiftedLon = 106.845600;

        double distance = HaversineDistanceCalculator.calculateDistanceMeters(baseLat, baseLon, shiftedLat, shiftedLon);
        assertTrue(distance > 9 && distance < 13, "Distance should be ~11.1m, got: " + distance);
    }
}

