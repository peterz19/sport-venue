# MySQL 安装和配置指南

## 安装MySQL (macOS)

### 1. 使用Homebrew安装
```bash
# 安装MySQL
brew install mysql

# 启动MySQL服务
brew services start mysql
```

### 2. 设置root密码
```bash
# 设置root密码为Aa123456
mysql_secure_installation
```

在交互过程中：
- 选择密码强度级别（推荐选择0，简单密码）
- 设置root密码为：`Aa123456`
- 其他选项可以按默认设置

### 3. 验证安装
```bash
# 检查MySQL服务状态
brew services list | grep mysql

# 测试连接
# 如果mysql命令在PATH中
mysql -u root -pAa123456 -e "SELECT 1;"

# 或者使用完整路径
# Homebrew安装
/opt/homebrew/bin/mysql -u root -pAa123456 -e "SELECT 1;"

# 官方安装包
/usr/local/mysql/bin/mysql -u root -pAa123456 -e "SELECT 1;"
```

### 4. 创建数据库
```bash
# 创建sport_venue数据库
# 如果mysql命令在PATH中
mysql -u root -pAa123456 -e "CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 或者使用完整路径
# Homebrew安装
/opt/homebrew/bin/mysql -u root -pAa123456 -e "CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 官方安装包
/usr/local/mysql/bin/mysql -u root -pAa123456 -e "CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 验证数据库创建
mysql -u root -pAa123456 -e "SHOW DATABASES;" | grep sport_venue
```

## 常见问题

### 1. MySQL服务启动失败
```bash
# 检查MySQL状态
brew services list | grep mysql

# 重启MySQL服务
brew services restart mysql

# 查看MySQL日志
tail -f /usr/local/var/mysql/*.err
```

### 2. 连接被拒绝
```bash
# 检查MySQL是否监听3306端口
lsof -i :3306

# 检查MySQL配置文件
cat /usr/local/etc/my.cnf
```

### 3. 密码认证问题
```bash
# 重置root密码
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;
EXIT;
```

## 配置说明

### 默认配置
- **主机**: localhost
- **端口**: 3306
- **用户名**: root
- **密码**: Aa123456
- **数据库**: sport_venue

### 配置文件位置
- **配置文件**: `/usr/local/etc/my.cnf`
- **数据目录**: `/usr/local/var/mysql`
- **日志文件**: `/usr/local/var/mysql/*.err`

## 管理命令

### 启动/停止服务
```bash
# 启动MySQL
brew services start mysql

# 停止MySQL
brew services stop mysql

# 重启MySQL
brew services restart mysql
```

### 连接MySQL
```bash
# 使用密码连接
# 如果mysql命令在PATH中
mysql -u root -pAa123456

# 或者使用完整路径
# Homebrew安装
/opt/homebrew/bin/mysql -u root -pAa123456

# 官方安装包
/usr/local/mysql/bin/mysql -u root -pAa123456

# 进入指定数据库
mysql -u root -pAa123456 sport_venue
```

### 查看状态
```bash
# 查看服务状态
brew services list | grep mysql

# 查看进程
ps aux | grep mysql

# 查看端口
lsof -i :3306
``` 