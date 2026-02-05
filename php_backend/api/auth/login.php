<?php
require_once __DIR__ . '/../../config/config.php';

// Nhận dữ liệu JSON từ request
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!isset($data['email']) || !isset($data['password'])) {
    sendResponse(false, 'Email hoặc số điện thoại và mật khẩu là bắt buộc');
}

$emailOrPhone = trim($data['email']);
$password = $data['password'];

// Kiểm tra xem là email hay số điện thoại
$isEmail = filter_var($emailOrPhone, FILTER_VALIDATE_EMAIL);
$isPhone = preg_match('/^[0-9]{10,11}$/', $emailOrPhone);

if (!$isEmail && !$isPhone) {
    sendResponse(false, 'Email hoặc số điện thoại không hợp lệ');
}

$conn = getDBConnection();

// Tìm user theo email hoặc phone
if ($isEmail) {
    $stmt = $conn->prepare("SELECT id, full_name, email, phone, password FROM users WHERE email = ?");
} else {
    $stmt = $conn->prepare("SELECT id, full_name, email, phone, password FROM users WHERE phone = ?");
}

$stmt->bind_param("s", $emailOrPhone);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    sendResponse(false, 'Email/số điện thoại hoặc mật khẩu không đúng');
}

$user = $result->fetch_assoc();

// Kiểm tra mật khẩu
if (!password_verify($password, $user['password'])) {
    sendResponse(false, 'Email/số điện thoại hoặc mật khẩu không đúng');
}

// Đăng nhập thành công
$userData = [
    'id' => $user['id'],
    'fullName' => $user['full_name'],
    'email' => $user['email'],
    'phone' => $user['phone']
];

sendResponse(true, 'Đăng nhập thành công', $userData);

$stmt->close();
$conn->close();
?>
