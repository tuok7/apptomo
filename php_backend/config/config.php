<?php
// Cấu hình database
define('DB_HOST', 'localhost');
define('DB_PORT', 3307);
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'myapp_db');

// Hàm kết nối database với MySQLi
function getDBConnection() {
    try {
        $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME, DB_PORT);
        
        if ($conn->connect_error) {
            throw new Exception("Connection failed: " . $conn->connect_error);
        }
        
        $conn->set_charset('utf8mb4');
        return $conn;
    } catch (Exception $e) {
        error_log("Database connection error: " . $e->getMessage());
        return false;
    }
}

// Hàm trả về JSON response
function sendResponse($success, $message, $data = null) {
    header('Content-Type: application/json');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');
    
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ]);
    exit;
}

// Test connection khi truy cập trực tiếp
if (basename($_SERVER['PHP_SELF']) == 'config.php') {
    header('Content-Type: application/json');
    
    try {
        $conn = getDBConnection();
        if ($conn) {
            $result = $conn->query("SELECT COUNT(*) as count FROM users");
            if ($result) {
                $row = $result->fetch_assoc();
                echo json_encode([
                    'success' => true,
                    'message' => 'Database connection successful',
                    'users_count' => (int)$row['count']
                ]);
            } else {
                echo json_encode([
                    'success' => false,
                    'message' => 'Query failed: ' . $conn->error
                ]);
            }
            $conn->close();
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'Database connection failed - MySQL may not be running'
            ]);
        }
    } catch (Exception $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Error: ' . $e->getMessage()
        ]);
    }
}
?>
