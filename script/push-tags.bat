@echo off

for /f "delims=" %%r in ('git remote') do (
    echo.
    echo Pushing tags to %%r...
    git push %%r --tags
)

echo.
pause
