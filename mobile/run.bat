@echo off
setlocal

echo ==============================
echo ANDROID RUN SCRIPT (PRO)
echo ==============================

:: ===== CONFIG =====
:: Đã sửa lại đúng package name của bạn
set AVD_NAME=test
set PACKAGE_NAME=com.example.travelplanning
:: Đảm bảo file MainActivity của bạn có tên như thế này (nếu khác thì sửa lại nhé)
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

:: Dọn dẹp cache cũ để tránh lỗi "ngáo" của VSCode/Lombok
call gradlew clean 
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
:: Xóa logcat cũ trước khi show log mới để màn hình console không bị rác
adb logcat -c 
adb logcat | findstr %PACKAGE_NAME%

pause