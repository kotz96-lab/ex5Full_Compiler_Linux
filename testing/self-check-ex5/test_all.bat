@echo off
REM Test all 26 self-check tests

set PASS=0
set FAIL=0
set TOTAL=0

echo ========================================
echo  Testing All 26 Self-Check Tests
echo ========================================
echo.

for %%f in (self-check-ex5\tests\*.txt) do (
    set /a TOTAL+=1
    echo Testing %%~nf...
    java -jar COMPILER "%%f" "output\%%~nf.s" > nul 2>&1
    if errorlevel 1 (
        echo   FAIL
        set /a FAIL+=1
    ) else (
        echo   PASS
        set /a PASS+=1
    )
)

echo.
echo ========================================
echo  Results
echo ========================================
echo Total:  %TOTAL%
echo Passed: %PASS%
echo Failed: %FAIL%
echo.
pause
