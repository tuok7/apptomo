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

// Nhận dữ liệu JSON từ request
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!isset($data['fullName']) || !isset($data['email']) || !isset($data['phone']) || !isset($data['password'])) {
    echo json_encode(['success' => false, 'message' => 'Vui lòng điền đầy đủ thông tin']);
    exit();
}

$fullName = trim($data['fullName']);
$email = trim($data['email']);
$phone = trim($data['phone']);
$password = $data['password'];

// Validate
if (empty($fullName)) {
    echo json_encode(['success' => false, 'message' => 'Họ tên không được để trống']);
    exit();
}

if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Email không hợp lệ']);
    exit();
}

if (empty($phone) || !preg_match('/^[0-9]{10,11}$/', $phone)) {
    echo json_encode(['success' => false, 'message' => 'Số điện thoại không hợp lệ (10-11 số)']);
    exit();
}

if (strlen($password) < 6) {
    echo json_encode(['success' => false, 'message' => 'Mật khẩu phải có ít nhất 6 ký tự']);
    exit();
}

try {
    $conn = getDBConnection();

    // Kiểm tra email đã tồn tại chưa
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(['success' => false, 'message' => 'Email đã được sử dụng']);
        exit();
    }

    // Kiểm tra phone đã tồn tại chưa
    $stmt = $conn->prepare("SELECT id FROM users WHERE phone = ?");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(['success' => false, 'message' => 'Số điện thoại đã được sử dụng']);
        exit();
    }

    // Hash mật khẩu
    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
    $currentTime = time() * 1000; // Convert to milliseconds

    // Thêm user mới
    $stmt = $conn->prepare("INSERT INTO users (fullName, email, phone, password, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("ssssii", $fullName, $email, $phone, $hashedPassword, $currentTime, $currentTime);

    if ($stmt->execute()) {
        $userId = $conn->insert_id;
        
        $userData = [
            'id' => $userId,
            'fullName' => $fullName,
            'email' => $email,
            'phone' => $phone
        ];
        
        echo json_encode([
            'success' => true,
            'message' => 'Đăng ký thành công! Bạn có thể đăng nhập bằng email hoặc số điện thoại.',
            'data' => $userData
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Đăng ký thất bại: ' . $conn->error]);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Lỗi server: ' . $e->getMessage()]);
}
?>