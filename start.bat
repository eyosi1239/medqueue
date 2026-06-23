@echo off
echo Starting Python DSA Server (port 5000)...
start "Python DSA" cmd /k "cd python-dsa && pip install flask && python app.py"
timeout /t 3 /nobreak > nul
echo Starting Java Backend (port 8080)...
start "Java Backend" cmd /k "cd java-backend && run.bat"
echo.
echo Both servers starting! Wait 15 seconds then open frontend\index.html
pause
