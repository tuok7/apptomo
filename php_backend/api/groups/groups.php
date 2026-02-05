<?php
// Tắt hiển thị lỗi PHP để không làm hỏng JSON
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once __DIR__ . '/../../config/config.php';

$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        getGroups();
        break;
    case 'POST':
        $json = file_get_contents('php://input');
        $data = json_decode($json, true);
        createGroup($data);
        break;
    case 'PUT':
        $json = file_get_contents('php://input');
        $data = json_decode($json, true);
        updateGroup($data);
        break;
    case 'DELETE':
        deleteGroup();
        break;
    default:
        sendResponse(false, 'Method not allowed');
}

// Lấy danh sách nhóm của user
function getGroups() {
    try {
        if (!isset($_GET['userId'])) {
            sendResponse(false, 'User ID is required');
            return;
        }
        
        $userId = intval($_GET['userId']);
        
        if ($userId <= 0) {
            sendResponse(false, 'Invalid User ID');
            return;
        }
        
        $conn = getDBConnection();
        
        $sql = "SELECT g.id, g.name, g.description, g.created_by as createdBy, 
                u.full_name as creatorName,
                (SELECT COUNT(*) FROM group_members WHERE group_id = g.id) as memberCount,
                g.created_at as createdAt
                FROM groups g
                INNER JOIN group_members gm ON g.id = gm.group_id
                INNER JOIN users u ON g.created_by = u.id
                WHERE gm.user_id = ?
                ORDER BY g.created_at DESC";
        
        $stmt = $conn->prepare($sql);
        
        if (!$stmt) {
            sendResponse(false, 'Database error: ' . $conn->error);
            return;
        }
        
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $groups = [];
        while ($row = $result->fetch_assoc()) {
            $groups[] = [
                'id' => (int)$row['id'],
                'name' => $row['name'],
                'description' => $row['description'],
                'createdBy' => (int)$row['createdBy'],
                'creatorName' => $row['creatorName'],
                'memberCount' => (int)$row['memberCount'],
                'createdAt' => $row['createdAt']
            ];
        }
        
        sendResponse(true, 'Groups retrieved successfully', $groups);
    } catch (Exception $e) {
        sendResponse(false, 'Error: ' . $e->getMessage());
    }
}

// Tạo nhóm mới
function createGroup($data) {
    if (!isset($data['name']) || !isset($data['createdBy'])) {
        sendResponse(false, 'Name and createdBy are required');
        return;
    }
    
    $name = trim($data['name']);
    $description = isset($data['description']) ? trim($data['description']) : '';
    $userId = intval($data['createdBy']);
    
    if (empty($name)) {
        sendResponse(false, 'Group name cannot be empty');
        return;
    }
    
    $conn = getDBConnection();
    $conn->begin_transaction();
    
    try {
        // Tạo nhóm
        $stmt = $conn->prepare("INSERT INTO groups (name, description, created_by) VALUES (?, ?, ?)");
        $stmt->bind_param("ssi", $name, $description, $userId);
        $stmt->execute();
        $groupId = $conn->insert_id;
        
        // Thêm người tạo vào nhóm với role admin
        $stmt = $conn->prepare("INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, 'admin')");
        $stmt->bind_param("ii", $groupId, $userId);
        $stmt->execute();
        
        $conn->commit();
        
        // Lấy thông tin nhóm vừa tạo
        $stmt = $conn->prepare("SELECT g.id, g.name, g.description, g.created_by as createdBy, 
                                u.full_name as creatorName, 1 as memberCount, g.created_at as createdAt
                                FROM groups g
                                INNER JOIN users u ON g.created_by = u.id
                                WHERE g.id = ?");
        $stmt->bind_param("i", $groupId);
        $stmt->execute();
        $result = $stmt->get_result();
        $group = $result->fetch_assoc();
        
        $groupData = [
            'id' => (int)$group['id'],
            'name' => $group['name'],
            'description' => $group['description'],
            'createdBy' => (int)$group['createdBy'],
            'creatorName' => $group['creatorName'],
            'memberCount' => (int)$group['memberCount'],
            'createdAt' => $group['createdAt']
        ];
        
        sendResponse(true, 'Group created successfully', $groupData);
    } catch (Exception $e) {
        $conn->rollback();
        sendResponse(false, 'Failed to create group: ' . $e->getMessage());
    }
}

// Cập nhật nhóm
function updateGroup($data) {
    if (!isset($data['id']) || !isset($data['name'])) {
        sendResponse(false, 'ID and name are required');
        return;
    }
    
    $id = intval($data['id']);
    $name = trim($data['name']);
    $description = isset($data['description']) ? trim($data['description']) : '';
    
    $conn = getDBConnection();
    $stmt = $conn->prepare("UPDATE groups SET name = ?, description = ? WHERE id = ?");
    $stmt->bind_param("ssi", $name, $description, $id);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Group updated successfully');
    } else {
        sendResponse(false, 'Failed to update group');
    }
}

// Xóa nhóm
function deleteGroup() {
    if (!isset($_GET['id'])) {
        sendResponse(false, 'Group ID is required');
        return;
    }
    
    $id = intval($_GET['id']);
    $conn = getDBConnection();
    $stmt = $conn->prepare("DELETE FROM groups WHERE id = ?");
    $stmt->bind_param("i", $id);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Group deleted successfully');
    } else {
        sendResponse(false, 'Failed to delete group');
    }
}
?>
