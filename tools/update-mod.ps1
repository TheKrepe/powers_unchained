# Downloads the newest Powers Unchained build (GitHub release 'dev', updated by CI on every push to main)
# into the Minecraft instance's mods folder. Made for Prism Launcher's pre-launch command:
#
#   powershell -NoProfile -ExecutionPolicy Bypass -File "C:\path\to\update-mod.ps1" "$INST_MC_DIR"
#
# The repository is private, so a GitHub token is needed: create a fine-grained token with read-only
# "Contents" access to TheKrepe/powers_unchained and put it into %APPDATA%\powers_unchained_token.txt
# (or the POWERS_UNCHAINED_TOKEN environment variable).
# Never blocks the game start: on any error (offline, no token, ...) it keeps the current jar.

param([string]$GameDir = (Get-Location).Path)

$ErrorActionPreference = 'Stop'
$repo = 'TheKrepe/powers_unchained'
try {
    $token = $env:POWERS_UNCHAINED_TOKEN
    $tokenFile = Join-Path $env:APPDATA 'powers_unchained_token.txt'
    if (-not $token -and (Test-Path $tokenFile)) { $token = (Get-Content $tokenFile -Raw).Trim() }
    if (-not $token) { Write-Host "[Powers Unchained] No token found ($tokenFile), skipping update."; exit 0 }

    $headers = @{ Authorization = "Bearer $token"; 'User-Agent' = 'powers-unchained-updater'; 'X-GitHub-Api-Version' = '2022-11-28' }
    $release = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases/tags/dev" -TimeoutSec 15
    $asset = $release.assets | Where-Object { $_.name -like 'powers_unchained-neoforge-*.jar' } | Select-Object -First 1
    if (-not $asset) { Write-Host '[Powers Unchained] No jar in the dev release.'; exit 0 }

    $mods = Join-Path $GameDir 'mods'
    New-Item -ItemType Directory -Force -Path $mods | Out-Null
    $marker = Join-Path $mods '.powers_unchained_build'
    $target = Join-Path $mods $asset.name
    if ((Test-Path $marker) -and (Test-Path $target) -and ((Get-Content $marker -Raw).Trim() -eq "$($asset.id)")) {
        Write-Host '[Powers Unchained] Already up to date.'
        exit 0
    }

    $tmp = "$target.download"
    $dl = $headers.Clone(); $dl['Accept'] = 'application/octet-stream'
    Invoke-WebRequest -Headers $dl -Uri $asset.url -OutFile $tmp -TimeoutSec 120
    Get-ChildItem $mods -Filter 'powers_unchained-neoforge-*.jar' | Remove-Item -Force
    Move-Item -Force $tmp $target
    Set-Content -Path $marker -Value "$($asset.id)"
    Write-Host "[Powers Unchained] Installed $($asset.name) ($($release.body))"
} catch {
    Write-Host "[Powers Unchained] Update skipped: $($_.Exception.Message)"
}
exit 0
