@echo off

echo.
mvn -B release:prepare release:perform

echo.
pause
