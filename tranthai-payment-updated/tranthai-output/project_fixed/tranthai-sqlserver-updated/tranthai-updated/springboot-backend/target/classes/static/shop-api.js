// api.js – Tất cả giao tiếp với server, không dùng localStorage cho data
const BASE = '/api';

// ── Token (sessionStorage – chỉ tab hiện tại, không lưu vĩnh viễn) ──
const session = {
  get: (key) => { try { return JSON.parse(sessionStorage.getItem(key) || 'null'); } catch { return null; } },
  set: (key, val) => sessionStorage.setItem(key, JSON.stringify(val)),
  remove: (key) => sessionStorage.removeItem(key),
  clear: () => ['adminSession','staffSession','customerSession'].forEach(k => sessionStorage.removeItem(k)),
};

function getToken() {
  const a = session.get('adminSession');
  const s = session.get('staffSession');
  const c = session.get('customerSession');
  return (a || s || c)?.token || null;
}

async function req(method, path, body = null, customToken = null) {
  const token = customToken || getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(BASE + path, {
    method, headers,
    body: body ? JSON.stringify(body) : null,
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Lỗi server: ' + res.status);
  return data;
}

const api = {
  auth: {
    adminLogin:    (email, password) => req('POST', '/auth/admin/login',    { email, password }),
    staffLogin:    (email, password) => req('POST', '/auth/staff/login',    { email, password }),
    customerLogin: (email, password) => req('POST', '/auth/customer/login', { email, password }),
    register:      (data)            => req('POST', '/auth/customer/register', data),
  },
  products: {
    getAll:    ()       => req('GET',  '/products'),
    getAllAdmin:()       => req('GET',  '/products/all'),
    getById:   (id)     => req('GET',  '/products/' + id),
    create:    (data)   => req('POST', '/products', data),
    update:    (id, d)  => req('PUT',  '/products/' + id, d),
    delete:    (id)     => req('DELETE','/products/' + id),
    restore:   (id)     => req('PUT',  '/products/' + id + '/restore'),
  },
  orders: {
    getAll:        ()      => req('GET',  '/orders'),
    getById:       (id)    => req('GET',  '/orders/' + id),
    getByCustomer: (cid)   => req('GET',  '/orders/customer/' + cid),
    getMyOrders:   ()      => req('GET',  '/orders/my'),
    create:        (data)  => req('POST', '/orders', data),
    updateStatus:  (id, s) => req('PUT',  '/orders/' + id + '/status', { status: s }),
    confirmPayment:(id)    => req('PUT',  '/orders/' + id + '/confirm-payment'),
  },
  cart: {
    get:          ()           => req('GET',    '/cart'),
    add:          (item)       => req('POST',   '/cart', item),
    updateQty:    (id, qty)    => req('PUT',    '/cart/' + id, { qty }),
    remove:       (cartKey)    => req('DELETE', '/cart/item/' + cartKey),
    clear:        ()           => req('DELETE', '/cart'),
    checkoutClear:(keys)       => req('POST',   '/cart/checkout-clear', keys),
  },
  staffs: {
    getAll:  ()       => req('GET',    '/staffs'),
    getById: (id)     => req('GET',    '/staffs/' + id),
    create:  (data)   => req('POST',   '/staffs', data),
    update:  (id, d)  => req('PUT',    '/staffs/' + id, d),
    delete:  (id)     => req('DELETE', '/staffs/' + id),
  },
  customers: {
    getAll:        ()             => req('GET', '/customers'),
    getById:       (id)           => req('GET', '/customers/' + id),
    updateStatus:  (id, status)   => req('PUT', '/customers/' + id + '/status', { status }),
    updateProfile: (data)         => req('PUT', '/customers/profile', data),
    changePassword:(data)         => req('PUT', '/customers/change-password', data),
  },
  support: {
    getAll:      ()                       => req('GET',  '/support'),
    openChat:    (customerId, customerName) => req('POST', '/support/chat', { customerId, customerName }),
    sendMessage: (chatId, sender, text)   => req('POST', '/support/' + chatId + '/messages', { sender, text }),
    closeChat:   (chatId)                 => req('PUT',  '/support/' + chatId + '/close'),
  },
  dashboard: {
    getStats: () => req('GET', '/dashboard/stats'),
  },
};