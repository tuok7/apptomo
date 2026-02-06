@echo off
echo ========================================
echo   SETUP CONFIG FILES
echo ========================================
echo.

echo [1/2] Kiem tra file config.php...
if not exist "%~dp0config.php" (
    echo [!] Chua co file config.php
    echo [+] Dang tao tu file mau...
    copy "%~dp0config.example.php" "%~dp0config.php"
    echo [SUCCESS] Da tao file config.php
    echo.
    echo [WARNING] Vui long sua thong tin database:
    echo - DB_HOST: localhost
    echo - DB_USER: root
    echo - DB_PASS: (de trong neu khong co password)
    echo - DB_NAME: myapp_db
    echo.
) else (
    echo [OK] File config.php da ton tai
    echo.
)

echo [2/2] Kiem tra file .env...
if not exist "%~dp0.env" (
    echo [!] Chua co file .env
    echo [+] Dang tao tu file mau...
    copy "%~dp0.env.example" "%~dp0.env"
    echo [SUCCESS] Da tao file .env
    echo.
) else (
    echo [OK] File .env da ton tai
    echo.
)

echo ========================================
echo   HOAN THANH!
echo ========================================
echo.
echo LUU Y BAO MAT:
echo - File config.php va .env KHONG duoc commit len Git
echo - Chi commit file .example
echo - Moi nguoi clone project phai chay script nay
echo.
pause
