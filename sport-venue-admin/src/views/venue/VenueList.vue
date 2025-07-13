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
        <el-form-item label="空间类型">
          <el-select
            v-model="searchForm.spaceType"
            placeholder="请选择空间类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="type in venueSpaceTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付费类型">
          <el-select
            v-model="searchForm.chargeType"
            placeholder="请选择付费类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="type in venueChargeTypes"
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
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加场馆
      </el-button>
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
        <el-table-column prop="spaceType" label="空间类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getSpaceTypeTagType(row.spaceType)">
              {{ getSpaceTypeLabel(row.spaceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chargeType" label="付费类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getChargeTypeTagType(row.chargeType)">
              {{ getChargeTypeLabel(row.chargeType) }}
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
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
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
  </div>
</template>

<script>
import { ref, reactive, onMounted } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { Search, Refresh, Plus } from "@element-plus/icons-vue"
import { venueApi } from "@/api/venue"

export default {
  name: "VenueList",
  components: {
    Search,
    Refresh,
    Plus
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const venueList = ref([])
    const venueTypes = ref([])
    const venueSpaceTypes = ref([])
    const venueChargeTypes = ref([])
    const venueStatuses = ref([])

    // 搜索表单
    const searchForm = reactive({
      name: "",
      type: "",
      status: "",
      spaceType: "",
      chargeType: ""
    })

    // 分页
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0
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
        const data = await venueApi.getVenueList(params)
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
        const data = await venueApi.getVenueTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取场馆类型失败:", error)
      }
    }

    // 获取场馆状态
    const getVenueStatuses = async () => {
      try {
        const data = await venueApi.getVenueStatuses()
        // 转换数据格式：{code, description} -> {value, label}
        venueStatuses.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取场馆状态失败:", error)
      }
    }

    // 获取空间类型
    const getVenueSpaceTypes = async () => {
      try {
        const data = await venueApi.getVenueSpaceTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueSpaceTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取空间类型失败:", error)
      }
    }

    // 获取付费类型
    const getVenueChargeTypes = async () => {
      try {
        const data = await venueApi.getVenueChargeTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueChargeTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取付费类型失败:", error)
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
        status: "",
        spaceType: "",
        chargeType: ""
      })
      pagination.page = 1
      getVenueList()
    }

    // 添加场馆
    const handleAdd = () => {
      router.push("/venue/add")
    }

    // 查看场馆
    const handleView = (row) => {
      router.push(`/venue/detail/${row.id}`)
    }

    // 编辑场馆
    const handleEdit = (row) => {
      router.push(`/venue/edit/${row.id}`)
    }

    // 删除场馆
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除场馆"${row.name}"吗？`,
          "确认删除",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        )
        
        await venueApi.deleteVenue(row.id)
        ElMessage.success("删除成功")
        getVenueList()
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除场馆失败:", error)
          ElMessage.error("删除场馆失败")
        }
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

    // 获取空间类型标签样式
    const getSpaceTypeTagType = (type) => {
      const typeMap = {
        "PRIVATE": "success",
        "GROUP": "primary",
        "PUBLIC": "warning"
      }
      return typeMap[type] || "info"
    }

    // 获取空间类型标签文本
    const getSpaceTypeLabel = (type) => {
      const typeMap = {
        "PRIVATE": "私密",
        "GROUP": "团体",
        "PUBLIC": "公开"
      }
      return typeMap[type] || type
    }

    // 获取付费类型标签样式
    const getChargeTypeTagType = (type) => {
      const typeMap = {
        "FREE": "success",
        "PAID": "primary",
        "MEMBER": "warning"
      }
      return typeMap[type] || "info"
    }

    // 获取付费类型标签文本
    const getChargeTypeLabel = (type) => {
      const typeMap = {
        "FREE": "免费",
        "PAID": "付费",
        "MEMBER": "会员"
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
      const statusItem = venueStatuses.value.find(item => item.value === status)
      return statusItem ? statusItem.label : status
    }

    onMounted(() => {
      getVenueTypes()
      getVenueSpaceTypes()
      getVenueChargeTypes()
      getVenueStatuses()
      getVenueList()
    })

    return {
      loading,
      venueList,
      venueTypes,
      venueSpaceTypes,
      venueChargeTypes,
      venueStatuses,
      searchForm,
      pagination,
      handleSearch,
      handleReset,
      handleAdd,
      handleView,
      handleEdit,
      handleDelete,
      handleRefresh,
      handleSizeChange,
      handleCurrentChange,
      getTypeTagType,
      getTypeLabel,
      getSpaceTypeTagType,
      getSpaceTypeLabel,
      getChargeTypeTagType,
      getChargeTypeLabel,
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
