# TranThai Shop – Hướng dẫn cài đặt với SQL Server

## Yêu cầu
- Java 17+
- Maven 3.8+
- SQL Server 2019+ (hoặc Azure SQL)

## Bước 1: Tạo Database SQL Server
Mở SQL Server Management Studio (SSMS), kết nối server rồi chạy file `TranThaiDB.sql`.

## Bước 2: Cấu hình kết nối
Mở `springboot-backend/src/main/resources/application.properties` và sửa:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TranThaiDB;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourPassword123   ← đổi thành mật khẩu của bạn
```

## Bước 3: Chạy ứng dụng
```bash
cd springboot-backend
mvn spring-boot:run
```

## Tài khoản mặc định
| Vai trò | Email | Mật khẩu | URL truy cập |
|---------|-------|----------|--------------|
| Admin   | admin@tranthai.vn | admin123 | http://localhost:8080/admin/login |
| Nhân viên | staff@tranthai.vn | staff123 | http://localhost:8080/staff/login |
| Khách hàng | Đăng ký mới | — | http://localhost:8080/shop/login |

## Điểm nổi bật sau khi cập nhật
- ✅ **Không còn localStorage** – tất cả dữ liệu lưu trên SQL Server
- ✅ **Giỏ hàng lưu server** – đăng nhập trình duyệt nào cũng thấy giỏ hàng
- ✅ **Session dùng sessionStorage** – token JWT (không bao giờ persist vĩnh viễn)
- ✅ **SQL Server** – production-ready database
- ✅ **CartItem API** – CRUD giỏ hàng đầy đủ
