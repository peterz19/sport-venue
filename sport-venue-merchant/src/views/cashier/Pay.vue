<template>
  <div class="pay-page" v-loading="loading">
    <el-card v-if="!success">
      <div class="title">请顾客扫码支付</div>
      <div class="amount">¥{{ Number(order.totalAmount || 0).toFixed(2) }}</div>

      <div class="meta">
        <span>订单号：{{ order.orderNo }}</span>
        <span>场馆：{{ order.venueName }}</span>
      </div>

      <div class="items">
        <div v-for="(item, idx) in order.items || []" :key="idx" class="item-row">
          <span>{{ item.productName }} x{{ item.quantity }}</span>
          <span>¥{{ Number(item.subtotal).toFixed(2) }}</span>
        </div>
      </div>

      <div class="qr-box">
        <div class="qr-placeholder">
          <div class="qr-icon">收款码</div>
          <p>{{ order.payment?.tip || "扫码支付即将接入，当前请使用现金支付" }}</p>
        </div>
        <div class="waiting">等待支付中...</div>
      </div>

      <div class="actions">
        <el-button type="warning" size="large" :loading="paying" @click="confirmCash">
          现金支付
        </el-button>
        <el-button size="large" @click="cancelOrder">取消订单</el-button>
      </div>
    </el-card>

    <el-card v-else class="success-card">
      <div class="success-icon">✓</div>
      <div class="title">收款成功</div>
      <div class="amount">¥{{ Number(order.totalAmount || 0).toFixed(2) }}</div>
      <div class="meta">
        <span>{{ payMethodLabel }}</span>
        <span>{{ order.orderNo }}</span>
      </div>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/cashier')">继续收银</el-button>
        <el-button size="large" @click="$router.push('/sales/daily')">查看报表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { computed, onMounted, onUnmounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import { salesApi } from "@/api"

export default {
  name: "CashierPay",
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const paying = ref(false)
    const success = ref(false)
    const order = ref({})
    let timer = null

    const payMethodLabel = computed(() => {
      const map = { CASH: "现金", WECHAT: "微信", ALIPAY: "支付宝" }
      return map[order.value.payMethod] || order.value.payMethod || ""
    })

    const load = async () => {
      loading.value = true
      try {
        order.value = (await salesApi.getOrder(route.params.orderId)) || {}
        if (order.value.status === "PAID") {
          success.value = true
        } else if (order.value.status === "CANCELLED") {
          ElMessage.warning("订单已取消")
          router.replace("/cashier")
        }
      } finally {
        loading.value = false
      }
    }

    const pollStatus = async () => {
      try {
        const data = await salesApi.getOrderStatus(route.params.orderId)
        if (data.status === "PAID") {
          success.value = true
          await load()
          stopPoll()
        }
      } catch (e) {
        // ignore poll errors
      }
    }

    const startPoll = () => {
      stopPoll()
      timer = setInterval(pollStatus, 2000)
    }

    const stopPoll = () => {
      if (timer) {
        clearInterval(timer)
        timer = null
      }
    }

    const confirmCash = async () => {
      try {
        await ElMessageBox.confirm(
          `应收 ¥${Number(order.value.totalAmount || 0).toFixed(2)}，请确认已收到现金`,
          "确认现金收款",
          { confirmButtonText: "现金支付确认", cancelButtonText: "取消", type: "warning" }
        )
        paying.value = true
        order.value = await salesApi.payCash(route.params.orderId, {
          receivedAmount: order.value.totalAmount
        })
        success.value = true
        stopPoll()
        ElMessage.success("现金收款成功")
      } catch (e) {
        // cancel or error
      } finally {
        paying.value = false
      }
    }

    const cancelOrder = async () => {
      try {
        await ElMessageBox.confirm("确定取消该订单吗？", "取消订单", { type: "warning" })
        await salesApi.cancelOrder(route.params.orderId, { reason: "收银台取消" })
        ElMessage.success("订单已取消")
        router.replace("/cashier")
      } catch (e) {
        // cancel
      }
    }

    onMounted(async () => {
      await load()
      if (!success.value) startPoll()
    })

    onUnmounted(stopPoll)

    return {
      loading,
      paying,
      success,
      order,
      payMethodLabel,
      confirmCash,
      cancelOrder
    }
  }
}
</script>

<style scoped>
.pay-page {
  max-width: 640px;
  margin: 0 auto;
}

.title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.amount {
  text-align: center;
  font-size: 40px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 16px;
}

.meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  margin-bottom: 16px;
  font-size: 13px;
}

.items {
  border-top: 1px dashed #e4e7ed;
  border-bottom: 1px dashed #e4e7ed;
  padding: 12px 0;
  margin-bottom: 20px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}

.qr-box {
  text-align: center;
  margin-bottom: 24px;
}

.qr-placeholder {
  width: 220px;
  height: 220px;
  margin: 0 auto 12px;
  border: 2px dashed #c0c4cc;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  color: #909399;
  padding: 16px;
}

.qr-icon {
  width: 88px;
  height: 88px;
  border: 2px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: #606266;
}

.waiting {
  color: #409eff;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.success-card {
  text-align: center;
  padding: 24px 0;
}

.success-icon {
  width: 64px;
  height: 64px;
  line-height: 64px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: #67c23a;
  color: #fff;
  font-size: 32px;
}
</style>
