-- SIMPONI Initial Seed Data
-- Created by Deddy Harisman

-- Initial Office Location: Headquarters (Jakarta Central)
INSERT INTO office_locations (name, latitude, longitude, radius_meters, is_active, created_at, updated_at)
VALUES ('Kantor Pusat (Headquarters)', -6.2088, 106.8456, 100, TRUE, NOW(), NOW())
ON CONFLICT DO NOTHING;

