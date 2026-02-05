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

$emailOrPhone = trim($input['email']);

// Kiểm tra xem là email hay số điện thoại
$isEmail = filter_var($emailOrPhone, FILTER_VALIDATE_EMAIL);
$isPhone = preg_match('/^[0-9]{10,11}$/', $emailOrPhone);

if (!$isEmail && !$isPhone) {
    echo json_encode(['success' => false, 'message' => 'Email hoặc số điện thoại không hợp lệ']);
    exit();
}

try {
    // Check if user exists
    if ($isEmail) {
        $stmt = $pdo->prepare("SELECT id, full_name, email, phone FROM users WHERE email = ?");
    } else {
        $stmt = $pdo->prepare("SELECT id, full_name, email, phone FROM users WHERE phone = ?");
    }
    $stmt->execute([$emailOrPhone]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        $message = $isEmail ? 'Email không tồn tại trong hệ thống' : 'Số điện thoại không tồn tại trong hệ thống';
        echo json_encode(['success' => false, 'message' => $message]);
        exit();
    }
    
    // Generate 6-digit verification code
    $verificationCode = sprintf("%06d", mt_rand(0, 999999));
    
    // Store verification code in database (expires in 15 minutes)
    $expiresAt = date('Y-m-d H:i:s', strtotime('+15 minutes'));
    
    // Check if there's already a code for this user
    $stmt = $pdo->prepare("SELECT id FROM password_reset_codes WHERE email = ?");
    $stmt->execute([$emailOrPhone]);
    $existingCode = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($existingCode) {
        // Update existing code
        $stmt = $pdo->prepare("UPDATE password_reset_codes SET code = ?, expires_at = ?, used = 0 WHERE email = ?");
        $stmt->execute([$verificationCode, $expiresAt, $emailOrPhone]);
    } else {
        // Insert new code
        $stmt = $pdo->prepare("INSERT INTO password_reset_codes (email, code, expires_at) VALUES (?, ?, ?)");
        $stmt->execute([$emailOrPhone, $verificationCode, $expiresAt]);
    }
    
    // In production:
    // - If email: send via email (PHPMailer)
    // - If phone: send via SMS (Twilio, etc.)
    
    $message = $isEmail 
        ? 'Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.'
        : 'Mã xác nhận đã được gửi đến số điện thoại của bạn qua SMS.';
    
    echo json_encode([
        'success' => true,
        'message' => $message,
        'type' => $isEmail ? 'email' : 'phone',
        // For development only - remove in production
        'debug_code' => $verificationCode
    ]);
    
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Lỗi hệ thống. Vui lòng thử lại sau.']);
}
?>
