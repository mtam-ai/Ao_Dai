# TranThai Backend – Spring Boot

## 📦 Cấu trúc dự án

```
src/main/java/com/tranthai/
├── TranThaiApplication.java
├── config/
│   ├── DataSeeder.java          # Tạo admin + demo data khi khởi động
│   ├── GlobalExceptionHandler.java
│   ├── JacksonConfig.java
│   └── SecurityConfig.java      # JWT + CORS + phân quyền
├── controller/
│   ├── AuthController.java
│   ├── CustomerController.java
│   ├── DashboardController.java
│   ├── OrderController.java
│   ├── ProductController.java
│   ├── StaffController.java
│   └── SupportController.java
├── dto/
│   ├── AuthDtos.java
│   ├── OrderDtos.java
│   ├── ProductDtos.java
│   ├── StaffDtos.java
│   └── SupportDtos.java
├── model/
│   ├── Customer.java
│   ├── CustomerAccount.java
│   ├── Order.java
│   ├── Product.java
│   ├── Staff.java
│   └── SupportChat.java
├── repository/          # JPA Repositories (1 file / entity)
├── security/
│   ├── JwtAuthFilter.java
│   └── JwtUtils.java
└── service/
    ├── AuthService.java
    ├── CustomerService.java
    ├── OrderService.java
    ├── ProductService.java
    ├── StaffService.java
    └── SupportService.java
```

---

## 🚀 Khởi động nhanh

### Yêu cầu
- Java 17+
- Maven 3.8+

### Chạy với H2 (dev – không cần cài database)
```bash
mvn spring-boot:run
```

Server sẽ chạy tại: **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console**

### Tài khoản mặc định (được tạo tự động)
| Loại | Email | Mật khẩu |
|---|---|---|
| Admin | admin@tranthai.vn | admin123 |
| Staff (demo) | staff1@tranthai.vn | staff123 |
| Khách (demo) | khach@gmail.com | khach123 |

---

## 🗄️ Chuyển sang MySQL (production)

1. Tạo database:
```sql
CREATE DATABASE tranthai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Trong `application.properties`, bỏ comment phần MySQL và comment phần H2:
```properties
# Comment dòng H2:
# spring.datasource.url=jdbc:h2:mem:...

# Bỏ comment MySQL:
spring.datasource.url=jdbc:mysql://localhost:3306/tranthai_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

## 🛣️ API Endpoints

### Auth (`/api/auth`)
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/admin/login` | Đăng nhập Admin |
| POST | `/api/auth/staff/login` | Đăng nhập Staff |
| POST | `/api/auth/customer/login` | Đăng nhập Customer |
| POST | `/api/auth/customer/register` | Đăng ký tài khoản |
| PUT | `/api/auth/customer/change-password` | Đổi mật khẩu |

### Products (`/api/products`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/products` | Public | Lấy sản phẩm đang bán |
| GET | `/api/products/all` | Admin/Staff | Bao gồm sản phẩm đã xóa |
| GET | `/api/products/{id}` | Public | Chi tiết sản phẩm |
| POST | `/api/products` | Admin/Staff | Tạo sản phẩm mới |
| PUT | `/api/products/{id}` | Admin/Staff | Cập nhật sản phẩm |
| DELETE | `/api/products/{id}` | Admin/Staff | Xóa mềm |
| PUT | `/api/products/{id}/restore` | Admin/Staff | Khôi phục |

### Orders (`/api/orders`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/orders` | Admin/Staff | Tất cả đơn hàng |
| GET | `/api/orders/{id}` | Auth | Chi tiết đơn |
| GET | `/api/orders/customer/{id}` | Auth | Đơn của khách |
| POST | `/api/orders` | Public | Đặt hàng (checkout) |
| PUT | `/api/orders/{id}/status` | Admin/Staff | Cập nhật trạng thái |

### Staffs (`/api/staffs`) – Chỉ Admin
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/staffs` | Danh sách nhân viên |
| POST | `/api/staffs` | Thêm nhân viên |
| PUT | `/api/staffs/{id}` | Sửa nhân viên |
| DELETE | `/api/staffs/{id}` | Xóa mềm nhân viên |

### Customers (`/api/customers`) – Admin/Staff
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/customers` | Danh sách khách hàng |
| PUT | `/api/customers/{id}/status` | Khóa / mở tài khoản |

### Support (`/api/support`)
| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/support` | Admin/Staff | Tất cả hội thoại |
| POST | `/api/support/chat` | Auth | Mở/lấy chat của khách |
| POST | `/api/support/{id}/messages` | Auth | Gửi tin nhắn |
| PUT | `/api/support/{id}/close` | Admin/Staff | Đóng hội thoại |

### Dashboard (`/api/dashboard`) – Admin/Staff
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/dashboard/stats` | Thống kê tổng quan |

---

## 🔑 Xác thực JWT

Mọi request cần auth phải gửi header:
```
Authorization: Bearer <token>
```

Token nhận được từ response đăng nhập (`/api/auth/*/login`).

---

## 🔄 Hướng dẫn migrate Vue Frontend

### Bước 1: Copy `api.js` vào `src/`
File `api.js` (trong `src/main/resources/`) là phiên bản mới thay thế `db.js`.

### Bước 2: Cập nhật import trong mỗi Vue file

**Trước (db.js):**
```js
import { dbGet, dbSet, session } from '../db'
```

**Sau (api.js):**
```js
import { products, orders, session } from '../api'
```

### Bước 3: Cập nhật logic từng tính năng

#### Admin Login (`AdminLogin.vue`)
```js
// Trước:
const admin = await dbGet("adminAccount")
if (admin.email === email && admin.password === password) { ... }

// Sau:
import { auth, session } from '../../api'
const res = await auth.adminLogin(email, password)
session.set("adminSession", res)  // res chứa { token, name, email, role }
```

#### Staff Login (`StaffLogin.vue`)
```js
// Sau:
const res = await auth.staffLogin(email, password)
session.set("currentStaff", res)
```

#### Customer Login (`Login.vue`)
```js
// Sau:
const res = await auth.customerLogin(email, password)
session.set("customerSession", res)
```

#### Đổi mật khẩu (`Login.vue` – phần forgot password)
```js
// Sau:
await auth.changePassword({ email, oldPassword, newPassword })
```

#### Lấy sản phẩm
```js
// Trước:
this.products = await dbGet("products") || []

// Sau:
this.products = await products.getAllAdmin()  // hoặc products.getAll() cho client
```

#### Tạo/sửa/xóa sản phẩm
```js
// Trước:
await dbSet("products", this.products)

// Sau:
await products.create(formData)          // thêm mới
await products.update(id, formData)      // sửa
await products.softDelete(id)            // xóa mềm
await products.restore(id)              // khôi phục
```

#### Checkout (`Checkout.vue`)
```js
// Trước:
const orders = await dbGet("orders") || []
orders.push({ id: Date.now(), customerName, ... })
await dbSet("orders", orders)

// Sau:
import { orders as ordersApi } from '../../api'
const newOrder = await ordersApi.create({
  customerId: this.currentUser?.id,
  customerName: this.customer.name,
  phone: this.customer.phone,
  address: this.customer.address,
  items: this.cart,
  total: this.total + this.shipping,
  payment: this.payment,
})
```

#### Cập nhật trạng thái đơn hàng
```js
// Sau:
await ordersApi.updateStatus(orderId, "shipping")
```

#### Quản lý nhân viên (`Staff.vue`)
```js
// Sau:
import { staffApi } from '../api'
staffs.value = await staffApi.getAll()
await staffApi.create({ name, email, password, phone, role })
await staffApi.update(id, formData)
await staffApi.delete(id)
```

#### Dashboard stats
```js
// Sau:
import { dashboard } from '../api'
const stats = await dashboard.getStats()
// stats: { totalOrders, pendingOrders, totalRevenue, totalProducts, totalCustomers, ... }
```

---

## 🔒 Lưu ý bảo mật khi deploy

1. **Thay JWT secret**: Đổi `app.jwt.secret` trong `application.properties` thành chuỗi ngẫu nhiên dài 64+ ký tự
2. **Đổi mật khẩu admin**: Sau khi chạy lần đầu, vào H2/MySQL đổi ngay mật khẩu admin
3. **CORS**: Cập nhật `app.cors.allowed-origins` thành domain thực của frontend
4. **HTTPS**: Luôn dùng HTTPS ở môi trường production
