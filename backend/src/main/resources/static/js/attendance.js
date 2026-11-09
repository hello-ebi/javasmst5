/**
 * SIMPONI — Attendance & Geofencing (Frontend)
 * Created by Deddy Harisman
 */

let currentPosition = null;
let activeOffice = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = await API.requireAuth('EMPLOYEE');
  if (!user) return;

  initClock();
  initGeolocation();
  await loadTodayAttendance();
  await loadAttendanceHistory();

  const checkInBtn = document.getElementById('btn-check-in');
  const checkOutBtn = document.getElementById('btn-check-out');

  if (checkInBtn) {
    checkInBtn.addEventListener('click', handleCheckIn);
  }
  if (checkOutBtn) {
    checkOutBtn.addEventListener('click', handleCheckOut);
  }
});

function initClock() {
  const clockEl = document.getElementById('live-clock');
  const dateEl = document.getElementById('live-date');

  function update() {
    const now = new Date();
    if (clockEl) {
      clockEl.textContent = now.toLocaleTimeString('id-ID', { hour12: false });
    }
    if (dateEl) {
      dateEl.textContent = now.toLocaleDateString('id-ID', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
  }
  update();
  setInterval(update, 1000);
}

function initGeolocation() {
  const statusEl = document.getElementById('geo-status');
  const latLngEl = document.getElementById('geo-coordinates');

  if (!navigator.geolocation) {
    if (statusEl) statusEl.textContent = 'Geolocation tidak didukung oleh browser ini.';
    return;
  }

  if (statusEl) statusEl.textContent = 'Mencari sinyal GPS...';

  const options = {
    enableHighAccuracy: true,
    timeout: 10000,
    maximumAge: 0
  };

  navigator.geolocation.watchPosition(
    (pos) => {
      currentPosition = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
        accuracy: pos.coords.accuracy
      };

      if (statusEl) {
        statusEl.innerHTML = `GPS Terkunci (Akurasi: &plusmn;${Math.round(pos.coords.accuracy)} meter)`;
        statusEl.style.color = '#16A34A';
      }
      if (latLngEl) {
        latLngEl.textContent = `Lat: ${pos.coords.latitude.toFixed(6)}, Lng: ${pos.coords.longitude.toFixed(6)}`;
      }
    },
    (err) => {
      console.warn('Geolocation error:', err);
      if (statusEl) {
        statusEl.textContent = 'Izin lokasi GPS belum diaktifkan. Mohon izinkan akses lokasi pada browser Anda.';
        statusEl.style.color = '#DC2626';
      }
    },
    options
  );
}

async function loadTodayAttendance() {
  const checkInBtn = document.getElementById('btn-check-in');
  const checkOutBtn = document.getElementById('btn-check-out');
  const statusBadge = document.getElementById('today-status-badge');
  const officeInfo = document.getElementById('office-info');

  try {
    const data = await API.get('/api/employee/attendance/today');
    activeOffice = {
      name: data.officeName,
      latitude: data.officeLatitude,
      longitude: data.officeLongitude,
      radiusMeters: data.officeRadiusMeters
    };

    if (officeInfo) {
      officeInfo.textContent = `Lokasi Kantor: ${data.officeName} (Radius Maks: ${data.officeRadiusMeters}m)`;
    }

    if (data.hasCheckedOut) {
      if (statusBadge) {
        statusBadge.className = 'badge badge-approved';
        statusBadge.textContent = 'Presensi Pulang Selesai';
      }
      if (checkInBtn) checkInBtn.disabled = true;
      if (checkOutBtn) checkOutBtn.disabled = true;
    } else if (data.hasCheckedIn) {
      if (statusBadge) {
        statusBadge.className = 'badge badge-present';
        const inTime = new Date(data.attendance.checkInTime).toLocaleTimeString('id-ID', { hour12: false });
        statusBadge.textContent = `Sudah Masuk (${inTime})`;
      }
      if (checkInBtn) checkInBtn.disabled = true;
      if (checkOutBtn) checkOutBtn.disabled = false;
    } else {
      if (statusBadge) {
        statusBadge.className = 'badge badge-pending';
        statusBadge.textContent = 'Belum Presensi Masuk';
      }
      if (checkInBtn) checkInBtn.disabled = false;
      if (checkOutBtn) checkOutBtn.disabled = true;
    }
  } catch (err) {
    console.error('Failed to load today status:', err);
  }
}

async function handleCheckIn() {
  if (!ensureGpsLocked()) return;

  const btn = document.getElementById('btn-check-in');
  btn.disabled = true;
  btn.textContent = 'Mengirim presensi masuk...';

  try {
    const res = await API.post('/api/employee/attendance/check-in', {
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude,
      accuracy: currentPosition.accuracy
    });

    alert(`Presensi Masuk Berhasil!\nJarak ke kantor: ${res.distanceMeters} meter.`);
    await loadTodayAttendance();
    await loadAttendanceHistory();
  } catch (err) {
    alert(`Presensi Masuk Gagal:\n${err.message}`);
    btn.disabled = false;
  } finally {
    btn.textContent = 'Presensi Masuk';
  }
}

async function handleCheckOut() {
  if (!ensureGpsLocked()) return;

  const btn = document.getElementById('btn-check-out');
  btn.disabled = true;
  btn.textContent = 'Mengirim presensi pulang...';

  try {
    const res = await API.post('/api/employee/attendance/check-out', {
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude,
      accuracy: currentPosition.accuracy
    });

    alert(`Presensi Pulang Berhasil!\nJarak ke kantor: ${res.distanceMeters} meter.`);
    await loadTodayAttendance();
    await loadAttendanceHistory();
  } catch (err) {
    alert(`Presensi Pulang Gagal:\n${err.message}`);
    btn.disabled = false;
  } finally {
    btn.textContent = 'Presensi Pulang';
  }
}

function ensureGpsLocked() {
  if (!currentPosition) {
    alert('GPS belum mengunci lokasi Anda. Mohon tunggu beberapa detik atau pastikan izin GPS aktif.');
    return false;
  }
  return true;
}

async function loadAttendanceHistory() {
  const tbody = document.getElementById('attendance-history-body');
  if (!tbody) return;

  try {
    const list = await API.get('/api/employee/attendance/history');
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#64748B;">Belum ada riwayat presensi.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(item => {
      const inTime = item.checkInTime ? new Date(item.checkInTime).toLocaleTimeString('id-ID', { hour12: false }) : '-';
      const outTime = item.checkOutTime ? new Date(item.checkOutTime).toLocaleTimeString('id-ID', { hour12: false }) : '-';
      const inDist = item.checkInDistance != null ? `${item.checkInDistance}m` : '-';

      return `
        <tr>
          <td><strong>${item.attendanceDate}</strong></td>
          <td>${inTime} <span style="font-size:11px; color:#64748B;">(${inDist})</span></td>
          <td>${outTime}</td>
          <td><span class="badge badge-present">${item.status}</span></td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="color:#DC2626;">Gagal memuat riwayat: ${err.message}</td></tr>`;
  }
}

