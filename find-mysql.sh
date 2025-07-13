#!/bin/bash

echo "🔍 查找MySQL安装路径..."

# 检查PATH中的mysql命令
if command -v mysql &> /dev/null; then
    echo "✅ 在PATH中找到mysql命令:"
    which mysql
    echo ""
fi

# 检查常见的MySQL安装路径
echo "📂 检查常见安装路径:"

PATHS=(
    "/usr/local/mysql/bin/mysql"
    "/opt/homebrew/bin/mysql"
    "/usr/local/bin/mysql"
    "/usr/bin/mysql"
    "/Applications/MySQL.app/Contents/MacOS/mysql"
    "/usr/local/Cellar/mysql/*/bin/mysql"
)

for path in "${PATHS[@]}"; do
    if [ -f "$path" ]; then
        echo "✅ 找到MySQL: $path"
    fi
done

# 使用find命令搜索
echo ""
echo "🔎 使用find命令搜索mysql:"
find /usr/local /opt /Applications -name "mysql" -type f 2>/dev/null | head -10

echo ""
echo "💡 如果找到了MySQL路径，可以将其添加到PATH中:"
echo "export PATH=\"/path/to/mysql/bin:\$PATH\""
echo ""
echo "或者直接使用完整路径运行MySQL命令" 