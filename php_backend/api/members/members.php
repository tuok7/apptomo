<?php
require_once __DIR__ . '/../../config/config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

$method = $_SERVER['REQUEST_METHOD'];
$conn = getDBConnection();

switch ($method) {
    case 'GET':
        handleGetMembers($conn);
        break;
    case 'POST':
        handleAddMember($conn);
        break;
    case 'DELETE':
        handleRemoveMember($conn);
        break;
    default:
        sendResponse(false, 'Method not allowed');
}

function handleGetMembers($conn) {
    $groupId = $_GET['groupId'] ?? null;
    
    if (!$groupId) {
        sendResponse(false, 'Group ID is required');
    }
    
    $sql = "SELECT u.id, u.full_name, u.email, gm.role, gm.joined_at
            FROM group_members gm
            JOIN users u ON gm.user_id = u.id
            WHERE gm.group_id = ?
            ORDER BY gm.role DESC, gm.joined_at ASC";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $groupId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $members = [];
    while ($row = $result->fetch_assoc()) {
        $members[] = [
            'id' => (int)$row['id'],
            'fullName' => $row['full_name'],
            'email' => $row['email'],
            'role' => $row['role'],
            'joinedAt' => $row['joined_at']
        ];
    }
    
    sendResponse(true, 'Members retrieved successfully', $members);
}

function handleAddMember($conn) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (!isset($data['groupId']) || !isset($data['email'])) {
        sendResponse(false, 'Group ID and email are required');
    }
    
    $groupId = (int)$data['groupId'];
    $email = trim($data['email']);
    $role = $data['role'] ?? 'member';
    
    // Find user by email
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows == 0) {
        sendResponse(false, 'User not found with this email');
    }
    
    $user = $result->fetch_assoc();
    $userId = $user['id'];
    
    // Check if user is already a member
    $stmt = $conn->prepare("SELECT id FROM group_members WHERE group_id = ? AND user_id = ?");
    $stmt->bind_param("ii", $groupId, $userId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        sendResponse(false, 'User is already a member of this group');
    }
    
    // Add member
    $stmt = $conn->prepare("INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES (?, ?, ?, NOW())");
    $stmt->bind_param("iis", $groupId, $userId, $role);
    
    if ($stmt->execute()) {
        // Get user info
        $stmt = $conn->prepare("SELECT id, full_name, email FROM users WHERE id = ?");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $result = $stmt->get_result();
        $userInfo = $result->fetch_assoc();
        
        $memberData = [
            'id' => (int)$userInfo['id'],
            'fullName' => $userInfo['full_name'],
            'email' => $userInfo['email'],
            'role' => $role
        ];
        
        sendResponse(true, 'Member added successfully', $memberData);
    } else {
        sendResponse(false, 'Failed to add member');
    }
}

function handleRemoveMember($conn) {
    $groupId = $_GET['groupId'] ?? null;
    $userId = $_GET['userId'] ?? null;
    
    if (!$groupId || !$userId) {
        sendResponse(false, 'Group ID and User ID are required');
    }
    
    $stmt = $conn->prepare("DELETE FROM group_members WHERE group_id = ? AND user_id = ?");
    $stmt->bind_param("ii", $groupId, $userId);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Member removed successfully');
    } else {
        sendResponse(false, 'Failed to remove member');
    }
}

$conn->close();
?>