<?php
// API Groups tương thích với Android models
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

// Lấy danh sách nhóm (tương thích với Android Group model)
function getGroups() {
    try {
        $conn = getDBConnection();
        
        // Lấy tất cả groups (Android model không có userId filter)
        $sql = "SELECT id, name, description, createdAt FROM groups ORDER BY createdAt DESC";
        
        $result = $conn->query($sql);
        
        $groups = [];
        while ($row = $result->fetch_assoc()) {
            $groups[] = [
                'id' => (int)$row['id'],
                'name' => $row['name'],
                'description' => $row['description'],
                'createdAt' => (int)$row['createdAt']
            ];
        }
        
        sendResponse(true, 'Groups retrieved successfully', $groups);
    } catch (Exception $e) {
        sendResponse(false, 'Error: ' . $e->getMessage());
    }
}

// Tạo nhóm mới (tương thích với Android Group model)
function createGroup($data) {
    if (!isset($data['name'])) {
        sendResponse(false, 'Name is required');
        return;
    }
    
    $name = trim($data['name']);
    $description = isset($data['description']) ? trim($data['description']) : '';
    
    if (empty($name)) {
        sendResponse(false, 'Group name cannot be empty');
        return;
    }
    
    $conn = getDBConnection();
    
    try {
        // Tạo nhóm (Android model không có createdBy)
        $stmt = $conn->prepare("INSERT INTO groups (name, description) VALUES (?, ?)");
        $stmt->bind_param("ss", $name, $description);
        $stmt->execute();
        $groupId = $conn->insert_id;
        
        // Lấy thông tin nhóm vừa tạo
        $stmt = $conn->prepare("SELECT id, name, description, createdAt FROM groups WHERE id = ?");
        $stmt->bind_param("i", $groupId);
        $stmt->execute();
        $result = $stmt->get_result();
        $group = $result->fetch_assoc();
        
        $groupData = [
            'id' => (int)$group['id'],
            'name' => $group['name'],
            'description' => $group['description'],
            'createdAt' => (int)$group['createdAt']
        ];
        
        sendResponse(true, 'Group created successfully', $groupData);
    } catch (Exception $e) {
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