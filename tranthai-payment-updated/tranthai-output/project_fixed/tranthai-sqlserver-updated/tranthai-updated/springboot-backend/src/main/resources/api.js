// api.js  –  Thay thế db.js
// Đặt file này vào src/api.js

const BASE_URL = "http://localhost:8080/api";

// ══════════════════════════════════════════════════════
//  HTTP helper
// ══════════════════════════════════════════════════════
async function request(method, path, body = null, token = null) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : null,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "Lỗi không xác định");
  }

  if (res.status === 204) return null;
  return res.json();
}

// Lấy token từ session
function getToken() {
  const admin    = session.get("adminSession");
  const staff    = session.get("currentStaff");
  const customer = session.get("customerSession");
  return (admin || staff || customer)?.token || null;
}

const api = (method, path, body = null) => request(method, path, body, getToken());

// ══════════════════════════════════════════════════════
//  Auth
// ══════════════════════════════════════════════════════
export const auth = {
  adminLogin:      (email, password) => request("POST", "/auth/admin/login",    { email, password }),
  staffLogin:      (email, password) => request("POST", "/auth/staff/login",    { email, password }),
  customerLogin:   (email, password) => request("POST", "/auth/customer/login", { email, password }),
  register:        (data)            => request("POST", "/auth/customer/register", data),
  changePassword:  (data)            => api("PUT",  "/auth/customer/change-password", data),
};

// ══════════════════════════════════════════════════════
//  Products
// ══════════════════════════════════════════════════════
export const products = {
  getAll:            ()       => request("GET",  "/products"),
  getAllAdmin:        ()       => api("GET",  "/products/all"),
  getById:           (id)     => request("GET",  `/products/${id}`),
  create:            (data)   => api("POST", "/products", data),
  update:            (id, d)  => api("PUT",  `/products/${id}`, d),
  softDelete:        (id)     => api("DELETE", `/products/${id}`),
  restore:           (id)     => api("PUT",  `/products/${id}/restore`),
};

// ══════════════════════════════════════════════════════
//  Orders
// ══════════════════════════════════════════════════════
export const orders = {
  getAll:            ()       => api("GET",  "/orders"),
  getById:           (id)     => api("GET",  `/orders/${id}`),
  getByCustomer:     (cid)    => api("GET",  `/orders/customer/${cid}`),
  create:            (data)   => api("POST", "/orders", data),
  updateStatus:      (id, s)  => api("PUT",  `/orders/${id}/status`, { status: s }),
};

// ══════════════════════════════════════════════════════
//  Staff
// ══════════════════════════════════════════════════════
export const staffApi = {
  getAll:    ()        => api("GET",    "/staffs"),
  getById:   (id)      => api("GET",    `/staffs/${id}`),
  create:    (data)    => api("POST",   "/staffs", data),
  update:    (id, d)   => api("PUT",    `/staffs/${id}`, d),
  delete:    (id)      => api("DELETE", `/staffs/${id}`),
};

// ══════════════════════════════════════════════════════
//  Customers
// ══════════════════════════════════════════════════════
export const customersApi = {
  getAll:        ()           => api("GET",  "/customers"),
  getById:       (id)         => api("GET",  `/customers/${id}`),
  updateStatus:  (id, status) => api("PUT",  `/customers/${id}/status`, { status }),
};

// ══════════════════════════════════════════════════════
//  Support Chat
// ══════════════════════════════════════════════════════
export const support = {
  getAll:        ()               => api("GET",  "/support"),
  openChat:      (customerId, customerName) =>
                   api("POST", "/support/chat", { customerId, customerName }),
  sendMessage:   (chatId, sender, text) =>
                   api("POST", `/support/${chatId}/messages`, { sender, text }),
  closeChat:     (chatId)         => api("PUT",  `/support/${chatId}/close`),
};

// ══════════════════════════════════════════════════════
//  Dashboard
// ══════════════════════════════════════════════════════
export const dashboard = {
  getStats: () => api("GET", "/dashboard/stats"),
};

// ══════════════════════════════════════════════════════
//  Session (giữ nguyên logic cũ)
// ══════════════════════════════════════════════════════
export const session = {
  get(key) {
    try { return JSON.parse(localStorage.getItem(key) || "null"); }
    catch { return null; }
  },
  set(key, val) { localStorage.setItem(key, JSON.stringify(val)); },
  remove(key)   { localStorage.removeItem(key); },
  clear() {
    ["adminSession", "customerSession", "currentStaff", "checkoutCart"]
      .forEach(k => localStorage.removeItem(k));
  },
};

// ══════════════════════════════════════════════════════
//  Cart (giữ nguyên logic cũ)
// ══════════════════════════════════════════════════════
export const cart = {
  getKey(user) { return user ? `cart_${user.id || user.email}` : "cart"; },
  get(user) {
    try { return JSON.parse(localStorage.getItem(cart.getKey(user)) || "[]"); }
    catch { return []; }
  },
  set(user, val) {
    localStorage.setItem(cart.getKey(user), JSON.stringify(val));
    localStorage.setItem("cart", JSON.stringify(val));
  },
  remove(user) {
    localStorage.removeItem(cart.getKey(user));
    localStorage.removeItem("cart");
  },
};
