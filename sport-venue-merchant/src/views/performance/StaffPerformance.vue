<template>
  <div class="rank-page">
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="items" border stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="staffName" label="员工" min-width="120" />
        <el-table-column prop="role" label="角色" width="90">
          <template #default="{ row }">
            {{ row.role === "OWNER" ? "老板" : "店员" }}
          </template>
        </el-table-column>
        <el-table-column prop="salesOrderCount" label="收银笔数" width="100" />
        <el-table-column label="收银金额" width="120">
          <template #default="{ row }">¥{{ Number(row.salesAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="salesQty" label="件数" width="80" />
        <el-table-column prop="bookingOperateCount" label="订场操作单" width="110" />
        <el-table-column prop="bookingLiaisonCount" label="订场对接单" width="110" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { performanceApi } from "@/api"

export default {
  name: "StaffPerformance",
  setup() {
    const date = ref(new Date().toISOString().slice(0, 10))
    const loading = ref(false)
    const items = ref([])

    const load = async () => {
      loading.value = true
      try {
        const data = await performanceApi.rank({ date: date.value })
        items.value = data.items || []
      } finally {
        loading.value = false
      }
    }

    onMounted(load)
    return { date, loading, items, load }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
</style>
