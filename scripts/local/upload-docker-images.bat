@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..") do set "PROJECT_ROOT=%%~fI"
set "BAKE_FILE=%PROJECT_ROOT%\docker-bake.hcl"

if not exist "%BAKE_FILE%" (
  echo ERROR: docker-bake.hcl not found at %BAKE_FILE%
  exit /b 1
)

pushd "%PROJECT_ROOT%"
docker buildx bake -f docker-bake.hcl --push
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
