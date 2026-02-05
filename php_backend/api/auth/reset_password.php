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
    echo json_encode(['success' => false, 'message' => 'Email là bắt buộc']);
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

$email = trim($input['email']);
$code = trim($input['code']);
$newPassword = $input['newPassword'];

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Email không hợp lệ']);
    exit();
}

// Validate password length
if (strlen($newPassword) < 6) {
    echo json_encode(['success' => false, 'message' => 'Mật khẩu phải có ít nhất 6 ký tự']);
    exit();
}

try {
    // Check if user exists
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        echo json_encode(['success' => false, 'message' => 'Email không tồn tại trong hệ thống']);
        exit();
    }
    
    // Verify code
    $stmt = $pdo->prepare("
        SELECT id, expires_at, used 
        FROM password_reset_codes 
        WHERE email = ? AND code = ?
    ");
    $stmt->execute([$email, $code]);
    $resetCode = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$resetCode) {
        echo json_encode(['success' => false, 'message' => 'Mã xác nhận không đúng']);
        exit();
    }
    
    if ($resetCode['used'] == 1) {
        echo json_encode(['success' => false, 'message' => 'Mã xác nhận đã được sử dụng']);
        exit();
    }
    
    // Check if code is expired
    $expiresAt = strtotime($resetCode['expires_at']);
    $now = time();
    
    if ($now > $expiresAt) {
        echo json_encode(['success' => false, 'message' => 'Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.']);
        exit();
    }
    
    // Hash new password
    $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
    
    // Update user password
    $stmt = $pdo->prepare("UPDATE users SET password = ? WHERE id = ?");
    $stmt->execute([$hashedPassword, $user['id']]);
    
    // Mark code as used
    $stmt = $pdo->prepare("UPDATE password_reset_codes SET used = 1 WHERE id = ?");
    $stmt->execute([$resetCode['id']]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.'
    ]);
    
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Lỗi hệ thống. Vui lòng thử lại sau.']);
}
?>
