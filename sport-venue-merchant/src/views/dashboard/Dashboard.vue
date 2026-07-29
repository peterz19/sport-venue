<template>
  <div class="dashboard">
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card><div class="label">我的场馆</div><div class="value">{{ data.venueCount || 0 }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="label">今日收银</div>
          <div class="value">¥{{ Number(data.salesAmount || 0).toFixed(2) }}</div>
          <div class="sub">{{ data.salesOrderCount || 0 }} 笔</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="label">今日订场</div>
          <div class="value">{{ data.bookingCount || 0 }}</div>
          <div class="sub">金额 ¥{{ Number(data.bookingAmount || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="label">订场状态</div>
          <div class="value-sm">预订 {{ data.bookingBooked || 0 }} · 完成 {{ data.bookingCompleted || 0 }} · 取消 {{ data.bookingCancelled || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的场馆</span>
          <div>
            <el-button size="small" @click="$router.push('/venue/list')">场馆管理</el-button>
            <el-button size="small" type="primary" @click="$router.push('/sales/daily')">销售报表</el-button>
            <el-button size="small" @click="$router.push('/booking/list')">订场列表</el-button>
          </div>
        </div>
      </template>
      <el-table :data="data.venues || []" border stripe v-loading="loading">
        <el-table-column prop="name" label="场馆" min-width="140" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="currentOccupancy" label="当前人数" width="100" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/venue/detail/${row.id}`)">查看</el-button>
            <el-button size="small" type="primary" @click="$router.push('/venue/list')">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { merchantVenueApi } from "@/api"

export default {
  name: "Dashboard",
  setup() {
    const loading = ref(false)
    const date = ref(new Date().toISOString().slice(0, 10))
    const data = ref({})

    const load = async () => {
      loading.value = true
      try {
        data.value = (await merchantVenueApi.dashboardOverview({ date: date.value })) || {}
      } finally {
        loading.value = false
      }
    }

    onMounted(load)
    return { loading, date, data, load }
  }
}
</script>

<style scoped>
.filter-card { margin-bottom: 16px; }
.stats-row { margin-bottom: 16px; }
.label { color: #909399; font-size: 13px; }
.value { margin-top: 8px; font-size: 26px; font-weight: 700; }
.value-sm { margin-top: 10px; font-size: 15px; font-weight: 600; line-height: 1.5; }
.sub { margin-top: 4px; color: #909399; font-size: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
