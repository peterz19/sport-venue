<template>
  <div class="page">
    <el-card class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
      <span class="hint">平台抽成应收（A1）：线上交易按规则累计，结清生成快照；通道手续费不在此列。</span>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="merchantId" label="ID" width="70" />
        <el-table-column prop="merchantCode" label="编码" width="140" />
        <el-table-column prop="merchantName" label="商户" min-width="140" />
        <el-table-column label="抽成比例" width="100">
          <template #default="{ row }">{{ formatRate(row.rate) }}</template>
        </el-table-column>
        <el-table-column label="未结抽成" width="120">
          <template #default="{ row }">
            <b style="color:#e6a23c">¥{{ Number(row.pendingCommission || 0).toFixed(2) }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="pendingEntryCount" label="未结笔数" width="90" />
        <el-table-column label="计入渠道" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.includeWechat" size="small" class="tag">微信</el-tag>
            <el-tag v-if="row.includeAlipay" size="small" class="tag" type="success">支付宝</el-tag>
            <el-tag v-if="row.includeCash" size="small" class="tag" type="info">现金</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openRule(row)">规则</el-button>
            <el-button size="small" type="primary" @click="openDetail(row)">明细</el-button>
            <el-button size="small" type="warning" :disabled="!row.pendingCommission" @click="openSettle(row)">结清</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rule.visible" title="抽成规则" width="480px">
      <el-form v-if="rule.form" label-width="120px">
        <el-form-item label="抽成比例">
          <el-input-number v-model="rule.ratePercent" :min="0" :max="100" :step="0.1" :precision="2" />
          <span class="unit">%（如 3 表示 3%）</span>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="rule.form.enabled" /></el-form-item>
        <el-form-item label="计微信"><el-switch v-model="rule.form.includeWechat" /></el-form-item>
        <el-form-item label="计支付宝"><el-switch v-model="rule.form.includeAlipay" /></el-form-item>
        <el-form-item label="计现金">
          <el-switch v-model="rule.form.includeCash" />
          <div class="tip">默认不计现金；真支付上线前可临时打开做联调</div>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="rule.form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rule.visible = false">取消</el-button>
        <el-button type="primary" :loading="rule.loading" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detail.visible" title="未结明细 / 历史结算" size="640px">
      <div v-loading="detail.loading" v-if="detail.data">
        <p>未结合计：<b>¥{{ Number(detail.data.pendingCommission || 0).toFixed(2) }}</b></p>
        <el-divider>未结明细</el-divider>
        <el-table :data="detail.data.pendingEntries || []" size="small" border max-height="280">
          <el-table-column prop="orderNo" label="单号" width="150" />
          <el-table-column prop="payMethod" label="渠道" width="80" />
          <el-table-column prop="orderAmount" label="成交额" width="90" />
          <el-table-column label="抽成" width="90">
            <template #default="{ row }">{{ row.commissionAmount }}</template>
          </el-table-column>
          <el-table-column prop="paidAt" label="支付时间" min-width="150" />
        </el-table>
        <el-divider>历史结算单</el-divider>
        <el-table :data="detail.data.settlements || []" size="small" border>
          <el-table-column prop="settlementNo" label="结算单号" width="170" />
          <el-table-column prop="periodType" label="类型" width="80" />
          <el-table-column prop="commissionSum" label="结清金额" width="100" />
          <el-table-column prop="settledAt" label="结清时间" min-width="150" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="primary" @click="showSnapshot(row)">快照</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <el-dialog v-model="settle.visible" title="结清平台抽成" width="480px">
      <el-form label-width="100px">
        <el-form-item label="商户">{{ settle.merchantName }}</el-form-item>
        <el-form-item label="账期类型">
          <el-select v-model="settle.periodType" style="width: 100%">
            <el-option label="日结" value="DAY" />
            <el-option label="月结" value="MONTH" />
            <el-option label="年结" value="YEAR" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始日">
          <el-date-picker v-model="settle.periodStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="settle.periodType === 'CUSTOM'" label="结束日">
          <el-date-picker v-model="settle.periodEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="收款凭证号"><el-input v-model="settle.voucherNo" placeholder="对公转账流水号等" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="settle.remark" type="textarea" /></el-form-item>
        <div class="tip">将生成结算快照并标记账期内未结明细为已结；历史不可删除。</div>
      </el-form>
      <template #footer>
        <el-button @click="settle.visible = false">取消</el-button>
        <el-button type="warning" :loading="settle.loading" @click="doSettle">确认结清</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="snapshot.visible" title="结算快照" width="640px">
      <pre class="snap">{{ snapshot.text }}</pre>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { commissionApi } from "@/api/commission"

export default {
  name: "CommissionList",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const rule = reactive({ visible: false, loading: false, merchantId: null, form: null, ratePercent: 0 })
    const detail = reactive({ visible: false, loading: false, data: null })
    const settle = reactive({
      visible: false,
      loading: false,
      merchantId: null,
      merchantName: "",
      periodType: "MONTH",
      periodStart: "",
      periodEnd: "",
      voucherNo: "",
      remark: ""
    })
    const snapshot = reactive({ visible: false, text: "" })

    const formatRate = (r) => `${(Number(r || 0) * 100).toFixed(2)}%`

    const load = async () => {
      loading.value = true
      try {
        list.value = (await commissionApi.summary()) || []
      } finally {
        loading.value = false
      }
    }

    const openRule = async (row) => {
      const data = await commissionApi.getRule(row.merchantId)
      rule.merchantId = row.merchantId
      rule.form = { ...data }
      rule.ratePercent = Number(data.rate || 0) * 100
      rule.visible = true
    }

    const saveRule = async () => {
      rule.loading = true
      try {
        await commissionApi.updateRule(rule.merchantId, {
          ...rule.form,
          rate: Number((rule.ratePercent / 100).toFixed(4))
        })
        ElMessage.success("规则已保存")
        rule.visible = false
        load()
      } finally {
        rule.loading = false
      }
    }

    const openDetail = async (row) => {
      detail.visible = true
      detail.loading = true
      try {
        detail.data = await commissionApi.merchantDetail(row.merchantId)
      } finally {
        detail.loading = false
      }
    }

    const openSettle = (row) => {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, "0")
      settle.merchantId = row.merchantId
      settle.merchantName = row.merchantName
      settle.periodType = "CUSTOM"
      settle.periodStart = `${y}-${m}-01`
      settle.periodEnd = `${y}-${m}-${String(now.getDate()).padStart(2, "0")}`
      settle.voucherNo = ""
      settle.remark = ""
      settle.visible = true
    }

    const doSettle = async () => {
      await ElMessageBox.confirm("确认结清该账期未结抽成？将生成不可篡改快照。")
      settle.loading = true
      try {
        const res = await commissionApi.settle(settle.merchantId, {
          periodType: settle.periodType,
          periodStart: settle.periodStart,
          periodEnd: settle.periodEnd,
          voucherNo: settle.voucherNo,
          remark: settle.remark
        })
        ElMessage.success(`已结清 ${res.settlementNo}，金额 ¥${res.commissionSum}`)
        settle.visible = false
        load()
      } finally {
        settle.loading = false
      }
    }

    const showSnapshot = (row) => {
      try {
        snapshot.text = JSON.stringify(JSON.parse(row.snapshotJson || "{}"), null, 2)
      } catch (e) {
        snapshot.text = row.snapshotJson || ""
      }
      snapshot.visible = true
    }

    onMounted(load)
    return {
      loading, list, rule, detail, settle, snapshot,
      load, formatRate, openRule, saveRule, openDetail, openSettle, doSettle, showSnapshot
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; display: flex; align-items: center; gap: 12px; }
.hint { color: #909399; font-size: 13px; }
.tag { margin-right: 4px; }
.unit { margin-left: 8px; color: #909399; }
.tip { color: #909399; font-size: 12px; line-height: 1.4; }
.snap { background: #f5f7fa; padding: 12px; max-height: 480px; overflow: auto; font-size: 12px; }
</style>
