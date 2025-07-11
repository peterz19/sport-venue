<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#409EFF"><Location /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalVenues }}</div>
              <div class="stats-label">总场馆数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#67C23A"><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalOccupancy }}</div>
              <div class="stats-label">当前总人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#E6A23C"><Star /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.avgRating }}</div>
              <div class="stats-label">平均评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#F56C6C"><Warning /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.highOccupancyVenues }}</div>
              <div class="stats-label">高使用率场馆</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>场馆类型分布</span>
          </template>
          <div class="chart-container">
            <div ref="typeChartRef" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>场馆状态分布</span>
          </template>
          <div class="chart-container">
            <div ref="statusChartRef" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门场馆 -->
    <el-card class="popular-venues">
      <template #header>
        <span>热门场馆</span>
        <el-button type="primary" size="small" @click="refreshPopularVenues">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
      
      <el-table :data="popularVenues" border stripe>
        <el-table-column prop="name" label="场馆名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOccupancy" label="当前人数" width="100" />
        <el-table-column prop="maxCapacity" label="最大容量" width="100" />
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewVenueDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { Location, User, Star, Warning, Refresh } from "@element-plus/icons-vue"
import { venueApi } from "@/api/venue"
import * as echarts from "echarts"

export default {
  name: "Dashboard",
  components: {
    Location,
    User,
    Star,
    Warning,
    Refresh
  },
  setup() {
    const router = useRouter()
    const typeChartRef = ref()
    const statusChartRef = ref()
    const popularVenues = ref([])

    // 统计数据
    const stats = ref({
      totalVenues: 0,
      totalOccupancy: 0,
      avgRating: 0,
      highOccupancyVenues: 0
    })

    // 图表实例
    let typeChart = null
    let statusChart = null

    // 获取统计数据
    const getStats = async () => {
      try {
        const venueList = await venueApi.getVenueList({ page: 0, size: 1000 })
        const venues = venueList.content || []
        
        stats.value = {
          totalVenues: venues.length,
          totalOccupancy: venues.reduce((sum, venue) => sum + (venue.currentOccupancy || 0), 0),
          avgRating: venues.length > 0 ? 
            (venues.reduce((sum, venue) => sum + (venue.rating || 0), 0) / venues.length).toFixed(1) : 0,
          highOccupancyVenues: venues.filter(venue => {
            const rate = venue.currentOccupancy / venue.maxCapacity
            return rate > 0.8
          }).length
        }
      } catch (error) {
        console.error("获取统计数据失败:", error)
      }
    }

    // 获取热门场馆
    const getPopularVenues = async () => {
      try {
        const data = await venueApi.getVenueList({ page: 0, size: 10 })
        popularVenues.value = data.content || []
      } catch (error) {
        console.error("获取热门场馆失败:", error)
      }
    }

    // 初始化类型分布图表
    const initTypeChart = async () => {
      try {
        const venueList = await venueApi.getVenueList({ page: 0, size: 1000 })
        const venues = venueList.content || []
        
        const typeCount = {}
        venues.forEach(venue => {
          const type = venue.type || "UNKNOWN"
          typeCount[type] = (typeCount[type] || 0) + 1
        })

        const typeData = Object.entries(typeCount).map(([type, count]) => ({
          name: getTypeLabel(type),
          value: count
        }))

        typeChart = echarts.init(typeChartRef.value)
        const option = {
          tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b}: {c} ({d}%)"
          },
          legend: {
            orient: "vertical",
            left: "left"
          },
          series: [
            {
              name: "场馆类型",
              type: "pie",
              radius: "50%",
              data: typeData,
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: "rgba(0, 0, 0, 0.5)"
                }
              }
            }
          ]
        }
        typeChart.setOption(option)
      } catch (error) {
        console.error("初始化类型图表失败:", error)
      }
    }

    // 初始化状态分布图表
    const initStatusChart = async () => {
      try {
        const venueList = await venueApi.getVenueList({ page: 0, size: 1000 })
        const venues = venueList.content || []
        
        const statusCount = {}
        venues.forEach(venue => {
          const status = venue.status || "UNKNOWN"
          statusCount[status] = (statusCount[status] || 0) + 1
        })

        const statusData = Object.entries(statusCount).map(([status, count]) => ({
          name: getStatusLabel(status),
          value: count
        }))

        statusChart = echarts.init(statusChartRef.value)
        const option = {
          tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b}: {c} ({d}%)"
          },
          legend: {
            orient: "vertical",
            left: "left"
          },
          series: [
            {
              name: "场馆状态",
              type: "pie",
              radius: "50%",
              data: statusData,
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: "rgba(0, 0, 0, 0.5)"
                }
              }
            }
          ]
        }
        statusChart.setOption(option)
      } catch (error) {
        console.error("初始化状态图表失败:", error)
      }
    }

    // 获取类型标签样式
    const getTypeTagType = (type) => {
      const typeMap = {
        "GYM": "success",
        "SWIMMING": "primary",
        "TENNIS": "warning",
        "BASKETBALL": "danger",
        "FOOTBALL": "info"
      }
      return typeMap[type] || "info"
    }

    // 获取类型标签文本
    const getTypeLabel = (type) => {
      const typeMap = {
        "GYM": "健身房",
        "SWIMMING": "游泳",
        "TENNIS": "网球",
        "BASKETBALL": "篮球",
        "FOOTBALL": "足球"
      }
      return typeMap[type] || type
    }

    // 获取状态标签文本
    const getStatusLabel = (status) => {
      const statusMap = {
        "ACTIVE": "正常",
        "INACTIVE": "停用",
        "MAINTENANCE": "维护中"
      }
      return statusMap[status] || status
    }

    // 刷新热门场馆
    const refreshPopularVenues = () => {
      getPopularVenues()
      ElMessage.success("刷新成功")
    }

    // 查看场馆详情
    const viewVenueDetail = (row) => {
      router.push(`/venue/detail/${row.id}`)
    }

    onMounted(async () => {
      await getStats()
      await getPopularVenues()
      
      await nextTick()
      initTypeChart()
      initStatusChart()
    })

    return {
      stats,
      popularVenues,
      typeChartRef,
      statusChartRef,
      getTypeTagType,
      getTypeLabel,
      getStatusLabel,
      refreshPopularVenues,
      viewVenueDetail
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  margin-right: 20px;
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.popular-venues {
  margin-bottom: 20px;
}

.popular-venues :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
