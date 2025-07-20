<template>
  <div class="map-picker">
    <div class="map-container">
      <div id="map-container" class="map"></div>
      <div class="map-controls">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索地址（输入关键词后按回车或点击搜索）"
          class="search-input"
          @keyup.enter="searchLocation"
          clearable
        >
          <template #append>
            <el-button @click="searchLocation">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-button @click="getCurrentLocation" type="primary" size="small">
          定位当前位置
        </el-button>
      </div>
    </div>
    
    <div class="location-info">
      <el-form-item label="详细地址" prop="address">
        <el-input
          v-model="selectedAddress"
          placeholder="请在地图上选择位置或搜索地址，也可手动输入"
          @input="onAddressInput"
        />
        <div class="address-tip">
          <small>💡 提示：如果地址解析失败，您可以手动输入详细地址</small>
        </div>
      </el-form-item>
      
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input
              v-model="selectedLongitude"
              placeholder="经度"
              readonly
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input
              v-model="selectedLatitude"
              placeholder="纬度"
              readonly
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    
    <!-- 调试信息 -->
    <div v-if="debugInfo" class="debug-info">
      <p>调试信息: {{ debugInfo }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getMapLoaderConfig, validateMapConfig, MAP_CONFIG } from '../config/map.js'

export default {
  name: 'MapPicker',
  components: {
    Search
  },
  props: {
    // 初始地址
    address: {
      type: String,
      default: ''
    },
    // 初始经度
    longitude: {
      type: [String, Number],
      default: ''
    },
    // 初始纬度
    latitude: {
      type: [String, Number],
      default: ''
    }
  },
  emits: ['update:address', 'update:longitude', 'update:latitude', 'change'],
  setup(props, { emit }) {
    const map = ref(null)
    const marker = ref(null)
    const searchKeyword = ref('')
    const selectedAddress = ref('')
    const selectedLongitude = ref('')
    const selectedLatitude = ref('')
    const placeSearch = ref(null)
    const debugInfo = ref('')
    const AMapInstance = ref(null)
    
    // 初始化地图
    const initMap = async () => {
      try {
        // 验证地图配置
        if (!validateMapConfig()) {
          ElMessage.warning('地图配置未完成，部分功能可能不可用')
        }
        
        const AMap = await AMapLoader.load(getMapLoaderConfig())
        AMapInstance.value = AMap
        
        // 创建地图实例
        map.value = new AMap.Map('map-container', {
          zoom: MAP_CONFIG.DEFAULT_ZOOM,
          center: MAP_CONFIG.DEFAULT_CENTER,
          mapStyle: MAP_CONFIG.MAP_STYLE
        })
        
        // 创建标记点
        marker.value = new AMap.Marker({
          draggable: true,
          cursor: 'move'
        })
        
        // 添加标记到地图
        map.value.add(marker.value)
        
        // 初始化地点搜索
        placeSearch.value = new AMap.PlaceSearch({
          pageSize: 10,
          pageIndex: 1,
          city: '全国'
        })
        
        // 监听地图点击事件
        map.value.on('click', (e) => {
          const lnglat = e.lnglat
          updateMarkerPosition(lnglat)
          reverseGeocode(lnglat)
        })
        
        // 监听标记拖拽事件
        marker.value.on('dragend', (e) => {
          const lnglat = e.lnglat
          reverseGeocode(lnglat)
        })
        
        // 如果有初始坐标，设置地图中心点和标记
        if (props.longitude && props.latitude) {
          const lnglat = [Number(props.longitude), Number(props.latitude)]
          map.value.setCenter(lnglat)
          updateMarkerPosition(lnglat)
          reverseGeocode(lnglat)
        }
        
        // 如果有初始地址，进行地理编码
        if (props.address && !props.longitude && !props.latitude) {
          geocode(props.address)
        }
        
      } catch (error) {
        console.error('地图初始化失败:', error)
        ElMessage.error('地图加载失败，请检查网络连接')
      }
    }
    
    // 更新标记位置
    const updateMarkerPosition = (lnglat) => {
      marker.value.setPosition(lnglat)
      selectedLongitude.value = lnglat.getLng().toFixed(6)
      selectedLatitude.value = lnglat.getLat().toFixed(6)
      
      emit('update:longitude', selectedLongitude.value)
      emit('update:latitude', selectedLatitude.value)
    }
    
    // 逆地理编码（坐标转地址）
    const reverseGeocode = (lnglat) => {
      debugInfo.value = `正在解析坐标: ${lnglat.getLng()}, ${lnglat.getLat()}`
      
      try {
        if (!AMapInstance.value) {
          throw new Error('地图API未初始化')
        }
        
        console.log('开始逆地理编码，坐标:', lnglat)
        const geocoder = new AMapInstance.value.Geocoder()
        
        geocoder.getAddress(lnglat, (status, result) => {
          console.log('逆地理编码完整结果:', status, result)
          
          if (status === 'complete' && result.info === 'OK') {
            const address = result.regeocode.formattedAddress
            selectedAddress.value = address
            emit('update:address', address)
            emit('change', {
              address,
              longitude: lnglat.getLng(),
              latitude: lnglat.getLat()
            })
            debugInfo.value = `地址解析成功: ${address}`
            ElMessage.success('地址解析成功')
          } else {
            console.error('逆地理编码失败:', status, result)
            const errorInfo = result ? result.info : '未知错误'
            const errorMessage = result ? result.message : ''
            debugInfo.value = `地址解析失败: ${status} - ${errorInfo} ${errorMessage}`
            
            // 详细错误信息
            if (result) {
              console.error('详细错误信息:', JSON.stringify(result))
            }
            
            // 尝试使用浏览器原生反向地理编码
            tryBrowserReverseGeocode(lnglat)
          }
        })
      } catch (error) {
        console.error('逆地理编码初始化失败:', error)
        debugInfo.value = `逆地理编码初始化失败: ${error.message}`
        // 尝试使用浏览器原生反向地理编码
        tryBrowserReverseGeocode(lnglat)
      }
    }
    
    // 尝试使用浏览器原生反向地理编码
    const tryBrowserReverseGeocode = (lnglat) => {
      // 尝试使用免费的Nominatim服务作为备用方案
      tryNominatimGeocode(lnglat)
    }
    
    // 使用Nominatim免费地理编码服务
    const tryNominatimGeocode = async (lnglat) => {
      try {
        const lat = lnglat.getLat()
        const lng = lnglat.getLng()
        const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`
        
        const response = await fetch(url)
        const data = await response.json()
        
        if (data.display_name) {
          const address = data.display_name
          selectedAddress.value = address
          emit('update:address', address)
          emit('change', {
            address,
            longitude: lnglat.getLng(),
            latitude: lnglat.getLat()
          })
          debugInfo.value = `地址解析成功（备用服务）: ${address}`
          ElMessage.success('地址解析成功（备用服务）')
        } else {
          handleGeocodeFailure(lnglat)
        }
      } catch (error) {
        console.error('备用地理编码服务失败:', error)
        handleGeocodeFailure(lnglat)
      }
    }
    
    // 处理地理编码失败的情况
    const handleGeocodeFailure = (lnglat) => {
      debugInfo.value = `地址解析服务不可用，请手动输入地址或联系管理员配置API密钥`
      
      // 提示用户手动输入地址
      ElMessage.warning('地址解析服务暂时不可用，请手动输入详细地址')
      
      // 设置一个默认地址格式，用户可以编辑
      const defaultAddress = `经度: ${lnglat.getLng().toFixed(6)}, 纬度: ${lnglat.getLat().toFixed(6)}`
      selectedAddress.value = defaultAddress
      emit('update:address', defaultAddress)
      emit('change', {
        address: defaultAddress,
        longitude: lnglat.getLng(),
        latitude: lnglat.getLat()
      })
    }
    
    // 处理地址输入
    const onAddressInput = (value) => {
      selectedAddress.value = value
      emit('update:address', value)
      emit('change', {
        address: value,
        longitude: selectedLongitude.value,
        latitude: selectedLatitude.value
      })
    }
    
    // 地理编码（地址转坐标）
    const geocode = (address) => {
      if (!AMapInstance.value) {
        console.error('地图API未初始化')
        return
      }
      
      console.log('开始地理编码，地址:', address)
      const geocoder = new AMapInstance.value.Geocoder()
      
      geocoder.getLocation(address, (status, result) => {
        console.log('地理编码完整结果:', status, result)
        
        if (status === 'complete' && result.info === 'OK') {
          const lnglat = result.geocodes[0].location
          map.value.setCenter(lnglat)
          updateMarkerPosition(lnglat)
          selectedAddress.value = address
          emit('update:address', address)
          emit('change', {
            address,
            longitude: lnglat.getLng(),
            latitude: lnglat.getLat()
          })
          debugInfo.value = `地址编码成功: ${lnglat.getLng()}, ${lnglat.getLat()}`
          ElMessage.success('地址编码成功')
        } else {
          console.error('地理编码失败:', status, result)
          const errorInfo = result ? result.info : '未知错误'
          const errorMessage = result ? result.message : ''
          debugInfo.value = `地址编码失败: ${status} - ${errorInfo} ${errorMessage}`
          
          // 详细错误信息
          if (result) {
            console.error('详细错误信息:', JSON.stringify(result))
          }
          
          ElMessage.warning('地址编码失败，请检查地址是否正确')
        }
      })
    }
    
    // 搜索位置
    const searchLocation = () => {
      if (!searchKeyword.value.trim()) {
        ElMessage.warning('请输入搜索关键词')
        return
      }
      
      debugInfo.value = `正在搜索: ${searchKeyword.value}`
      
      placeSearch.value.search(searchKeyword.value, (status, result) => {
        console.log('搜索结果:', status, result)
        
        if (status === 'complete' && result.info === 'OK') {
          const pois = result.poiList.pois
          if (pois && pois.length > 0) {
            const poi = pois[0]
            const lnglat = poi.location
            map.value.setCenter(lnglat)
            updateMarkerPosition(lnglat)
            selectedAddress.value = poi.address || poi.name
            emit('update:address', selectedAddress.value)
            emit('change', {
              address: selectedAddress.value,
              longitude: lnglat.getLng(),
              latitude: lnglat.getLat()
            })
            debugInfo.value = `搜索成功: ${selectedAddress.value}`
            ElMessage.success(`找到: ${selectedAddress.value}`)
          } else {
            debugInfo.value = '未找到相关地址'
            ElMessage.warning('未找到相关地址')
          }
        } else {
          debugInfo.value = `搜索失败: ${status} - ${result?.info || '未知错误'}`
          ElMessage.warning('搜索失败，请重试')
        }
      })
    }
    
    // 获取当前位置
    const getCurrentLocation = () => {
      ElMessage.info('正在获取当前位置...')
      debugInfo.value = '正在获取当前位置...'
      
      // 先尝试使用浏览器原生定位
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude
            const lng = position.coords.longitude
            
            debugInfo.value = `浏览器定位成功: ${lng}, ${lat}`
            ElMessage.success('定位成功，正在解析地址...')
            
            // 创建高德地图LngLat对象
            const amapLnglat = new AMapInstance.value.LngLat(lng, lat)
            
            // 更新地图中心点和标记位置
            map.value.setCenter(amapLnglat)
            updateMarkerPosition(amapLnglat)
            
            // 进行逆地理编码
            reverseGeocode(amapLnglat)
          },
          (error) => {
            console.error('浏览器定位失败:', error)
            debugInfo.value = `浏览器定位失败: ${error.message}`
            
            // 如果浏览器定位失败，尝试使用高德地图定位
            tryUseAMapGeolocation()
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 60000
          }
        )
      } else {
        // 浏览器不支持定位，使用高德地图定位
        tryUseAMapGeolocation()
      }
    }
    
    // 尝试使用高德地图定位
    const tryUseAMapGeolocation = () => {
      try {
        if (!AMapInstance.value) {
          throw new Error('地图API未初始化')
        }
        const geolocation = new AMapInstance.value.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          zoomToAccuracy: true
        })
        
        map.value.addControl(geolocation)
        
        geolocation.getCurrentPosition((status, result) => {
          console.log('高德地图定位结果:', status, result)
          
          if (status === 'complete') {
            const lnglat = result.position
            
            debugInfo.value = `高德地图定位成功: ${lnglat.getLng()}, ${lnglat.getLat()}`
            ElMessage.success('定位成功，正在解析地址...')
            
            // 更新地图中心点和标记位置
            map.value.setCenter(lnglat)
            updateMarkerPosition(lnglat)
            
            // 高德地图定位返回的已经是LngLat对象，直接使用
            reverseGeocode(lnglat)
          } else {
            console.error('高德地图定位失败:', status, result)
            debugInfo.value = `高德地图定位失败: ${status} - ${result?.info || '未知错误'}`
            ElMessage.error('获取当前位置失败，请检查定位权限或网络连接')
          }
        })
      } catch (error) {
        console.error('高德地图定位初始化失败:', error)
        debugInfo.value = `高德地图定位初始化失败: ${error.message}`
        ElMessage.error('定位服务初始化失败')
      }
    }
    
    // 监听props变化
    watch(() => props.address, (newAddress) => {
      if (newAddress && newAddress !== selectedAddress.value) {
        selectedAddress.value = newAddress
      }
    })
    
    watch(() => props.longitude, (newLng) => {
      if (newLng && newLng !== selectedLongitude.value) {
        selectedLongitude.value = newLng
      }
    })
    
    watch(() => props.latitude, (newLat) => {
      if (newLat && newLat !== selectedLatitude.value) {
        selectedLatitude.value = newLat
      }
    })
    
    onMounted(() => {
      initMap()
    })
    
    onUnmounted(() => {
      if (map.value) {
        map.value.destroy()
      }
    })
    
    return {
      searchKeyword,
      selectedAddress,
      selectedLongitude,
      selectedLatitude,
      searchLocation,
      getCurrentLocation,
      onAddressInput,
      debugInfo
    }
  }
}
</script>

<style scoped>
.map-picker {
  width: 100%;
}

.map-container {
  position: relative;
  margin-bottom: 20px;
}

.map {
  width: 100%;
  height: 400px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.map-controls {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  gap: 10px;
  z-index: 1000;
}

.search-input {
  flex: 1;
}

.location-info {
  margin-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

.debug-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.address-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style> 