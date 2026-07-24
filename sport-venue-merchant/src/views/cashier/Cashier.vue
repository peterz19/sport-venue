<template>
  <div class="cashier" v-loading="booting">
    <div class="top-bar">
      <div class="venue-select">
        <span>当前场馆：</span>
        <el-select v-model="venueId" placeholder="请选择场馆" style="width: 220px" @change="onVenueChange">
          <el-option v-for="v in venues" :key="v.id" :label="v.name" :value="v.id" />
        </el-select>
      </div>
      <div class="operator">操作员：{{ operatorName }}</div>
    </div>

    <div class="main-grid">
      <el-card class="product-panel">
        <div class="tabs">
          <el-radio-group v-model="activeCategory" size="small">
            <el-radio-button label="全部" />
            <el-radio-button v-for="c in categories" :key="c" :label="c" />
          </el-radio-group>
        </div>

        <el-empty v-if="!filteredProducts.length" description="暂无上架商品，请先在商品管理中添加" />
        <div v-else class="product-grid">
          <div
            v-for="p in filteredProducts"
            :key="p.id"
            class="product-card"
            @click="addProduct(p)"
          >
            <div class="name">{{ p.name }}</div>
            <div class="price">¥{{ Number(p.price).toFixed(2) }}/{{ p.unit }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="cart-panel">
        <template #header>
          <div class="cart-header">
            <span>购物车</span>
            <el-button link type="danger" @click="clearCart">清空</el-button>
          </div>
        </template>

        <el-empty v-if="!cart.length" description="点击左侧商品加入购物车" :image-size="80" />
        <div v-else class="cart-list">
          <div v-for="item in cart" :key="item.productId" class="cart-item">
            <div class="info">
              <div class="name">{{ item.name }}</div>
              <div class="unit-price">¥{{ Number(item.unitPrice).toFixed(2) }}</div>
            </div>
            <div class="qty">
              <el-button size="small" @click="changeQty(item, -1)">-</el-button>
              <span>{{ item.quantity }}</span>
              <el-button size="small" @click="changeQty(item, 1)">+</el-button>
            </div>
            <div class="subtotal">¥{{ (item.unitPrice * item.quantity).toFixed(2) }}</div>
          </div>
        </div>

        <div class="cart-footer">
          <div class="summary">
            <div>合计件数：{{ totalQty }}</div>
            <div class="amount">应收：¥{{ totalAmount.toFixed(2) }}</div>
          </div>
          <el-button
            type="primary"
            size="large"
            class="confirm-btn"
            :disabled="!cart.length || !venueId"
            :loading="submitting"
            @click="confirmPay"
          >
            确认收款
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { productApi, salesApi } from "@/api"

export default {
  name: "Cashier",
  setup() {
    const router = useRouter()
    const booting = ref(false)
    const submitting = ref(false)
    const venues = ref([])
    const venueId = ref(null)
    const products = ref([])
    const cart = ref([])
    const activeCategory = ref("全部")

    const merchantInfo = JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    const operatorName = merchantInfo.realName || merchantInfo.username || merchantInfo.merchantName || "-"

    const categories = computed(() => {
      const set = new Set()
      products.value.forEach(p => {
        if (p.category) set.add(p.category)
      })
      return Array.from(set)
    })

    const filteredProducts = computed(() => {
      if (activeCategory.value === "全部") return products.value
      return products.value.filter(p => p.category === activeCategory.value)
    })

    const totalQty = computed(() => cart.value.reduce((s, i) => s + i.quantity, 0))
    const totalAmount = computed(() =>
      cart.value.reduce((s, i) => s + Number(i.unitPrice) * i.quantity, 0)
    )

    const loadVenues = async () => {
      venues.value = (await salesApi.myVenues()) || []
      if (venues.value.length >= 1 && !venueId.value) {
        venueId.value = venues.value[0].id
        await loadProducts()
      }
    }

    const loadProducts = async () => {
      if (!venueId.value) {
        products.value = []
        return
      }
      products.value = (await productApi.cashierList(venueId.value)) || []
    }

    const onVenueChange = async () => {
      cart.value = []
      activeCategory.value = "全部"
      await loadProducts()
    }

    const addProduct = (p) => {
      const exist = cart.value.find(i => i.productId === p.id)
      if (exist) {
        exist.quantity += 1
      } else {
        cart.value.push({
          productId: p.id,
          name: p.name,
          unit: p.unit,
          unitPrice: Number(p.price),
          quantity: 1
        })
      }
    }

    const changeQty = (item, delta) => {
      item.quantity += delta
      if (item.quantity <= 0) {
        cart.value = cart.value.filter(i => i.productId !== item.productId)
      }
    }

    const clearCart = () => {
      cart.value = []
    }

    const confirmPay = async () => {
      if (!venueId.value) {
        ElMessage.warning("请选择场馆")
        return
      }
      if (!cart.value.length) {
        ElMessage.warning("请先选择商品")
        return
      }
      submitting.value = true
      try {
        const order = await salesApi.createOrder({
          venueId: venueId.value,
          items: cart.value.map(i => ({
            productId: i.productId,
            quantity: i.quantity
          }))
        })
        cart.value = []
        router.push(`/cashier/pay/${order.orderId}`)
      } finally {
        submitting.value = false
      }
    }

    onMounted(async () => {
      booting.value = true
      try {
        await loadVenues()
      } finally {
        booting.value = false
      }
    })

    return {
      booting,
      submitting,
      venues,
      venueId,
      products,
      cart,
      activeCategory,
      categories,
      filteredProducts,
      totalQty,
      totalAmount,
      operatorName,
      onVenueChange,
      addProduct,
      changeQty,
      clearCart,
      confirmPay
    }
  }
}
</script>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 12px 16px;
  border-radius: 8px;
}

.main-grid {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: 16px;
  min-height: calc(100vh - 180px);
}

.product-panel,
.cart-panel {
  height: 100%;
}

.tabs {
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.product-card {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px 12px;
  cursor: pointer;
  background: #fafafa;
  transition: all 0.15s ease;
}

.product-card:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
}

.product-card .name {
  font-weight: 600;
  margin-bottom: 8px;
}

.product-card .price {
  color: #e6a23c;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-list {
  max-height: calc(100vh - 360px);
  overflow: auto;
}

.cart-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.cart-item .name {
  font-weight: 500;
}

.cart-item .unit-price {
  color: #909399;
  font-size: 12px;
}

.qty {
  display: flex;
  align-items: center;
  gap: 8px;
}

.subtotal {
  width: 70px;
  text-align: right;
  font-weight: 600;
}

.cart-footer {
  margin-top: 16px;
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.summary {
  margin-bottom: 12px;
}

.amount {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
  margin-top: 6px;
}

.confirm-btn {
  width: 100%;
}

@media (max-width: 960px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
}
</style>
