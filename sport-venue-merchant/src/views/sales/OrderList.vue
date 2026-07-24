<template>
  <div class="orders-page">
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="query.payMethod" clearable placeholder="全部" style="width: 120px">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="$router.push('/sales/daily')">返回报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="paidAt" label="支付时间" width="170" />
        <el-table-column prop="venueName" label="场馆" min-width="120" />
        <el-table-column prop="totalQty" label="件数" width="80" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ ({ CASH: "现金", WECHAT: "微信", ALIPAY: "支付宝" })[row.payMethod] || row.payMethod }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="场馆">{{ current.venueName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(current.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ current.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ current.paidAt }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="current.items || []" border style="margin-top: 12px">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ Number(row.subtotal || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { useRoute } from "vue-router"
import { salesApi } from "@/api"

export default {
  name: "OrderList",
  setup() {
    const route = useRoute()
    const loading = ref(false)
    const list = ref([])
    const detailVisible = ref(false)
    const current = ref({})
    const query = reactive({
      date: route.query.date || new Date().toISOString().slice(0, 10),
      payMethod: ""
    })
    const pager = reactive({ page: 1, size: 10, total: 0 })

    const load = async () => {
      loading.value = true
      try {
        const data = await salesApi.listOrders({
          date: query.date,
          payMethod: query.payMethod || undefined,
          status: "PAID",
          page: pager.page - 1,
          size: pager.size
        })
        list.value = data.content || []
        pager.total = data.totalElements || 0
      } finally {
        loading.value = false
      }
    }

    const showDetail = async (row) => {
      current.value = (await salesApi.getOrder(row.orderId)) || row
      detailVisible.value = true
    }

    onMounted(load)

    return { loading, list, query, pager, detailVisible, current, load, showDetail }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
