import { createRouter, createWebHistory } from "vue-router"
import Layout from "@/components/Layout.vue"

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
    meta: { title: "管理员登录" }
  },
  {
    path: "/",
    component: Layout,
    redirect: "/dashboard",
    children: [
      {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("@/views/Dashboard.vue"),
        meta: { title: "数据看板", requiresAuth: true }
      },
      {
        path: "/venue/list",
        name: "VenueList",
        component: () => import("@/views/venue/VenueList.vue"),
        meta: { title: "场馆列表", requiresAuth: true }
      },
      {
        path: "/venue/add",
        name: "VenueAdd",
        component: () => import("@/views/venue/VenueForm.vue"),
        meta: { title: "添加场馆", requiresAuth: true }
      },
      {
        path: "/venue/edit/:id",
        name: "VenueEdit",
        component: () => import("@/views/venue/VenueForm.vue"),
        meta: { title: "编辑场馆", requiresAuth: true }
      },
      {
        path: "/venue/detail/:id",
        name: "VenueDetail",
        component: () => import("@/views/venue/VenueDetail.vue"),
        meta: { title: "场馆详情", requiresAuth: true }
      },
      {
        path: "/profile",
        name: "Profile",
        component: () => import("@/views/Profile.vue"),
        meta: { title: "个人信息", requiresAuth: true }
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
  const token = localStorage.getItem("token")
  const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}")
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + " - 运动场馆管理系统"
  }
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next("/login")
  } else if (to.path === "/login" && token) {
    // 已登录但访问登录页，跳转到首页
    next("/")
  } else {
    next()
  }
})

export default router
