<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
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
        case 'PUT':
            handlePut($pdo);
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
    if (!isset($_GET['groupId'])) {
        echo json_encode(['success' => false, 'message' => 'Group ID là bắt buộc']);
        exit();
    }
    
    $groupId = $_GET['groupId'];
    $fileType = isset($_GET['fileType']) ? $_GET['fileType'] : null;
    
    $sql = "SELECT 
                d.id,
                d.group_id,
                d.title,
                d.description,
                d.file_name,
                d.file_path,
                d.file_type,
                d.file_size,
                d.download_count,
                d.created_at,
                d.updated_at,
                u.id as uploader_id,
                u.full_name as uploader_name,
                u.email as uploader_email
            FROM group_documents d
            JOIN users u ON d.uploaded_by = u.id
            WHERE d.group_id = ?";
    
    $params = [$groupId];
    
    if ($fileType) {
        $sql .= " AND d.file_type LIKE ?";
        $params[] = "%$fileType%";
    }
    
    $sql .= " ORDER BY d.created_at DESC";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $documents = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách tài liệu thành công',
        'data' => $documents
    ]);
}

function handlePost($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['groupId']) || !isset($input['title']) || !isset($input['fileName']) || 
        !isset($input['filePath']) || !isset($input['fileType']) || !isset($input['fileSize']) || 
        !isset($input['uploadedBy'])) {
        echo json_encode(['success' => false, 'message' => 'Thiếu thông tin bắt buộc']);
        exit();
    }
    
    $groupId = $input['groupId'];
    $title = trim($input['title']);
    $description = isset($input['description']) ? trim($input['description']) : '';
    $fileName = $input['fileName'];
    $filePath = $input['filePath'];
    $fileType = $input['fileType'];
    $fileSize = $input['fileSize'];
    $uploadedBy = $input['uploadedBy'];
    
    // Check if user is member of group
    $stmt = $pdo->prepare("SELECT id FROM group_members WHERE group_id = ? AND user_id = ?");
    $stmt->execute([$groupId, $uploadedBy]);
    if (!$stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Bạn không phải thành viên của nhóm này']);
        exit();
    }
    
    $stmt = $pdo->prepare("
        INSERT INTO group_documents (group_id, title, description, file_name, file_path, file_type, file_size, uploaded_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ");
    
    $stmt->execute([$groupId, $title, $description, $fileName, $filePath, $fileType, $fileSize, $uploadedBy]);
    $documentId = $pdo->lastInsertId();
    
    // Get the created document
    $stmt = $pdo->prepare("
        SELECT 
            d.id,
            d.group_id,
            d.title,
            d.description,
            d.file_name,
            d.file_path,
            d.file_type,
            d.file_size,
            d.download_count,
            d.created_at,
            u.full_name as uploader_name,
            u.email as uploader_email
        FROM group_documents d
        JOIN users u ON d.uploaded_by = u.id
        WHERE d.id = ?
    ");
    $stmt->execute([$documentId]);
    $document = $stmt->fetch(PDO::FETCH_ASSOC);
    
    echo json_encode([
        'success' => true,
        'message' => 'Tải lên tài liệu thành công',
        'data' => $document
    ]);
}

function handlePut($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['id'])) {
        echo json_encode(['success' => false, 'message' => 'Document ID là bắt buộc']);
        exit();
    }
    
    $documentId = $input['id'];
    
    // Check if document exists
    $stmt = $pdo->prepare("SELECT id, uploaded_by FROM group_documents WHERE id = ?");
    $stmt->execute([$documentId]);
    $document = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$document) {
        echo json_encode(['success' => false, 'message' => 'Tài liệu không tồn tại']);
        exit();
    }
    
    // Build update query
    $updates = [];
    $params = [];
    
    if (isset($input['title'])) {
        $updates[] = "title = ?";
        $params[] = trim($input['title']);
    }
    
    if (isset($input['description'])) {
        $updates[] = "description = ?";
        $params[] = trim($input['description']);
    }
    
    if (isset($input['incrementDownload']) && $input['incrementDownload'] === true) {
        $updates[] = "download_count = download_count + 1";
    }
    
    if (empty($updates)) {
        echo json_encode(['success' => false, 'message' => 'Không có thông tin để cập nhật']);
        exit();
    }
    
    $params[] = $documentId;
    $sql = "UPDATE group_documents SET " . implode(", ", $updates) . " WHERE id = ?";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    
    echo json_encode([
        'success' => true,
        'message' => 'Cập nhật tài liệu thành công'
    ]);
}

function handleDelete($pdo) {
    if (!isset($_GET['id'])) {
        echo json_encode(['success' => false, 'message' => 'Document ID là bắt buộc']);
        exit();
    }
    
    $documentId = $_GET['id'];
    $userId = isset($_GET['userId']) ? $_GET['userId'] : null;
    
    // Check if document exists
    $stmt = $pdo->prepare("SELECT uploaded_by, file_path FROM group_documents WHERE id = ?");
    $stmt->execute([$documentId]);
    $document = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$document) {
        echo json_encode(['success' => false, 'message' => 'Tài liệu không tồn tại']);
        exit();
    }
    
    // Only uploader can delete
    if ($userId && $document['uploaded_by'] != $userId) {
        echo json_encode(['success' => false, 'message' => 'Bạn không có quyền xóa tài liệu này']);
        exit();
    }
    
    // Delete file from server (if needed)
    // if (file_exists($document['file_path'])) {
    //     unlink($document['file_path']);
    // }
    
    $stmt = $pdo->prepare("DELETE FROM group_documents WHERE id = ?");
    $stmt->execute([$documentId]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Xóa tài liệu thành công'
    ]);
}
?>
