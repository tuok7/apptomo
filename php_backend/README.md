# 🚀 Hướng dẫn cài đặt Backend PHP cho XAMPP

## 📋 Bước 1: Cài đặt XAMPP
1. Tải XAMPP từ: https://www.apachefriends.org/
2. Cài đặt XAMPP
3. Mở XAMPP Control Panel
4. Khởi động **Apache** và **MySQL**

## 🗄️ Bước 2: Tạo Database
1. Mở trình duyệt và truy cập: http://localhost/phpmyadmin
2. Nhấn **New** để tạo database mới
3. Đặt tên database: `myapp_db`
4. Chọn Collation: `utf8mb4_unicode_ci`
5. Nhấn **Create**
6. Chọn database `myapp_db` vừa tạo
7. Vào tab **SQL**
8. Copy toàn bộ nội dung file `create_database.sql` và paste vào
9. Nhấn **Go** để chạy

## 📁 Bước 3: Copy file PHP
1. Copy toàn bộ thư mục `php_backend` 
2. Paste vào `C:\xampp\htdocs\`
3. Đổi tên thư mục thành `myapp_api`
4. Đường dẫn cuối cùng: `C:\xampp\htdocs\myapp_api\`

## ✅ Bước 4: Test API
1. Mở trình duyệt
2. Truy cập: http://localhost/myapp_api/test_api.html
3. Test đăng nhập với:
   - Email: `test@example.com`
   - Password: `123456`
4. Test đăng ký với thông tin mới

## 📱 Bước 5: Cấu hình Android App

### Nếu dùng Android Emulator:
- IP đã được cấu hình sẵn: `10.0.2.2`
- URL: `http://10.0.2.2/myapp_api/`
- Không cần thay đổi gì

### Nếu dùng thiết bị thật:
1. Mở **Command Prompt** (CMD)
2. Gõ lệnh: `ipconfig`
3. Tìm **IPv4 Address** (ví dụ: 192.168.1.100)
4. Mở file `RetrofitClient.kt` trong Android Studio
5. Tìm dòng:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2/myapp_api/"
   ```
6. Thay đổi thành:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100/myapp_api/"
   ```
   (Thay 192.168.1.100 bằng IP máy tính của bạn)
7. **Quan trọng**: Đảm bảo điện thoại và máy tính cùng mạng WiFi

## 🔐 Tài khoản test mặc định
- Email: `test@example.com`
- Password: `123456`

## 🛠️ Cấu trúc file
```
php_backend/
├── config.php           # Cấu hình database
├── login.php           # API đăng nhập
├── register.php        # API đăng ký
├── create_database.sql # Script tạo database
├── test_api.html       # Trang test API
└── README.md          # File này
```

## ⚠️ Lưu ý quan trọng
- Đảm bảo Apache và MySQL đang chạy trong XAMPP
- Kiểm tra firewall không chặn port 80
- Nếu dùng thiết bị thật, máy tính và điện thoại phải cùng mạng WiFi
- Nếu lỗi kết nối, kiểm tra lại IP address

## 🐛 Troubleshooting

### Lỗi: "Connection refused"
- Kiểm tra Apache đã chạy chưa
- Kiểm tra firewall
- Thử truy cập http://localhost/myapp_api/login.php trên trình duyệt

### Lỗi: "Database connection failed"
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra tên database trong `config.php`
- Đảm bảo đã chạy script `create_database.sql`

### Lỗi: "Email đã được sử dụng"
- Email đã tồn tại trong database
- Thử email khác hoặc xóa user cũ trong phpMyAdmin

## 📞 Hỗ trợ
Nếu gặp vấn đề, kiểm tra:
1. XAMPP Control Panel - Apache và MySQL có màu xanh
2. http://localhost/phpmyadmin - Có truy cập được không
3. http://localhost/myapp_api/test_api.html - Có hiển thị không
4. Console trong trình duyệt (F12) - Có lỗi gì không

## 🎉 Hoàn tất!
Sau khi hoàn thành các bước trên, app Android của bạn đã có thể:
- ✅ Đăng ký tài khoản mới
- ✅ Đăng nhập
- ✅ Lưu thông tin user vào MySQL
- ✅ Xác thực mật khẩu an toàn với bcrypt


## 🆕 Cập nhật mới: Chức năng Quên mật khẩu

### Thêm bảng mới vào database
Chạy SQL sau trong phpMyAdmin (chọn database `myapp_db` -> tab SQL):

```sql
CREATE TABLE IF NOT EXISTS password_reset_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_code (code),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### API Endpoints mới
- `POST /forgot_password.php` - Gửi mã xác nhận đến email
- `POST /reset_password.php` - Đặt lại mật khẩu với mã xác nhận

### Cách hoạt động
1. Người dùng nhập email trong màn hình "Quên mật khẩu"
2. Hệ thống tạo mã 6 số ngẫu nhiên
3. Mã được lưu vào database với thời hạn 15 phút
4. Trong môi trường development, mã sẽ được trả về trong response (field `debug_code`)
5. Người dùng nhập mã xác nhận và mật khẩu mới
6. Hệ thống kiểm tra mã và cập nhật mật khẩu

### Lưu ý cho Production
Trong môi trường production, bạn cần:
1. Xóa dòng `'debug_code' => $verificationCode` trong `forgot_password.php`
2. Cấu hình gửi email thực tế (PHPMailer, SendGrid, AWS SES, etc.)
3. Thêm rate limiting để tránh spam
4. Sử dụng CAPTCHA
5. Bật HTTPS

### Test chức năng
1. Mở app Android
2. Nhấn "Quên mật khẩu?" trên màn hình đăng nhập
3. Nhập email: `test@example.com`
4. Nhấn "Gửi mã xác nhận"
5. Kiểm tra response trong Logcat để lấy mã (trong development)
6. Nhập mã 6 số và mật khẩu mới
7. Đăng nhập với mật khẩu mới


## 📁 Cập nhật mới: Lưu trữ Tin nhắn và Tài liệu

### Các bảng mới đã thêm

#### 1. Bảng lưu trữ tài liệu
- `group_documents` - Tài liệu nhóm
- `document_versions` - Phiên bản tài liệu
- `document_shares` - Chia sẻ tài liệu

#### 2. Bảng tin nhắn nâng cao
- `message_attachments` - Tệp đính kèm tin nhắn
- `message_reactions` - Phản ứng tin nhắn (like, love, haha, wow, sad, angry)
- `message_read_status` - Trạng thái đã đọc
- `pinned_messages` - Tin nhắn được ghim

### Cập nhật Database

Chạy các lệnh SQL sau trong phpMyAdmin:

```sql
-- Bảng tệp đính kèm tin nhắn
CREATE TABLE IF NOT EXISTS message_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES group_chat(id) ON DELETE CASCADE,
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng phản ứng tin nhắn
CREATE TABLE IF NOT EXISTS message_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES group_chat(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_message_user_reaction (message_id, user_id, reaction),
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng tài liệu nhóm
CREATE TABLE IF NOT EXISTS group_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng trạng thái đã đọc
CREATE TABLE IF NOT EXISTS message_read_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES group_chat(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_message_user (message_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng tin nhắn được ghim
CREATE TABLE IF NOT EXISTS pinned_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    pinned_by BIGINT NOT NULL,
    pinned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES group_chat(id) ON DELETE CASCADE,
    FOREIGN KEY (pinned_by) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_message (group_id, message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

Hoặc import lại file `create_database.sql` đã được cập nhật.

### API Endpoints mới

#### Quản lý Tài liệu
- `GET /documents.php?groupId={id}` - Lấy danh sách tài liệu
- `GET /documents.php?groupId={id}&fileType={type}` - Lọc theo loại file
- `POST /documents.php` - Tải lên tài liệu mới
- `PUT /documents.php` - Cập nhật thông tin tài liệu
- `DELETE /documents.php?id={id}&userId={id}` - Xóa tài liệu

#### Upload File
- `POST /upload.php` - Upload file (tài liệu hoặc tệp đính kèm)
  - Hỗ trợ: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT, CSV
  - Hình ảnh: JPG, PNG, GIF, SVG, WEBP
  - Video: MP4, AVI, MOV, MKV
  - Audio: MP3, WAV, OGG
  - Nén: ZIP, RAR, 7Z
  - Kích thước tối đa: 50MB

#### Phản ứng Tin nhắn
- `GET /message_reactions.php?messageId={id}` - Lấy reactions của tin nhắn
- `POST /message_reactions.php` - Thêm reaction
  - Reactions: like, love, haha, wow, sad, angry
- `DELETE /message_reactions.php?messageId={id}&userId={id}&reaction={type}` - Xóa reaction

#### Tệp đính kèm Tin nhắn
- `GET /message_attachments.php?messageId={id}` - Lấy tệp đính kèm
- `POST /message_attachments.php` - Thêm tệp đính kèm
- `DELETE /message_attachments.php?id={id}` - Xóa tệp đính kèm

### Cấu trúc thư mục Upload

```
php_backend/
├── uploads/
│   ├── documents/        # Tài liệu nhóm
│   │   └── [files]
│   └── attachments/      # Tệp đính kèm tin nhắn
│       └── [files]
```

### Ví dụ sử dụng API

#### 1. Upload file
```bash
curl -X POST http://localhost/myapp_api/upload.php \
  -F "file=@document.pdf" \
  -F "type=document"
```

Response:
```json
{
  "success": true,
  "message": "Tải file lên thành công",
  "data": {
    "fileName": "document.pdf",
    "uniqueFileName": "abc123_1234567890.pdf",
    "filePath": "uploads/documents/abc123_1234567890.pdf",
    "fileType": "application/pdf",
    "fileSize": 1024000,
    "fileExtension": "pdf"
  }
}
```

#### 2. Tạo tài liệu
```json
POST /documents.php
{
  "groupId": 1,
  "title": "Tài liệu dự án",
  "description": "Tài liệu chi tiết về dự án",
  "fileName": "document.pdf",
  "filePath": "uploads/documents/abc123_1234567890.pdf",
  "fileType": "application/pdf",
  "fileSize": 1024000,
  "uploadedBy": 1
}
```

#### 3. Thêm reaction
```json
POST /message_reactions.php
{
  "messageId": 1,
  "userId": 2,
  "reaction": "like"
}
```

#### 4. Lấy reactions
```bash
GET /message_reactions.php?messageId=1
```

Response:
```json
{
  "success": true,
  "data": [
    {
      "reaction": "like",
      "count": 3,
      "users": [
        {"userId": 2, "userName": "Nguyễn Văn A"},
        {"userId": 3, "userName": "Trần Thị B"}
      ]
    },
    {
      "reaction": "love",
      "count": 1,
      "users": [
        {"userId": 4, "userName": "Lê Văn C"}
      ]
    }
  ]
}
```

### Lưu ý quan trọng

1. **Bảo mật file upload**
   - Kiểm tra loại file trước khi upload
   - Giới hạn kích thước file (hiện tại: 50MB)
   - Đổi tên file để tránh trùng lặp
   - Không cho phép upload file thực thi (.php, .exe, .sh)

2. **Quản lý dung lượng**
   - Định kỳ dọn dẹp file không sử dụng
   - Theo dõi dung lượng thư mục uploads
   - Cân nhắc sử dụng cloud storage (AWS S3, Google Cloud Storage)

3. **Performance**
   - Sử dụng CDN cho file tĩnh
   - Nén file trước khi lưu trữ
   - Tạo thumbnail cho hình ảnh

4. **Backup**
   - Backup thư mục uploads định kỳ
   - Lưu trữ metadata trong database
   - Có kế hoạch khôi phục dữ liệu

### Tính năng nâng cao (tùy chọn)

1. **Xem trước file**
   - PDF viewer
   - Image gallery
   - Video player

2. **Tìm kiếm tài liệu**
   - Full-text search
   - Lọc theo loại file, ngày tải lên
   - Sắp xếp theo tên, kích thước, lượt tải

3. **Phân quyền**
   - Chỉ admin mới được xóa tài liệu
   - Giới hạn dung lượng upload theo role
   - Private/Public documents

4. **Thông báo**
   - Thông báo khi có tài liệu mới
   - Thông báo khi có reaction mới
   - Email notification
