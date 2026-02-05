@echo off
echo ========================================
echo SETUP PHP BACKEND
echo ========================================
echo.

set XAMPP_PATH=C:\xampp\htdocs
set PROJECT_NAME=myapp_api

echo Checking XAMPP installation...
if not exist "%XAMPP_PATH%" (
    echo ERROR: XAMPP not found at %XAMPP_PATH%
    echo Please install XAMPP or update XAMPP_PATH in this script
    pause
    exit /b 1
)

echo.
echo Copying PHP backend to XAMPP...
xcopy /E /I /Y "php_backend\*" "%XAMPP_PATH%\%PROJECT_NAME%\"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Backend copied to:
    echo %XAMPP_PATH%\%PROJECT_NAME%\
    echo ========================================
    echo.
    echo Next steps:
    echo 1. Start XAMPP Control Panel
    echo 2. Start Apache and MySQL
    echo 3. Open browser: http://localhost/myapp_api/
    echo 4. Import database: php_backend/database/create_database.sql
    echo.
    echo Test API:
    echo http://localhost/myapp_api/api/auth/register.php
    echo.
) else (
    echo.
    echo ERROR: Failed to copy files
    echo Please run this script as Administrator
)

pause
