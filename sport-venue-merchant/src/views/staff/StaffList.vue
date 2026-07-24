<template>
  <div class="staff-page">
    <el-card class="toolbar">
      <el-button type="primary" @click="openCreate">新增店员</el-button>
      <el-button @click="load">刷新</el-button>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="role" label="角色" width="90">
          <template #default="{ row }">
            {{ row.role === "OWNER" ? "老板" : "店员" }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === "ACTIVE" ? "在职" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)" :disabled="row.role === 'OWNER'">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              :disabled="row.role === 'OWNER'"
              @click="toggleStatus(row)"
            >
              {{ row.status === "ACTIVE" ? "停用" : "启用" }}
            </el-button>
            <el-button size="small" type="primary" @click="resetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增店员' : '编辑员工'" width="460px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item v-if="dialog.mode === 'create'" label="登录账号" required>
          <el-input v-model="dialog.form.username" />
        </el-form-item>
        <el-form-item v-if="dialog.mode === 'create'" label="初始密码" required>
          <el-input v-model="dialog.form.password" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="dialog.form.realName" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="dialog.form.phone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { staffApi } from "@/api"

export default {
  name: "StaffList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const dialog = reactive({
      visible: false,
      saving: false,
      mode: "create",
      form: {
        id: null,
        username: "",
        password: "",
        realName: "",
        phone: "",
        remark: ""
      }
    })

    const load = async () => {
      loading.value = true
      try {
        list.value = (await staffApi.list()) || []
      } finally {
        loading.value = false
      }
    }

    const openCreate = () => {
      dialog.mode = "create"
      Object.assign(dialog.form, {
        id: null,
        username: "",
        password: "123456",
        realName: "",
        phone: "",
        remark: ""
      })
      dialog.visible = true
    }

    const openEdit = (row) => {
      dialog.mode = "edit"
      Object.assign(dialog.form, {
        id: row.id,
        username: row.username,
        password: "",
        realName: row.realName || "",
        phone: row.phone || "",
        remark: row.remark || ""
      })
      dialog.visible = true
    }

    const save = async () => {
      if (!dialog.form.realName) {
        ElMessage.warning("请填写姓名")
        return
      }
      dialog.saving = true
      try {
        if (dialog.mode === "create") {
          if (!dialog.form.username || !dialog.form.password) {
            ElMessage.warning("请填写账号和密码")
            return
          }
          await staffApi.create({
            username: dialog.form.username,
            password: dialog.form.password,
            realName: dialog.form.realName,
            phone: dialog.form.phone,
            remark: dialog.form.remark
          })
        } else {
          await staffApi.update(dialog.form.id, {
            realName: dialog.form.realName,
            phone: dialog.form.phone,
            remark: dialog.form.remark
          })
        }
        ElMessage.success("保存成功")
        dialog.visible = false
        await load()
      } finally {
        dialog.saving = false
      }
    }

    const toggleStatus = async (row) => {
      const status = row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE"
      await ElMessageBox.confirm(
        `确定将「${row.realName || row.username}」设为${status === "ACTIVE" ? "在职" : "停用"}吗？`,
        "提示",
        { type: "warning" }
      )
      await staffApi.updateStatus(row.id, status)
      ElMessage.success("状态已更新")
      load()
    }

    const resetPwd = async (row) => {
      const { value } = await ElMessageBox.prompt(
        `为「${row.realName || row.username}」设置新密码`,
        "重置密码",
        {
          inputValue: "123456",
          inputPattern: /.{6,}/,
          inputErrorMessage: "密码至少6位"
        }
      )
      await staffApi.resetPassword(row.id, value)
      ElMessage.success("密码已重置")
    }

    onMounted(load)

    return {
      loading,
      list,
      dialog,
      load,
      openCreate,
      openEdit,
      save,
      toggleStatus,
      resetPwd
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
