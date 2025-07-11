<template>
  <div class="venue-detail">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>场馆详情</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="场馆ID">
                {{ venueDetail.id }}
              </el-descriptions-item>
              <el-descriptions-item label="场馆名称">
                {{ venueDetail.name }}
              </el-descriptions-item>
              <el-descriptions-item label="场馆类型">
                <el-tag :type="getTypeTagType(venueDetail.type)">
                  {{ getTypeLabel(venueDetail.type) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="场馆状态">
                <el-tag :type="getStatusTagType(venueDetail.status)">
                  {{ getStatusLabel(venueDetail.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="商户ID">
                {{ venueDetail.merchantId }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions title="容量信息" :column="1" border>
              <el-descriptions-item label="最大容量">
                {{ venueDetail.maxCapacity }} 人
              </el-descriptions-item>
              <el-descriptions-item label="当前人数">
                {{ venueDetail.currentOccupancy }} 人
              </el-descriptions-item>
              <el-descriptions-item label="使用率">
                <el-progress
                  :percentage="getOccupancyRate()"
                  :color="getOccupancyColor()"
                />
              </el-descriptions-item>
              <el-descriptions-item label="评分">
                <el-rate
                  v-model="venueDetail.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
            <el-descriptions title="详细信息" :column="1" border>
              <el-descriptions-item label="地址">
                {{ venueDetail.address }}
              </el-descriptions-item>
              <el-descriptions-item label="描述">
                {{ venueDetail.description || '暂无描述' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(venueDetail.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ formatDate(venueDetail.updatedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
        
        <!-- 操作区域 -->
        <el-row style="margin-top: 20px;">
          <el-col :span="24">
            <el-card>
              <template #header>
                <span>快速操作</span>
              </template>
              
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-button
                    type="success"
                    @click="handleUpdateStatus('ACTIVE')"
                    :disabled="venueDetail.status === 'ACTIVE'"
                  >
                    设为正常
                  </el-button>
                </el-col>
                <el-col :span="6">
                  <el-button
                    type="warning"
                    @click="handleUpdateStatus('MAINTENANCE')"
                    :disabled="venueDetail.status === 'MAINTENANCE'"
                  >
                    设为维护
                  </el-button>
                </el-col>
                <el-col :span="6">
                  <el-button
                    type="danger"
                    @click="handleUpdateStatus('INACTIVE')"
                    :disabled="venueDetail.status === 'INACTIVE'"
                  >
                    设为停用
                  </el-button>
                </el-col>
                <el-col :span="6">
                  <el-button
                    type="primary"
                    @click="handleUpdateOccupancy"
                  >
                    更新人数
                  </el-button>
                </el-col>
              </el-row>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 更新人数对话框 -->
    <el-dialog
      v-model="occupancyDialog.visible"
      title="更新场馆人数"
      width="400px"
    >
      <el-form :model="occupancyDialog.form" label-width="100px">
        <el-form-item label="场馆名称">
          <span>{{ venueDetail.name }}</span>
        </el-form-item>
        <el-form-item label="当前人数">
          <el-input-number
            v-model="occupancyDialog.form.occupancy"
            :min="0"
            :max="venueDetail.maxCapacity"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大容量">
          <span>{{ venueDetail.maxCapacity }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="occupancyDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateOccupancy" :loading="occupancyDialog.loading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { venueApi } from "@/api/venue"

export default {
  name: "VenueDetail",
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const venueDetail = ref({})

    // 更新人数对话框
    const occupancyDialog = reactive({
      visible: false,
      loading: false,
      form: {
        occupancy: 0
      }
    })

    // 获取场馆详情
    const getVenueDetail = async () => {
      loading.value = true
      try {
        const data = await venueApi.getVenueById(route.params.id)
        venueDetail.value = data || {}
      } catch (error) {
        console.error("获取场馆详情失败:", error)
        ElMessage.error("获取场馆详情失败")
      } finally {
        loading.value = false
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

    // 计算使用率
    const getOccupancyRate = () => {
      if (!venueDetail.value.maxCapacity) return 0
      return Math.round((venueDetail.value.currentOccupancy / venueDetail.value.maxCapacity) * 100)
    }

    // 获取使用率颜色
    const getOccupancyColor = () => {
      const rate = getOccupancyRate()
      if (rate > 80) return "#F56C6C"
      if (rate > 60) return "#E6A23C"
      return "#67C23A"
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return "未知"
      return new Date(date).toLocaleString()
    }

    // 编辑场馆
    const handleEdit = () => {
      router.push(`/venue/edit/${route.params.id}`)
    }

    // 返回列表
    const handleBack = () => {
      router.push("/venue/list")
    }

    // 更新状态
    const handleUpdateStatus = async (status) => {
      try {
        await ElMessageBox.confirm(
          `确定要将场馆状态更改为"${getStatusLabel(status)}"吗？`,
          "确认操作",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        )
        
        await venueApi.updateVenueStatus(route.params.id, status)
        ElMessage.success("状态更新成功")
        getVenueDetail()
      } catch (error) {
        if (error !== "cancel") {
          console.error("更新状态失败:", error)
          ElMessage.error("更新状态失败")
        }
      }
    }

    // 更新人数
    const handleUpdateOccupancy = () => {
      occupancyDialog.form.occupancy = venueDetail.value.currentOccupancy || 0
      occupancyDialog.visible = true
    }

    // 确认更新人数
    const confirmUpdateOccupancy = async () => {
      try {
        occupancyDialog.loading = true
        await venueApi.updateVenueOccupancy(
          route.params.id,
          occupancyDialog.form.occupancy
        )
        ElMessage.success("人数更新成功")
        occupancyDialog.visible = false
        getVenueDetail()
      } catch (error) {
        console.error("更新人数失败:", error)
        ElMessage.error("更新人数失败")
      } finally {
        occupancyDialog.loading = false
      }
    }

    onMounted(() => {
      getVenueDetail()
    })

    return {
      loading,
      venueDetail,
      occupancyDialog,
      getTypeTagType,
      getTypeLabel,
      getStatusTagType,
      getStatusLabel,
      getOccupancyRate,
      getOccupancyColor,
      formatDate,
      handleEdit,
      handleBack,
      handleUpdateStatus,
      handleUpdateOccupancy,
      confirmUpdateOccupancy
    }
  }
}
</script>

<style scoped>
.venue-detail {
  padding: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.detail-content {
  min-height: 400px;
}
</style> 