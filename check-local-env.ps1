# Windows PowerShell 环境检查脚本
Write-Host "🔍 检查本地环境..." -ForegroundColor Green

# 检查Java版本
Write-Host "检查Java版本..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "✅ Java版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java未安装或未配置" -ForegroundColor Red
    exit 1
}

# 检查Maven版本
Write-Host "检查Maven版本..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version | Select-String "Apache Maven" | Select-Object -First 1
    Write-Host "✅ Maven版本: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven未安装或未配置" -ForegroundColor Red
    exit 1
}

# 检查MySQL连接
Write-Host "检查MySQL连接..." -ForegroundColor Yellow
try {
    $mysqlResult = mysql -u root -pAa123456 -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ MySQL连接正常" -ForegroundColor Green
        
        # 检查数据库是否存在
        $dbResult = mysql -u root -pAa123456 -e "USE sport_venue;" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ 数据库sport_venue存在" -ForegroundColor Green
        } else {
            Write-Host "⚠️  数据库sport_venue不存在，正在创建..." -ForegroundColor Yellow
            mysql -u root -pAa123456 -e "CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
            Write-Host "✅ 数据库sport_venue创建完成" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ MySQL连接失败" -ForegroundColor Red
        Write-Host "请确保MySQL在localhost:3306运行，用户名: root，密码: Aa123456" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ MySQL未安装或无法连接" -ForegroundColor Red
    exit 1
}

# 检查Redis连接
Write-Host "检查Redis连接..." -ForegroundColor Yellow
try {
    $redisResult = redis-cli -h localhost -p 6379 ping 2>$null
    if ($redisResult -eq "PONG") {
        Write-Host "✅ Redis连接正常" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis连接失败" -ForegroundColor Red
        Write-Host "请确保Redis在localhost:6379运行，无密码" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Redis未安装或无法连接" -ForegroundColor Red
    exit 1
}

# 检查端口占用
Write-Host "检查端口占用情况..." -ForegroundColor Yellow
$ports = @(8888, 8761, 8080, 8081, 8082, 8083, 8084)
foreach ($port in $ports) {
    $portCheck = netstat -an | findstr ":$port "
    if ($portCheck) {
        Write-Host "⚠️  端口 $port 被占用" -ForegroundColor Yellow
    } else {
        Write-Host "✅ 端口 $port 可用" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "🎉 环境检查完成！" -ForegroundColor Green
Write-Host "如果所有检查都通过，可以运行项目启动脚本" -ForegroundColor Green 