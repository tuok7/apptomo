<?php
/**
 * FILE MẪU CẤU HÌNH - KHÔNG CHỨA THÔNG TIN NHẠY CẢM
 * 
 * Hướng dẫn sử dụng:
 * 1. Copy file này thành config.php
 * 2. Thay đổi các thông tin database theo môi trường của bạn
 * 3. File config.php sẽ KHÔNG được commit lên Git (đã thêm vào .gitignore)
 */

// Cấu hình database
define('DB_HOST', 'localhost');           // Thay đổi nếu cần
define('DB_USER', 'root');                // Thay đổi username
define('DB_PASS', '');                    // Thay đổi password
define('DB_NAME', 'myapp_db');            // Thay đổi tên database

// Cấu hình bảo mật
define('JWT_SECRET_KEY', 'your-secret-key-here');  // Thay đổi secret key
define('API_KEY', 'your-api-key-here');            // Thay đổi API key

// Cấu hình upload
define('UPLOAD_MAX_SIZE', 10 * 1024 * 1024);  // 10MB
define('ALLOWED_EXTENSIONS', ['jpg', 'jpeg', 'png', 'pdf', 'doc', 'docx', 'xls', 'xlsx']);

// Kết nối database với PDO
try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER,
        DB_PASS,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]
    );
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Kết nối database thất bại'
    ]);
    exit();
}

// Kết nối database với MySQLi (cho các file cũ)
$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Kết nối database thất bại'
    ]);
    exit();
}

$conn->set_charset('utf8mb4');

// Hàm trả về MySQLi connection
function getDBConnection() {
    global $conn;
    return $conn;
}

// Hàm trả về JSON response
function sendResponse($success, $message, $data = null) {
    header('Content-Type: application/json');
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ]);
    exit;
}
?>
