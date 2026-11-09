@REM Maven Wrapper Script for Windows
@REM SIMPONI Attendance System
@echo off
setlocal

set "DIR=%~dp0"
if "%JAVA_HOME%"=="" (
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set "JAVA_EXE=%%i"
        goto :found_java
    )
    echo Error: JAVA_HOME is not set and java could not be found in PATH.
    exit /b 1
) else (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

:found_java
if not exist "%JAVA_EXE%" (
    echo Error: Java executable not found at "%JAVA_EXE%".
    exit /b 1
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

echo [SIMPONI] Apache Maven is not detected in system PATH.
echo [SIMPONI] Please ensure Apache Maven is installed or run in an environment with Maven in PATH.
exit /b 1

