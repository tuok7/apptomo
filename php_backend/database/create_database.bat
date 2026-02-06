@echo off
echo ========================================
echo   TAO DATABASE
echo ========================================
echo.

set MYSQL_PATH=C:\xampp\mysql\bin
set DB_NAME=myapp_db
set DB_USER=root
set DB_PASS=
set SQL_FILE=%~dp0create_database.sql

if not exist "%SQL_FILE%" (
    echo [ERROR] Khong tim thay file: %SQL_FILE%
    pause
    exit /b 1
)

echo [1/2] Dang tao database %DB_NAME%...
"%MYSQL_PATH%\mysql.exe" -u%DB_USER% < "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Database da duoc tao thanh cong!
) else (
    echo [ERROR] Tao database that bai!
    echo Kiem tra: XAMPP da chay chua?
    pause
    exit /b 1
)

echo.
echo [2/2] Kiem tra database...
"%MYSQL_PATH%\mysql.exe" -u%DB_USER% -e "USE %DB_NAME%; SHOW TABLES;"

echo.
echo ========================================
echo   HOAN THANH!
echo ========================================
echo.
echo Database: %DB_NAME%
echo Tables da duoc tao thanh cong
echo.
echo Ban co the restore du lieu: restore_database.bat
echo.
pause
