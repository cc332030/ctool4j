@echo off

echo.
mvn -B release:clean release:prepare release:perform

git push

mvn deploy

echo.
pause
