@echo off
echo ========================================
echo   DocShare Frontend - Development Mode
echo ========================================
echo.

echo [1/3] Checking Node.js...
node --version
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed!
    echo Please install Node.js 18+ from https://nodejs.org/
    pause
    exit /b 1
)
echo.

echo [2/3] Installing dependencies...
call npm install
if %errorlevel% neq 0 (
    echo ERROR: npm install failed!
    pause
    exit /b 1
)
echo.

echo [3/3] Starting development server...
echo.
echo Frontend will be available at: http://localhost:4200
echo Press Ctrl+C to stop the server
echo.
call npm run dev
