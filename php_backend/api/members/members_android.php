<?php
// API Members tương thích với Android Member model
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
        getMembers();
        break;
    case 'POST':
        $json = file_get_contents('php://input');
        $data = json_decode($json, true);
        addMember($data);
        break;
    case 'PUT':
        $json = file_get_contents('php://input');
        $data = json_decode($json, true);
        updateMember($data);
        break;
    case 'DELETE':
        deleteMember();
        break;
    default:
        sendResponse(false, 'Method not allowed');
}

// Lấy danh sách thành viên (sử dụng bảng users thông qua group_members)
function getMembers() {
    try {
        if (!isset($_GET['groupId'])) {
            sendResponse(false, 'Group ID is required');
            return;
        }
        
        $groupId = intval($_GET['groupId']);
        
        $conn = getDBConnection();
        
        $sql = "SELECT u.id, u.fullName as name, u.email, gm.role, u.isActive as isOnline, 
                       u.lastLoginAt as lastSeen, u.updatedAt as lastActivity, ? as groupId
                FROM users u
                INNER JOIN group_members gm ON u.id = gm.userId
                WHERE gm.groupId = ? AND gm.isActive = 1
                ORDER BY gm.role DESC, u.fullName ASC";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $groupId, $groupId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $members = [];
        while ($row = $result->fetch_assoc()) {
            $members[] = [
                'id' => (int)$row['id'],
                'groupId' => (int)$row['groupId'],
                'name' => $row['name'],
                'email' => $row['email'],
                'role' => $row['role'],
                'isOnline' => (bool)$row['isOnline'],
                'lastSeen' => (int)($row['lastSeen'] ?? time() * 1000),
                'lastActivity' => (int)($row['lastActivity'] ?? time() * 1000)
            ];
        }
        
        sendResponse(true, 'Members retrieved successfully', $members);
    } catch (Exception $e) {
        sendResponse(false, 'Error: ' . $e->getMessage());
    }
}

// Thêm thành viên mới (thêm vào group_members, không tạo user mới)
function addMember($data) {
    if (!isset($data['groupId']) || !isset($data['email'])) {
        sendResponse(false, 'GroupId and email are required');
        return;
    }
    
    $groupId = intval($data['groupId']);
    $email = trim($data['email']);
    $role = isset($data['role']) ? trim($data['role']) : 'MEMBER';
    
    $conn = getDBConnection();
    
    try {
        // Tìm user theo email
        $stmt = $conn->prepare("SELECT id, fullName FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows == 0) {
            sendResponse(false, 'User with this email does not exist');
            return;
        }
        
        $user = $result->fetch_assoc();
        $userId = $user['id'];
        
        // Kiểm tra đã là thành viên nhóm chưa
        $stmt = $conn->prepare("SELECT id FROM group_members WHERE groupId = ? AND userId = ?");
        $stmt->bind_param("ii", $groupId, $userId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            sendResponse(false, 'User is already a member of this group');
            return;
        }
        
        // Thêm vào group_members
        $stmt = $conn->prepare("INSERT INTO group_members (groupId, userId, role) VALUES (?, ?, ?)");
        $stmt->bind_param("iis", $groupId, $userId, $role);
        $stmt->execute();
        
        // Trả về thông tin member
        $memberData = [
            'id' => (int)$userId,
            'groupId' => $groupId,
            'name' => $user['fullName'],
            'email' => $email,
            'role' => $role,
            'isOnline' => false,
            'lastSeen' => time() * 1000,
            'lastActivity' => time() * 1000
        ];
        
        sendResponse(true, 'Member added successfully', $memberData);
    } catch (Exception $e) {
        sendResponse(false, 'Failed to add member: ' . $e->getMessage());
    }
}

// Cập nhật thành viên (cập nhật role trong group_members)
function updateMember($data) {
    if (!isset($data['id']) || !isset($data['groupId'])) {
        sendResponse(false, 'Member ID and Group ID are required');
        return;
    }
    
    $userId = intval($data['id']);
    $groupId = intval($data['groupId']);
    $conn = getDBConnection();
    
    try {
        // Chỉ cho phép cập nhật role trong group_members
        if (isset($data['role'])) {
            $role = trim($data['role']);
            $stmt = $conn->prepare("UPDATE group_members SET role = ? WHERE userId = ? AND groupId = ?");
            $stmt->bind_param("sii", $role, $userId, $groupId);
            $stmt->execute();
        }
        
        // Cập nhật lastActivity trong users
        $stmt = $conn->prepare("UPDATE users SET updatedAt = ? WHERE id = ?");
        $currentTime = time() * 1000;
        $stmt->bind_param("ii", $currentTime, $userId);
        $stmt->execute();
        
        sendResponse(true, 'Member updated successfully');
    } catch (Exception $e) {
        sendResponse(false, 'Failed to update member: ' . $e->getMessage());
    }
}

// Xóa thành viên (xóa khỏi group_members, không xóa user)
function deleteMember() {
    if (!isset($_GET['id']) || !isset($_GET['groupId'])) {
        sendResponse(false, 'Member ID and Group ID are required');
        return;
    }
    
    $userId = intval($_GET['id']);
    $groupId = intval($_GET['groupId']);
    $conn = getDBConnection();
    
    $stmt = $conn->prepare("DELETE FROM group_members WHERE userId = ? AND groupId = ?");
    $stmt->bind_param("ii", $userId, $groupId);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Member removed from group successfully');
    } else {
        sendResponse(false, 'Failed to remove member from group');
    }
}
?>