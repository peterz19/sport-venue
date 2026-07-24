<template>
  <div class="page">
    <el-card class="toolbar">
      <el-form inline>
        <el-form-item label="场馆">
          <el-select v-model="venueId" clearable placeholder="全部" style="width: 200px" @change="load">
            <el-option v-for="v in venues" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openCreate">新增片场</el-button>
          <el-button @click="load">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="name" label="片场" min-width="140" />
        <el-table-column prop="venueName" label="场馆" min-width="140" />
        <el-table-column prop="code" label="编号" width="100" />
        <el-table-column prop="courtType" label="类型" width="90">
          <template #default="{ row }">{{ typeLabel(row.courtType) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === "ACTIVE" ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === "ACTIVE" ? "停用" : "启用" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增片场' : '编辑片场'" width="460px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="场馆" required>
          <el-select v-model="dialog.form.venueId" style="width: 100%">
            <el-option v-for="v in venues" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="dialog.form.code" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="dialog.form.courtType" style="width: 100%">
            <el-option label="全场" value="FULL" />
            <el-option label="半场" value="HALF" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" type="textarea" />
        </el-form-item>
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
import { ElMessage, ElMessageBox } from "element-plus"
import { courtApi, salesApi } from "@/api"

export default {
  name: "CourtList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const venues = ref([])
    const venueId = ref()
    const dialog = reactive({
      visible: false,
      mode: "create",
      form: { id: null, venueId: null, name: "", code: "", courtType: "FULL", sortOrder: 0, remark: "" }
    })

    const typeLabel = (t) => ({ FULL: "全场", HALF: "半场", OTHER: "其他" }[t] || t)

    const loadVenues = async () => {
      venues.value = (await salesApi.myVenues()) || []
    }

    const load = async () => {
      loading.value = true
      try {
        list.value = (await courtApi.list({ venueId: venueId.value || undefined })) || []
      } finally {
        loading.value = false
      }
    }

    const openCreate = () => {
      dialog.mode = "create"
      dialog.form = {
        id: null,
        venueId: venues.value[0]?.id || null,
        name: "",
        code: "",
        courtType: "FULL",
        sortOrder: 0,
        remark: ""
      }
      dialog.visible = true
    }

    const openEdit = (row) => {
      dialog.mode = "edit"
      dialog.form = { ...row }
      dialog.visible = true
    }

    const save = async () => {
      if (!dialog.form.venueId || !dialog.form.name) {
        ElMessage.warning("请填写场馆和名称")
        return
      }
      if (dialog.mode === "create") {
        await courtApi.create(dialog.form)
      } else {
        await courtApi.update(dialog.form.id, dialog.form)
      }
      ElMessage.success("已保存")
      dialog.visible = false
      load()
    }

    const toggleStatus = async (row) => {
      const status = row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE"
      await ElMessageBox.confirm(`确认${status === "ACTIVE" ? "启用" : "停用"}该片场？`)
      await courtApi.updateStatus(row.id, status)
      ElMessage.success("已更新")
      load()
    }

    onMounted(async () => {
      await loadVenues()
      await load()
    })

    return { loading, list, venues, venueId, dialog, typeLabel, load, openCreate, openEdit, save, toggleStatus }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
