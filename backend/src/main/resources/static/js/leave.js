/**
 * SIMPONI — Leave & Permission Management (Frontend)
 * Created by Deddy Harisman
 */

document.addEventListener('DOMContentLoaded', async () => {
  const user = await API.requireAuth('EMPLOYEE');
  if (!user) return;

  await loadColleagues();
  await loadLeaveHistory();

  const form = document.getElementById('leave-form');
  const needsReplacementCheck = document.getElementById('needs-replacement');
  const replacementGroup = document.getElementById('replacement-group');

  if (needsReplacementCheck && replacementGroup) {
    needsReplacementCheck.addEventListener('change', () => {
      replacementGroup.style.display = needsReplacementCheck.checked ? 'block' : 'none';
      const replacementSelect = document.getElementById('replacement-employee');
      if (replacementSelect) {
        replacementSelect.required = needsReplacementCheck.checked;
      }
    });
  }

  if (form) {
    form.addEventListener('submit', handleLeaveSubmit);
  }
});

async function loadColleagues() {
  const select = document.getElementById('replacement-employee');
  if (!select) return;

  try {
    const colleagues = await API.get('/api/employee/colleagues');
    select.innerHTML = '<option value="">-- Pilih Karyawan Pengganti --</option>' +
      colleagues.map(c => `<option value="${c.id}">${c.fullName} (${c.employeeId}) - ${c.department || 'Umum'}</option>`).join('');
  } catch (err) {
    console.error('Failed to load colleagues:', err);
  }
}

async function handleLeaveSubmit(e) {
  e.preventDefault();

  const leaveType = document.getElementById('leave-type').value;
  const startDate = document.getElementById('start-date').value;
  const endDate = document.getElementById('end-date').value;
  const reason = document.getElementById('reason').value.trim();
  const needsReplacement = document.getElementById('needs-replacement').checked;
  const replacementEmployeeId = needsReplacement ? document.getElementById('replacement-employee').value : null;

  if (new Date(endDate) < new Date(startDate)) {
    alert('Tanggal selesai tidak boleh sebelum tanggal mulai.');
    return;
  }

  if (needsReplacement && !replacementEmployeeId) {
    alert('Pilih karyawan pengganti terlebih dahulu.');
    return;
  }

  const submitBtn = e.target.querySelector('button[type="submit"]');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Mengirim pengajuan...';

  try {
    const payload = {
      leaveType,
      startDate,
      endDate,
      reason,
      needsReplacement,
      replacementEmployeeId: replacementEmployeeId ? parseInt(replacementEmployeeId, 10) : null
    };

    const res = await API.post('/api/employee/leaves', payload);
    alert(res.message || 'Pengajuan cuti/izin berhasil dikirim!');

    e.target.reset();
    document.getElementById('replacement-group').style.display = 'none';
    await loadLeaveHistory();
  } catch (err) {
    alert(`Pengajuan Gagal:\n${err.message}`);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Kirim Pengajuan Cuti';
  }
}

async function loadLeaveHistory() {
  const tbody = document.getElementById('leave-history-body');
  if (!tbody) return;

  try {
    const list = await API.get('/api/employee/leaves');
    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#64748B;">Belum ada pengajuan cuti/izin.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(item => {
      let badgeClass = 'badge-pending';
      if (item.status === 'APPROVED') badgeClass = 'badge-approved';
      if (item.status === 'REJECTED') badgeClass = 'badge-rejected';

      const replacementText = item.needsReplacement 
        ? (item.replacementEmployeeName ? item.replacementEmployeeName : 'Belum ditentukan')
        : 'Tidak Perlu';

      let statusDisplay = `<span class="badge ${badgeClass}">${item.status}</span>`;
      if (item.status === 'REJECTED' && item.rejectionReason) {
        statusDisplay += `<div style="font-size:11px; color:#DC2626; margin-top:4px;">Alasan: ${item.rejectionReason}</div>`;
      }

      return `
        <tr>
          <td><strong>${item.leaveType}</strong></td>
          <td>${item.startDate} s/d ${item.endDate}</td>
          <td>${item.reason}</td>
          <td>${replacementText}</td>
          <td>${statusDisplay}</td>
          <td style="font-size:11px; color:#64748B;">${new Date(item.createdAt).toLocaleDateString('id-ID')}</td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:#DC2626;">Gagal memuat riwayat cuti: ${err.message}</td></tr>`;
  }
}

