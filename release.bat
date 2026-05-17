@echo off

echo.
mvn -B release:clean release:prepare release:perform

git push

echo.
pause
