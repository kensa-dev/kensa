@echo off
setlocal

:: Kensa CLI wrapper (Windows). Installs the release binary under .kensa\bin
:: beside this script on first run and keeps it current. Everything it prints
:: goes to stderr so that `kensa mcp`, which speaks JSON-RPC over stdout, has
:: an uncontaminated stream.
::
:: Set KENSA_VERSION (e.g. set KENSA_VERSION=0.9.1) to pin a release. The
:: wrapper then never asks GitHub for the latest version and only downloads
:: when the pinned version is not the one installed. Unset, it tracks the
:: latest release.
::
:: Set KENSA_HOME to install somewhere other than beside the script. A script
:: that already lives in a directory named .kensa uses that directory rather
:: than nesting another.
::
:: Downloads are verified against the release's checksums.txt. A release that
:: publishes none (0.9.1 and earlier) installs unverified with a warning; a
:: checksums.txt that cannot be fetched or does not match aborts the install.

if defined KENSA_HOME goto home_set
for %%d in ("%~dp0.") do set "SCRIPT_DIR=%%~fd"
for %%d in ("%SCRIPT_DIR%") do set "SCRIPT_DIR_NAME=%%~nxd"
set "KENSA_HOME=%SCRIPT_DIR%\.kensa"
if /i "%SCRIPT_DIR_NAME%"==".kensa" set "KENSA_HOME=%SCRIPT_DIR%"
:home_set
set "BIN_DIR=%KENSA_HOME%\bin"
set "VERSION_FILE=%KENSA_HOME%\version.txt"
set "RELEASES=https://github.com/kensa-dev/kensa/releases"

set ARCH=amd64
if /i "%PROCESSOR_ARCHITECTURE%"=="ARM64" set ARCH=arm64
if /i "%PROCESSOR_ARCHITEW6432%"=="ARM64" set ARCH=arm64
set "BIN_NAME=kensa-windows-%ARCH%.exe"
set "BIN=%BIN_DIR%\%BIN_NAME%"

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

set LOCAL_VERSION=0.0.0
if exist "%VERSION_FILE%" set /p LOCAL_VERSION=<"%VERSION_FILE%"

set WANT_VERSION=
if defined KENSA_VERSION (
    set "WANT_VERSION=%KENSA_VERSION%"
    set "DOWNLOAD_URL=%RELEASES%/download/%KENSA_VERSION%"
) else (
    set "DOWNLOAD_URL=%RELEASES%/latest/download"
    for /f "usebackq delims=" %%v in (`powershell -NoProfile -Command "try { (Invoke-WebRequest -Uri '%RELEASES%/latest/download/version.txt' -UseBasicParsing -TimeoutSec 10).Content.Trim() } catch { '' }"`) do set "WANT_VERSION=%%v"
)
if "%WANT_VERSION%"=="" (
    echo kensa: could not check for updates; using local version %LOCAL_VERSION%. 1>&2
    set "WANT_VERSION=%LOCAL_VERSION%"
)

:: Download when the binary is missing OR the version moved on. Batch has no
:: OR operator, and chaining two `if`s is an AND.
set NEED_DOWNLOAD=
if not exist "%BIN%" set NEED_DOWNLOAD=1
if not "%WANT_VERSION%"=="%LOCAL_VERSION%" set NEED_DOWNLOAD=1
if not defined NEED_DOWNLOAD goto run

if "%WANT_VERSION%"=="0.0.0" (
    echo kensa: no installed binary and no reachable release. Set KENSA_VERSION or retry with a connection. 1>&2
    exit /b 1
)

echo Downloading kensa %WANT_VERSION% for windows-%ARCH%... 1>&2
set "TMP_BIN=%BIN%.download"
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%DOWNLOAD_URL%/%BIN_NAME%' -OutFile '%TMP_BIN%' -UseBasicParsing -TimeoutSec 300"
if errorlevel 1 (
    echo kensa: failed to download %DOWNLOAD_URL%/%BIN_NAME% 1>&2
    if exist "%TMP_BIN%" del "%TMP_BIN%"
    exit /b 1
)

set "CHECKSUMS=%KENSA_HOME%\checksums.txt"
if exist "%CHECKSUMS%" del "%CHECKSUMS%"
:: Exit codes: 0 fetched, 44 the release publishes none (HTTP 404), 1 any other failure.
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; try { Invoke-WebRequest -Uri '%DOWNLOAD_URL%/checksums.txt' -OutFile '%CHECKSUMS%' -UseBasicParsing -TimeoutSec 10; exit 0 } catch { $code = 0; try { $code = [int]$_.Exception.Response.StatusCode } catch {}; if ($code -eq 404) { exit 44 } else { exit 1 } }" 2>nul
if errorlevel 44 (
    echo kensa: release %WANT_VERSION% publishes no checksums.txt; installing unverified. 1>&2
    goto install
)
if errorlevel 1 (
    echo kensa: could not fetch %DOWNLOAD_URL%/checksums.txt. Not installing; retry when the release is reachable. 1>&2
    del "%TMP_BIN%"
    exit /b 1
)
powershell -NoProfile -Command "$expected = Get-Content '%CHECKSUMS%' | Where-Object { $_ -match '\s\*?%BIN_NAME%$' } | ForEach-Object { ($_ -split '\s+')[0].ToLower() } | Select-Object -First 1; if (-not $expected) { [Console]::Error.WriteLine('kensa: checksums.txt for %WANT_VERSION% has no entry for %BIN_NAME%. Not installing.'); exit 1 }; $actual = (Get-FileHash -Algorithm SHA256 '%TMP_BIN%').Hash.ToLower(); if ($actual -ne $expected) { [Console]::Error.WriteLine('kensa: checksum mismatch for %BIN_NAME% (expected ' + $expected + ', got ' + $actual + '). Not installing.'); exit 1 }"
if errorlevel 1 (
    del "%TMP_BIN%"
    exit /b 1
)
del "%CHECKSUMS%"

:install
move /y "%TMP_BIN%" "%BIN%" >nul
if errorlevel 1 (
    echo kensa: could not install %BIN% 1>&2
    exit /b 1
)
:: Redirect first: `echo %VAR% > file` writes a trailing space, which reads
:: back as a different version every run, and a version ending in a digit
:: immediately before `>` would be parsed as a stream number.
>"%VERSION_FILE%" echo %WANT_VERSION%
echo Successfully installed kensa %WANT_VERSION%. 1>&2

:run
"%BIN%" %*
