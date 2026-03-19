<?php
echo "<h1>Tạo Database và Bảng</h1>";

try {
    // Kết nối MySQL (không chọn database)
    $conn = new mysqli('localhost', 'root', '', '', 3307);
    
    if ($conn->connect_error) {
        die("❌ Kết nối thất bại: " . $conn->connect_error);
    }
    
    echo "✅ Kết nối MySQL thành công!<br>";
    
    // Tạo database
    $sql = "CREATE DATABASE IF NOT EXISTS myapp_db";
    if ($conn->query($sql) === TRUE) {
        echo "✅ Database myapp_db đã được tạo<br>";
    } else {
        echo "❌ Lỗi tạo database: " . $conn->error . "<br>";
    }
    
    // Chọn database
    $conn->select_db('myapp_db');
    
    // Tạo bảng users
    $sql = "CREATE TABLE IF NOT EXISTS users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        fullName VARCHAR(255) NOT NULL,
        email VARCHAR(255) UNIQUE NOT NULL,
        phone VARCHAR(20),
        password VARCHAR(255) NOT NULL,
        createdAt BIGINT DEFAULT 0,
        updatedAt BIGINT DEFAULT 0,
        lastLoginAt BIGINT DEFAULT NULL
    )";
    
    if ($conn->query($sql) === TRUE) {
        echo "✅ Bảng users đã được tạo<br>";
    } else {
        echo "❌ Lỗi tạo bảng: " . $conn->error . "<br>";
    }
    
    // Tạo bảng password_reset_codes
    $sql = "CREATE TABLE IF NOT EXISTS password_reset_codes (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        email VARCHAR(255),
        phone VARCHAR(20),
        code VARCHAR(10) NOT NULL,
        expiresAt BIGINT NOT NULL,
        isUsed TINYINT DEFAULT 0,
        createdAt BIGINT DEFAULT 0
    )";
    
    if ($conn->query($sql) === TRUE) {
        echo "✅ Bảng password_reset_codes đã được tạo<br>";
    } else {
        echo "❌ Lỗi tạo bảng: " . $conn->error . "<br>";
    }
    
    // Thêm user test
    $email = 'tranthibinh@email.com';
    $password = password_hash('password', PASSWORD_DEFAULT);
    $currentTime = time() * 1000;
    
    $sql = "INSERT IGNORE INTO users (fullName, email, phone, password, createdAt, updatedAt) 
            VALUES ('Trần Thị Bình', '$email', '0986358888', '$password', $currentTime, $currentTime)";
    
    if ($conn->query($sql) === TRUE) {
        echo "✅ User test đã được thêm<br>";
        echo "<strong>Thông tin đăng nhập:</strong><br>";
        echo "Email: tranthibinh@email.com<br>";
        echo "Password: password<br>";
    } else {
        echo "⚠️ User có thể đã tồn tại: " . $conn->error . "<br>";
    }
    
    $conn->close();
    
    echo "<br><h2>✅ Hoàn thành!</h2>";
    echo "<a href='test_api.php'>Test API ngay</a>";
    
} catch (Exception $e) {
    echo "❌ Lỗi: " . $e->getMessage();
}
?>