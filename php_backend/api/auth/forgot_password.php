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
    $conn = getDBConnection();
    
    // Check if user exists
    if ($isEmail) {
        $stmt = $conn->prepare("SELECT id, fullName, email, phone FROM users WHERE email = ?");
    } else {
        $stmt = $conn->prepare("SELECT id, fullName, email, phone FROM users WHERE phone = ?");
    }
    $stmt->bind_param("s", $emailOrPhone);
    $stmt->execute();
    $result = $stmt->get_result();
    $user = $result->fetch_assoc();
    
    if (!$user) {
        echo json_encode(['success' => false, 'message' => 'Không tìm thấy tài khoản với thông tin này']);
        exit();
    }
    
    // Generate 6-digit code
    $code = sprintf('%06d', mt_rand(0, 999999));
    $expiresAt = (time() + 900) * 1000; // 15 minutes from now in milliseconds
    $createdAt = time() * 1000;
    
    // Save reset code to database
    $stmt = $conn->prepare("INSERT INTO password_reset_codes (email, phone, code, expiresAt, createdAt) VALUES (?, ?, ?, ?, ?)");
    $email = $isEmail ? $emailOrPhone : null;
    $phone = $isPhone ? $emailOrPhone : null;
    $stmt->bind_param("sssii", $email, $phone, $code, $expiresAt, $createdAt);
    
    if ($stmt->execute()) {
        if ($isEmail) {
            // TODO: Send email with code
            // For now, return code in response for testing
            echo json_encode([
                'success' => true,
                'message' => 'Mã xác nhận đã được gửi đến email của bạn',
                'debug_code' => $code // Remove this in production
            ]);
        } else {
            // TODO: Send SMS with code
            // For now, return code in response for testing
            echo json_encode([
                'success' => true,
                'message' => 'Mã xác nhận đã được gửi đến số điện thoại của bạn',
                'debug_code' => $code // Remove this in production
            ]);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Không thể tạo mã xác nhận']);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Lỗi server: ' . $e->getMessage()]);
}
?>