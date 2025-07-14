# 高德地图配置指南

## 1. 申请高德地图API密钥

1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册并登录账号
3. 创建应用，选择"Web端(JS API)"
4. 获取API Key

## 2. 配置API密钥

编辑 `src/config/map.js` 文件，更新以下配置：

```javascript
export const MAP_CONFIG = {
  // 替换为你的高德地图API密钥
  AMAP_KEY: 'your_amap_key_here',
  
  // 如果配置了安全密钥，请填写
  AMAP_SECURITY_KEY: 'ddaa63cdcc534f87953433fa1557f4de',
  
  // 其他配置保持不变
  AMAP_VERSION: '2.0',
  DEFAULT_CENTER: [116.397428, 39.90923],
  DEFAULT_ZOOM: 13,
  MAP_STYLE: 'amap://styles/normal'
}
```

## 3. 配置域名白名单

在高德开放平台控制台中：

1. 进入应用管理
2. 选择你的应用
3. 在"设置"中添加以下域名到白名单：
   - `localhost`
   - `127.0.0.1`
   - 你的生产环境域名

## 4. 安全密钥配置（可选）

如果需要更高的安全性，可以配置安全密钥：

1. 在高德开放平台生成安全密钥
2. 将安全密钥填入 `AMAP_SECURITY_KEY` 字段

## 5. 常见问题

### INVALID_USER_SCODE 错误
- 检查API密钥是否正确
- 确认域名是否在白名单中
- 检查安全密钥配置是否正确

### 定位失败
- 确保浏览器允许定位权限
- 检查HTTPS环境（某些浏览器要求HTTPS才能定位）
- 确认网络连接正常

### 地址解析失败
- 检查API密钥是否有效
- 确认服务是否在有效期内
- 检查网络连接

## 6. 测试

配置完成后，重启前端服务：

```bash
npm run dev
```

然后测试地图功能：
1. 地图加载是否正常
2. 搜索功能是否可用
3. 定位功能是否正常
4. 地址解析是否成功

## 7. 生产环境部署

在生产环境中，请确保：

1. 使用HTTPS协议
2. 配置正确的域名白名单
3. 设置适当的API调用限制
4. 监控API使用情况 