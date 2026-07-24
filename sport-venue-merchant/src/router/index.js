import { createRouter, createWebHistory } from "vue-router"
import Layout from "@/components/Layout.vue"

function getMerchantInfo() {
  return JSON.parse(localStorage.getItem("merchantInfo") || "{}")
}

function isOwner() {
  const info = getMerchantInfo()
  return info.role === "OWNER" || info.userType === "B_MERCHANT"
}

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
      },
      {
        path: "/staff",
        name: "StaffList",
        component: () => import("@/views/staff/StaffList.vue"),
        meta: { title: "员工管理", requiresAuth: true, requiresOwner: true }
      },
      {
        path: "/performance/me",
        name: "MyPerformance",
        component: () => import("@/views/performance/MyPerformance.vue"),
        meta: { title: "我的业绩", requiresAuth: true }
      },
      {
        path: "/performance/staff",
        name: "StaffPerformance",
        component: () => import("@/views/performance/StaffPerformance.vue"),
        meta: { title: "员工业绩", requiresAuth: true, requiresOwner: true }
      },
      {
        path: "/courts",
        name: "CourtList",
        component: () => import("@/views/court/CourtList.vue"),
        meta: { title: "片场管理", requiresAuth: true }
      },
      {
        path: "/teams",
        name: "TeamList",
        component: () => import("@/views/team/TeamList.vue"),
        meta: { title: "球队管理", requiresAuth: true }
      },
      {
        path: "/teams/:id",
        name: "TeamDetail",
        component: () => import("@/views/team/TeamDetail.vue"),
        meta: { title: "球队详情", requiresAuth: true }
      },
      {
        path: "/booking/calendar",
        name: "BookingCalendar",
        component: () => import("@/views/booking/BookingCalendar.vue"),
        meta: { title: "订场日历", requiresAuth: true }
      },
      {
        path: "/booking/list",
        name: "BookingList",
        component: () => import("@/views/booking/BookingList.vue"),
        meta: { title: "订场列表", requiresAuth: true }
      },
      {
        path: "/matches",
        name: "MatchList",
        component: () => import("@/views/match/MatchList.vue"),
        meta: { title: "赛果管理", requiresAuth: true }
      },
      {
        path: "/ranking/teams",
        name: "TeamRanking",
        component: () => import("@/views/match/TeamRanking.vue"),
        meta: { title: "球队排行榜", requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const merchantInfo = getMerchantInfo()
  const loggedIn = !!(merchantInfo.token && merchantInfo.merchantId)

  if (to.meta.title) {
    document.title = to.meta.title + " - 商户管理"
  }

  if (to.meta.requiresAuth && !loggedIn) {
    next("/login")
  } else if (to.path === "/login" && loggedIn) {
    next("/cashier")
  } else if (to.meta.requiresOwner && !isOwner()) {
    next("/cashier")
  } else {
    next()
  }
})

export default router
