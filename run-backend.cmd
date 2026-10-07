@echo off
rem ================= 教务器材预约系统 · 后端启动 =================
rem 使用本机用户级工具链（免安装 zip 版），无需配置系统环境变量
set "JAVA_HOME=%USERPROFILE%\.local\opt\jdk"
set "PATH=%JAVA_HOME%\bin;%USERPROFILE%\.local\opt\maven\bin;%PATH%"

cd /d "%~dp0backend"

echo [run-backend] Java 版本：
java -version
echo.
echo [run-backend] 启动 Spring Boot（首次运行需下载依赖，请耐心等待）...
echo [run-backend] 就绪标志：看到 "Started ReservationApplication"，端口 8080
call mvn spring-boot:run
pause
