-- Thêm cột phone vào bảng users nếu chưa có
USE myapp_db;

-- Kiểm tra và thêm cột phone
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS phone VARCHAR(20) UNIQUE AFTER email,
ADD INDEX IF NOT EXISTS idx_phone (phone);

-- Cho phép email có thể NULL (vì có thể đăng ký bằng SĐT)
ALTER TABLE users 
MODIFY COLUMN email VARCHAR(255) UNIQUE NULL;

-- Cập nhật bảng password_reset_codes để hỗ trợ cả email và phone
ALTER TABLE password_reset_codes 
MODIFY COLUMN email VARCHAR(255) NOT NULL COMMENT 'Email hoặc số điện thoại';
