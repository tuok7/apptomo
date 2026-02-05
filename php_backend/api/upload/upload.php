<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../../config/config.php';

// Handle preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

// Check if file was uploaded
if (!isset($_FILES['file'])) {
    echo json_encode(['success' => false, 'message' => 'Không có file được tải lên']);
    exit();
}

$file = $_FILES['file'];
$uploadType = isset($_POST['type']) ? $_POST['type'] : 'document'; // document or message_attachment

// Validate file
if ($file['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(['success' => false, 'message' => 'Lỗi khi tải file lên']);
    exit();
}

// Check file size (max 50MB)
$maxSize = 50 * 1024 * 1024; // 50MB
if ($file['size'] > $maxSize) {
    echo json_encode(['success' => false, 'message' => 'File quá lớn. Kích thước tối đa là 50MB']);
    exit();
}

// Get file info
$fileName = basename($file['name']);
$fileSize = $file['size'];
$fileType = $file['type'];
$fileExtension = strtolower(pathinfo($fileName, PATHINFO_EXTENSION));

// Allowed file types
$allowedTypes = [
    // Documents
    'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv',
    // Images
    'jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp',
    // Videos
    'mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv',
    // Audio
    'mp3', 'wav', 'ogg', 'aac',
    // Archives
    'zip', 'rar', '7z', 'tar', 'gz'
];

if (!in_array($fileExtension, $allowedTypes)) {
    echo json_encode(['success' => false, 'message' => 'Loại file không được hỗ trợ']);
    exit();
}

// Create upload directory if not exists
$uploadDir = __DIR__ . '/../../uploads/';
if (!file_exists($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

// Create subdirectory based on type
$subDir = $uploadType === 'document' ? 'documents/' : 'attachments/';
$targetDir = $uploadDir . $subDir;
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

// Generate unique filename
$uniqueFileName = uniqid() . '_' . time() . '.' . $fileExtension;
$targetPath = $targetDir . $uniqueFileName;
$relativePath = 'uploads/' . $subDir . $uniqueFileName;

// Move uploaded file
if (!move_uploaded_file($file['tmp_name'], $targetPath)) {
    echo json_encode(['success' => false, 'message' => 'Không thể lưu file']);
    exit();
}

// Return file info
echo json_encode([
    'success' => true,
    'message' => 'Tải file lên thành công',
    'data' => [
        'fileName' => $fileName,
        'uniqueFileName' => $uniqueFileName,
        'filePath' => $relativePath,
        'fileType' => $fileType,
        'fileSize' => $fileSize,
        'fileExtension' => $fileExtension
    ]
]);
?>
