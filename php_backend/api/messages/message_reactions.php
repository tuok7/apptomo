<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../../config/config.php';

// Handle preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$method = $_SERVER['REQUEST_METHOD'];

try {
    switch ($method) {
        case 'GET':
            handleGet($pdo);
            break;
        case 'POST':
            handlePost($pdo);
            break;
        case 'DELETE':
            handleDelete($pdo);
            break;
        default:
            http_response_code(405);
            echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    }
} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Lỗi hệ thống']);
}

function handleGet($pdo) {
    if (!isset($_GET['messageId'])) {
        echo json_encode(['success' => false, 'message' => 'Message ID là bắt buộc']);
        exit();
    }
    
    $messageId = $_GET['messageId'];
    
    $stmt = $pdo->prepare("
        SELECT 
            mr.id,
            mr.message_id,
            mr.user_id,
            mr.reaction,
            mr.created_at,
            u.full_name as user_name
        FROM message_reactions mr
        JOIN users u ON mr.user_id = u.id
        WHERE mr.message_id = ?
        ORDER BY mr.created_at ASC
    ");
    
    $stmt->execute([$messageId]);
    $reactions = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Group reactions by type
    $groupedReactions = [];
    foreach ($reactions as $reaction) {
        $type = $reaction['reaction'];
        if (!isset($groupedReactions[$type])) {
            $groupedReactions[$type] = [
                'reaction' => $type,
                'count' => 0,
                'users' => []
            ];
        }
        $groupedReactions[$type]['count']++;
        $groupedReactions[$type]['users'][] = [
            'userId' => $reaction['user_id'],
            'userName' => $reaction['user_name']
        ];
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách reactions thành công',
        'data' => array_values($groupedReactions)
    ]);
}

function handlePost($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['messageId']) || !isset($input['userId']) || !isset($input['reaction'])) {
        echo json_encode(['success' => false, 'message' => 'Thiếu thông tin bắt buộc']);
        exit();
    }
    
    $messageId = $input['messageId'];
    $userId = $input['userId'];
    $reaction = trim($input['reaction']);
    
    // Allowed reactions
    $allowedReactions = ['like', 'love', 'haha', 'wow', 'sad', 'angry'];
    if (!in_array($reaction, $allowedReactions)) {
        echo json_encode(['success' => false, 'message' => 'Reaction không hợp lệ']);
        exit();
    }
    
    // Check if message exists
    $stmt = $pdo->prepare("SELECT id FROM group_chat WHERE id = ?");
    $stmt->execute([$messageId]);
    if (!$stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Tin nhắn không tồn tại']);
        exit();
    }
    
    // Check if user already reacted with this reaction
    $stmt = $pdo->prepare("
        SELECT id FROM message_reactions 
        WHERE message_id = ? AND user_id = ? AND reaction = ?
    ");
    $stmt->execute([$messageId, $userId, $reaction]);
    
    if ($stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Bạn đã react với tin nhắn này rồi']);
        exit();
    }
    
    // Add reaction
    $stmt = $pdo->prepare("
        INSERT INTO message_reactions (message_id, user_id, reaction)
        VALUES (?, ?, ?)
    ");
    
    $stmt->execute([$messageId, $userId, $reaction]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Thêm reaction thành công'
    ]);
}

function handleDelete($pdo) {
    if (!isset($_GET['messageId']) || !isset($_GET['userId'])) {
        echo json_encode(['success' => false, 'message' => 'Message ID và User ID là bắt buộc']);
        exit();
    }
    
    $messageId = $_GET['messageId'];
    $userId = $_GET['userId'];
    $reaction = isset($_GET['reaction']) ? $_GET['reaction'] : null;
    
    if ($reaction) {
        // Remove specific reaction
        $stmt = $pdo->prepare("
            DELETE FROM message_reactions 
            WHERE message_id = ? AND user_id = ? AND reaction = ?
        ");
        $stmt->execute([$messageId, $userId, $reaction]);
    } else {
        // Remove all reactions from this user on this message
        $stmt = $pdo->prepare("
            DELETE FROM message_reactions 
            WHERE message_id = ? AND user_id = ?
        ");
        $stmt->execute([$messageId, $userId]);
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Xóa reaction thành công'
    ]);
}
?>
