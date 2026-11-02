<#
.SYNOPSIS
    Starts the full local dev stack for order-inventory-app:
    infra (SQL Server service, MongoDB, Kafka) + all 6 Spring Boot services + the React frontend.

.DESCRIPTION
    This is for LOCAL DEVELOPMENT only - it does not touch Docker or Jenkins.
    Those are separate concerns:
      - docker-compose.yml / per-service Dockerfiles -> only relevant if you install Docker Desktop later
      - Jenkinsfile -> only runs on an actual Jenkins server, not part of local dev
    What this script brings up is exactly what you use day-to-day while coding: the databases,
    the message broker, the 6 backend services, and the frontend dev server.

.PARAMETER Rebuild
    Pass -Rebuild to run `mvn clean install` before starting the Java services
    (use this after you've changed backend code).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\start-all.ps1
    powershell -ExecutionPolicy Bypass -File .\start-all.ps1 -Rebuild
#>

param(
    [switch]$Rebuild
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$MavenBin    = "C:\tools\maven\apache-maven-3.9.9\bin"
$KafkaHome   = "C:\kafka"
$MongodExe   = "C:\Program Files\MongoDB\Server\9.0\bin\mongod.exe"
$MongoData   = "D:\JAVA APPS\mongodb-data"

function Test-PortOpen($port) {
    return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

function Wait-ForPort($port, $name, $timeoutSec = 60) {
    $elapsed = 0
    while (-not (Test-PortOpen $port)) {
        Start-Sleep -Seconds 2
        $elapsed += 2
        if ($elapsed -ge $timeoutSec) {
            Write-Warning "$name did not open port $port within $timeoutSec seconds - check its run-err.log"
            return $false
        }
    }
    Write-Host "  $name is up (port $port)" -ForegroundColor Green
    return $true
}

function Start-JavaService($folder, $jarName, $port) {
    if (Test-PortOpen $port) {
        Write-Host "[$folder] already running on port $port - skipping" -ForegroundColor Yellow
        return
    }
    Write-Host "[$folder] starting..." -ForegroundColor Cyan
    $jarPath = Join-Path $ProjectRoot "$folder\target\$jarName"
    Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath `
        -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $ProjectRoot "$folder\run.log") `
        -RedirectStandardError (Join-Path $ProjectRoot "$folder\run-err.log")
    Wait-ForPort $port $folder | Out-Null
}

Write-Host "=== 1. Infrastructure ===" -ForegroundColor Magenta

# --- SQL Server Express (Windows service - should already be set to Automatic) ---
$sql = Get-Service -Name "MSSQL`$SQLEXPRESS" -ErrorAction SilentlyContinue
if ($sql -and $sql.Status -ne "Running") {
    Write-Host "Starting SQL Server (SQLEXPRESS) service..." -ForegroundColor Cyan
    Start-Service -Name "MSSQL`$SQLEXPRESS"
}
Wait-ForPort 1433 "SQL Server" | Out-Null

# --- MongoDB (plain process, not a service) ---
if (Test-PortOpen 27017) {
    Write-Host "MongoDB already running - skipping" -ForegroundColor Yellow
} else {
    Write-Host "Starting MongoDB..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $MongoData | Out-Null
    Start-Process -FilePath $MongodExe -ArgumentList "--dbpath", $MongoData, "--port", "27017" -WindowStyle Hidden
    Wait-ForPort 27017 "MongoDB" | Out-Null
}

# --- Kafka (KRaft mode, plain process) ---
if (Test-PortOpen 9092) {
    Write-Host "Kafka already running - skipping" -ForegroundColor Yellow
} else {
    Write-Host "Starting Kafka..." -ForegroundColor Cyan
    Push-Location $KafkaHome
    $env:KAFKA_HEAP_OPTS = "-Xmx1G -Xms1G"   # skips a broken `wmic` call in kafka-server-start.bat on newer Windows
    Start-Process -FilePath ".\bin\windows\kafka-server-start.bat" -ArgumentList ".\config\kraft\server.properties" `
        -WindowStyle Hidden `
        -RedirectStandardOutput "$KafkaHome\kafka-out.log" `
        -RedirectStandardError "$KafkaHome\kafka-err.log"
    Pop-Location
    Wait-ForPort 9092 "Kafka" | Out-Null
}

Write-Host "`n=== 2. Backend build (optional) ===" -ForegroundColor Magenta
if ($Rebuild) {
    $env:PATH = "$MavenBin;$env:PATH"
    Push-Location $ProjectRoot
    Write-Host "Running mvn clean install..." -ForegroundColor Cyan
    mvn clean install -DskipTests --no-transfer-progress
    Pop-Location
} else {
    Write-Host "Skipping rebuild (pass -Rebuild to force one)" -ForegroundColor Yellow
}

Write-Host "`n=== 3. Backend services ===" -ForegroundColor Magenta
Push-Location $ProjectRoot

Start-JavaService "eureka-server"        "eureka-server-1.0.0.jar"        8761
Start-JavaService "auth-service"         "auth-service-1.0.0.jar"         8081
Start-JavaService "order-service"        "order-service-1.0.0.jar"        8082
Start-JavaService "inventory-service"    "inventory-service-1.0.0.jar"    8083
Start-JavaService "notification-service" "notification-service-1.0.0.jar" 8084
Start-JavaService "api-gateway"          "api-gateway-1.0.0.jar"          8080

Pop-Location

Write-Host "`n=== 4. Frontend ===" -ForegroundColor Magenta
if (Test-PortOpen 3000) {
    Write-Host "Frontend already running - skipping" -ForegroundColor Yellow
} else {
    Write-Host "Starting React dev server..." -ForegroundColor Cyan
    Push-Location (Join-Path $ProjectRoot "frontend")
    Start-Process -FilePath "npm.cmd" -ArgumentList "run", "dev" -WindowStyle Hidden `
        -RedirectStandardOutput "run.log" -RedirectStandardError "run-err.log"
    Pop-Location
    Wait-ForPort 3000 "Frontend" | Out-Null
}

Write-Host "`n=== All up ===" -ForegroundColor Green
Write-Host "Eureka dashboard : http://localhost:8761"
Write-Host "API Gateway      : http://localhost:8080"
Write-Host "Frontend         : http://localhost:3000"
Write-Host "`nLogs are in each service's folder as run.log / run-err.log if something didn't come up."
