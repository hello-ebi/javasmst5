/**
 * SIMPONI — Admin Portal Controller (Frontend)
 * Created by Deddy Harisman
 */

document.addEventListener('DOMContentLoaded', async () => {
  const user = await API.requireAuth('ADMIN');
  if (!user) return;

  const path = window.location.pathname;

  if (path.includes('admin-employees.html')) {
    initEmployeePage();
  } else if (path.includes('admin-leaves.html')) {
    initLeavePage();
  } else if (path.includes('admin-locations.html')) {
    initLocationPage();
  }
});

// ==========================================
// 1. Employee Management
// ==========================================
function initEmployeePage() {
  loadEmployees();

  const addBtn = document.getElementById('btn-add-employee');
  const modal = document.getElementById('employee-modal');
  const closeBtn = document.getElementById('modal-close-btn');
  const cancelBtn = document.getElementById('modal-cancel-btn');
  const form = document.getElementById('employee-form');

  if (addBtn) {
    addBtn.addEventListener('click', () => {
      form.reset();
      document.getElementById('emp-modal-id').value = '';
      document.getElementById('emp-password-group').style.display = 'block';
      document.getElementById('emp-password').required = true;
      document.getElementById('modal-title').textContent = 'Tambah Karyawan Baru';
      modal.style.display = 'flex';
    });
  }

  const closeModal = () => { if (modal) modal.style.display = 'none'; };
  if (closeBtn) closeBtn.addEventListener('click', closeModal);
  if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('emp-modal-id').value;
      const employeeId = document.getElementById('emp-code').value.trim();
      const fullName = document.getElementById('emp-fullname').value.trim();
      const email = document.getElementById('emp-email').value.trim();
      const password = document.getElementById('emp-password').value;
      const role = document.getElementById('emp-role').value;
      const department = document.getElementById('emp-dept').value.trim();
      const position = document.getElementById('emp-position').value.trim();
      const phone = document.getElementById('emp-phone').value.trim();

      try {
        if (id) {
          // Update
          await API.put(`/api/admin/employees/${id}`, {
            fullName, email, password: password || null, role, department, position, phone, isActive: true
          });
          alert('Data karyawan berhasil diperbarui.');
        } else {
          // Create
          await API.post('/api/admin/employees', {
            employeeId, fullName, email, password, role, department, position, phone
          });
          alert('Karyawan baru berhasil ditambahkan.');
        }
        closeModal();
        loadEmployees();
      } catch (err) {
        alert(`Operasi Gagal:\n${err.message}`);
      }
    });
  }
}

async function loadEmployees() {
  const tbody = document.getElementById('employee-table-body');
  if (!tbody) return;

  try {
    const list = await API.get('/api/admin/employees');
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#64748B;">Belum ada data karyawan.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(emp => {
      const statusBadge = emp.isActive 
        ? '<span class="badge badge-approved">Aktif</span>'
        : '<span class="badge badge-rejected">Nonaktif</span>';

      return `
        <tr>
          <td><strong>${emp.employeeId}</strong></td>
          <td>${emp.fullName}</td>
          <td>${emp.email}</td>
          <td>${emp.department || '-'} / ${emp.position || '-'}</td>
          <td><span class="badge badge-present">${emp.role}</span> ${statusBadge}</td>
          <td>
            <button class="btn btn-secondary btn-sm" onclick="editEmployee(${emp.id})">Edit</button>
            ${emp.isActive ? `<button class="btn btn-danger btn-sm" onclick="deactivateEmployee(${emp.id})">Nonaktifkan</button>` : ''}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:#DC2626;">Gagal memuat karyawan: ${err.message}</td></tr>`;
  }
}

window.editEmployee = async function(id) {
  try {
    const emp = await API.get(`/api/admin/employees/${id}`);
    const modal = document.getElementById('employee-modal');

    document.getElementById('emp-modal-id').value = emp.id;
    document.getElementById('emp-code').value = emp.employeeId;
    document.getElementById('emp-code').disabled = true; // cannot change code
    document.getElementById('emp-fullname').value = emp.fullName;
    document.getElementById('emp-email').value = emp.email;
    document.getElementById('emp-password').value = '';
    document.getElementById('emp-password').required = false;
    document.getElementById('emp-password-group').style.display = 'block';
    document.getElementById('emp-password-help').textContent = 'Kosongkan jika tidak ingin mengubah kata sandi.';
    document.getElementById('emp-role').value = emp.role;
    document.getElementById('emp-dept').value = emp.department || '';
    document.getElementById('emp-position').value = emp.position || '';
    document.getElementById('emp-phone').value = emp.phone || '';

    document.getElementById('modal-title').textContent = 'Edit Data Karyawan';
    modal.style.display = 'flex';
  } catch (err) {
    alert(`Gagal mengambil data: ${err.message}`);
  }
};

window.deactivateEmployee = async function(id) {
  if (!confirm('Yakin ingin menonaktifkan akun karyawan ini? Akun tidak akan dapat masuk ke sistem.')) return;
  try {
    await API.delete(`/api/admin/employees/${id}`);
    alert('Karyawan berhasil dinonaktifkan.');
    loadEmployees();
  } catch (err) {
    alert(`Gagal menonaktifkan: ${err.message}`);
  }
};

// ==========================================
// 2. Leave Management
// ==========================================
function initLeavePage() {
  loadAdminLeaves();

  const filterSelect = document.getElementById('filter-leave-status');
  if (filterSelect) {
    filterSelect.addEventListener('change', () => loadAdminLeaves(filterSelect.value));
  }
}

async function loadAdminLeaves(status = 'ALL') {
  const tbody = document.getElementById('admin-leave-table-body');
  if (!tbody) return;

  try {
    const list = await API.get(`/api/admin/leaves?status=${status}`);
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#64748B;">Tidak ada pengajuan cuti.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(item => {
      let badgeClass = 'badge-pending';
      if (item.status === 'APPROVED') badgeClass = 'badge-approved';
      if (item.status === 'REJECTED') badgeClass = 'badge-rejected';

      const replacementInfo = item.needsReplacement 
        ? `Perlu: <strong>${item.replacementEmployeeName || 'Belum ada'}</strong>`
        : '<span style="color:#64748B;">Tidak Perlu</span>';

      const actions = item.status === 'PENDING' ? `
        <button class="btn btn-primary btn-sm" onclick="approveLeave(${item.id})">Setujui</button>
        <button class="btn btn-danger btn-sm" onclick="rejectLeave(${item.id})">Tolak</button>
      ` : `<span style="font-size:12px; color:#64748B;">Selesai (${item.reviewedByName || '-'})</span>`;

      return `
        <tr>
          <td><strong>${item.employeeName}</strong><br><span style="font-size:11px; color:#64748B;">${item.employeeCode}</span></td>
          <td>${item.leaveType}</td>
          <td>${item.startDate} s/d ${item.endDate}</td>
          <td>${item.reason}</td>
          <td>${replacementInfo}</td>
          <td><span class="badge ${badgeClass}">${item.status}</span></td>
          <td>${actions}</td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="color:#DC2626;">Gagal memuat daftar cuti: ${err.message}</td></tr>`;
  }
}

window.approveLeave = async function(id) {
  if (!confirm('Setujui pengajuan cuti ini?')) return;
  try {
    await API.post(`/api/admin/leaves/${id}/approve`, {});
    alert('Pengajuan cuti berhasil disetujui.');
    loadAdminLeaves(document.getElementById('filter-leave-status').value);
  } catch (err) {
    alert(`Gagal menyetujui: ${err.message}`);
  }
};

window.rejectLeave = async function(id) {
  const reason = prompt('Masukkan alasan penolakan cuti:');
  if (reason === null) return;
  if (!reason.trim()) {
    alert('Alasan penolakan wajib diisi.');
    return;
  }

  try {
    await API.post(`/api/admin/leaves/${id}/reject`, { rejectionReason: reason.trim() });
    alert('Pengajuan cuti telah ditolak.');
    loadAdminLeaves(document.getElementById('filter-leave-status').value);
  } catch (err) {
    alert(`Gagal menolak: ${err.message}`);
  }
};

// ==========================================
// 3. Office Location Settings
// ==========================================
function initLocationPage() {
  loadLocations();

  const form = document.getElementById('location-form');
  const pickCurrentBtn = document.getElementById('btn-pick-current-gps');

  if (pickCurrentBtn) {
    pickCurrentBtn.addEventListener('click', () => {
      if (!navigator.geolocation) {
        alert('Browser tidak mendukung Geolocation.');
        return;
      }
      pickCurrentBtn.disabled = true;
      pickCurrentBtn.textContent = 'Mendeteksi...';
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          document.getElementById('loc-latitude').value = pos.coords.latitude.toFixed(6);
          document.getElementById('loc-longitude').value = pos.coords.longitude.toFixed(6);
          alert(`Lokasi terisi dari GPS saat ini!\nAkurasi: ±${Math.round(pos.coords.accuracy)}m`);
          pickCurrentBtn.disabled = false;
          pickCurrentBtn.textContent = 'Gunakan Lokasi Saya Saat Ini';
        },
        (err) => {
          alert(`Gagal mendeteksi lokasi GPS: ${err.message}`);
          pickCurrentBtn.disabled = false;
          pickCurrentBtn.textContent = 'Gunakan Lokasi Saya Saat Ini';
        },
        { enableHighAccuracy: true }
      );
    });
  }

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('loc-id').value;
      const name = document.getElementById('loc-name').value.trim();
      const latitude = parseFloat(document.getElementById('loc-latitude').value);
      const longitude = parseFloat(document.getElementById('loc-longitude').value);
      const radiusMeters = parseInt(document.getElementById('loc-radius').value, 10);
      const isActive = document.getElementById('loc-active').checked;

      try {
        if (id) {
          await API.put(`/api/admin/locations/${id}`, { name, latitude, longitude, radiusMeters, isActive });
          alert('Lokasi kantor berhasil diperbarui.');
        } else {
          await API.post('/api/admin/locations', { name, latitude, longitude, radiusMeters, isActive });
          alert('Lokasi kantor berhasil disimpan.');
        }
        form.reset();
        document.getElementById('loc-id').value = '';
        document.getElementById('loc-active').checked = true;
        loadLocations();
      } catch (err) {
        alert(`Gagal menyimpan lokasi: ${err.message}`);
      }
    });
  }
}

async function loadLocations() {
  const tbody = document.getElementById('location-table-body');
  if (!tbody) return;

  try {
    const list = await API.get('/api/admin/locations');
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#64748B;">Belum ada konfigurasi kantor.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(loc => {
      const activeBadge = loc.isActive 
        ? '<span class="badge badge-approved">AKTIF (GEOFENCE)</span>' 
        : '<span class="badge badge-rejected">Nonaktif</span>';

      const activateBtn = !loc.isActive 
        ? `<button class="btn btn-primary btn-sm" onclick="activateLocation(${loc.id})">Jadikan Aktif</button>`
        : '';

      return `
        <tr>
          <td><strong>${loc.name}</strong></td>
          <td>${loc.latitude}</td>
          <td>${loc.longitude}</td>
          <td>${loc.radiusMeters} meter</td>
          <td>${activeBadge}</td>
          <td>
            <button class="btn btn-secondary btn-sm" onclick="editLocation(${loc.id}, '${loc.name}', ${loc.latitude}, ${loc.longitude}, ${loc.radiusMeters}, ${loc.isActive})">Edit</button>
            ${activateBtn}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:#DC2626;">Gagal memuat lokasi: ${err.message}</td></tr>`;
  }
}

window.editLocation = function(id, name, lat, lng, radius, isActive) {
  document.getElementById('loc-id').value = id;
  document.getElementById('loc-name').value = name;
  document.getElementById('loc-latitude').value = lat;
  document.getElementById('loc-longitude').value = lng;
  document.getElementById('loc-radius').value = radius;
  document.getElementById('loc-active').checked = isActive;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

window.activateLocation = async function(id) {
  if (!confirm('Aktifkan lokasi ini sebagai satu-satunya geofence presensi kantor?')) return;
  try {
    await API.put(`/api/admin/locations/${id}/activate`, {});
    alert('Lokasi kantor aktif berhasil diubah.');
    loadLocations();
  } catch (err) {
    alert(`Gagal mengaktifkan: ${err.message}`);
  }
};

