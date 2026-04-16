param(
    [string]$ServerAddr = $env:ATLAS_NACOS_HOST,
    [string]$Username = $env:ATLAS_NACOS_USERNAME,
    [string]$Password = $env:ATLAS_NACOS_PASSWORD,
    [string]$Group = $(if ($env:ATLAS_NACOS_CONFIG_GROUP) { $env:ATLAS_NACOS_CONFIG_GROUP } else { "ATLAS" }),
    [string]$Namespace = $env:ATLAS_NACOS_CONFIG_NAMESPACE,
    [string]$ConfigDir = (Join-Path $PSScriptRoot "configs")
)

function Resolve-NacosConfigType {
    param(
        [string]$FileName
    )

    $extension = [System.IO.Path]::GetExtension($FileName).ToLowerInvariant()
    switch ($extension) {
        ".yml" { return "yaml" }
        ".yaml" { return "yaml" }
        ".properties" { return "properties" }
        ".json" { return "json" }
        ".xml" { return "xml" }
        ".html" { return "html" }
        ".txt" { return "text" }
        default { return "text" }
    }
}

if ([string]::IsNullOrWhiteSpace($ServerAddr)) {
    $ServerAddr = "10.0.8.132:8848"
}

$ServerAddr = $ServerAddr.Trim()
if ($ServerAddr -match "^https?://") {
    $ServerAddr = $ServerAddr -replace "^https?://", ""
}
if ($ServerAddr -notmatch ":\d+$") {
    $ServerAddr = "$ServerAddr`:8848"
}

if (-not (Test-Path $ConfigDir)) {
    throw "Config directory not found: $ConfigDir"
}

$endpoint = "http://$ServerAddr/nacos/v1/cs/configs"
$healthEndpoint = "http://$ServerAddr/nacos/v1/console/health/readiness"
$files = Get-ChildItem -Path $ConfigDir -Filter "*.yaml" | Sort-Object Name

Write-Host "Using Nacos endpoint: $endpoint"
try {
    $health = Invoke-RestMethod -Method Get -Uri $healthEndpoint -TimeoutSec 10 -ErrorAction Stop
    Write-Host "Nacos readiness: $health"
}
catch {
    throw "Unable to reach Nacos at $healthEndpoint. Please verify server address, port, and network connectivity. Original error: $($_.Exception.Message)"
}

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $configType = Resolve-NacosConfigType -FileName $file.Name
    $body = @{
        dataId  = $file.Name
        group   = $Group
        content = $content
        type    = $configType
    }

    if (-not [string]::IsNullOrWhiteSpace($Namespace)) {
        $body.tenant = $Namespace
    }
    if (-not [string]::IsNullOrWhiteSpace($Username)) {
        $body.username = $Username
    }
    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        $body.password = $Password
    }

    Write-Host "Publishing $($file.Name) to Nacos group=$Group namespace=$Namespace type=$configType"
    $result = Invoke-RestMethod -Method Post -Uri $endpoint -Body $body -ContentType "application/x-www-form-urlencoded; charset=UTF-8" -ErrorAction Stop
    Write-Host "Result: $result"
}
