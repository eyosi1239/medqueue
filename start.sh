#!/bin/bash

echo "Installing Python dependencies..."
pip3 install flask --quiet

echo "Starting Python DSA server (port 5000)..."
cd python-dsa && python3 app.py &
PYTHON_PID=$!
cd ..

sleep 2

echo "Starting Java backend (port 8080)..."
cd java-backend && bash run.sh &
JAVA_PID=$!
cd ..

echo ""
echo "Both servers running."
echo "  Python queue: http://localhost:5001"
echo "  Java backend: http://localhost:8080"
echo ""
echo "Open frontend/index.html in your browser."
echo ""
echo "Press Ctrl+C to stop both servers."

trap "kill $PYTHON_PID $JAVA_PID 2>/dev/null; echo 'Servers stopped.'" EXIT
wait
