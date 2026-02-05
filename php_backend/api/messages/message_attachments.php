<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../../config/config.php';

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
            id,
            message_id,
            file_name,
            file_path,
            file_type,
            file_size,
            created_at
        FROM message_attachments
        WHERE message_id = ?
        ORDER BY created_at ASC
    ");
    
    $stmt->execute([$messageId]);
    $attachments = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách tệp đính kèm thành công',
        'data' => $attachments
    ]);
}

function handlePost($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['messageId']) || !isset($input['fileName']) || 
        !isset($input['filePath']) || !isset($input['fileType']) || 
        !isset($input['fileSize'])) {
        echo json_encode(['success' => false, 'message' => 'Thiếu thông tin bắt buộc']);
        exit();
    }
    
    $messageId = $input['messageId'];
    $fileName = $input['fileName'];
    $filePath = $input['filePath'];
    $fileType = $input['fileType'];
    $fileSize = $input['fileSize'];
    
    $stmt = $pdo->prepare("
        INSERT INTO message_attachments (message_id, file_name, file_path, file_type, file_size)
        VALUES (?, ?, ?, ?, ?)
    ");
    
    $stmt->execute([$messageId, $fileName, $filePath, $fileType, $fileSize]);
    $attachmentId = $pdo->lastInsertId();
    
    $stmt = $pdo->prepare("
        SELECT * FROM message_attachments WHERE id = ?
    ");
    $stmt->execute([$attachmentId]);
    $attachment = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'message' => 'Thêm tệp đính kèm thành công',
        'data' => $attachment
    ]);
}

function handleDelete($pdo) {
    if (!isset($_GET['id'])) {
        echo json_encode(['success' => false, 'message' => 'Attachment ID là bắt buộc']);
        exit();
    }
    
    $attachmentId = $_GET['id'];
    
    $stmt = $pdo->prepare("SELECT file_path FROM message_attachments WHERE id = ?");
    $stmt->execute([$attachmentId]);
    $attachment = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$attachment) {
        echo json_encode(['success' => false, 'message' => 'Tệp đính kèm không tồn tại']);
        exit();
    }
    
    $stmt = $pdo->prepare("DELETE FROM message_attachments WHERE id = ?");
    $stmt->execute([$attachmentId]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Xóa tệp đính kèm thành công'
    ]);
}
?>
