<?php
require_once __DIR__ . '/../../config/config.php';

// Nhận dữ liệu JSON từ request
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!isset($data['fullName']) || !isset($data['email']) || !isset($data['password'])) {
    sendResponse(false, 'Vui lòng điền đầy đủ thông tin');
}

$fullName = trim($data['fullName']);
$emailOrPhone = trim($data['email']); // Có thể là email hoặc SĐT
$password = $data['password'];

// Validate
if (empty($fullName)) {
    sendResponse(false, 'Họ tên không được để trống');
}

// Kiểm tra xem là email hay số điện thoại
$isEmail = filter_var($emailOrPhone, FILTER_VALIDATE_EMAIL);
$isPhone = preg_match('/^[0-9]{10,11}$/', $emailOrPhone);

if (!$isEmail && !$isPhone) {
    sendResponse(false, 'Email hoặc số điện thoại không hợp lệ');
}

if (strlen($password) < 6) {
    sendResponse(false, 'Mật khẩu phải có ít nhất 6 ký tự');
}

$conn = getDBConnection();

// Kiểm tra email/phone đã tồn tại chưa
if ($isEmail) {
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $emailOrPhone);
} else {
    $stmt = $conn->prepare("SELECT id FROM users WHERE phone = ?");
    $stmt->bind_param("s", $emailOrPhone);
}

$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    sendResponse(false, $isEmail ? 'Email đã được sử dụng' : 'Số điện thoại đã được sử dụng');
}

// Hash mật khẩu
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Thêm user mới
if ($isEmail) {
    $stmt = $conn->prepare("INSERT INTO users (full_name, email, password, created_at) VALUES (?, ?, ?, NOW())");
    $stmt->bind_param("sss", $fullName, $emailOrPhone, $hashedPassword);
} else {
    $stmt = $conn->prepare("INSERT INTO users (full_name, phone, password, created_at) VALUES (?, ?, ?, NOW())");
    $stmt->bind_param("sss", $fullName, $emailOrPhone, $hashedPassword);
}

if ($stmt->execute()) {
    $userId = $conn->insert_id;
    
    $userData = [
        'id' => $userId,
        'fullName' => $fullName,
        'email' => $isEmail ? $emailOrPhone : null,
        'phone' => $isPhone ? $emailOrPhone : null
    ];
    
    sendResponse(true, 'Đăng ký thành công', $userData);
} else {
    sendResponse(false, 'Đăng ký thất bại: ' . $conn->error);
}

$stmt->close();
$conn->close();
?>
