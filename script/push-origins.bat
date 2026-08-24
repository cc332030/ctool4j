@echo off

for /f "delims=" %%r in ('git remote') do (
    echo.
    echo Pushing to %%r...
    git push %%r --all
    echo Pushing tags to %%r...
    git push %%r --tags
)

echo.
pause
