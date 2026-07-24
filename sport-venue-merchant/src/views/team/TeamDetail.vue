<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.push('/teams')" content="球队详情" style="margin-bottom: 16px" />
    <el-card v-if="detail.team">
      <template #header>基本信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="队名">{{ detail.team.name }}</el-descriptions-item>
        <el-descriptions-item label="队长">{{ detail.team.captainName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.team.phone }}</el-descriptions-item>
        <el-descriptions-item label="对接员工">{{ detail.team.liaisonStaffName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.team.status }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.team.remark || "-" }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px" v-if="detail.recordSummary">
      <template #header>战绩摘要</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="场次">{{ detail.recordSummary.played }}</el-descriptions-item>
        <el-descriptions-item label="胜">{{ detail.recordSummary.win }}</el-descriptions-item>
        <el-descriptions-item label="平">{{ detail.recordSummary.draw }}</el-descriptions-item>
        <el-descriptions-item label="负">{{ detail.recordSummary.loss }}</el-descriptions-item>
        <el-descriptions-item label="得分">{{ detail.recordSummary.pointsFor }}</el-descriptions-item>
        <el-descriptions-item label="失分">{{ detail.recordSummary.pointsAgainst }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>近期订场</template>
      <el-table :data="detail.recentBookings || []" border>
        <el-table-column prop="orderNo" label="单号" width="160" />
        <el-table-column prop="startTime" label="开始" width="170" />
        <el-table-column prop="endTime" label="结束" width="170" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="liaisonStaffName" label="对接人快照" width="110" />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>审计日志</template>
      <el-table :data="detail.audits || []" border>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="action" label="动作" width="140" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="reason" label="原因" min-width="120" />
        <el-table-column prop="afterJson" label="变更后" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import { teamApi } from "@/api"

export default {
  name: "TeamDetail",
  setup() {
    const route = useRoute()
    const loading = ref(false)
    const detail = ref({})

    const load = async () => {
      loading.value = true
      try {
        detail.value = (await teamApi.detail(route.params.id)) || {}
      } finally {
        loading.value = false
      }
    }

    onMounted(load)
    return { loading, detail }
  }
}
</script>
