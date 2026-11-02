[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$TargetPath
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $TargetPath -PathType Container)) {
    throw "TargetPath does not exist: $TargetPath"
}

$target = (Resolve-Path -LiteralPath $TargetPath).Path
$requiredFiles = @(
    "build.gradle",
    "src/main/java/com/example/soundattract/client/AwarenessHud.java",
    "src/main/java/com/example/soundattract/config/ClientConfig.java"
)

foreach ($relativePath in $requiredFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $target $relativePath) -PathType Leaf)) {
        throw "The target folder does not look like Attract To Sound NeoForge: missing $relativePath"
    }
}

$overlayRoot = Join-Path $PSScriptRoot "overlay/src/main"
$destinationRoot = Join-Path $target "src/main"

Copy-Item -Path (Join-Path $overlayRoot "java") -Destination $destinationRoot -Recurse -Force
Copy-Item -Path (Join-Path $overlayRoot "resources") -Destination $destinationRoot -Recurse -Force

Write-Host "HUD customization files were copied into:"
Write-Host "  $target"
Write-Host "Open Options -> Controls -> Key Binds, find Sound Attract, and bind Configure awareness HUD."
