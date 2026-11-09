/**
 * SIMPONI — Authentication Controller (Frontend)
 * Created by Deddy Harisman
 */

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('login-form');
  const alertBox = document.getElementById('login-alert');

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      showAlert('', false);

      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      const submitBtn = loginForm.querySelector('button[type="submit"]');

      if (!email || !password) {
        showAlert('Email dan kata sandi wajib diisi.', true);
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Memverifikasi...';

      try {
        const response = await API.post('/api/auth/login', { email, password });
        if (response.success) {
          const role = response.user.role;
          if (role === 'ADMIN') {
            window.location.href = '/pages/admin-employees.html';
          } else {
            window.location.href = '/pages/dashboard.html';
          }
        } else {
          showAlert(response.message || 'Gagal masuk.', true);
        }
      } catch (err) {
        showAlert(err.message, true);
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Masuk ke SIMPONI';
      }
    });
  }

  function showAlert(msg, isError = true) {
    if (!alertBox) return;
    if (!msg) {
      alertBox.style.display = 'none';
      return;
    }
    alertBox.className = isError ? 'alert alert-danger' : 'alert alert-success';
    alertBox.textContent = msg;
    alertBox.style.display = 'block';
  }
});

