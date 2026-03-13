-- MySQL Database Schema for Group Chat with Assignment Management
-- Compatible with phpMyAdmin and MySQL/MariaDB

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS `group_chat_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `group_chat_db`;

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Groups table
CREATE TABLE IF NOT EXISTS `groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_groups_created_by` (`createdBy`),
  KEY `idx_groups_active` (`isActive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Members table
CREATE TABLE IF NOT EXISTS `members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fullName` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  `avatar` varchar(500) DEFAULT NULL,
  `joinedAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_members_email` (`email`),
  KEY `idx_members_active` (`isActive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignments table
CREATE TABLE IF NOT EXISTS `assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL,
  `title` varchar(500) NOT NULL,
  `description` text DEFAULT NULL,
  `dueDate` varchar(50) DEFAULT NULL,
  `priority` enum('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  `status` enum('TODO','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'TODO',
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `updatedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_assignments_group` (`groupId`),
  KEY `idx_assignments_created_by` (`createdBy`),
  KEY `idx_assignments_status` (`status`),
  KEY `idx_assignments_due_date` (`dueDate`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`createdBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignment Members table
CREATE TABLE IF NOT EXISTS `assignment_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignmentId` bigint(20) NOT NULL,
  `memberId` bigint(20) NOT NULL,
  `status` enum('TODO','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'TODO',
  `assignedAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `completedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_assignment_members_assignment` (`assignmentId`),
  KEY `idx_assignment_members_member` (`memberId`),
  FOREIGN KEY (`assignmentId`) REFERENCES `assignments`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`memberId`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `lastMessageId` bigint(20) DEFAULT NULL,
  `lastMessageTime` bigint(20) DEFAULT NULL,
  `unreadCount` int(11) NOT NULL DEFAULT 0,
  `memberCount` int(11) NOT NULL DEFAULT 0,
  `chatType` enum('GROUP','ASSIGNMENT','STUDY_SESSION','ANNOUNCEMENT') NOT NULL DEFAULT 'GROUP',
  `settings` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_rooms_group` (`groupId`),
  KEY `idx_chat_rooms_type` (`chatType`),
  KEY `idx_chat_rooms_active` (`isActive`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`createdBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `timestamp` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `isEdited` tinyint(1) NOT NULL DEFAULT 0,
  `editedAt` bigint(20) DEFAULT NULL,
  `replyToMessageId` bigint(20) DEFAULT NULL,
  `replyToContent` text DEFAULT NULL,
  `replyToSenderName` varchar(255) DEFAULT NULL,
  `reactions` json DEFAULT NULL,
  `mentions` json DEFAULT NULL,
  `isPinned` tinyint(1) NOT NULL DEFAULT 0,
  `pinnedAt` bigint(20) DEFAULT NULL,
  `pinnedBy` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_messages_group` (`groupId`),
  KEY `idx_group_messages_sender` (`senderId`),
  KEY `idx_group_messages_timestamp` (`timestamp`),
  KEY `idx_group_messages_type` (`messageType`),
  KEY `idx_group_messages_pinned` (`isPinned`),
  KEY `idx_group_messages_reply` (`replyToMessageId`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`senderId`) REFERENCES `members`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`replyToMessageId`) REFERENCES `group_messages`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`pinnedBy`) REFERENCES `members`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat Members table
CREATE TABLE IF NOT EXISTS `chat_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chatRoomId` bigint(20) NOT NULL,
  `memberId` bigint(20) NOT NULL,
  `role` enum('ADMIN','MODERATOR','MEMBER','VIEWER') NOT NULL DEFAULT 'MEMBER',
  `joinedAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `lastReadMessageId` bigint(20) DEFAULT NULL,
  `lastReadAt` bigint(20) DEFAULT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `isMuted` tinyint(1) NOT NULL DEFAULT 0,
  `mutedUntil` bigint(20) DEFAULT NULL,
  `permissions` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_chat_member` (`chatRoomId`, `memberId`),
  KEY `idx_chat_members_room` (`chatRoomId`),
  KEY `idx_chat_members_member` (`memberId`),
  KEY `idx_chat_members_active` (`isActive`),
  FOREIGN KEY (`chatRoomId`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`memberId`) REFERENCES `members`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`lastReadMessageId`) REFERENCES `group_messages`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Message Reactions table
CREATE TABLE IF NOT EXISTS `message_reactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `emoji` varchar(10) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_reaction` (`messageId`, `userId`, `emoji`),
  KEY `idx_message_reactions_message` (`messageId`),
  KEY `idx_message_reactions_user` (`userId`),
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`userId`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_message_attachments_message` (`messageId`),
  KEY `idx_message_attachments_type` (`fileType`),
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `readAt` bigint(20) DEFAULT NULL,
  `actionData` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_notifications_user` (`userId`),
  KEY `idx_chat_notifications_room` (`chatRoomId`),
  KEY `idx_chat_notifications_read` (`isRead`),
  KEY `idx_chat_notifications_type` (`type`),
  FOREIGN KEY (`userId`) REFERENCES `members`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`chatRoomId`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_document_folders_group` (`groupId`),
  KEY `idx_document_folders_parent` (`parentId`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`parentId`) REFERENCES `document_folders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`createdBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `updatedAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_documents_group` (`groupId`),
  KEY `idx_documents_folder` (`folderId`),
  KEY `idx_documents_uploader` (`uploadedBy`),
  KEY `idx_documents_type` (`fileType`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`folderId`) REFERENCES `document_folders`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`uploadedBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `options` json NOT NULL,
  `votes` json DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `expiresAt` bigint(20) DEFAULT NULL,
  `allowMultipleVotes` tinyint(1) NOT NULL DEFAULT 0,
  `isAnonymous` tinyint(1) NOT NULL DEFAULT 0,
  `isClosed` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_polls_group` (`groupId`),
  KEY `idx_polls_message` (`messageId`),
  KEY `idx_polls_creator` (`createdBy`),
  KEY `idx_polls_status` (`isClosed`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`createdBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Templates table
CREATE TABLE IF NOT EXISTS `message_templates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `category` varchar(50) NOT NULL DEFAULT 'general',
  `usageCount` int(11) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `lastUsedAt` bigint(20) DEFAULT NULL,
  `isFavorite` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_message_templates_user` (`userId`),
  KEY `idx_message_templates_category` (`category`),
  KEY `idx_message_templates_favorite` (`isFavorite`),
  FOREIGN KEY (`userId`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `lastSeen` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `isOnline` tinyint(1) NOT NULL DEFAULT 0,
  `updatedAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_settings` (`userId`),
  KEY `idx_user_settings_online` (`isOnline`),
  FOREIGN KEY (`userId`) REFERENCES `members`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Insert sample groups
INSERT INTO `groups` (`id`, `name`, `description`, `createdBy`, `createdAt`) VALUES
(1, 'Nhóm Toán Cao Cấp A1', 'Nhóm học tập môn Toán Cao Cấp lớp A1 - Học kỳ 1', 1, 1709251200000),
(2, 'Lập Trình Android 2024', 'Nhóm phát triển ứng dụng Android - Khóa 2024', 2, 1709337600000),
(3, 'English Conversation Club', 'Nhóm luyện tập tiếng Anh giao tiếp hàng tuần', 3, 1709424000000),
(4, 'Data Science Study Group', 'Nhóm nghiên cứu khoa học dữ liệu và machine learning', 1, 1709510400000);

-- Insert sample members
INSERT INTO `members` (`id`, `fullName`, `email`, `avatar`, `joinedAt`) VALUES
(1, 'Nguyễn Văn An', 'nguyenvanan@email.com', NULL, 1709251200000),
(2, 'Trần Thị Bình', 'tranthibinh@email.com', NULL, 1709251200000),
(3, 'Lê Hoàng Cường', 'lehoangcuong@email.com', NULL, 1709251200000),
(4, 'Phạm Thị Dung', 'phamthidung@email.com', NULL, 1709251200000),
(5, 'Hoàng Văn Em', 'hoangvanem@email.com', NULL, 1709251200000),
(6, 'Vũ Thị Phương', 'vuthiphuong@email.com', NULL, 1709251200000),
(7, 'Đặng Minh Quân', 'dangminhquan@email.com', NULL, 1709251200000),
(8, 'Bùi Thị Hoa', 'buithihoa@email.com', NULL, 1709251200000);

-- Insert sample assignments
INSERT INTO `assignments` (`id`, `groupId`, `title`, `description`, `dueDate`, `priority`, `status`, `createdBy`, `createdAt`) VALUES
(1, 1, 'Bài tập Đạo hàm và Vi phân', 'Giải các bài tập từ 1-25 trong sách giáo khoa, tập trung vào quy tắc đạo hàm cơ bản', '2024-03-25', 'HIGH', 'TODO', 1, 1709251200000),
(2, 1, 'Bài tập Tích phân', 'Làm bài tập tích phân chương 5, bao gồm tích phân xác định và không xác định', '2024-03-30', 'MEDIUM', 'IN_PROGRESS', 2, 1709337600000),
(3, 2, 'Tạo ứng dụng Todo List', 'Phát triển ứng dụng quản lý công việc sử dụng Kotlin và Jetpack Compose', '2024-04-05', 'HIGH', 'TODO', 2, 1709424000000),
(4, 2, 'Thiết kế UI/UX cho ứng dụng', 'Tạo mockup và prototype cho ứng dụng mobile', '2024-04-10', 'MEDIUM', 'TODO', 3, 1709510400000),
(5, 3, 'Presentation về Travel', 'Chuẩn bị bài thuyết trình 10 phút về chủ đề du lịch', '2024-04-01', 'LOW', 'COMPLETED', 3, 1709596800000),
(6, 4, 'Phân tích dữ liệu bán hàng', 'Sử dụng Python và Pandas để phân tích dataset bán hàng', '2024-04-15', 'HIGH', 'IN_PROGRESS', 1, 1709683200000);

-- Insert sample chat rooms
INSERT INTO `chat_rooms` (`id`, `groupId`, `name`, `description`, `createdBy`, `chatType`, `createdAt`, `memberCount`) VALUES
(1, 1, 'Chat Nhóm Toán', 'Thảo luận chung về môn Toán Cao Cấp', 1, 'GROUP', 1709251200000, 4),
(2, 2, 'Android Dev Chat', 'Thảo luận về lập trình Android', 2, 'GROUP', 1709337600000, 5),
(3, 3, 'English Practice', 'English conversation practice', 3, 'GROUP', 1709424000000, 3),
(4, 4, 'Data Science Discussion', 'Thảo luận về khoa học dữ liệu', 1, 'GROUP', 1709510400000, 4);

-- Insert sample chat members
INSERT INTO `chat_members` (`chatRoomId`, `memberId`, `role`, `joinedAt`) VALUES
-- Chat Nhóm Toán
(1, 1, 'ADMIN', 1709251200000),
(1, 2, 'MEMBER', 1709251200000),
(1, 3, 'MEMBER', 1709251200000),
(1, 4, 'MODERATOR', 1709251200000),
-- Android Dev Chat
(2, 2, 'ADMIN', 1709337600000),
(2, 3, 'MEMBER', 1709337600000),
(2, 4, 'MEMBER', 1709337600000),
(2, 5, 'MEMBER', 1709337600000),
(2, 6, 'MEMBER', 1709337600000),
-- English Practice
(3, 3, 'ADMIN', 1709424000000),
(3, 6, 'MEMBER', 1709424000000),
(3, 7, 'MEMBER', 1709424000000),
-- Data Science Discussion
(4, 1, 'ADMIN', 1709510400000),
(4, 5, 'MEMBER', 1709510400000),
(4, 7, 'MEMBER', 1709510400000),
(4, 8, 'MEMBER', 1709510400000);

-- Insert sample messages
INSERT INTO `group_messages` (`id`, `groupId`, `senderId`, `senderName`, `content`, `messageType`, `timestamp`, `isPinned`) VALUES
-- Nhóm Toán messages
(1, 1, 1, 'Nguyễn Văn An', 'Chào mọi người! Chúng ta bắt đầu học nhóm môn Toán Cao Cấp nhé! 📚', 'TEXT', 1709251200000, 1),
(2, 1, 2, 'Trần Thị Bình', 'Chào bạn An! Mình đã sẵn sàng rồi. Hôm nay chúng ta học chương nào? 😊', 'TEXT', 1709254800000, 0),
(3, 1, 3, 'Lê Hoàng Cường', 'Mình nghĩ nên bắt đầu từ chương Đạo hàm. Ai có tài liệu không?', 'TEXT', 1709258400000, 0),
(4, 1, 4, 'Phạm Thị Dung', 'Mình có slide bài giảng của thầy, để mình gửi lên nhé!', 'TEXT', 1709262000000, 0),
(5, 1, 1, 'Nguyễn Văn An', '📝 Bài tập: Bài tập Đạo hàm và Vi phân\nHạn nộp: 25/03/2024\nTrạng thái: Chưa làm\n#assignment_1', 'TEXT', 1709265600000, 1),
-- Android Dev messages
(6, 2, 2, 'Trần Thị Bình', 'Chào team! Chúng ta bắt đầu dự án Android nhé! 🚀', 'TEXT', 1709337600000, 1),
(7, 2, 3, 'Lê Hoàng Cường', 'Awesome! Mình đã setup môi trường development rồi. Android Studio latest version.', 'TEXT', 1709341200000, 0),
(8, 2, 4, 'Phạm Thị Dung', 'Mình gặp lỗi khi sync Gradle. Có ai giúp được không? 😅', 'TEXT', 1709344800000, 0),
(9, 2, 5, 'Hoàng Văn Em', 'Bạn thử clean project rồi rebuild xem. Thường lỗi đó do cache.', 'TEXT', 1709348400000, 0),
(10, 2, 2, 'Trần Thị Bình', '📝 Bài tập: Tạo ứng dụng Todo List\nHạn nộp: 05/04/2024\nTrạng thái: Chưa làm\n#assignment_3', 'TEXT', 1709352000000, 0);

-- Insert sample message reactions
INSERT INTO `message_reactions` (`messageId`, `userId`, `emoji`, `createdAt`) VALUES
(1, 2, '👍', 1709251800000),
(1, 3, '❤️', 1709252400000),
(1, 4, '👍', 1709253000000),
(2, 1, '😊', 1709255400000),
(6, 3, '🚀', 1709338200000),
(6, 4, '👍', 1709338800000);

-- Insert sample assignment members
INSERT INTO `assignment_members` (`assignmentId`, `memberId`, `status`, `assignedAt`) VALUES
-- Bài tập Đạo hàm
(1, 1, 'TODO', 1709265600000),
(1, 2, 'IN_PROGRESS', 1709265600000),
(1, 3, 'TODO', 1709265600000),
(1, 4, 'TODO', 1709265600000),
-- Todo App
(3, 2, 'TODO', 1709424000000),
(3, 3, 'TODO', 1709424000000),
(3, 4, 'TODO', 1709424000000),
(3, 5, 'TODO', 1709424000000);

-- Insert sample document folders
INSERT INTO `document_folders` (`id`, `groupId`, `name`, `parentId`, `createdBy`) VALUES
(1, 1, 'Bài giảng', NULL, 1),
(2, 1, 'Bài tập', NULL, 1),
(3, 2, 'Source Code', NULL, 2),
(4, 2, 'Documentation', NULL, 2);

-- Insert sample documents
INSERT INTO `documents` (`id`, `groupId`, `title`, `fileName`, `filePath`, `fileType`, `fileSize`, `folderId`, `uploadedBy`, `uploaderName`) VALUES
(1, 1, 'Bài giảng Đạo hàm', 'dao_ham_slide.pdf', '/documents/1/dao_ham_slide.pdf', 'PDF', 2048576, 1, 1, 'Nguyễn Văn An'),
(2, 2, 'Android Architecture Guide', 'android_guide.pdf', '/documents/2/android_guide.pdf', 'PDF', 3072000, 4, 2, 'Trần Thị Bình');

-- Insert sample message templates
INSERT INTO `message_templates` (`id`, `userId`, `title`, `content`, `category`) VALUES
(1, 1, 'Chào buổi sáng', 'Chào mọi người! Chúc một ngày tốt lành! 🌅', 'greeting'),
(2, 1, 'Nhắc deadline', 'Nhắc nhở: Deadline {task} vào {date} nhé! ⏰', 'deadline'),
(3, 2, 'Cảm ơn', 'Cảm ơn mọi người! 🙏', 'general');

-- Insert sample user settings
INSERT INTO `user_settings` (`userId`, `theme`, `language`, `isOnline`) VALUES
(1, 'light', 'vi', 1),
(2, 'dark', 'vi', 1),
(3, 'light', 'en', 0),
(4, 'light', 'vi', 1);

-- Insert sample polls
INSERT INTO `polls` (`id`, `groupId`, `messageId`, `question`, `options`, `votes`, `createdBy`) VALUES
(1, 1, 5, 'Thời gian học nhóm phù hợp nhất?', '["Thứ 2, 19:00", "Thứ 4, 19:00", "Cuối tuần"]', '{"0": [1, 3], "1": [2]}', 1);

-- Update chat room statistics
UPDATE `chat_rooms` cr 
SET 
    `lastMessageId` = (
        SELECT `id` FROM `group_messages` gm 
        WHERE gm.`groupId` = cr.`groupId` 
        ORDER BY gm.`timestamp` DESC 
        LIMIT 1
    ),
    `lastMessageTime` = (
        SELECT `timestamp` FROM `group_messages` gm 
        WHERE gm.`groupId` = cr.`groupId` 
        ORDER BY gm.`timestamp` DESC 
        LIMIT 1
    );

COMMIT;

-- =====================================================
-- ADDITIONAL ADVANCED FEATURES TABLES
-- =====================================================

-- Mentions table
CREATE TABLE IF NOT EXISTS `mentions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `messageId` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `userName` varchar(255) NOT NULL,
  `startIndex` int(11) NOT NULL,
  `endIndex` int(11) NOT NULL,
  `isRead` tinyint(1) NOT NULL DEFAULT 0,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_mentions_message` (`messageId`),
  KEY `idx_mentions_user` (`userId`),
  KEY `idx_mentions_read` (`isRead`),
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`userId`) REFERENCES `members`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_link_previews_message` (`messageId`),
  KEY `idx_link_previews_url` (`url`(255)),
  FOREIGN KEY (`messageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `status` enum('pending','sent','cancelled','failed') NOT NULL DEFAULT 'pending',
  `replyToMessageId` bigint(20) DEFAULT NULL,
  `replyToContent` text DEFAULT NULL,
  `replyToSenderName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_messages_group` (`groupId`),
  KEY `idx_scheduled_messages_sender` (`senderId`),
  KEY `idx_scheduled_messages_status` (`status`),
  KEY `idx_scheduled_messages_time` (`scheduledTime`),
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`senderId`) REFERENCES `members`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`replyToMessageId`) REFERENCES `group_messages`(`id`) ON DELETE SET NULL
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
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  KEY `idx_sticker_packs_category` (`category`),
  KEY `idx_sticker_packs_installed` (`isInstalled`),
  KEY `idx_sticker_packs_favorite` (`isFavorite`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stickers table
CREATE TABLE IF NOT EXISTS `stickers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `packId` bigint(20) NOT NULL,
  `imageUrl` varchar(500) NOT NULL,
  `thumbnailUrl` varchar(500) DEFAULT NULL,
  `keywords` json DEFAULT NULL,
  `usageCount` int(11) NOT NULL DEFAULT 0,
  `lastUsedAt` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_stickers_pack` (`packId`),
  KEY `idx_stickers_usage` (`usageCount`),
  FOREIGN KEY (`packId`) REFERENCES `sticker_packs`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Thread table (for threaded conversations)
CREATE TABLE IF NOT EXISTS `message_threads` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parentMessageId` bigint(20) NOT NULL,
  `groupId` bigint(20) NOT NULL,
  `threadTitle` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  `lastMessageAt` bigint(20) DEFAULT NULL,
  `messageCount` int(11) NOT NULL DEFAULT 0,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_message_threads_parent` (`parentMessageId`),
  KEY `idx_message_threads_group` (`groupId`),
  KEY `idx_message_threads_active` (`isActive`),
  FOREIGN KEY (`parentMessageId`) REFERENCES `group_messages`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`groupId`) REFERENCES `groups`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`createdBy`) REFERENCES `members`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignment Chat table (linking assignments to chat discussions)
CREATE TABLE IF NOT EXISTS `assignment_chats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignmentId` bigint(20) NOT NULL,
  `chatRoomId` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT (UNIX_TIMESTAMP() * 1000),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_assignment_chat` (`assignmentId`, `chatRoomId`),
  KEY `idx_assignment_chats_assignment` (`assignmentId`),
  KEY `idx_assignment_chats_chat` (`chatRoomId`),
  FOREIGN KEY (`assignmentId`) REFERENCES `assignments`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`chatRoomId`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SAMPLE DATA FOR NEW TABLES
-- =====================================================

-- Insert sample sticker packs
INSERT INTO `sticker_packs` (`id`, `name`, `author`, `thumbnailUrl`, `isInstalled`, `category`, `stickerCount`) VALUES
(1, 'Emoji Classic', 'System', '/stickers/emoji/thumb.png', 1, 'emoji', 50),
(2, 'Cute Animals', 'StickerStudio', '/stickers/animals/thumb.png', 1, 'cute', 30),
(3, 'Funny Memes', 'MemeFactory', '/stickers/memes/thumb.png', 0, 'funny', 25);

-- Insert sample stickers
INSERT INTO `stickers` (`id`, `packId`, `imageUrl`, `thumbnailUrl`, `keywords`) VALUES
(1, 1, '/stickers/emoji/happy.png', '/stickers/emoji/happy_thumb.png', '["happy", "smile", "joy"]'),
(2, 1, '/stickers/emoji/sad.png', '/stickers/emoji/sad_thumb.png', '["sad", "cry", "upset"]'),
(3, 2, '/stickers/animals/cat.png', '/stickers/animals/cat_thumb.png', '["cat", "cute", "pet"]'),
(4, 2, '/stickers/animals/dog.png', '/stickers/animals/dog_thumb.png', '["dog", "cute", "pet"]');

-- Insert sample message templates
INSERT INTO `message_templates` (`id`, `userId`, `title`, `content`, `category`) VALUES
(4, 1, 'Deadline Reminder', 'Nhắc nhở: Deadline {task} vào ngày {date}. Mọi người nhớ hoàn thành đúng hạn nhé! ⏰', 'deadline'),
(5, 2, 'Meeting Invitation', 'Họp nhóm lúc {time} tại {location}. Link meeting: {link}', 'meeting'),
(6, 1, 'Assignment Update', '📝 Cập nhật bài tập: {title}\nTrạng thái: {status}\nGhi chú: {note}', 'assignment');

-- Insert sample scheduled messages
INSERT INTO `scheduled_messages` (`id`, `groupId`, `senderId`, `senderName`, `content`, `scheduledTime`, `status`) VALUES
(1, 1, 1, 'Nguyễn Văn An', 'Nhắc nhở: Hôm nay là deadline nộp bài tập Toán! 📚', 1709596800000, 'pending'),
(2, 2, 2, 'Trần Thị Bình', 'Họp nhóm lúc 19:00 hôm nay. Mọi người chuẩn bị sẵn sàng nhé!', 1709683200000, 'pending');

-- Insert sample mentions
INSERT INTO `mentions` (`messageId`, `userId`, `userName`, `startIndex`, `endIndex`) VALUES
(7, 1, 'Nguyễn Văn A', 0, 13),
(10, 2, 'Trần Thị Bình', 25, 38);

-- Update sticker pack counts
UPDATE `sticker_packs` SET `stickerCount` = (
    SELECT COUNT(*) FROM `stickers` WHERE `packId` = `sticker_packs`.`id`
);
