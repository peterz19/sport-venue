import { createRouter, createWebHistory } from "vue-router"
import Layout from "@/components/Layout.vue"

const routes = [
  {
    path: "/",
    component: Layout,
    redirect: "/venue/list",
    children: [
      {
        path: "/venue/list",
        name: "VenueList",
        component: () => import("@/views/venue/VenueList.vue"),
        meta: { title: "场馆列表" }
      },
      {
        path: "/venue/add",
        name: "VenueAdd",
        component: () => import("@/views/venue/VenueForm.vue"),
        meta: { title: "添加场馆" }
      },
      {
        path: "/venue/edit/:id",
        name: "VenueEdit",
        component: () => import("@/views/venue/VenueForm.vue"),
        meta: { title: "编辑场馆" }
      },
      {
        path: "/venue/detail/:id",
        name: "VenueDetail",
        component: () => import("@/views/venue/VenueDetail.vue"),
        meta: { title: "场馆详情" }
      },
      {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("@/views/Dashboard.vue"),
        meta: { title: "数据看板" }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
