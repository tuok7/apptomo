@echo off
echo ========================================
echo   BACKUP DATABASE VA COMMIT GIT
echo ========================================
echo.

REM Backup database
echo [1/3] Dang backup database...
cd php_backend\database
call backup_database.bat
cd ..\..

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Backup that bai!
    pause
    exit /b 1
)

echo.
echo [2/3] Dang add files vao Git...
git add .

echo.
echo [3/3] Nhap commit message:
set /p commit_msg="Message: "

if "%commit_msg%"=="" (
    set commit_msg=Update code and database
)

git commit -m "%commit_msg%"

echo.
echo ========================================
echo   HOAN THANH!
echo ========================================
echo.
echo Database da duoc backup: php_backend/database/myapp_db_backup.sql
echo Code da duoc commit voi message: %commit_msg%
echo.
echo Ban co the chay: git push
echo.
pause
