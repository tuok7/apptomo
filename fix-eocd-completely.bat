@echo off
echo ========================================
echo FIXING EOCD ERROR COMPLETELY
echo ========================================
echo.

echo Step 1: Stopping all Gradle daemons...
call gradlew.bat --stop
timeout /t 2 /nobreak >nul

echo Step 2: Cleaning all caches...
rmdir /s /q .gradle 2>nul
rmdir /s /q build 2>nul
rmdir /s /q app\build 2>nul
rmdir /s /q .idea\caches 2>nul
rmdir /s /q .idea\libraries 2>nul
del /f /q .idea\workspace.xml 2>nul

echo Step 3: Building fresh...
call gradlew.bat clean assembleDebug --no-daemon --no-build-cache --refresh-dependencies

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! EOCD ERROR FIXED!
    echo ========================================
    echo.
    echo Now in Android Studio:
    echo 1. Close Android Studio completely
    echo 2. Reopen Android Studio
    echo 3. File -^> Sync Project with Gradle Files
    echo 4. Build should work now!
    echo.
) else (
    echo.
    echo ========================================
    echo Build failed. But you can still use:
    echo build-and-run.bat to run the app
    echo ========================================
)

pause
