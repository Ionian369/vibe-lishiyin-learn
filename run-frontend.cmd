@echo off
rem ================= 教务器材预约系统 · 前端启动 =================
rem 使用本机用户级工具链（免安装 zip 版），无需配置系统环境变量
set "PATH=%USERPROFILE%\.local\opt\node;%PATH%"

cd /d "%~dp0frontend"

where node >nul 2>nul
if errorlevel 1 (
  echo [run-frontend] 错误：未找到 Node.js，请先完成工具链部署
  pause
  exit /b 1
)

if not exist node_modules (
  echo [run-frontend] 首次运行，安装依赖...
  call npm install
)

echo [run-frontend] 启动 Vite 开发服务器...
echo [run-frontend] 就绪标志：看到 "Local: http://localhost:5173/"
call npm run dev
pause
