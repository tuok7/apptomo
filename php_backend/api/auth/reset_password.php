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

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['email']) || empty($input['email'])) {
    echo json_encode(['success' => false, 'message' => 'Email hoặc số điện thoại là bắt buộc']);
    exit();
}

if (!isset($input['code']) || empty($input['code'])) {
    echo json_encode(['success' => false, 'message' => 'Mã xác nhận là bắt buộc']);
    exit();
}

if (!isset($input['newPassword']) || empty($input['newPassword'])) {
    echo json_encode(['success' => false, 'message' => 'Mật khẩu mới là bắt buộc']);
    exit();
}

$emailOrPhone = trim($input['email']);
$code = trim($input['code']);
$newPassword = $input['newPassword'];

// Kiểm tra xem là email hay số điện thoại
$isEmail = filter_var($emailOrPhone, FILTER_VALIDATE_EMAIL);
$isPhone = preg_match('/^[0-9]{10,11}$/', $emailOrPhone);

if (!$isEmail && !$isPhone) {
    echo json_encode(['success' => false, 'message' => 'Email hoặc số điện thoại không hợp lệ']);
    exit();
}

// Validate password length
if (strlen($newPassword) < 6) {
    echo json_encode(['success' => false, 'message' => 'Mật khẩu mới phải có ít nhất 6 ký tự']);
    exit();
}

try {
    $conn = getDBConnection();
    $currentTime = time() * 1000;
    
    // Verify reset code
    if ($isEmail) {
        $stmt = $conn->prepare("SELECT id FROM password_reset_codes WHERE email = ? AND code = ? AND expiresAt > ? AND isUsed = 0");
    } else {
        $stmt = $conn->prepare("SELECT id FROM password_reset_codes WHERE phone = ? AND code = ? AND expiresAt > ? AND isUsed = 0");
    }
    $stmt->bind_param("ssi", $emailOrPhone, $code, $currentTime);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        echo json_encode(['success' => false, 'message' => 'Mã xác nhận không hợp lệ hoặc đã hết hạn']);
        exit();
    }
    
    $resetRecord = $result->fetch_assoc();
    
    // Mark code as used
    $stmt = $conn->prepare("UPDATE password_reset_codes SET isUsed = 1 WHERE id = ?");
    $stmt->bind_param("i", $resetRecord['id']);
    $stmt->execute();
    
    // Update user password
    $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
    
    if ($isEmail) {
        $stmt = $conn->prepare("UPDATE users SET password = ?, updatedAt = ? WHERE email = ?");
    } else {
        $stmt = $conn->prepare("UPDATE users SET password = ?, updatedAt = ? WHERE phone = ?");
    }
    $stmt->bind_param("sis", $hashedPassword, $currentTime, $emailOrPhone);
    
    if ($stmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => 'Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập với mật khẩu mới.'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Không thể cập nhật mật khẩu']);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Lỗi server: ' . $e->getMessage()]);
}
?>