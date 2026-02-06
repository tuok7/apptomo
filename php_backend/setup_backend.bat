@echo off
echo ========================================
echo   SETUP PHP BACKEND
echo ========================================
echo.

REM Kiem tra XAMPP
if not exist "C:\xampp\htdocs" (
    echo [ERROR] Khong tim thay XAMPP!
    echo Vui long cai dat XAMPP truoc.
    echo Download: https://www.apachefriends.org/
    pause
    exit /b 1
)

echo [1/4] Dang copy backend files vao XAMPP...
xcopy /E /I /Y "%~dp0" "C:\xampp\htdocs\myapp_api\"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Da copy thanh cong!
) else (
    echo [ERROR] Copy that bai!
    pause
    exit /b 1
)

echo.
echo [2/4] Kiem tra cau hinh...
if not exist "C:\xampp\htdocs\myapp_api\config\config.php" (
    echo [!] Chua co file config.php
    if exist "C:\xampp\htdocs\myapp_api\config\config.example.php" (
        echo [+] Dang tao tu file mau...
        copy "C:\xampp\htdocs\myapp_api\config\config.example.php" "C:\xampp\htdocs\myapp_api\config\config.php"
        echo [SUCCESS] Da tao file config.php
        echo [WARNING] Vui long sua thong tin database trong file config.php
    )
)

echo.
echo [3/4] Tao thu muc uploads...
if not exist "C:\xampp\htdocs\myapp_api\uploads\attachments" (
    mkdir "C:\xampp\htdocs\myapp_api\uploads\attachments"
)
if not exist "C:\xampp\htdocs\myapp_api\uploads\documents" (
    mkdir "C:\xampp\htdocs\myapp_api\uploads\documents"
)
echo [OK] Thu muc uploads da san sang

echo.
echo [4/4] Hoan thanh!
echo.
echo ========================================
echo   BACKEND DA SAN SANG!
echo ========================================
echo.
echo Duong dan: C:\xampp\htdocs\myapp_api\
echo API URL: http://localhost/myapp_api/api/
echo.
echo CAC BUOC TIEP THEO:
echo 1. Bat XAMPP (Apache + MySQL)
echo 2. Tao database: cd database ^&^& create_database.bat
echo 3. Restore data: cd database ^&^& restore_database.bat
echo 4. Test API: http://localhost/myapp_api/api/auth/login.php
echo.
pause
