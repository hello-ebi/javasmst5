/**
 * SIMPONI — API & Network Client Helper
 * Created by Deddy Harisman
 */

const API = {
  getCsrfToken() {
    const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  },

  async request(url, options = {}) {
    options.headers = options.headers || {};
    options.credentials = 'same-origin';

    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes((options.method || 'GET').toUpperCase())) {
      const csrf = this.getCsrfToken();
      if (csrf) {
        options.headers['X-XSRF-TOKEN'] = csrf;
      }
      if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(options.body);
      }
    }

    const response = await fetch(url, options);

    if (response.status === 401) {
      if (!window.location.pathname.includes('login.html')) {
        window.location.href = '/pages/login.html?session=expired';
      }
      throw new Error('Sesi telah berakhir. Silakan login kembali.');
    }

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = { message: await response.text() };
    }

    if (!response.ok) {
      const errorMessage = data.message || data.error || 'Terjadi kesalahan sistem.';
      throw new Error(errorMessage);
    }

    return data;
  },

  get(url) {
    return this.request(url, { method: 'GET' });
  },

  post(url, body) {
    return this.request(url, { method: 'POST', body });
  },

  put(url, body) {
    return this.request(url, { method: 'PUT', body });
  },

  delete(url) {
    return this.request(url, { method: 'DELETE' });
  },

  async getCurrentUser() {
    try {
      const res = await this.get('/api/auth/me');
      return res.user;
    } catch (e) {
      return null;
    }
  },

  async requireAuth(requiredRole = null) {
    const user = await this.getCurrentUser();
    if (!user) {
      window.location.href = '/pages/login.html';
      return null;
    }

    if (requiredRole && user.role !== requiredRole) {
      alert('Akses Ditolak: Anda tidak memiliki hak akses untuk halaman ini.');
      window.location.href = user.role === 'ADMIN' ? '/pages/admin-employees.html' : '/pages/dashboard.html';
      return null;
    }

    this.renderHeaderUserInfo(user);
    return user;
  },

  renderHeaderUserInfo(user) {
    const nameEl = document.getElementById('header-user-name');
    const roleEl = document.getElementById('header-user-role');
    if (nameEl) nameEl.textContent = user.fullName || user.email;
    if (roleEl) roleEl.textContent = (user.role === 'ADMIN' ? 'Administrator' : 'Karyawan (' + (user.employeeId || '-') + ')');
  },

  async logout() {
    try {
      await this.post('/api/auth/logout', {});
    } catch (e) {
      console.warn('Logout warning:', e);
    }
    window.location.href = '/pages/login.html';
  }
};

