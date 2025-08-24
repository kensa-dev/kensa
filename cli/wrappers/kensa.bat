@echo off

:: Configuration
set REPO_URL=https://github.com/kensa-dev/kensa/releases/latest/download
set BIN_DIR=.kensa\bin
set VERSION_FILE=.kensa\version.txt
set REMOTE_VERSION_URL=%REPO_URL%/version.txt

:: Detect architecture (assume amd64; add logic if needed)
set OS=windows
set ARCH=amd64
set BIN_NAME=kensa-%OS%-%ARCH%.exe

:: Create directory for binaries
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

:: Check or update version
if exist "%VERSION_FILE%" (
    set /p LOCAL_VERSION=<"%VERSION_FILE%"
) else (
    set LOCAL_VERSION=0.0.0
)

:: Use PowerShell for fetching remote version
powershell -Command "$remote = (Invoke-WebRequest -Uri '%REMOTE_VERSION_URL%' -UseBasicParsing).Content.Trim(); echo $remote" > temp.txt
set /p REMOTE_VERSION=<temp.txt
del temp.txt
if "%REMOTE_VERSION%"=="" (
    echo Failed to check for updates. Please check your internet connection. Using local version if available.
    set REMOTE_VERSION=%LOCAL_VERSION%
)

if not "%REMOTE_VERSION%"=="%LOCAL_VERSION%" if not exist "%BIN_DIR%\%BIN_NAME%" (
    echo Downloading kensa %REMOTE_VERSION% for %OS%-%ARCH%...
    powershell -Command "Invoke-WebRequest -Uri '%REPO_URL%/%BIN_NAME%' -OutFile '%BIN_DIR%\%BIN_NAME%'"
    if errorlevel 1 (
        echo Failed to download kensa %REMOTE_VERSION%. Please check your connection or the repository.
        exit /b 1
    )
    echo %REMOTE_VERSION% > "%VERSION_FILE%"
    echo Successfully updated to kensa %REMOTE_VERSION%.
)

:: Run the CLI
"%BIN_DIR%\%BIN_NAME%" %*