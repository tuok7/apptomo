<?php
require_once __DIR__ . '/../../config/config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

$method = $_SERVER['REQUEST_METHOD'];
$conn = getDBConnection();

switch ($method) {
    case 'GET':
        handleGetAssignments($conn);
        break;
    case 'POST':
        handleCreateAssignment($conn);
        break;
    case 'PUT':
        handleUpdateAssignment($conn);
        break;
    case 'DELETE':
        handleDeleteAssignment($conn);
        break;
    default:
        sendResponse(false, 'Method not allowed');
}

function handleGetAssignments($conn) {
    $groupId = $_GET['groupId'] ?? null;
    
    if (!$groupId) {
        sendResponse(false, 'Group ID is required');
    }
    
    $sql = "SELECT a.*, u.full_name as creator_name,
                   GROUP_CONCAT(DISTINCT CONCAT(u2.id, ':', u2.full_name) SEPARATOR '|') as assigned_members
            FROM assignments a 
            JOIN users u ON a.created_by = u.id
            LEFT JOIN assignment_members am ON a.id = am.assignment_id
            LEFT JOIN users u2 ON am.user_id = u2.id
            WHERE a.group_id = ? 
            GROUP BY a.id
            ORDER BY a.created_at DESC";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $groupId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $assignments = [];
    while ($row = $result->fetch_assoc()) {
        $assignedMembers = [];
        if ($row['assigned_members']) {
            $members = explode('|', $row['assigned_members']);
            foreach ($members as $member) {
                $parts = explode(':', $member);
                if (count($parts) == 2) {
                    $assignedMembers[] = [
                        'id' => (int)$parts[0],
                        'name' => $parts[1]
                    ];
                }
            }
        }
        
        $assignments[] = [
            'id' => (int)$row['id'],
            'groupId' => (int)$row['group_id'],
            'title' => $row['title'],
            'description' => $row['description'],
            'dueDate' => $row['due_date'],
            'status' => $row['status'],
            'priority' => $row['priority'],
            'createdBy' => (int)$row['created_by'],
            'creatorName' => $row['creator_name'],
            'assignedMembers' => $assignedMembers,
            'createdAt' => $row['created_at']
        ];
    }
    
    sendResponse(true, 'Assignments retrieved successfully', $assignments);
}

function handleCreateAssignment($conn) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (!isset($data['groupId']) || !isset($data['title']) || !isset($data['createdBy'])) {
        sendResponse(false, 'Group ID, title, and creator ID are required');
    }
    
    $groupId = (int)$data['groupId'];
    $title = trim($data['title']);
    $description = trim($data['description'] ?? '');
    $dueDate = $data['dueDate'] ?? null;
    $priority = $data['priority'] ?? 'medium';
    $createdBy = (int)$data['createdBy'];
    $assignedMembers = $data['assignedMembers'] ?? [];
    
    if (empty($title)) {
        sendResponse(false, 'Assignment title cannot be empty');
    }
    
    // Convert timestamp to MySQL datetime if provided
    if ($dueDate) {
        $dueDate = date('Y-m-d H:i:s', $dueDate / 1000); // Convert from milliseconds
    }
    
    // Start transaction
    $conn->begin_transaction();
    
    try {
        // Create assignment
        $stmt = $conn->prepare("INSERT INTO assignments (group_id, title, description, due_date, priority, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("issssi", $groupId, $title, $description, $dueDate, $priority, $createdBy);
        $stmt->execute();
        
        $assignmentId = $conn->insert_id;
        
        // Assign members if provided
        if (!empty($assignedMembers)) {
            $stmt = $conn->prepare("INSERT INTO assignment_members (assignment_id, user_id, status, assigned_at) VALUES (?, ?, 'assigned', NOW())");
            foreach ($assignedMembers as $memberId) {
                $stmt->bind_param("ii", $assignmentId, $memberId);
                $stmt->execute();
            }
        }
        
        $conn->commit();
        
        // Get created assignment info
        $stmt = $conn->prepare("SELECT a.*, u.full_name as creator_name FROM assignments a JOIN users u ON a.created_by = u.id WHERE a.id = ?");
        $stmt->bind_param("i", $assignmentId);
        $stmt->execute();
        $result = $stmt->get_result();
        $assignment = $result->fetch_assoc();
        
        $assignmentData = [
            'id' => (int)$assignment['id'],
            'groupId' => (int)$assignment['group_id'],
            'title' => $assignment['title'],
            'description' => $assignment['description'],
            'dueDate' => $assignment['due_date'],
            'status' => $assignment['status'],
            'priority' => $assignment['priority'],
            'createdBy' => (int)$assignment['created_by'],
            'creatorName' => $assignment['creator_name'],
            'createdAt' => $assignment['created_at']
        ];
        
        sendResponse(true, 'Assignment created successfully', $assignmentData);
        
    } catch (Exception $e) {
        $conn->rollback();
        sendResponse(false, 'Failed to create assignment: ' . $e->getMessage());
    }
}

function handleUpdateAssignment($conn) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (!isset($data['id'])) {
        sendResponse(false, 'Assignment ID is required');
    }
    
    $id = (int)$data['id'];
    $title = trim($data['title'] ?? '');
    $description = trim($data['description'] ?? '');
    $dueDate = $data['dueDate'] ?? null;
    $status = $data['status'] ?? null;
    $priority = $data['priority'] ?? null;
    
    // Convert timestamp to MySQL datetime if provided
    if ($dueDate) {
        $dueDate = date('Y-m-d H:i:s', $dueDate / 1000);
    }
    
    $updates = [];
    $params = [];
    $types = '';
    
    if (!empty($title)) {
        $updates[] = "title = ?";
        $params[] = $title;
        $types .= 's';
    }
    
    if (!empty($description)) {
        $updates[] = "description = ?";
        $params[] = $description;
        $types .= 's';
    }
    
    if ($dueDate) {
        $updates[] = "due_date = ?";
        $params[] = $dueDate;
        $types .= 's';
    }
    
    if ($status) {
        $updates[] = "status = ?";
        $params[] = $status;
        $types .= 's';
    }
    
    if ($priority) {
        $updates[] = "priority = ?";
        $params[] = $priority;
        $types .= 's';
    }
    
    if (empty($updates)) {
        sendResponse(false, 'No fields to update');
    }
    
    $updates[] = "updated_at = NOW()";
    $params[] = $id;
    $types .= 'i';
    
    $sql = "UPDATE assignments SET " . implode(', ', $updates) . " WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param($types, ...$params);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Assignment updated successfully');
    } else {
        sendResponse(false, 'Failed to update assignment');
    }
}

function handleDeleteAssignment($conn) {
    $assignmentId = $_GET['id'] ?? null;
    
    if (!$assignmentId) {
        sendResponse(false, 'Assignment ID is required');
    }
    
    $stmt = $conn->prepare("DELETE FROM assignments WHERE id = ?");
    $stmt->bind_param("i", $assignmentId);
    
    if ($stmt->execute()) {
        sendResponse(true, 'Assignment deleted successfully');
    } else {
        sendResponse(false, 'Failed to delete assignment');
    }
}

$conn->close();
?>