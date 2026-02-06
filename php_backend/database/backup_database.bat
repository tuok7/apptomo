@echo off
echo ========================================
echo   BACKUP DATABASE
echo ========================================
echo.

REM Thay doi cac thong tin nay neu can
set MYSQL_PATH=C:\xampp\mysql\bin
set DB_NAME=myapp_db
set DB_USER=root
set DB_PASS=
set BACKUP_FILE=%~dp0myapp_db_backup.sql

echo Dang backup database %DB_NAME%...
"%MYSQL_PATH%\mysqldump.exe" -u%DB_USER% %DB_NAME% > "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database da duoc backup thanh cong!
    echo File: %BACKUP_FILE%
    echo.
    echo Ban co the commit file nay len Git de dong bo du lieu
) else (
    echo.
    echo [ERROR] Backup that bai!
    echo Kiem tra:
    echo - XAMPP da chay chua?
    echo - Database %DB_NAME% da ton tai chua?
)

echo.
pause
