$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir "..\..")
$BakeFile = Join-Path $ProjectRoot "docker-bake.hcl"

if (-not (Test-Path $BakeFile)) {
  Write-Error "docker-bake.hcl not found at $BakeFile"
}

Push-Location $ProjectRoot
try {
  docker buildx bake -f docker-bake.hcl --push
} finally {
  Pop-Location
}
