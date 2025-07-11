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
    redirect: "/dashboard",
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const merchantInfo = JSON.parse(localStorage.getItem("merchantInfo") || "{}")
  
  if (to.meta.requiresAuth && !merchantInfo.merchantId) {
    next("/login")
  } else if (to.path === "/login" && merchantInfo.merchantId) {
    next("/")
  } else {
    next()
  }
})

export default router
