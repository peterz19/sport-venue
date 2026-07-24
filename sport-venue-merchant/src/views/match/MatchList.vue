<template>
  <div class="page">
    <el-card class="toolbar">
      <el-button @click="load">刷新</el-button>
      <el-button type="primary" @click="$router.push('/booking/list')">从订场录赛果</el-button>
    </el-card>
    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="bookingOrderNo" label="订场单" width="150" />
        <el-table-column label="对阵" min-width="220">
          <template #default="{ row }">
            {{ row.homeTeamName }} {{ row.homeScore }} : {{ row.awayScore }} {{ row.awayTeamName }}
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="110" />
        <el-table-column prop="operatorName" label="录入人" width="100" />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">修正</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" title="修正赛果" width="420px">
      <el-form label-width="90px">
        <el-form-item label="主队比分"><el-input-number v-model="dialog.homeScore" :min="0" /></el-form-item>
        <el-form-item label="客队比分"><el-input-number v-model="dialog.awayScore" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="dialog.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { ElMessage } from "element-plus"
import { matchApi } from "@/api"

export default {
  name: "MatchList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const dialog = reactive({ visible: false, id: null, homeScore: 0, awayScore: 0, remark: "", homeTeamId: null, awayTeamId: null, bookingId: null })

    const load = async () => {
      loading.value = true
      try {
        list.value = (await matchApi.list()) || []
      } finally {
        loading.value = false
      }
    }

    const edit = (row) => {
      Object.assign(dialog, {
        visible: true,
        id: row.id,
        bookingId: row.bookingId,
        homeTeamId: row.homeTeamId,
        awayTeamId: row.awayTeamId,
        homeScore: row.homeScore,
        awayScore: row.awayScore,
        remark: row.remark || ""
      })
    }

    const save = async () => {
      await matchApi.update(dialog.id, {
        bookingId: dialog.bookingId,
        homeTeamId: dialog.homeTeamId,
        awayTeamId: dialog.awayTeamId,
        homeScore: dialog.homeScore,
        awayScore: dialog.awayScore,
        remark: dialog.remark
      })
      ElMessage.success("已修正")
      dialog.visible = false
      load()
    }

    onMounted(load)
    return { loading, list, dialog, load, edit, save }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
