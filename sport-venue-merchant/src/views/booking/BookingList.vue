<template>
  <div class="page">
    <el-card class="toolbar">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 140px">
            <el-option label="已预订" value="BOOKED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="$router.push('/booking/calendar')">去日历订场</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="单号" width="150" />
        <el-table-column prop="courtName" label="片场" width="120" />
        <el-table-column prop="startTime" label="开始" width="160" />
        <el-table-column prop="endTime" label="结束" width="160" />
        <el-table-column label="对象" min-width="140">
          <template #default="{ row }">
            {{ row.bookType === "TEAM" ? row.teamName : `${row.personName} ${row.personPhone || ""}` }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="liaisonStaffName" label="对接人" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" v-if="row.status === 'BOOKED'" @click="complete(row)">完成</el-button>
            <el-button size="small" type="danger" v-if="row.status === 'BOOKED'" @click="cancel(row)">取消</el-button>
            <el-button size="small" type="primary" v-if="!row.matchResultId && (row.status === 'BOOKED' || row.status === 'COMPLETED')" @click="openMatch(row)">录赛果</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="match.visible" title="录入赛果" width="480px">
      <el-form :model="match.form" label-width="90px">
        <el-form-item label="订场单">{{ match.orderNo }}</el-form-item>
        <el-form-item label="主队" required>
          <el-select v-model="match.form.homeTeamId" style="width: 100%">
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="客队" required>
          <el-select v-model="match.form.awayTeamId" style="width: 100%">
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主队比分" required>
          <el-input-number v-model="match.form.homeScore" :min="0" />
        </el-form-item>
        <el-form-item label="客队比分" required>
          <el-input-number v-model="match.form.awayScore" :min="0" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="match.form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="match.visible = false">取消</el-button>
        <el-button type="primary" @click="saveMatch">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { bookingApi, matchApi, teamApi } from "@/api"

export default {
  name: "BookingList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const teams = ref([])
    const query = reactive({ date: new Date().toISOString().slice(0, 10), status: "" })
    const match = reactive({
      visible: false,
      orderNo: "",
      form: { bookingId: null, homeTeamId: null, awayTeamId: null, homeScore: 0, awayScore: 0, remark: "" }
    })

    const load = async () => {
      loading.value = true
      try {
        const data = await bookingApi.list({
          date: query.date || undefined,
          status: query.status || undefined,
          page: 0,
          size: 50
        })
        list.value = data?.content || []
      } finally {
        loading.value = false
      }
    }

    const cancel = async (row) => {
      try {
        await ElMessageBox.confirm("确认取消该订场？取消后将释放时段。", "取消订场")
        await bookingApi.cancel(row.id, { reason: "手动取消" })
        ElMessage.success("已取消")
        load()
      } catch (e) {
        // 用户取消确认框时忽略
      }
    }

    const complete = async (row) => {
      await bookingApi.complete(row.id)
      ElMessage.success("已完成")
      load()
    }

    const openMatch = (row) => {
      match.orderNo = row.orderNo
      match.form = {
        bookingId: row.id,
        homeTeamId: row.teamId || teams.value[0]?.id || null,
        awayTeamId: teams.value[1]?.id || teams.value[0]?.id || null,
        homeScore: 0,
        awayScore: 0,
        remark: ""
      }
      match.visible = true
    }

    const saveMatch = async () => {
      await matchApi.create(match.form)
      ElMessage.success("赛果已保存")
      match.visible = false
      load()
    }

    onMounted(async () => {
      teams.value = (await teamApi.options()) || []
      await load()
    })

    return { loading, list, teams, query, match, load, cancel, complete, openMatch, saveMatch }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
