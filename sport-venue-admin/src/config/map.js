// 高德地图配置
export const MAP_CONFIG = {
  // 高德地图API密钥
  AMAP_KEY: 'd16c4f8b60ffec2c8af025b192686cc5',
  
  // 安全密钥（如果配置了的话）
  AMAP_SECURITY_KEY: 'ddaa63cdcc534f87953433fa1557f4de',
  
  // 地图版本
  AMAP_VERSION: '2.0',
  
  // 默认地图中心点（北京）
  DEFAULT_CENTER: [116.397428, 39.90923],
  
  // 默认缩放级别
  DEFAULT_ZOOM: 13,
  
  // 地图样式
  MAP_STYLE: 'amap://styles/normal'
}

// 检查API密钥是否有效
export const validateMapConfig = () => {
  if (!MAP_CONFIG.AMAP_KEY || MAP_CONFIG.AMAP_KEY === 'your_amap_key_here') {
    console.warn('高德地图API密钥未配置或使用默认值，请配置有效的API密钥')
    return false
  }
  return true
}

// 获取地图加载配置
export const getMapLoaderConfig = () => {
  const config = {
    key: MAP_CONFIG.AMAP_KEY,
    version: MAP_CONFIG.AMAP_VERSION,
    plugins: [
      'AMap.Geocoder',
      'AMap.PlaceSearch',
      'AMap.Geolocation'
    ]
  }
  
  // 如果有安全密钥，添加安全密钥配置
  if (MAP_CONFIG.AMAP_SECURITY_KEY) {
    config.securityJsCode = MAP_CONFIG.AMAP_SECURITY_KEY
  }
  
  return config
} 