Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$frontendDir = Join-Path $repoRoot "apps/frontend"
$gradleWrapper = Join-Path $repoRoot "gradlew.bat"

if (-not (Test-Path $gradleWrapper)) {
    throw "gradlew.bat wurde nicht gefunden unter $gradleWrapper"
}

if (-not (Test-Path $frontendDir)) {
    throw "Frontend-Verzeichnis wurde nicht gefunden unter $frontendDir"
}

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "& `".\gradlew.bat`" :apps:backend:bootRun"
) -WorkingDirectory $repoRoot

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "npm run dev"
) -WorkingDirectory $frontendDir

Write-Host "Backend und Frontend wurden in separaten PowerShell-Fenstern gestartet."
