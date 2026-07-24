<template>
  <div class="report-page">
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="场馆">
          <el-select v-model="venueId" clearable placeholder="全部场馆" style="width: 180px">
            <el-option v-for="v in venues" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="$router.push({ path: '/sales/orders', query: { date } })">查看当日订单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card><div class="stat-label">订单笔数</div><div class="stat-value">{{ summary.orderCount || 0 }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-label">销售总额</div><div class="stat-value">¥{{ Number(summary.totalAmount || 0).toFixed(2) }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-label">销售件数</div><div class="stat-value">{{ summary.totalQty || 0 }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-label">现金占比</div><div class="stat-value">{{ cashRatio }}%</div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card>
          <template #header>支付方式明细</template>
          <el-table :data="summary.byPayMethod || []" border>
            <el-table-column prop="label" label="支付方式" />
            <el-table-column prop="orderCount" label="笔数" width="80" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ Number(row.amount || 0).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header>商品销售明细</template>
          <el-table :data="productReport.items || []" border>
            <el-table-column prop="productName" label="商品" min-width="120" />
            <el-table-column label="单价" width="90">
              <template #default="{ row }">¥{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="totalQty" label="销量" width="80" />
            <el-table-column prop="unit" label="单位" width="70" />
            <el-table-column label="销售额" width="110">
              <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { computed, onMounted, ref } from "vue"
import { salesApi } from "@/api"

export default {
  name: "DailyReport",
  setup() {
    const today = new Date().toISOString().slice(0, 10)
    const date = ref(today)
    const venueId = ref(null)
    const venues = ref([])
    const summary = ref({})
    const productReport = ref({})

    const cashRatio = computed(() => {
      const total = Number(summary.value.totalAmount || 0)
      if (!total) return "0.0"
      const cash = (summary.value.byPayMethod || []).find(i => i.payMethod === "CASH")
      return ((Number(cash?.amount || 0) / total) * 100).toFixed(1)
    })

    const load = async () => {
      const params = { date: date.value, venueId: venueId.value || undefined }
      summary.value = (await salesApi.dailySummary(params)) || {}
      productReport.value = (await salesApi.dailyProducts(params)) || {}
    }

    onMounted(async () => {
      venues.value = (await salesApi.myVenues()) || []
      await load()
    })

    return { date, venueId, venues, summary, productReport, cashRatio, load }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
}
</style>
