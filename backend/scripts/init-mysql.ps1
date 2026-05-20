param(
  [string]$MysqlBin = "D:\dev\mysql8\mysql-8.0.39-winx64\bin\mysql.exe",
  [string]$User = "root",
  [string]$Password = "",
  [string]$HostName = "localhost",
  [int]$Port = 3306
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$schema = Join-Path $root "sql\schema.sql"
$data = Join-Path $root "sql\data.sql"
$schemaForMysql = $schema.Replace("\", "/")
$dataForMysql = $data.Replace("\", "/")

if (!(Test-Path $MysqlBin)) {
  throw "mysql.exe not found: $MysqlBin"
}
if (!(Test-Path $schema)) {
  throw "schema.sql not found: $schema"
}
if (!(Test-Path $data)) {
  throw "data.sql not found: $data"
}

$argsBase = @("--default-character-set=utf8mb4", "-h$HostName", "-P$Port", "-u$User")
if ($Password -ne "") {
  $argsBase += "-p$Password"
}

Write-Host "Initializing schema..."
& $MysqlBin @argsBase "--execute=source $schemaForMysql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Loading seed data..."
& $MysqlBin @argsBase "--execute=source $dataForMysql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "MySQL initialization completed: office_management_system"
