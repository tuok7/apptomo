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
    
    $sql = "SELECT a.*, u.fullName as creator_name,
                   GROUP_CONCAT(DISTINCT CONCAT(u2.id, ':', u2.fullName) SEPARATOR '|') as assigned_members
            FROM assignments a 
            JOIN users u ON a.createdBy = u.id
            LEFT JOIN assignment_members am ON a.id = am.assignmentId
            LEFT JOIN users u2 ON am.memberId = u2.id
            WHERE a.groupId = ? 
            GROUP BY a.id
            ORDER BY a.createdAt DESC";
    
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
            'groupId' => (int)$row['groupId'],
            'title' => $row['title'],
            'description' => $row['description'],
            'dueDate' => $row['dueDate'],
            'status' => $row['status'],
            'priority' => $row['priority'],
            'createdBy' => (int)$row['createdBy'],
            'creatorName' => $row['creator_name'],
            'assignedMembers' => $assignedMembers,
            'createdAt' => $row['createdAt']
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
        $dueDate = $dueDate; // Keep as timestamp for database
    }
    
    // Start transaction
    $conn->begin_transaction();
    
    try {
        // Create assignment
        $stmt = $conn->prepare("INSERT INTO assignments (groupId, title, description, dueDate, priority, createdBy, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $currentTime = time() * 1000; // Current timestamp in milliseconds
        $stmt->bind_param("issiiii", $groupId, $title, $description, $dueDate, $priority, $createdBy, $currentTime);
        $stmt->execute();
        
        $assignmentId = $conn->insert_id;
        
        // Assign members if provided
        if (!empty($assignedMembers)) {
            $stmt = $conn->prepare("INSERT INTO assignment_members (assignmentId, memberId, status, assignedAt) VALUES (?, ?, 'TODO', ?)");
            foreach ($assignedMembers as $memberId) {
                $stmt->bind_param("iii", $assignmentId, $memberId, $currentTime);
                $stmt->execute();
            }
        }
        
        $conn->commit();
        
        // Get created assignment info
        $stmt = $conn->prepare("SELECT a.*, u.fullName as creator_name FROM assignments a JOIN users u ON a.createdBy = u.id WHERE a.id = ?");
        $stmt->bind_param("i", $assignmentId);
        $stmt->execute();
        $result = $stmt->get_result();
        $assignment = $result->fetch_assoc();
        
        $assignmentData = [
            'id' => (int)$assignment['id'],
            'groupId' => (int)$assignment['groupId'],
            'title' => $assignment['title'],
            'description' => $assignment['description'],
            'dueDate' => $assignment['dueDate'],
            'status' => $assignment['status'],
            'priority' => $assignment['priority'],
            'createdBy' => (int)$assignment['createdBy'],
            'creatorName' => $assignment['creator_name'],
            'createdAt' => $assignment['createdAt']
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