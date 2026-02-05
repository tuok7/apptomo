<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once __DIR__ . '/../../config/config.php';

$method = $_SERVER['REQUEST_METHOD'];

try {
    switch ($method) {
        case 'GET':
            getMessages($conn);
            break;
        case 'POST':
            sendMessage($conn);
            break;
        case 'DELETE':
            deleteMessage($conn);
            break;
        default:
            http_response_code(405);
            echo json_encode(['success' => false, 'message' => 'Method not allowed']);
            break;
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}

$conn->close();

// Lấy danh sách tin nhắn của nhóm
function getMessages($conn) {
    if (!isset($_GET['groupId'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Group ID is required']);
        return;
    }
    
    $groupId = intval($_GET['groupId']);
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 50;
    $offset = isset($_GET['offset']) ? intval($_GET['offset']) : 0;
    
    $sql = "SELECT 
                gc.id,
                gc.group_id,
                gc.user_id,
                gc.message,
                gc.created_at,
                u.full_name as sender_name,
                u.email as sender_email
            FROM group_chat gc
            INNER JOIN users u ON gc.user_id = u.id
            WHERE gc.group_id = ?
            ORDER BY gc.created_at DESC
            LIMIT ? OFFSET ?";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("iii", $groupId, $limit, $offset);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $messages = [];
    while ($row = $result->fetch_assoc()) {
        $messages[] = [
            'id' => (int)$row['id'],
            'groupId' => (int)$row['group_id'],
            'userId' => (int)$row['user_id'],
            'message' => $row['message'],
            'senderName' => $row['sender_name'],
            'senderEmail' => $row['sender_email'],
            'createdAt' => $row['created_at']
        ];
    }
    
    // Đảo ngược mảng để tin nhắn cũ nhất ở đầu
    $messages = array_reverse($messages);
    
    echo json_encode([
        'success' => true,
        'message' => 'Messages retrieved successfully',
        'data' => $messages
    ]);
}

// Gửi tin nhắn mới
function sendMessage($conn) {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($data['groupId']) || !isset($data['userId']) || !isset($data['message'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Group ID, User ID, and message are required']);
        return;
    }
    
    $groupId = intval($data['groupId']);
    $userId = intval($data['userId']);
    $message = trim($data['message']);
    
    if (empty($message)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Message cannot be empty']);
        return;
    }
    
    // Kiểm tra user có trong nhóm không
    $checkSql = "SELECT id FROM group_members WHERE group_id = ? AND user_id = ?";
    $checkStmt = $conn->prepare($checkSql);
    $checkStmt->bind_param("ii", $groupId, $userId);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();
    
    if ($checkResult->num_rows === 0) {
        http_response_code(403);
        echo json_encode(['success' => false, 'message' => 'User is not a member of this group']);
        return;
    }
    
    // Thêm tin nhắn
    $sql = "INSERT INTO group_chat (group_id, user_id, message) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("iis", $groupId, $userId, $message);
    
    if ($stmt->execute()) {
        $messageId = $conn->insert_id;
        
        // Lấy thông tin tin nhắn vừa tạo
        $getSql = "SELECT 
                    gc.id,
                    gc.group_id,
                    gc.user_id,
                    gc.message,
                    gc.created_at,
                    u.full_name as sender_name,
                    u.email as sender_email
                FROM group_chat gc
                INNER JOIN users u ON gc.user_id = u.id
                WHERE gc.id = ?";
        
        $getStmt = $conn->prepare($getSql);
        $getStmt->bind_param("i", $messageId);
        $getStmt->execute();
        $result = $getStmt->get_result();
        $row = $result->fetch_assoc();
        
        $messageData = [
            'id' => (int)$row['id'],
            'groupId' => (int)$row['group_id'],
            'userId' => (int)$row['user_id'],
            'message' => $row['message'],
            'senderName' => $row['sender_name'],
            'senderEmail' => $row['sender_email'],
            'createdAt' => $row['created_at']
        ];
        
        echo json_encode([
            'success' => true,
            'message' => 'Message sent successfully',
            'data' => $messageData
        ]);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Failed to send message']);
    }
}

// Xóa tin nhắn
function deleteMessage($conn) {
    if (!isset($_GET['id']) || !isset($_GET['userId'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Message ID and User ID are required']);
        return;
    }
    
    $messageId = intval($_GET['id']);
    $userId = intval($_GET['userId']);
    
    // Kiểm tra tin nhắn có thuộc về user không
    $checkSql = "SELECT id FROM group_chat WHERE id = ? AND user_id = ?";
    $checkStmt = $conn->prepare($checkSql);
    $checkStmt->bind_param("ii", $messageId, $userId);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();
    
    if ($checkResult->num_rows === 0) {
        http_response_code(403);
        echo json_encode(['success' => false, 'message' => 'You can only delete your own messages']);
        return;
    }
    
    // Xóa tin nhắn
    $sql = "DELETE FROM group_chat WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $messageId);
    
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Message deleted successfully']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Failed to delete message']);
    }
}
?>
