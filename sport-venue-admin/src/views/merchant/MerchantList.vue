<template>
  <div class="page">
    <el-card class="toolbar">
      <el-button type="primary" @click="openOnboard">商户开户</el-button>
      <el-button @click="load">刷新</el-button>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="merchantCode" label="编码" width="160" />
        <el-table-column prop="name" label="商户名称" min-width="140" />
        <el-table-column prop="merchantType" label="类型" width="100" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === "ACTIVE" ? "停用" : "启用" }}
            </el-button>
            <el-button size="small" @click="goVenues(row)">场馆</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="onboard.visible" title="商户开户" width="620px">
      <el-form :model="onboard.form" label-width="110px">
        <el-divider content-position="left">商户信息</el-divider>
        <el-form-item label="商户名称" required><el-input v-model="onboard.form.name" /></el-form-item>
        <el-form-item label="商户编码"><el-input v-model="onboard.form.merchantCode" placeholder="可空，自动生成" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="onboard.form.merchantType" style="width: 100%">
            <el-option label="企业" value="COMPANY" />
            <el-option label="个人" value="INDIVIDUAL" />
            <el-option label="连锁" value="CHAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="onboard.form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="onboard.form.contactPhone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="onboard.form.address" /></el-form-item>
        <el-divider content-position="left">老板账号</el-divider>
        <el-form-item label="登录账号" required><el-input v-model="onboard.form.ownerUsername" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="onboard.form.ownerPassword" type="password" show-password placeholder="至少6位" /></el-form-item>
        <el-form-item label="老板姓名" required><el-input v-model="onboard.form.ownerRealName" /></el-form-item>
        <el-form-item label="老板手机"><el-input v-model="onboard.form.ownerPhone" /></el-form-item>
        <el-divider content-position="left">可选首场馆</el-divider>
        <el-form-item label="场馆名称"><el-input v-model="onboard.form.firstVenue.name" placeholder="不填则跳过" /></el-form-item>
        <el-form-item label="场馆地址"><el-input v-model="onboard.form.firstVenue.address" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="onboard.form.firstVenue.type" style="width: 100%">
            <el-option label="健身房" value="GYM" />
            <el-option label="体育场" value="STADIUM" />
            <el-option label="公园" value="PARK" />
            <el-option label="机构" value="INSTITUTION" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onboard.visible = false">取消</el-button>
        <el-button type="primary" :loading="onboard.loading" @click="submitOnboard">开户</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="edit.visible" title="编辑商户" width="520px">
      <el-form :model="edit.form" label-width="100px">
        <el-form-item label="商户名称" required><el-input v-model="edit.form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="edit.form.merchantType" style="width: 100%">
            <el-option label="企业" value="COMPANY" />
            <el-option label="个人" value="INDIVIDUAL" />
            <el-option label="连锁" value="CHAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="edit.form.contactName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="edit.form.contactPhone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="edit.form.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="edit.form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="edit.visible = false">取消</el-button>
        <el-button type="primary" :loading="edit.loading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detail.visible" title="商户详情 / SaaS 配置" size="560px">
      <div v-loading="detail.loading" v-if="detail.data">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="商户">{{ detail.data.name }}（{{ detail.data.merchantCode }}）</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.data.status }}</el-descriptions-item>
          <el-descriptions-item label="老板">{{ detail.data.ownerUsername }} / {{ detail.data.ownerRealName }}</el-descriptions-item>
          <el-descriptions-item label="场馆/员工">{{ detail.data.venueCount }} / {{ detail.data.staffCount }}</el-descriptions-item>
          <el-descriptions-item label="小程序">{{ detail.data.wxMiniBound ? detail.data.wxMiniAppId : '未绑定' }}</el-descriptions-item>
          <el-descriptions-item label="公众号">{{ detail.data.wxOaBound ? detail.data.wxOaAppId : '未绑定' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>功能开关</el-divider>
        <el-form v-if="detail.features" label-width="120px" size="small">
          <el-form-item label="收银"><el-switch v-model="detail.features.enableCashier" /></el-form-item>
          <el-form-item label="订场"><el-switch v-model="detail.features.enableBooking" /></el-form-item>
          <el-form-item label="球队赛果"><el-switch v-model="detail.features.enableTeamMatch" /></el-form-item>
          <el-form-item label="C端"><el-switch v-model="detail.features.enableCEnd" /></el-form-item>
          <el-form-item label="充值"><el-switch v-model="detail.features.enableRecharge" /></el-form-item>
          <el-button type="primary" size="small" @click="saveFeatures">保存开关</el-button>
        </el-form>

        <el-divider>绑定小程序</el-divider>
        <el-form :model="detail.wx" label-width="100px" size="small">
          <el-form-item label="AppId"><el-input v-model="detail.wx.appId" /></el-form-item>
          <el-form-item label="AppSecret"><el-input v-model="detail.wx.appSecret" type="password" show-password placeholder="仅写入不回显" /></el-form-item>
          <el-button type="primary" size="small" @click="saveWxMini">保存小程序</el-button>
        </el-form>

        <el-divider>最近审计</el-divider>
        <el-timeline v-if="detail.data.recentAudits && detail.data.recentAudits.length">
          <el-timeline-item
            v-for="a in detail.data.recentAudits.slice(0, 8)"
            :key="a.id"
            :timestamp="a.createTime"
          >
            {{ a.action }} · {{ a.operatorName || '-' }}
          </el-timeline-item>
        </el-timeline>
        <div v-else style="color:#999">暂无</div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { merchantApi } from "@/api/merchant"

const emptyFirstVenue = () => ({
  name: "",
  address: "",
  type: "GYM",
  spaceType: "INDOOR",
  chargeType: "PAID"
})

export default {
  name: "MerchantList",
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const list = ref([])
    const onboard = reactive({
      visible: false,
      loading: false,
      form: {
        name: "",
        merchantCode: "",
        merchantType: "COMPANY",
        contactName: "",
        contactPhone: "",
        address: "",
        ownerUsername: "",
        ownerPassword: "123456",
        ownerRealName: "",
        ownerPhone: "",
        firstVenue: emptyFirstVenue()
      }
    })
    const edit = reactive({ visible: false, loading: false, form: {} })
    const detail = reactive({
      visible: false,
      loading: false,
      data: null,
      features: null,
      wx: { appId: "", appSecret: "" }
    })

    const load = async () => {
      loading.value = true
      try {
        list.value = (await merchantApi.getMerchants()) || []
      } finally {
        loading.value = false
      }
    }

    const openOnboard = () => {
      onboard.form = {
        name: "",
        merchantCode: "",
        merchantType: "COMPANY",
        contactName: "",
        contactPhone: "",
        address: "",
        ownerUsername: "",
        ownerPassword: "123456",
        ownerRealName: "",
        ownerPhone: "",
        firstVenue: emptyFirstVenue()
      }
      onboard.visible = true
    }

    const submitOnboard = async () => {
      if (!onboard.form.name || !onboard.form.ownerUsername || !onboard.form.ownerPassword || !onboard.form.ownerRealName) {
        ElMessage.warning("请填写必填项")
        return
      }
      const payload = { ...onboard.form }
      if (!payload.firstVenue?.name) {
        payload.firstVenue = null
      }
      onboard.loading = true
      try {
        const res = await merchantApi.onboard(payload)
        ElMessage.success(`开户成功：商户#${res.merchantId}，老板 ${res.ownerUsername}`)
        onboard.visible = false
        load()
      } finally {
        onboard.loading = false
      }
    }

    const openEdit = (row) => {
      edit.form = {
        id: row.id,
        name: row.name,
        merchantType: row.merchantType,
        contactName: row.contactName,
        contactPhone: row.contactPhone,
        address: row.address,
        remark: row.remark
      }
      edit.visible = true
    }

    const submitEdit = async () => {
      edit.loading = true
      try {
        await merchantApi.update(edit.form.id, edit.form)
        ElMessage.success("已保存")
        edit.visible = false
        load()
      } finally {
        edit.loading = false
      }
    }

    const toggleStatus = async (row) => {
      const status = row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE"
      await ElMessageBox.confirm(
        status === "INACTIVE"
          ? "停用后该商户下账号将无法登录，确认？"
          : `确认将商户设为 ${status}？`
      )
      await merchantApi.updateStatus(row.id, status)
      ElMessage.success("已更新")
      load()
    }

    const openDetail = async (row) => {
      detail.visible = true
      detail.loading = true
      try {
        detail.data = await merchantApi.overview(row.id)
        detail.features = { ...(detail.data.features || {}) }
        detail.wx = {
          appId: detail.data.wxMiniAppId || "",
          appSecret: ""
        }
      } finally {
        detail.loading = false
      }
    }

    const saveFeatures = async () => {
      await merchantApi.updateFeatures(detail.data.merchantId, detail.features)
      ElMessage.success("功能开关已保存")
      openDetail({ id: detail.data.merchantId })
    }

    const saveWxMini = async () => {
      if (!detail.wx.appId) {
        ElMessage.warning("请填写 AppId")
        return
      }
      await merchantApi.upsertWxChannel(detail.data.merchantId, {
        channelType: "MINI_PROGRAM",
        appId: detail.wx.appId,
        appSecret: detail.wx.appSecret || undefined
      })
      ElMessage.success("小程序已保存")
      detail.wx.appSecret = ""
      openDetail({ id: detail.data.merchantId })
    }

    const goVenues = (row) => {
      router.push({ path: "/venue/list", query: { merchantId: row.id } })
    }

    onMounted(load)
    return {
      loading, list, onboard, edit, detail, load, openOnboard, submitOnboard,
      openEdit, submitEdit, toggleStatus, goVenues, openDetail, saveFeatures, saveWxMini
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
