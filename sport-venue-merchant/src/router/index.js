import { createRouter, createWebHistory } from "vue-router"
import Layout from "@/components/Layout.vue"

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/Login.vue"),
    meta: { title: "商户登录" }
  },
  {
    path: "/",
    component: Layout,
    redirect: "/cashier",
    children: [
      {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/Dashboard.vue"),
        meta: { title: "数据看板", requiresAuth: true }
      },
      {
        path: "/venue/list",
        name: "VenueList",
        component: () => import("@/views/venue/VenueList.vue"),
        meta: { title: "我的场馆", requiresAuth: true }
      },
      {
        path: "/venue/detail/:id",
        name: "VenueDetail",
        component: () => import("@/views/venue/VenueDetail.vue"),
        meta: { title: "场馆详情", requiresAuth: true }
      },
      {
        path: "/cashier",
        name: "Cashier",
        component: () => import("@/views/cashier/Cashier.vue"),
        meta: { title: "收银台", requiresAuth: true }
      },
      {
        path: "/cashier/pay/:orderId",
        name: "CashierPay",
        component: () => import("@/views/cashier/Pay.vue"),
        meta: { title: "收款", requiresAuth: true }
      },
      {
        path: "/products",
        name: "ProductList",
        component: () => import("@/views/products/ProductList.vue"),
        meta: { title: "商品管理", requiresAuth: true }
      },
      {
        path: "/sales/daily",
        name: "SalesDaily",
        component: () => import("@/views/sales/DailyReport.vue"),
        meta: { title: "销售报表", requiresAuth: true }
      },
      {
        path: "/sales/orders",
        name: "SalesOrders",
        component: () => import("@/views/sales/OrderList.vue"),
        meta: { title: "订单明细", requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const merchantInfo = JSON.parse(localStorage.getItem("merchantInfo") || "{}")
  const loggedIn = !!(merchantInfo.token && merchantInfo.merchantId)

  if (to.meta.title) {
    document.title = to.meta.title + " - 商户管理"
  }

  if (to.meta.requiresAuth && !loggedIn) {
    next("/login")
  } else if (to.path === "/login" && loggedIn) {
    next("/cashier")
  } else {
    next()
  }
})

export default router
