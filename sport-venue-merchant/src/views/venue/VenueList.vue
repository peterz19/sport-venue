<template>
  <div class="venue-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="场馆名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入场馆名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="场馆类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择场馆类型"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="type in venueTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="场馆状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="status in venueStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="action-card">
      <el-button type="success" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="venueList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="场馆名称" min-width="150" />
        <el-table-column prop="type" label="场馆类型" width="120">
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
        <el-table-column prop="rating" label="评分" width="100">
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="handleUpdateOccupancy(row)">
              更新人数
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
          <span>{{ occupancyDialog.venueName }}</span>
        </el-form-item>
        <el-form-item label="当前人数">
          <el-input-number
            v-model="occupancyDialog.form.occupancy"
            :min="0"
            :max="occupancyDialog.maxCapacity"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大容量">
          <span>{{ occupancyDialog.maxCapacity }}</span>
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
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { Search, Refresh } from "@element-plus/icons-vue"
import { merchantVenueApi } from "@/api"

export default {
  name: "VenueList",
  components: {
    Search,
    Refresh
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const venueList = ref([])
    const venueTypes = ref([])
    const venueStatuses = ref([])

    // 搜索表单
    const searchForm = reactive({
      name: "",
      type: "",
      status: ""
    })

    // 分页
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0
    })

    // 更新人数对话框
    const occupancyDialog = reactive({
      visible: false,
      loading: false,
      venueName: "",
      maxCapacity: 0,
      venueId: null,
      form: {
        occupancy: 0
      }
    })

    // 获取场馆列表
    const getVenueList = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.page - 1,
          size: pagination.size,
          ...searchForm
        }
        const data = await merchantVenueApi.getVenueList(params)
        venueList.value = data.content || []
        pagination.total = data.totalElements || 0
      } catch (error) {
        console.error("获取场馆列表失败:", error)
        ElMessage.error("获取场馆列表失败")
      } finally {
        loading.value = false
      }
    }

    // 获取场馆类型
    const getVenueTypes = async () => {
      try {
        const data = await merchantVenueApi.getVenueTypes()
        venueTypes.value = data || []
      } catch (error) {
        console.error("获取场馆类型失败:", error)
      }
    }

    // 获取场馆状态
    const getVenueStatuses = async () => {
      try {
        const data = await merchantVenueApi.getVenueStatuses()
        venueStatuses.value = data || []
      } catch (error) {
        console.error("获取场馆状态失败:", error)
      }
    }

    // 搜索
    const handleSearch = () => {
      pagination.page = 1
      getVenueList()
    }

    // 重置
    const handleReset = () => {
      Object.assign(searchForm, {
        name: "",
        type: "",
        status: ""
      })
      pagination.page = 1
      getVenueList()
    }

    // 查看场馆
    const handleView = (row) => {
      router.push(`/venue/detail/${row.id}`)
    }

    // 编辑场馆
    const handleEdit = (row) => {
      ElMessage.info("编辑功能开发中...")
    }

    // 更新人数
    const handleUpdateOccupancy = (row) => {
      occupancyDialog.venueName = row.name
      occupancyDialog.maxCapacity = row.maxCapacity
      occupancyDialog.venueId = row.id
      occupancyDialog.form.occupancy = row.currentOccupancy || 0
      occupancyDialog.visible = true
    }

    // 确认更新人数
    const confirmUpdateOccupancy = async () => {
      try {
        occupancyDialog.loading = true
        await merchantVenueApi.updateVenueOccupancy(
          occupancyDialog.venueId,
          occupancyDialog.form.occupancy
        )
        ElMessage.success("更新成功")
        occupancyDialog.visible = false
        getVenueList()
      } catch (error) {
        console.error("更新人数失败:", error)
        ElMessage.error("更新人数失败")
      } finally {
        occupancyDialog.loading = false
      }
    }

    // 刷新
    const handleRefresh = () => {
      getVenueList()
    }

    // 分页大小改变
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.page = 1
      getVenueList()
    }

    // 当前页改变
    const handleCurrentChange = (page) => {
      pagination.page = page
      getVenueList()
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

    onMounted(() => {
      getVenueTypes()
      getVenueStatuses()
      getVenueList()
    })

    return {
      loading,
      venueList,
      venueTypes,
      venueStatuses,
      searchForm,
      pagination,
      occupancyDialog,
      handleSearch,
      handleReset,
      handleView,
      handleEdit,
      handleUpdateOccupancy,
      confirmUpdateOccupancy,
      handleRefresh,
      handleSizeChange,
      handleCurrentChange,
      getTypeTagType,
      getTypeLabel,
      getStatusTagType,
      getStatusLabel
    }
  }
}
</script>

<style scoped>
.venue-list {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.action-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style> 