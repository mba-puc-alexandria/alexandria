[CmdletBinding()]
param(
    [ValidatePattern('^\d+\.\d+$')]
    [string]$DockerApiVersion = '1.44',
    [string]$MySqlImage = 'mysql:8.0.32'
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$backendPath = Join-Path $repoRoot 'alexandria-backend'
$probeSource = Join-Path $PSScriptRoot 'MySqlToolchainProbe.java'
$classpathFile = [System.IO.Path]::GetTempFileName()
$mavenWrapper = if ($env:OS -eq 'Windows_NT') { './mvnw.cmd' } else { './mvnw' }

# Usa apenas container novo e descartável, sem volumes/banco da aplicação.
# Credenciais no Java são exclusivas do container efêmero.
Push-Location $backendPath
try {
    & $mavenWrapper -q dependency:build-classpath "-Dmdep.outputFile=$classpathFile"
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível resolver o classpath Maven.' }
    $probeClasspath = (Get-Content -LiteralPath $classpathFile -Raw).Trim()
    & java "-Dapi.version=$DockerApiVersion" --class-path $probeClasspath $probeSource $MySqlImage
    if ($LASTEXITCODE -ne 0) { throw 'Probe MySQL/Flyway falhou; não considerar o gate aprovado.' }
}
finally {
    Pop-Location
    Remove-Item -LiteralPath $classpathFile -Force
}
