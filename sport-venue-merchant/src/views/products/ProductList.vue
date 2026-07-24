<template>
  <div class="product-page">
    <el-card class="toolbar">
      <el-form :model="query" inline>
        <el-form-item label="名称">
          <el-input v-model="query.keyword" clearable placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="上架" value="ON_SALE" />
            <el-option label="下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button type="success" @click="openDialog()">新增商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="venueName" label="所属场馆" min-width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'">
              {{ row.status === "ON_SALE" ? "上架" : "下架" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 'ON_SALE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === "ON_SALE" ? "下架" : "上架" }}
            </el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.size"
          :total="pager.total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑商品' : '新增商品'" width="480px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="商品名称" required>
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="单价" required>
          <el-input-number v-model="dialog.form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="dialog.form.unit" style="width: 100%">
            <el-option label="个" value="个" />
            <el-option label="瓶" value="瓶" />
            <el-option label="份" value="份" />
            <el-option label="条" value="条" />
            <el-option label="套" value="套" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="dialog.form.category" placeholder="如：饮料" />
        </el-form-item>
        <el-form-item label="所属场馆">
          <el-select v-model="dialog.form.venueId" clearable placeholder="空=商户通用" style="width: 100%">
            <el-option v-for="v in venues" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" style="width: 100%" />
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
import { productApi, salesApi } from "@/api"

export default {
  name: "ProductList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const categories = ref([])
    const venues = ref([])
    const query = reactive({ keyword: "", category: "", status: "" })
    const pager = reactive({ page: 1, size: 10, total: 0 })
    const dialog = reactive({
      visible: false,
      saving: false,
      form: {
        id: null,
        name: "",
        price: 1,
        unit: "个",
        category: "",
        venueId: null,
        sortOrder: 0,
        remark: ""
      }
    })

    const load = async () => {
      loading.value = true
      try {
        const data = await productApi.list({
          keyword: query.keyword || undefined,
          category: query.category || undefined,
          status: query.status || undefined,
          page: pager.page - 1,
          size: pager.size
        })
        list.value = data.content || []
        pager.total = data.totalElements || 0
      } finally {
        loading.value = false
      }
    }

    const loadMeta = async () => {
      categories.value = (await productApi.categories()) || []
      venues.value = (await salesApi.myVenues()) || []
    }

    const openDialog = (row) => {
      if (row) {
        Object.assign(dialog.form, {
          id: row.id,
          name: row.name,
          price: Number(row.price),
          unit: row.unit || "个",
          category: row.category || "",
          venueId: row.venueId,
          sortOrder: row.sortOrder || 0,
          remark: row.remark || ""
        })
      } else {
        Object.assign(dialog.form, {
          id: null,
          name: "",
          price: 1,
          unit: "个",
          category: "",
          venueId: null,
          sortOrder: 0,
          remark: ""
        })
      }
      dialog.visible = true
    }

    const save = async () => {
      if (!dialog.form.name || !dialog.form.price) {
        ElMessage.warning("请填写名称和单价")
        return
      }
      dialog.saving = true
      try {
        const payload = { ...dialog.form }
        delete payload.id
        if (dialog.form.id) {
          await productApi.update(dialog.form.id, payload)
        } else {
          await productApi.create(payload)
        }
        ElMessage.success("保存成功")
        dialog.visible = false
        await loadMeta()
        await load()
      } finally {
        dialog.saving = false
      }
    }

    const toggleStatus = async (row) => {
      const status = row.status === "ON_SALE" ? "OFF_SALE" : "ON_SALE"
      await productApi.updateStatus(row.id, status)
      ElMessage.success("状态已更新")
      load()
    }

    const remove = async (row) => {
      await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, "提示", { type: "warning" })
      await productApi.remove(row.id)
      ElMessage.success("已删除")
      load()
    }

    onMounted(async () => {
      await loadMeta()
      await load()
    })

    return {
      loading,
      list,
      categories,
      venues,
      query,
      pager,
      dialog,
      load,
      openDialog,
      save,
      toggleStatus,
      remove
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
