# SIMPONI — Employee Attendance System
**Created by Deddy Harisman**

---

## 1. Gambaran Umum (Overview)
**SIMPONI** adalah Aplikasi Web Manajemen Presensi dan Cuti Karyawan berbasis *mobile-first* yang dikembangkan sebagai Proof of Concept (POC). Fitur utama aplikasi meliputi:
- **Kontrol Akses Berbasis Peran (RBAC)**: Alur kerja terpisah untuk peran `ADMIN` dan `EMPLOYEE`.
- **Presensi Berbasis Validasi GPS**: Pengambilan koordinat browser secara real-time yang divalidasi langsung di backend menggunakan rumus jarak geofencing Haversine.
- **Manajemen Pengajuan Cuti & Izin**: Formulir permohonan dengan dukungan penunjukan rekan pengganti tugas (*peer replacement*) serta persetujuan langsung oleh administrator.
- **Sistem Desain Antarmuka Minimalis**: Tema warna biru dan putih solid (`#2563EB`, `#1E3A8A`, `#FFFFFF`) tanpa gradien, tanpa framework UI berlebih, dan bebas dari *AI slop*.
- **Portabilitas Tinggi**: Database berjalan mulus di dalam container PostgreSQL 16 via Docker Compose dengan *named volume* persisten.

---

## 2. Tumpukan Teknologi (Technology Stack)
- **Backend**: Java 17+, Spring Boot 3, Spring Security 6, Spring Data JPA, Flyway, Maven.
- **Frontend**: Native HTML5, Native CSS3, Vanilla JavaScript (disajikan langsung melalui *static resources* Spring Boot).
- **Database**: PostgreSQL 16 berjalan di container Docker Compose.
- **Autentikasi**: Autentikasi sesi aman berbasis server (`JSESSIONID`, HttpOnly, SameSite=Lax, serta proteksi CSRF).

---

## 3. Prasyarat Sistem (Prerequisites)
Pastikan perangkat Anda telah terpasang:
- **Java Development Kit (JDK)**: Versi 17 atau lebih tinggi.
- **Apache Maven**: Versi 3.8+ (atau gunakan wrapper `./mvnw` / `mvnw.cmd` yang disediakan).
- **Docker & Docker Compose**: Untuk menjalankan database PostgreSQL 16.

---

## 4. Panduan Cepat & Pengembangan Lokal

### Langkah 1: Kloning Repositori dan Konfigurasi Lingkungan
1. Masuk ke direktori utama proyek:
   ```bash
   cd c:\Users\febri\OneDrive\Desktop\dedyharisman
   ```
2. Buat file konfigurasi `.env` dari `.env.example`:
   - Pada Linux/macOS:
     ```bash
     cp .env.example .env
     ```
   - Pada Windows (PowerShell):
     ```powershell
     Copy-Item .env.example .env
     ```

### Langkah 2: Jalankan Database PostgreSQL
Jalankan container PostgreSQL 16:
```bash
docker compose up -d postgres
```
Periksa status container:
```bash
docker compose ps
```
Database akan berjalan sehat dan mendengarkan pada port `5432`. Seluruh data tersimpan secara aman di dalam volume Docker `simponi_pgdata`.

### Langkah 3: Jalankan Aplikasi Spring Boot
Pindah ke direktori `backend/` dan jalankan menggunakan Maven:
```bash
cd backend
mvn spring-boot:run
```
*(Atau gunakan `./mvnw spring-boot:run` pada Linux/macOS atau `mvnw.cmd spring-boot:run` pada Windows).*

Setelah aplikasi aktif, buka peramban web pada alamat:
```
http://localhost:8080
```

---

## 5. Kredensial Akun Default (Seed Data)

Saat aplikasi pertama kali dijalankan, migrasi Flyway dan Data Initializer secara otomatis membuat akun uji coba default:

| Role | Email | Password | ID Karyawan | Keterangan |
|---|---|---|---|---|
| **Admin** | `admin@simponi.local` | `AdminPass123!` | - | Akses penuh menu Administrator |
| **Employee** | `employee@simponi.local` | `UserPass123!` | `EMP-001` | Akun Karyawan Uji (Jane Doe) |
| **Employee** | `john.smith@simponi.local` | `UserPass123!` | `EMP-002` | Akun Rekan Kerja (John Smith) |

---

## 6. Operasional & Manajemen Database

### 6.1. Menjalankan dan Menghentikan Database
- **Menjalankan Database**:
  ```bash
  docker compose up -d postgres
  ```
- **Menghentikan Database** (data tetap aman tersimpan di volume persisten):
  ```bash
  docker compose down
  ```

### 6.2. Pencadangan Database (Backup / Dump)
Untuk membuat salinan lengkap database yang sedang aktif:
- **Linux/macOS**:
  ```bash
  docker exec -t simponi-postgres pg_dump -U simponi_user -d simponi_db > backup_simponi_$(date +%Y%m%d_%H%M%S).sql
  ```
- **Windows (PowerShell)**:
  ```powershell
  $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
  docker exec -t simponi-postgres pg_dump -U simponi_user -d simponi_db | Out-File -Encoding utf8 "backup_simponi_$timestamp.sql"
  ```

### 6.3. Pemulihan Database (Restore)
Untuk memulihkan data dari file cadangan (*backup*) ke dalam container yang berjalan:
- **Linux/macOS**:
  ```bash
  cat backup_simponi_YYYYMMDD_HHMMSS.sql | docker exec -i simponi-postgres psql -U simponi_user -d simponi_db
  ```
- **Windows (PowerShell)**:
  ```powershell
  Get-Content backup_simponi_YYYYMMDD_HHMMSS.sql | docker exec -i simponi-postgres psql -U simponi_user -d simponi_db
  ```

### 6.4. Mengatur Ulang Database Pengembangan (Reset)
Jika Anda perlu menghapus dan mereset database pengembangan lokal hingga bersih:
```bash
# 1. Hentikan container dan hapus volume data
docker compose down -v

# 2. Jalankan kembali container baru yang bersih
docker compose up -d postgres
```
*(Catatan: Jangan pernah menghapus volume database secara otomatis dalam skrip produksi).*

---

## 7. Panduan Pengujian Geolocation (GPS)
- Peramban web modern mewajibkan protokol HTTPS untuk fitur Geolocation API (`navigator.geolocation`), namun memberikan pengecualian resmi pada lingkungan `localhost` dan `127.0.0.1`.
- Saat membuka `http://localhost:8080`, peramban akan meminta izin akses lokasi (*Location Permission*). Pastikan Anda memilih **Izinkan (Allow)**.
- Koordinat kantor default yang telah terdaftar berada di Jakarta Pusat (`Latitude: -6.2088, Longitude: 106.8456, Radius: 100 meter`).
- Administrator dapat mengubah titik koordinat kantor melalui menu **Admin > Pengaturan Lokasi Kantor** atau menekan tombol **"Gunakan Lokasi Saya Saat Ini"** untuk menyamakan titik kantor dengan lokasi pengujian Anda saat ini.
