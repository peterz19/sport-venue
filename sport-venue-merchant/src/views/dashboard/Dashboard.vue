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
              <div class="stats-label">我的场馆</div>
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
              <el-icon size="40" color="#F56C6C"><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.occupancyRate }}%</div>
              <div class="stats-label">平均使用率</div>
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
            <span>场馆使用率</span>
          </template>
          <div class="chart-container">
            <div ref="occupancyChartRef" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>场馆评分分布</span>
          </template>
          <div class="chart-container">
            <div ref="ratingChartRef" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的场馆列表 -->
    <el-card class="my-venues">
      <template #header>
        <span>我的场馆</span>
        <el-button type="primary" size="small" @click="refreshVenues">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
      
      <el-table :data="myVenues" border stripe>
        <el-table-column prop="name" label="场馆名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewVenueDetail(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editVenue(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { Location, User, Star, TrendCharts, Refresh } from "@element-plus/icons-vue"
import { merchantVenueApi } from "@/api"
import * as echarts from "echarts"

export default {
  name: "Dashboard",
  components: {
    Location,
    User,
    Star,
    TrendCharts,
    Refresh
  },
  setup() {
    const router = useRouter()
    const occupancyChartRef = ref()
    const ratingChartRef = ref()
    const myVenues = ref([])

    // 统计数据
    const stats = ref({
      totalVenues: 0,
      totalOccupancy: 0,
      avgRating: 0,
      occupancyRate: 0
    })

    // 图表实例
    let occupancyChart = null
    let ratingChart = null

    // 商户信息
    const merchantInfo = computed(() => {
      return JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    })

    // 获取我的场馆列表
    const getMyVenues = async () => {
      try {
        const data = await merchantVenueApi.getVenueList({ page: 0, size: 100 })
        myVenues.value = data.content || []
        
        // 计算统计数据
        const venues = myVenues.value
        stats.value = {
          totalVenues: venues.length,
          totalOccupancy: venues.reduce((sum, venue) => sum + (venue.currentOccupancy || 0), 0),
          avgRating: venues.length > 0 ? 
            (venues.reduce((sum, venue) => sum + (venue.rating || 0), 0) / venues.length).toFixed(1) : 0,
          occupancyRate: venues.length > 0 ? 
            Math.round((venues.reduce((sum, venue) => sum + (venue.currentOccupancy || 0), 0) / 
                       venues.reduce((sum, venue) => sum + (venue.maxCapacity || 1), 0)) * 100) : 0
        }
      } catch (error) {
        console.error("获取场馆列表失败:", error)
        ElMessage.error("获取场馆列表失败")
      }
    }

    // 初始化使用率图表
    const initOccupancyChart = () => {
      if (!occupancyChartRef.value) return
      
      occupancyChart = echarts.init(occupancyChartRef.value)
      const venues = myVenues.value
      
      const data = venues.map(venue => ({
        name: venue.name,
        value: Math.round((venue.currentOccupancy / venue.maxCapacity) * 100)
      }))

      const option = {
        tooltip: {
          trigger: "axis",
          formatter: "{b}: {c}%"
        },
        xAxis: {
          type: "category",
          data: data.map(item => item.name),
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: "value",
          max: 100,
          axisLabel: {
            formatter: "{value}%"
          }
        },
        series: [
          {
            name: "使用率",
            type: "bar",
            data: data.map(item => item.value),
            itemStyle: {
              color: function(params) {
                const value = params.value
                if (value > 80) return "#F56C6C"
                if (value > 60) return "#E6A23C"
                return "#67C23A"
              }
            }
          }
        ]
      }
      occupancyChart.setOption(option)
    }

    // 初始化评分图表
    const initRatingChart = () => {
      if (!ratingChartRef.value) return
      
      ratingChart = echarts.init(ratingChartRef.value)
      const venues = myVenues.value
      
      const ratingCount = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
      venues.forEach(venue => {
        const rating = Math.floor(venue.rating || 0)
        if (rating >= 1 && rating <= 5) {
          ratingCount[rating]++
        }
      })

      const data = Object.entries(ratingCount).map(([rating, count]) => ({
        name: `${rating}星`,
        value: count
      }))

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
            name: "评分分布",
            type: "pie",
            radius: "50%",
            data: data,
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
      ratingChart.setOption(option)
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

    // 获取状态标签样式
    const getStatusTagType = (status) => {
      const statusMap = {
        "ACTIVE": "success",
        "INACTIVE": "danger",
        "MAINTENANCE": "warning"
      }
      return statusMap[status] || "info"
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

    // 刷新场馆列表
    const refreshVenues = () => {
      getMyVenues()
      ElMessage.success("刷新成功")
    }

    // 查看场馆详情
    const viewVenueDetail = (row) => {
      router.push(`/venue/detail/${row.id}`)
    }

    // 编辑场馆
    const editVenue = (row) => {
      // 这里可以跳转到编辑页面或打开编辑对话框
      ElMessage.info("编辑功能开发中...")
    }

    onMounted(async () => {
      await getMyVenues()
      
      await nextTick()
      initOccupancyChart()
      initRatingChart()
    })

    return {
      stats,
      myVenues,
      occupancyChartRef,
      ratingChartRef,
      merchantInfo,
      getTypeTagType,
      getTypeLabel,
      getStatusTagType,
      getStatusLabel,
      refreshVenues,
      viewVenueDetail,
      editVenue
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

.my-venues {
  margin-bottom: 20px;
}

.my-venues :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 