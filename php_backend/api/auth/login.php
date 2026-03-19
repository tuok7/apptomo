<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../../config/config.php';

// Handle preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

try {
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
        $stmt = $conn->prepare("SELECT id, fullName, email, phone, password FROM users WHERE email = ?");
    } else {
        $stmt = $conn->prepare("SELECT id, fullName, email, phone, password FROM users WHERE phone = ?");
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

    // Cập nhật lastLoginAt
    $currentTime = time() * 1000;
    $updateStmt = $conn->prepare("UPDATE users SET lastLoginAt = ? WHERE id = ?");
    $updateStmt->bind_param("ii", $currentTime, $user['id']);
    $updateStmt->execute();
    $updateStmt->close();

    // Đăng nhập thành công
    $userData = [
        'id' => (int)$user['id'],
        'fullName' => $user['fullName'],
        'email' => $user['email'],
        'phone' => $user['phone']
    ];

    sendResponse(true, 'Đăng nhập thành công', $userData);

} catch (Exception $e) {
    error_log("Login error: " . $e->getMessage());
    sendResponse(false, 'Lỗi server: ' . $e->getMessage());
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conn)) $conn->close();
}
?>
