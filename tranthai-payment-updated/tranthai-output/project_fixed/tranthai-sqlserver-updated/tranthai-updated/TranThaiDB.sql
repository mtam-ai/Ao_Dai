-- ============================================================
--  TranThai Shop – SQL Server Database Script
--  Chạy script này trong SQL Server Management Studio (SSMS)
--  hoặc Azure Data Studio để tạo database
-- ============================================================

-- 1. Tạo Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'TranThaiDB')
BEGIN
    CREATE DATABASE TranThaiDB
        COLLATE Vietnamese_CI_AS;
END
GO

USE TranThaiDB;
GO

-- 2. Bảng staffs (Nhân viên & Admin)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'staffs')
BEGIN
CREATE TABLE staffs (
                        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
                        name         NVARCHAR(255) NOT NULL,
                        email        NVARCHAR(255) NOT NULL UNIQUE,
                        password     NVARCHAR(255) NOT NULL,
                        phone        NVARCHAR(20),
                        role         NVARCHAR(20)  DEFAULT 'STAFF',   -- STAFF | ADMIN
                        status       NVARCHAR(20)  DEFAULT 'ACTIVE',  -- ACTIVE | INACTIVE
                        created_at   DATETIME2 DEFAULT GETDATE()
);
END
GO

-- 3. Bảng customer_accounts (Khách hàng)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_accounts')
BEGIN
CREATE TABLE customer_accounts (
                                   id          BIGINT IDENTITY(1,1) PRIMARY KEY,
                                   name        NVARCHAR(255) NOT NULL,
                                   email       NVARCHAR(255) NOT NULL UNIQUE,
                                   password    NVARCHAR(255) NOT NULL,
                                   phone       NVARCHAR(20),
                                   address     NVARCHAR(500),
                                   status      NVARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE | BANNED
                                   created_at  DATETIME2 DEFAULT GETDATE()
);
END
GO

-- 4. Bảng products (Sản phẩm)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
CREATE TABLE products (
                          id          BIGINT IDENTITY(1,1) PRIMARY KEY,
                          name        NVARCHAR(255) NOT NULL,
                          price       FLOAT,
                          description NVARCHAR(MAX),
                          stock       INT DEFAULT 0,
                          status      NVARCHAR(50)  DEFAULT N'Đang bán', -- Đang bán | Sắp hết | Ngừng bán
                          image       NVARCHAR(MAX),
                          category    NVARCHAR(100),                     -- Nam | Nữ
                          deleted     BIT DEFAULT 0,
                          deleted_at  DATETIME2,
                          created_at  DATETIME2 DEFAULT GETDATE()
);
END
GO

-- 5. Bảng orders (Đơn hàng)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
BEGIN
CREATE TABLE orders (
                        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
                        customer_id    BIGINT,
                        customer_name  NVARCHAR(255),
                        phone          NVARCHAR(20),
                        address        NVARCHAR(500),
                        items          NVARCHAR(MAX),   -- JSON array của sản phẩm
                        total          FLOAT,
                        payment        NVARCHAR(50) DEFAULT 'COD',
                        payment_status NVARCHAR(20) DEFAULT 'unpaid', -- unpaid | paid
                        transfer_code  NVARCHAR(50) NULL,                   -- mã đối soát SePay (AODAI{id})
                        status         NVARCHAR(50) DEFAULT 'pending',
    -- pending | confirmed | shipping | done | cancelled
                        note           NVARCHAR(MAX),
                        order_date     DATETIME2 DEFAULT GETDATE()
);
END
GO

-- 6. Bảng cart_items (Giỏ hàng – lưu server)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'cart_items')
BEGIN
CREATE TABLE cart_items (
                            id            BIGINT IDENTITY(1,1) PRIMARY KEY,
                            customer_id   BIGINT NOT NULL,
                            product_id    BIGINT NOT NULL,
                            product_name  NVARCHAR(255),
                            price         FLOAT,
                            qty           INT DEFAULT 1,
                            size          NVARCHAR(20),
                            image         NVARCHAR(MAX),
                            cart_key      NVARCHAR(100),
                            created_at    DATETIME2 DEFAULT GETDATE()
);
END
GO

-- 7. Bảng support_chats (Hỗ trợ khách hàng)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'support_chats')
BEGIN
CREATE TABLE support_chats (
                               id             BIGINT IDENTITY(1,1) PRIMARY KEY,
                               customer_id    BIGINT,
                               customer_name  NVARCHAR(255),
                               messages       NVARCHAR(MAX) DEFAULT '[]',  -- JSON array
                               status         NVARCHAR(20) DEFAULT 'open', -- open | closed
                               created_at     DATETIME2 DEFAULT GETDATE(),
                               updated_at     DATETIME2 DEFAULT GETDATE()
);
END
GO

-- ============================================================
--  DỮ LIỆU MẪU (Sample Data)
-- ============================================================

-- Admin mặc định (mật khẩu: admin123 đã được BCrypt hash)
IF NOT EXISTS (SELECT 1 FROM staffs WHERE email = 'admin@tranthai.vn')
BEGIN
INSERT INTO staffs (name, email, password, phone, role, status)
VALUES (
           N'Admin TranThai',
           'admin@tranthai.vn',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWq',  -- admin123
           '0901234567',
           'ADMIN',
           'ACTIVE'
       );
END
GO

-- Nhân viên mẫu (mật khẩu: staff123)
IF NOT EXISTS (SELECT 1 FROM staffs WHERE email = 'staff@tranthai.vn')
BEGIN
INSERT INTO staffs (name, email, password, phone, role, status)
VALUES (
           N'Nguyễn Thị Lan',
           'staff@tranthai.vn',
           '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3bw9HX5qg.LKK.R/Qh6A3H8K/sGLlK',  -- staff123
           '0912345678',
           'STAFF',
           'ACTIVE'
       );
END
GO

-- Sản phẩm mẫu
IF NOT EXISTS (SELECT 1 FROM products WHERE name = N'Áo dài thêu hoa mai')
BEGIN
INSERT INTO products (name, price, description, stock, status, image, category)
VALUES
    (
        N'Áo dài thêu hoa mai',
        850000,
        N'Áo dài truyền thống thêu hoa mai tinh tế, chất liệu lụa cao cấp. Phù hợp dịp Tết và các sự kiện trang trọng.',
        50,
        N'Đang bán',
        'https://images.unsplash.com/photo-1594938298603-c8148c4b0a8e?w=400',
        N'Nữ'
    ),
    (
        N'Áo dài gấm đỏ truyền thống',
        1200000,
        N'Áo dài gấm đỏ thêu rồng phượng, biểu tượng may mắn và thịnh vượng cho năm mới.',
        30,
        N'Đang bán',
        'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400',
        N'Nữ'
    ),
    (
        N'Áo dài nam cách tân',
        750000,
        N'Áo dài nam cách tân hiện đại, phù hợp với các buổi lễ trang trọng và sự kiện văn hóa.',
        40,
        N'Đang bán',
        'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400',
        N'Nam'
    ),
    (
        N'Áo dài trắng tinh khôi',
        680000,
        N'Áo dài trắng thanh lịch, tinh khôi – lựa chọn hoàn hảo cho nữ sinh và công sở.',
        25,
        N'Đang bán',
        'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=400',
        N'Nữ'
    ),
    (
        N'Áo dài vải lụa xanh ngọc',
        950000,
        N'Áo dài vải lụa màu xanh ngọc sang trọng, phù hợp với nhiều dịp lễ khác nhau.',
        15,
        N'Sắp hết',
        'https://images.unsplash.com/photo-1566616213894-2d4e1baee5d8?w=400',
        N'Nữ'
    ),
    (
        N'Áo dài nam đen lịch lãm',
        800000,
        N'Áo dài nam màu đen sang trọng, lịch lãm. Thích hợp cho các buổi tiệc tối và sự kiện quan trọng.',
        20,
        N'Đang bán',
        'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400',
        N'Nam'
    );
END
GO

-- ============================================================
--  HƯỚNG DẪN CẤU HÌNH Spring Boot
-- ============================================================
--
--  Mở file: springboot-backend/src/main/resources/application.properties
--
--  Thay thế các dòng datasource bằng:
--
--  spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TranThaiDB;encrypt=false;trustServerCertificate=true
--  spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
--  spring.datasource.username=sa
--  spring.datasource.password=YourPassword123   <-- đổi thành mật khẩu SQL Server của bạn
--  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
--  spring.jpa.hibernate.ddl-auto=update
--
--  Sau khi cấu hình xong, chạy: mvn spring-boot:run
--  Spring Boot sẽ tự động tạo/cập nhật các bảng theo Entity classes.
--
--  URL truy cập ứng dụng: http://localhost:8080
--  Admin: admin@tranthai.vn / admin123
--  Staff: staff@tranthai.vn / staff123
-- ============================================================

UPDATE staffs
SET password = '$2b$10$c9xpxk8m35W4OuvoI2uM9.ctT34/ejxat8CXYF.fXOSmDfhJeUv.K'
WHERE email = 'admin@tranthai.vn';

UPDATE staffs
SET password = '$2b$10$5zKtJCnpWjD3yN39.W1XkO0J9x2LTexWu9fyhqTOpCLvWNo5Wpbpm'
WHERE email = 'staff@tranthai.vn';