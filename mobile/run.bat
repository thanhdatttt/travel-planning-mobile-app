@echo off
setlocal

echo ==============================
echo ANDROID RUN SCRIPT (PRO)
echo ==============================

:: ===== CONFIG =====
set AVD_NAME=test
set PACKAGE_NAME=com.your.package
set MAIN_ACTIVITY=.MainActivity

:: ===== CHECK ADB =====
echo.
echo [1] Checking ADB...
adb devices > temp.txt

findstr /R "emulator" temp.txt > nul
if %errorlevel% neq 0 (
    echo No emulator found. Starting emulator...

    start "" emulator -avd %AVD_NAME%

    echo Waiting for emulator to boot...
    adb wait-for-device

    timeout /t 10 > nul
) else (
    echo Emulator already running.
)

del temp.txt

:: ===== BUILD & INSTALL =====
echo.
echo [2] Building & Installing APK...
call gradlew installDebug

if %errorlevel% neq 0 (
    echo BUILD FAILED ❌
    pause
    exit /b
)

echo INSTALL SUCCESS ✅

:: ===== OPEN APP =====
echo.
echo [3] Launching App...
adb shell am start -n %PACKAGE_NAME%/%MAIN_ACTIVITY%

:: ===== LOGCAT FILTER =====
echo.
echo [4] Showing logs (filtered by package)...
adb logcat | findstr %PACKAGE_NAME%

pause