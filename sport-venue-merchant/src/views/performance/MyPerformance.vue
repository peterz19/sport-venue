<template>
  <div class="perf-page">
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

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <div class="label">收银笔数</div>
          <div class="value">{{ data.salesOrderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="label">收银金额</div>
          <div class="value">¥{{ Number(data.salesAmount || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="label">销售件数</div>
          <div class="value">{{ data.salesQty || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>订场业绩</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作订场单量">{{ data.bookingOperateCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="操作订场金额">¥{{ Number(data.bookingOperateAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="对接订场单量">{{ data.bookingLiaisonCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="对接订场金额">¥{{ Number(data.bookingLiaisonAmount || 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { performanceApi } from "@/api"

export default {
  name: "MyPerformance",
  setup() {
    const date = ref(new Date().toISOString().slice(0, 10))
    const data = ref({})

    const load = async () => {
      data.value = (await performanceApi.me({ date: date.value })) || {}
    }

    onMounted(load)
    return { date, data, load }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
.label {
  color: #909399;
  font-size: 13px;
}
.value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 700;
}
</style>
