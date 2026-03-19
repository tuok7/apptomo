<?php
require_once __DIR__ . '/config/config.php';

echo "Updating passwords for all users...\n";

$conn = getDBConnection();

// Mật khẩu mới cho tất cả user test
$newPassword = 'password';
$hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

// Update tất cả users
$stmt = $conn->prepare("UPDATE users SET password = ?");
$stmt->bind_param("s", $hashedPassword);

if ($stmt->execute()) {
    echo "✅ All passwords updated successfully!\n";
    echo "New password for all users: password\n";
    echo "Hash: " . $hashedPassword . "\n";
} else {
    echo "❌ Error updating passwords: " . $conn->error . "\n";
}

$stmt->close();
$conn->close();

echo "\nNow you can login with:\n";
echo "Email: tranthibinh@email.com\n";
echo "Password: password\n";
?>