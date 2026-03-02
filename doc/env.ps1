# 定义环境变量列表
$envVars = @(
    @{Name = "ATLAS_NACOS_HOST"; Value = "10.0.8.138"; Scope = "User"},
    @{Name = "ATLAS_NACOS_USERNAME"; Value = "nacos"; Scope = "User"},
    @{Name = "ATLAS_NACOS_PASSWORD"; Value = "nacos"; Scope = "User"},
    @{Name = "ATLAS_MYSQL_URL"; Value = "jdbc:mysql://10.0.8.138:3306/atlas?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"; Scope = "User"},
    @{Name = "ATLAS_MYSQL_USERNAME"; Value = "root"; Scope = "User"},
    @{Name = "ATLAS_MYSQL_PASSWORD"; Value = "bubua12"; Scope = "User"},
    @{Name = "ATLAS_REDIS_HOST"; Value = "10.0.8.138"; Scope = "User"},
    @{Name = "ATLAS_REDIS_PORT"; Value = "6379"; Scope = "User"},
    @{Name = "ATLAS_REDIS_PASSWORD"; Value = "bubua12"; Scope = "User"}
)

# 遍历环境变量列表并添加
foreach ($envVar in $envVars) {
    if ($envVar.Scope -eq "User") {
        [System.Environment]::SetEnvironmentVariable($envVar.Name, $envVar.Value, [System.EnvironmentVariableTarget]::User)
    } elseif ($envVar.Scope -eq "System") {
        [System.Environment]::SetEnvironmentVariable($envVar.Name, $envVar.Value, [System.EnvironmentVariableTarget]::Machine)
    }
    Write-Host "Added: $($envVar.Name) = $($envVar.Value) to $($envVar.Scope) environment"
}