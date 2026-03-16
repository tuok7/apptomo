-- =====================================================
-- SIMPLE DATABASE SCHEMA FOR GROUP ASSIGNMENT APP
-- Compatible with phpMyAdmin and MySQL/MariaDB
-- =====================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Drop database if exists and create fresh
DROP DATABASE IF EXISTS `myapp_db`;
CREATE DATABASE `myapp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `myapp_db`;

-- =====================================================
-- USER MANAGEMENT TABLES
-- =====================================================

-- Users table (for authentication)
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fullName` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL DEFAULT '',
  `avatar` varchar(500) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `emailVerified` tinyint(1) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `updatedAt` bigint(20) NOT NULL DEFAULT 0,
  `lastLoginAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Password reset codes table
CREATE TABLE IF NOT EXISTS `password_reset_codes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `code` varchar(10) NOT NULL,
  `expiresAt` bigint(20) NOT NULL,
  `isUsed` tinyint(1) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- GROUP MANAGEMENT TABLES
-- =====================================================

-- Groups table
CREATE TABLE IF NOT EXISTS `groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Group Members table
CREATE TABLE IF NOT EXISTS `group_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `role` enum('ADMIN','MODERATOR','MEMBER') NOT NULL DEFAULT 'MEMBER',
  `joinedAt` bigint(20) NOT NULL DEFAULT 0,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_group_member` (`groupId`, `userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Group Invitations table
CREATE TABLE IF NOT EXISTS `group_invitations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `inviterUserId` bigint(20) NOT NULL,
  `inviteeEmail` varchar(255) NOT NULL,
  `inviteCode` varchar(50) NOT NULL,
  `status` enum('PENDING','ACCEPTED','DECLINED','EXPIRED') NOT NULL DEFAULT 'PENDING',
  `expiresAt` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `respondedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inviteCode` (`inviteCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ASSIGNMENT MANAGEMENT TABLES
-- =====================================================

-- Assignments table
CREATE TABLE IF NOT EXISTS `assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `title` varchar(500) NOT NULL,
  `description` text DEFAULT NULL,
  `dueDate` bigint(20) DEFAULT NULL,
  `status` enum('TODO','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'TODO',
  `priority` enum('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  `createdBy` bigint(20) NOT NULL,
  `creatorName` varchar(255) DEFAULT '',
  `maxScore` int(11) NOT NULL DEFAULT 100,
  `allowSubmission` tinyint(1) NOT NULL DEFAULT 1,
  `attachmentPath` varchar(500) DEFAULT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `updatedAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignment Members table
CREATE TABLE IF NOT EXISTS `assignment_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignmentId` bigint(20) NOT NULL,
  `memberId` bigint(20) NOT NULL,
  `status` enum('TODO','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'TODO',
  `assignedAt` bigint(20) NOT NULL DEFAULT 0,
  `completedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- =====================================================
-- CHAT SYSTEM TABLES
-- =====================================================

-- Chat Rooms table
CREATE TABLE IF NOT EXISTS `chat_rooms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `avatarUrl` varchar(500) DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `lastMessageId` bigint(20) DEFAULT NULL,
  `lastMessageTime` bigint(20) DEFAULT NULL,
  `unreadCount` int(11) NOT NULL DEFAULT 0,
  `memberCount` int(11) NOT NULL DEFAULT 0,
  `chatType` enum('GROUP','ASSIGNMENT','STUDY_SESSION','ANNOUNCEMENT') NOT NULL DEFAULT 'GROUP',
  `settings` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Group Messages table
CREATE TABLE IF NOT EXISTS `group_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `senderId` bigint(20) NOT NULL,
  `senderName` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `messageType` enum('TEXT','IMAGE','VIDEO','AUDIO','FILE','SYSTEM') NOT NULL DEFAULT 'TEXT',
  `fileUrl` varchar(500) DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `fileSize` bigint(20) DEFAULT NULL,
  `thumbnailUrl` varchar(500) DEFAULT NULL,
  `timestamp` bigint(20) NOT NULL DEFAULT 0,
  `isEdited` tinyint(1) NOT NULL DEFAULT 0,
  `editedAt` bigint(20) DEFAULT NULL,
  `replyToMessageId` bigint(20) DEFAULT NULL,
  `replyToContent` text DEFAULT NULL,
  `replyToSenderName` varchar(255) DEFAULT NULL,
  `reactions` text DEFAULT NULL,
  `mentions` text DEFAULT NULL,
  `isPinned` tinyint(1) NOT NULL DEFAULT 0,
  `pinnedAt` bigint(20) DEFAULT NULL,
  `pinnedBy` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat Members table
CREATE TABLE IF NOT EXISTS `chat_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chatRoomId` bigint(20) NOT NULL,
  `memberId` bigint(20) NOT NULL,
  `role` enum('ADMIN','MODERATOR','MEMBER','VIEWER') NOT NULL DEFAULT 'MEMBER',
  `joinedAt` bigint(20) NOT NULL DEFAULT 0,
  `lastReadMessageId` bigint(20) DEFAULT NULL,
  `lastReadAt` bigint(20) DEFAULT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `isMuted` tinyint(1) NOT NULL DEFAULT 0,
  `mutedUntil` bigint(20) DEFAULT NULL,
  `permissions` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_chat_member` (`chatRoomId`, `memberId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Message Reactions table
CREATE TABLE IF NOT EXISTS `message_reactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `emoji` varchar(10) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_reaction` (`messageId`, `userId`, `emoji`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Attachments table
CREATE TABLE IF NOT EXISTS `message_attachments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `fileName` varchar(255) NOT NULL,
  `filePath` varchar(500) NOT NULL,
  `fileType` enum('IMAGE','VIDEO','AUDIO','DOCUMENT','OTHER') NOT NULL,
  `fileSize` bigint(20) NOT NULL,
  `thumbnailPath` varchar(500) DEFAULT NULL,
  `width` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat Notifications table
CREATE TABLE IF NOT EXISTS `chat_notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `chatRoomId` bigint(20) NOT NULL,
  `messageId` bigint(20) DEFAULT NULL,
  `type` enum('NEW_MESSAGE','MENTION','ASSIGNMENT_DUE','ASSIGNMENT_SUBMITTED','ASSIGNMENT_GRADED','MEMBER_JOINED','MEMBER_LEFT','CHAT_CREATED','SYSTEM_ANNOUNCEMENT') NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `isRead` tinyint(1) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `readAt` bigint(20) DEFAULT NULL,
  `actionData` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DOCUMENT MANAGEMENT TABLES
-- =====================================================

-- Document Folders table
CREATE TABLE IF NOT EXISTS `document_folders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `parentId` bigint(20) DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Documents table
CREATE TABLE IF NOT EXISTS `documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT '',
  `fileName` varchar(255) NOT NULL,
  `filePath` varchar(500) NOT NULL,
  `fileType` enum('PDF','WORD','EXCEL','POWERPOINT','IMAGE','VIDEO','AUDIO','OTHER') NOT NULL,
  `fileSize` bigint(20) NOT NULL,
  `folderId` bigint(20) DEFAULT NULL,
  `uploadedBy` bigint(20) NOT NULL,
  `uploaderName` varchar(255) DEFAULT '',
  `downloadCount` int(11) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `updatedAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ADVANCED FEATURES TABLES
-- =====================================================

-- Polls table
CREATE TABLE IF NOT EXISTS `polls` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `messageId` bigint(20) NOT NULL,
  `question` varchar(500) NOT NULL,
  `options` text NOT NULL,
  `votes` text DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `expiresAt` bigint(20) DEFAULT NULL,
  `allowMultipleVotes` tinyint(1) NOT NULL DEFAULT 0,
  `isAnonymous` tinyint(1) NOT NULL DEFAULT 0,
  `isClosed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Templates table
CREATE TABLE IF NOT EXISTS `message_templates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `category` varchar(50) NOT NULL DEFAULT 'general',
  `usageCount` int(11) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `lastUsedAt` bigint(20) DEFAULT NULL,
  `isFavorite` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Sticker Packs table
CREATE TABLE IF NOT EXISTS `sticker_packs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `author` varchar(255) NOT NULL,
  `thumbnailUrl` varchar(500) NOT NULL,
  `isInstalled` tinyint(1) NOT NULL DEFAULT 0,
  `isFavorite` tinyint(1) NOT NULL DEFAULT 0,
  `stickerCount` int(11) NOT NULL DEFAULT 0,
  `category` varchar(50) NOT NULL DEFAULT 'general',
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stickers table
CREATE TABLE IF NOT EXISTS `stickers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `packId` bigint(20) NOT NULL,
  `imageUrl` varchar(500) NOT NULL,
  `thumbnailUrl` varchar(500) DEFAULT NULL,
  `keywords` text DEFAULT NULL,
  `usageCount` int(11) NOT NULL DEFAULT 0,
  `lastUsedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mentions table
CREATE TABLE IF NOT EXISTS `mentions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `userName` varchar(255) NOT NULL,
  `startIndex` int(11) NOT NULL,
  `endIndex` int(11) NOT NULL,
  `isRead` tinyint(1) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Link Previews table
CREATE TABLE IF NOT EXISTS `link_previews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `url` varchar(1000) NOT NULL,
  `title` varchar(500) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `imageUrl` varchar(1000) DEFAULT NULL,
  `siteName` varchar(255) DEFAULT NULL,
  `favicon` varchar(500) DEFAULT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Scheduled Messages table
CREATE TABLE IF NOT EXISTS `scheduled_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `senderId` bigint(20) NOT NULL,
  `senderName` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `messageType` enum('TEXT','IMAGE','VIDEO','AUDIO','FILE','SYSTEM') NOT NULL DEFAULT 'TEXT',
  `fileUrl` varchar(500) DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `scheduledTime` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `status` enum('pending','sent','cancelled','failed') NOT NULL DEFAULT 'pending',
  `replyToMessageId` bigint(20) DEFAULT NULL,
  `replyToContent` text DEFAULT NULL,
  `replyToSenderName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Thread table (for threaded conversations)
CREATE TABLE IF NOT EXISTS `message_threads` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parentMessageId` bigint(20) NOT NULL,
  `groupId` bigint(20) NOT NULL,
  `threadTitle` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  `lastMessageAt` bigint(20) DEFAULT NULL,
  `messageCount` int(11) NOT NULL DEFAULT 0,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignment Chat table (linking assignments to chat discussions)
CREATE TABLE IF NOT EXISTS `assignment_chats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignmentId` bigint(20) NOT NULL,
  `chatRoomId` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_assignment_chat` (`assignmentId`, `chatRoomId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Settings table
CREATE TABLE IF NOT EXISTS `user_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `notificationsEnabled` tinyint(1) NOT NULL DEFAULT 1,
  `soundEnabled` tinyint(1) NOT NULL DEFAULT 1,
  `vibrationEnabled` tinyint(1) NOT NULL DEFAULT 1,
  `theme` enum('light','dark','auto') NOT NULL DEFAULT 'light',
  `language` varchar(10) NOT NULL DEFAULT 'vi',
  `fontSize` enum('small','medium','large') NOT NULL DEFAULT 'medium',
  `autoDownloadImages` tinyint(1) NOT NULL DEFAULT 1,
  `autoDownloadVideos` tinyint(1) NOT NULL DEFAULT 0,
  `autoDownloadFiles` tinyint(1) NOT NULL DEFAULT 0,
  `lastSeen` bigint(20) NOT NULL DEFAULT 0,
  `isOnline` tinyint(1) NOT NULL DEFAULT 0,
  `updatedAt` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_settings` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Insert sample users with current timestamp
INSERT INTO `users` (`id`, `fullName`, `email`, `password`, `createdAt`) VALUES
(1, 'Nguyễn Văn An', 'nguyenvanan@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(2, 'Trần Thị Bình', 'tranthibinh@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(3, 'Lê Hoàng Cường', 'lehoangcuong@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(4, 'Phạm Thị Dung', 'phamthidung@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(5, 'Hoàng Văn Em', 'hoangvanem@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(6, 'Vũ Thị Phương', 'vuthiphuong@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(7, 'Đặng Minh Quân', 'dangminhquan@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000),
(8, 'Bùi Thị Hoa', 'buithihoa@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1640995200000);

-- Insert sample groups
INSERT INTO `groups` (`id`, `name`, `description`, `createdAt`) VALUES
(1, 'Nhóm Toán Cao Cấp A1', 'Nhóm học tập môn Toán Cao Cấp lớp A1 - Học kỳ 1', 1640995200000),
(2, 'Lập Trình Android 2024', 'Nhóm phát triển ứng dụng Android - Khóa 2024', 1640995200000),
(3, 'English Conversation Club', 'Nhóm luyện tập tiếng Anh giao tiếp hàng tuần', 1640995200000),
(4, 'Data Science Study Group', 'Nhóm nghiên cứu khoa học dữ liệu và machine learning', 1640995200000);

-- Insert sample group members
INSERT INTO `group_members` (`groupId`, `userId`, `role`, `joinedAt`) VALUES
-- Nhóm Toán Cao Cấp A1
(1, 1, 'ADMIN', 1640995200000),
(1, 2, 'MEMBER', 1640995200000),
(1, 3, 'MEMBER', 1640995200000),
(1, 4, 'MODERATOR', 1640995200000),
-- Lập Trình Android 2024
(2, 2, 'ADMIN', 1640995200000),
(2, 3, 'MEMBER', 1640995200000),
(2, 4, 'MEMBER', 1640995200000),
(2, 5, 'MEMBER', 1640995200000),
(2, 6, 'MEMBER', 1640995200000),
-- English Conversation Club
(3, 3, 'ADMIN', 1640995200000),
(3, 6, 'MEMBER', 1640995200000),
(3, 7, 'MEMBER', 1640995200000),
-- Data Science Study Group
(4, 1, 'ADMIN', 1640995200000),
(4, 5, 'MEMBER', 1640995200000),
(4, 7, 'MEMBER', 1640995200000),
(4, 8, 'MEMBER', 1640995200000);
-- Insert sample assignments
INSERT INTO `assignments` (`id`, `groupId`, `title`, `description`, `dueDate`, `priority`, `status`, `createdBy`, `createdAt`) VALUES
(1, 1, 'Bài tập Đạo hàm và Vi phân', 'Giải các bài tập từ 1-25 trong sách giáo khoa, tập trung vào quy tắc đạo hàm cơ bản', 1711324800000, 'HIGH', 'TODO', 1, 1640995200000),
(2, 1, 'Bài tập Tích phân', 'Làm bài tập tích phân chương 5, bao gồm tích phân xác định và không xác định', 1711756800000, 'MEDIUM', 'IN_PROGRESS', 2, 1640995200000),
(3, 2, 'Tạo ứng dụng Todo List', 'Phát triển ứng dụng quản lý công việc sử dụng Kotlin và Jetpack Compose', 1712188800000, 'HIGH', 'TODO', 2, 1640995200000),
(4, 2, 'Thiết kế UI/UX cho ứng dụng', 'Tạo mockup và prototype cho ứng dụng mobile', 1712620800000, 'MEDIUM', 'TODO', 3, 1640995200000),
(5, 3, 'Presentation về Travel', 'Chuẩn bị bài thuyết trình 10 phút về chủ đề du lịch', 1711670400000, 'LOW', 'COMPLETED', 3, 1640995200000),
(6, 4, 'Phân tích dữ liệu bán hàng', 'Sử dụng Python và Pandas để phân tích dataset bán hàng', 1713225600000, 'HIGH', 'IN_PROGRESS', 1, 1640995200000);

-- Insert sample chat rooms
INSERT INTO `chat_rooms` (`id`, `groupId`, `name`, `description`, `createdBy`, `chatType`, `createdAt`, `memberCount`) VALUES
(1, 1, 'Chat Nhóm Toán', 'Thảo luận chung về môn Toán Cao Cấp', 1, 'GROUP', 1640995200000, 4),
(2, 2, 'Android Dev Chat', 'Thảo luận về lập trình Android', 2, 'GROUP', 1640995200000, 5),
(3, 3, 'English Practice', 'English conversation practice', 3, 'GROUP', 1640995200000, 3),
(4, 4, 'Data Science Discussion', 'Thảo luận về khoa học dữ liệu', 1, 'GROUP', 1640995200000, 4);

-- Insert sample messages
INSERT INTO `group_messages` (`id`, `groupId`, `senderId`, `senderName`, `content`, `messageType`, `timestamp`, `isPinned`) VALUES
(1, 1, 1, 'Nguyễn Văn An', 'Chào mọi người! Chúng ta bắt đầu học nhóm môn Toán Cao Cấp nhé! 📚', 'TEXT', 1640995200000, 1),
(2, 1, 2, 'Trần Thị Bình', 'Chào bạn An! Mình đã sẵn sàng rồi. Hôm nay chúng ta học chương nào? 😊', 'TEXT', 1640995260000, 0),
(3, 1, 3, 'Lê Hoàng Cường', 'Mình nghĩ nên bắt đầu từ chương Đạo hàm. Ai có tài liệu không?', 'TEXT', 1640995320000, 0),
(4, 2, 2, 'Trần Thị Bình', 'Chào team! Chúng ta bắt đầu dự án Android nhé! 🚀', 'TEXT', 1640995380000, 1),
(5, 2, 3, 'Lê Hoàng Cường', 'Awesome! Mình đã setup môi trường development rồi. Android Studio latest version.', 'TEXT', 1640995440000, 0);

-- Insert sample user settings
INSERT INTO `user_settings` (`userId`, `theme`, `language`, `isOnline`) VALUES
(1, 'light', 'vi', 1),
(2, 'dark', 'vi', 1),
(3, 'light', 'en', 0),
(4, 'light', 'vi', 1);

-- Insert sample sticker packs
INSERT INTO `sticker_packs` (`id`, `name`, `author`, `thumbnailUrl`, `isInstalled`, `category`, `stickerCount`) VALUES
(1, 'Emoji Classic', 'System', '/stickers/emoji/thumb.png', 1, 'emoji', 50),
(2, 'Cute Animals', 'StickerStudio', '/stickers/animals/thumb.png', 1, 'cute', 30);

COMMIT;

-- =====================================================
-- END OF DATABASE SCHEMA
-- =====================================================