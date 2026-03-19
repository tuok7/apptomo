<?php
/**
 * Database Configuration for Group Assignment App
 * Kết nối với database đã tạo bằng complete_database.sql
 */

class Database {
    // Database credentials - cập nhật theo cấu hình XAMPP của bạn
    private $host = "localhost";
    private $port = 3307;           // Port MySQL XAMPP (3307)
    private $db_name = "myapp_db";  // Tên database đã tạo
    private $username = "root";     // Username MySQL (mặc định XAMPP)
    private $password = "";         // Password MySQL (mặc định XAMPP để trống)
    private $charset = "utf8mb4";
    
    public $conn;
    
    /**
     * Kết nối database
     */
    public function getConnection() {
        $this->conn = null;
        
        try {
            $dsn = "mysql:host=" . $this->host . ";port=" . $this->port . ";dbname=" . $this->db_name . ";charset=" . $this->charset;
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ];
            
            $this->conn = new PDO($dsn, $this->username, $this->password, $options);
            
            // Set timezone
            $this->conn->exec("SET time_zone = '+00:00'");
            
        } catch(PDOException $exception) {
            echo "Connection error: " . $exception->getMessage();
            error_log("Database connection failed: " . $exception->getMessage());
        }
        
        return $this->conn;
    }
    
    /**
     * Kiểm tra kết nối database
     */
    public function testConnection() {
        $conn = $this->getConnection();
        if ($conn) {
            try {
                $stmt = $conn->query("SELECT 1");
                return [
                    'success' => true,
                    'message' => 'Database connection successful',
                    'database' => $this->db_name,
                    'host' => $this->host
                ];
            } catch(PDOException $e) {
                return [
                    'success' => false,
                    'message' => 'Database query failed: ' . $e->getMessage()
                ];
            }
        } else {
            return [
                'success' => false,
                'message' => 'Failed to connect to database'
            ];
        }
    }
    
    /**
     * Lấy thông tin database
     */
    public function getDatabaseInfo() {
        $conn = $this->getConnection();
        if ($conn) {
            try {
                // Lấy danh sách bảng
                $stmt = $conn->query("SHOW TABLES");
                $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
                
                // Lấy số lượng users
                $stmt = $conn->query("SELECT COUNT(*) as count FROM users");
                $userCount = $stmt->fetch()['count'];
                
                // Lấy số lượng groups
                $stmt = $conn->query("SELECT COUNT(*) as count FROM groups");
                $groupCount = $stmt->fetch()['count'];
                
                return [
                    'success' => true,
                    'database' => $this->db_name,
                    'tables' => $tables,
                    'stats' => [
                        'users' => $userCount,
                        'groups' => $groupCount,
                        'total_tables' => count($tables)
                    ]
                ];
            } catch(PDOException $e) {
                return [
                    'success' => false,
                    'message' => 'Failed to get database info: ' . $e->getMessage()
                ];
            }
        }
        
        return [
            'success' => false,
            'message' => 'No database connection'
        ];
    }
}

// Test connection khi file được include
if (basename($_SERVER['PHP_SELF']) == basename(__FILE__)) {
    header('Content-Type: application/json');
    $database = new Database();
    $result = $database->testConnection();
    echo json_encode($result, JSON_PRETTY_PRINT);
}
?>