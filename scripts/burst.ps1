param(
  [string]$BaseUrl = "http://127.0.0.1:8080",
  [Parameter(Mandatory = $true)][string]$AdminToken,
  [Parameter(Mandatory = $true)][string]$UserToken,
  [int]$Threads = 50,
  [int]$RampSeconds = 10,
  [string]$JMeter = "jmeter"
)

$ErrorActionPreference = "Stop"
$base = $BaseUrl.TrimEnd('/')
$uri = [uri]$base
$headers = @{ Authorization = "Bearer $AdminToken" }
$ready = $false
1..60 | ForEach-Object {
  if (-not $ready) {
    try {
      $health = Invoke-RestMethod -Uri "$base/actuator/health" -TimeoutSec 3
      $ready = $health.status -eq 'UP'
    } catch { Start-Sleep -Seconds 2 }
  }
}
if (-not $ready) { throw "Application did not become healthy at $base within 120 seconds" }
$body = @{ name = "Burst $(Get-Date -Format s)"; seats = @('A1'); price_paise = 25000 } | ConvertTo-Json -Compress
$show = Invoke-RestMethod -Method Post -Uri "$base/api/v1/shows" -Headers $headers -ContentType "application/json" -Body $body
$result = Join-Path $PSScriptRoot "../performance/burst-results.jtl"
Remove-Item $result -Force -ErrorAction SilentlyContinue

& $JMeter -n -t (Join-Path $PSScriptRoot "../performance/seat-reservation-hot-seat.jmx") -l $result `
  "-JHOST=$($uri.Host)" "-JPORT=$(if ($uri.Port -gt 0) { $uri.Port } elseif ($uri.Scheme -eq 'https') { 443 } else { 80 })" "-JPROTOCOL=$($uri.Scheme)" `
  "-JSHOW_ID=$($show.id)" "-JUSER_TOKEN=$UserToken" "-JTHREADS=$Threads" "-JLOOPS=1" "-JRAMP_SECONDS=$RampSeconds"

$rows = Import-Csv $result
Write-Host "Show $($show.id): $($rows.Count) attempts"
$rows | Group-Object responseCode | Sort-Object Name | ForEach-Object { Write-Host "HTTP $($_.Name): $($_.Count)" }
$final = Invoke-RestMethod -Uri "$base/api/v1/shows/$($show.id)" -Headers @{ Authorization = "Bearer $UserToken" }
Write-Host "Final reconciliation: available=$($final.available), confirmed=$($final.confirmed), held=$($final.held)"
