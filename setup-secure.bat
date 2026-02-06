@echo off
echo ========================================
echo   SETUP BAO MAT CHO PROJECT
echo ========================================
echo.

echo [1/4] Setup config files...
cd php_backend\config
call setup_config.bat
cd ..\..

echo.
echo [2/4] Kiem tra thu muc uploads...
if not exist "php_backend\uploads\attachments" (
    mkdir "php_backend\uploads\attachments"
    echo [+] Da tao thu muc attachments
)
if not exist "php_backend\uploads\documents" (
    mkdir "php_backend\uploads\documents"
    echo [+] Da tao thu muc documents
)
echo [OK] Thu muc uploads da san sang

echo.
echo [3/4] Copy backend files vao XAMPP...
cd php_backend
call setup_backend.bat
cd ..

echo.
echo [4/4] Tao database...
echo Ban co muon tao database ngay bay gio? (Y/N)
set /p create_db="Chon: "
if /i "%create_db%"=="Y" (
    cd php_backend\database
    call create_database.bat
    cd ..\..
)

echo.
echo ========================================
echo   HOAN THANH SETUP!
echo ========================================
echo.
echo CAC FILE DA DUOC TAO:
echo - php_backend\config\config.php
echo - php_backend\config\.env
echo - C:\xampp\htdocs\myapp_api\
echo.
echo CAC BUOC TIEP THEO:
echo 1. Sua file: php_backend\config\config.php (thong tin database)
echo 2. Chay XAMPP (Apache + MySQL)
echo 3. Restore data: cd php_backend\database ^&^& restore_database.bat
echo 4. Build app: gradlew.bat assembleDebug
echo.
echo LUU Y BAO MAT:
echo - File config.php va .env KHONG duoc commit len Git
echo - File uploads/* KHONG duoc commit len Git
echo - Chi commit file .example va .gitkeep
echo.
pause
