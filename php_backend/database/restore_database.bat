@echo off
echo ========================================
echo   RESTORE DATABASE
echo ========================================
echo.

REM Thay doi cac thong tin nay neu can
set MYSQL_PATH=C:\xampp\mysql\bin
set DB_NAME=myapp_db
set DB_USER=root
set DB_PASS=
set BACKUP_FILE=%~dp0myapp_db_backup.sql

if not exist "%BACKUP_FILE%" (
    echo [ERROR] Khong tim thay file backup: %BACKUP_FILE%
    echo.
    echo Vui long chay backup_database.bat truoc hoac pull code tu Git
    echo.
    pause
    exit /b 1
)

echo Dang restore database %DB_NAME% tu backup...
"%MYSQL_PATH%\mysql.exe" -u%DB_USER% %DB_NAME% < "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database da duoc restore thanh cong!
    echo Tat ca du lieu da duoc khoi phuc
) else (
    echo.
    echo [ERROR] Restore that bai!
    echo Kiem tra:
    echo - XAMPP da chay chua?
    echo - Database %DB_NAME% da duoc tao chua? (chay create_database.bat)
)

echo.
pause
