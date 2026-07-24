<template>
  <div class="page">
    <el-card class="toolbar">
      <el-button type="primary" @click="openCreate">新增球队</el-button>
      <el-button @click="load">刷新</el-button>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="name" label="队名" min-width="140" />
        <el-table-column prop="captainName" label="队长" width="110" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="liaisonStaffName" label="对接员工" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === "ACTIVE" ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/teams/${row.id}`)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="openLiaison(row)">换对接人</el-button>
            <el-button size="small" :type="row.status === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === "ACTIVE" ? "停用" : "启用" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增球队' : '编辑球队'" width="480px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="队名" required><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="队长" required><el-input v-model="dialog.form.captainName" /></el-form-item>
        <el-form-item label="电话" required><el-input v-model="dialog.form.phone" /></el-form-item>
        <el-form-item label="对接员工" required>
          <el-select v-model="dialog.form.liaisonStaffId" style="width: 100%">
            <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName || s.username" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="dialog.form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="liaison.visible" title="更换对接员工" width="420px">
      <el-form label-width="90px">
        <el-form-item label="当前">{{ liaison.currentName }}</el-form-item>
        <el-form-item label="新对接人" required>
          <el-select v-model="liaison.staffId" style="width: 100%">
            <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName || s.username" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="liaison.reason" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="liaison.visible = false">取消</el-button>
        <el-button type="primary" @click="saveLiaison">确认更换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { staffApi, teamApi } from "@/api"

export default {
  name: "TeamList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const staffOptions = ref([])
    const dialog = reactive({
      visible: false,
      mode: "create",
      form: { id: null, name: "", captainName: "", phone: "", liaisonStaffId: null, remark: "" }
    })
    const liaison = reactive({ visible: false, teamId: null, currentName: "", staffId: null, reason: "" })

    const load = async () => {
      loading.value = true
      try {
        list.value = (await teamApi.list()) || []
      } finally {
        loading.value = false
      }
    }

    const loadStaff = async () => {
      staffOptions.value = (await staffApi.options()) || []
    }

    const openCreate = () => {
      dialog.mode = "create"
      dialog.form = { id: null, name: "", captainName: "", phone: "", liaisonStaffId: staffOptions.value[0]?.id || null, remark: "" }
      dialog.visible = true
    }

    const openEdit = (row) => {
      dialog.mode = "edit"
      dialog.form = { ...row }
      dialog.visible = true
    }

    const save = async () => {
      const f = dialog.form
      if (!f.name || !f.captainName || !f.phone || !f.liaisonStaffId) {
        ElMessage.warning("请填写必填项")
        return
      }
      if (dialog.mode === "create") await teamApi.create(f)
      else await teamApi.update(f.id, f)
      ElMessage.success("已保存")
      dialog.visible = false
      load()
    }

    const openLiaison = (row) => {
      liaison.teamId = row.id
      liaison.currentName = row.liaisonStaffName
      liaison.staffId = row.liaisonStaffId
      liaison.reason = ""
      liaison.visible = true
    }

    const saveLiaison = async () => {
      await teamApi.changeLiaison(liaison.teamId, { liaisonStaffId: liaison.staffId, reason: liaison.reason })
      ElMessage.success("对接人已更新")
      liaison.visible = false
      load()
    }

    const toggleStatus = async (row) => {
      const status = row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE"
      await ElMessageBox.confirm(`确认${status === "ACTIVE" ? "启用" : "停用"}？`)
      await teamApi.updateStatus(row.id, status)
      ElMessage.success("已更新")
      load()
    }

    onMounted(async () => {
      await loadStaff()
      await load()
    })

    return { loading, list, staffOptions, dialog, liaison, load, openCreate, openEdit, save, openLiaison, saveLiaison, toggleStatus }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
