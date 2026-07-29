<template>
  <div class="venue-list">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="场馆名称">
          <el-input v-model="searchForm.name" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="getVenueList">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="venueList" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="场馆名称" min-width="140" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOccupancy" label="当前人数" width="100" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleUpdateOccupancy(row)">更新人数</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="getVenueList"
          @current-change="getVenueList"
        />
      </div>
    </el-card>

    <el-dialog v-model="edit.visible" title="编辑场馆" width="560px">
      <el-form :model="edit.form" label-width="100px">
        <el-form-item label="名称" required><el-input v-model="edit.form.name" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="edit.form.type" style="width: 100%">
            <el-option label="公园" value="PARK" />
            <el-option label="机构" value="INSTITUTION" />
            <el-option label="体育场" value="STADIUM" />
            <el-option label="健身房" value="GYM" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="空间类型" required>
          <el-select v-model="edit.form.spaceType" style="width: 100%">
            <el-option label="室内" value="INDOOR" />
            <el-option label="室外" value="OUTDOOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="收费类型" required>
          <el-select v-model="edit.form.chargeType" style="width: 100%">
            <el-option label="收费" value="PAID" />
            <el-option label="免费" value="FREE" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" required><el-input v-model="edit.form.address" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="edit.form.phone" /></el-form-item>
        <el-form-item label="开放时间"><el-input v-model="edit.form.openTime" placeholder="如 08:00" /></el-form-item>
        <el-form-item label="关闭时间"><el-input v-model="edit.form.closeTime" placeholder="如 22:00" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="edit.form.capacity" :min="0" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="edit.form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="edit.form.status" style="width: 100%">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="edit.visible = false">取消</el-button>
        <el-button type="primary" :loading="edit.loading" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="occupancyDialog.visible" title="更新场馆人数" width="400px">
      <el-form label-width="100px">
        <el-form-item label="场馆">{{ occupancyDialog.venueName }}</el-form-item>
        <el-form-item label="当前人数">
          <el-input-number v-model="occupancyDialog.form.occupancy" :min="0" :max="occupancyDialog.maxCapacity || 9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="occupancyDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="occupancyDialog.loading" @click="confirmUpdateOccupancy">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { merchantVenueApi } from "@/api"

export default {
  name: "VenueList",
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const allVenues = ref([])
    const venueList = ref([])
    const searchForm = reactive({ name: "", status: "" })
    const pagination = reactive({ page: 1, size: 10, total: 0 })
    const edit = reactive({
      visible: false,
      loading: false,
      form: {}
    })
    const occupancyDialog = reactive({
      visible: false,
      loading: false,
      venueName: "",
      maxCapacity: 0,
      venueId: null,
      form: { occupancy: 0 }
    })

    const typeLabel = (t) => ({ PARK: "公园", INSTITUTION: "机构", STADIUM: "体育场", GYM: "健身房", OTHER: "其他" }[t] || t)
    const statusLabel = (s) => ({ ACTIVE: "正常", INACTIVE: "停用", MAINTENANCE: "维护中" }[s] || s)

    const applyPage = () => {
      let filtered = allVenues.value
      if (searchForm.name) filtered = filtered.filter((v) => (v.name || "").includes(searchForm.name))
      if (searchForm.status) filtered = filtered.filter((v) => v.status === searchForm.status)
      pagination.total = filtered.length
      const start = (pagination.page - 1) * pagination.size
      venueList.value = filtered.slice(start, start + pagination.size)
    }

    const getVenueList = async () => {
      loading.value = true
      try {
        const data = await merchantVenueApi.getVenueList()
        allVenues.value = Array.isArray(data) ? data : []
        applyPage()
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.page = 1
      applyPage()
    }
    const handleReset = () => {
      searchForm.name = ""
      searchForm.status = ""
      pagination.page = 1
      applyPage()
    }
    const handleView = (row) => router.push(`/venue/detail/${row.id}`)

    const openEdit = async (row) => {
      const detail = (await merchantVenueApi.getVenueById(row.id)) || row
      edit.form = {
        id: detail.id,
        name: detail.name,
        type: detail.type || "GYM",
        spaceType: detail.spaceType || "INDOOR",
        chargeType: detail.chargeType || "PAID",
        address: detail.address || "",
        phone: detail.phone || "",
        openTime: detail.openTime || "",
        closeTime: detail.closeTime || "",
        capacity: detail.capacity || 0,
        description: detail.description || "",
        status: detail.status || "ACTIVE",
        merchantId: detail.merchantId
      }
      edit.visible = true
    }

    const saveEdit = async () => {
      if (!edit.form.name || !edit.form.address) {
        ElMessage.warning("请填写名称和地址")
        return
      }
      edit.loading = true
      try {
        await merchantVenueApi.updateVenue(edit.form.id, edit.form)
        if (edit.form.status) {
          await merchantVenueApi.updateVenueStatus(edit.form.id, edit.form.status)
        }
        ElMessage.success("已保存")
        edit.visible = false
        getVenueList()
      } finally {
        edit.loading = false
      }
    }

    const handleUpdateOccupancy = (row) => {
      occupancyDialog.venueName = row.name
      occupancyDialog.maxCapacity = row.capacity || 9999
      occupancyDialog.venueId = row.id
      occupancyDialog.form.occupancy = row.currentOccupancy || 0
      occupancyDialog.visible = true
    }

    const confirmUpdateOccupancy = async () => {
      occupancyDialog.loading = true
      try {
        await merchantVenueApi.updateVenueOccupancy(occupancyDialog.venueId, occupancyDialog.form.occupancy)
        ElMessage.success("更新成功")
        occupancyDialog.visible = false
        getVenueList()
      } finally {
        occupancyDialog.loading = false
      }
    }

    onMounted(getVenueList)
    return {
      loading, venueList, searchForm, pagination, edit, occupancyDialog,
      typeLabel, statusLabel, getVenueList, handleSearch, handleReset, handleView,
      openEdit, saveEdit, handleUpdateOccupancy, confirmUpdateOccupancy
    }
  }
}
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
