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
    echo Failed to check for updates. Please check your internet connection. Using local version if available. 1>&2
    set REMOTE_VERSION=%LOCAL_VERSION%
)

:: Download when the binary is missing OR the version moved on. Batch has no
:: OR operator, and chaining two `if`s is an AND — which never re-downloaded an
:: existing binary after a release.
set NEED_DOWNLOAD=
if not exist "%BIN_DIR%\%BIN_NAME%" set NEED_DOWNLOAD=1
if not "%REMOTE_VERSION%"=="%LOCAL_VERSION%" set NEED_DOWNLOAD=1

if defined NEED_DOWNLOAD (
    echo Downloading kensa %REMOTE_VERSION% for %OS%-%ARCH%... 1>&2
    powershell -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%REPO_URL%/%BIN_NAME%' -OutFile '%BIN_DIR%\%BIN_NAME%' -UseBasicParsing"
    if errorlevel 1 (
        echo Failed to download kensa %REMOTE_VERSION%. Please check your connection or the repository. 1>&2
        exit /b 1
    )
    :: Redirect first: `echo %VAR% > file` writes a trailing space, which reads
    :: back as a different version every run, and a version ending in a digit
    :: immediately before `>` would be parsed as a stream number.
    >"%VERSION_FILE%" echo %REMOTE_VERSION%
    echo Successfully updated to kensa %REMOTE_VERSION%. 1>&2
)

:: Run the CLI. Everything above writes to stderr so that `kensa mcp`, which
:: speaks JSON-RPC over stdout, has an uncontaminated stream.
"%BIN_DIR%\%BIN_NAME%" %*