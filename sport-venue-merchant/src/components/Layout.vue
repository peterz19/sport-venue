<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h2>商户管理</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        class="menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/cashier">
          <el-icon><ShoppingCart /></el-icon>
          <span>收银台</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/booking/calendar">
          <el-icon><Calendar /></el-icon>
          <span>订场日历</span>
        </el-menu-item>
        <el-menu-item index="/booking/list">
          <el-icon><List /></el-icon>
          <span>订场列表</span>
        </el-menu-item>
        <el-menu-item index="/matches">
          <el-icon><Trophy /></el-icon>
          <span>赛果管理</span>
        </el-menu-item>
        <el-menu-item index="/courts">
          <el-icon><Grid /></el-icon>
          <span>片场管理</span>
        </el-menu-item>
        <el-menu-item index="/teams">
          <el-icon><User /></el-icon>
          <span>球队管理</span>
        </el-menu-item>
        <el-menu-item index="/ranking/teams">
          <el-icon><Medal /></el-icon>
          <span>球队排行榜</span>
        </el-menu-item>
        <el-menu-item index="/sales/daily">
          <el-icon><TrendCharts /></el-icon>
          <span>销售报表</span>
        </el-menu-item>
        <el-menu-item index="/performance/me">
          <el-icon><UserFilled /></el-icon>
          <span>我的业绩</span>
        </el-menu-item>
        <el-menu-item v-if="isOwner" index="/performance/staff">
          <el-icon><DataAnalysis /></el-icon>
          <span>员工业绩</span>
        </el-menu-item>
        <el-menu-item v-if="isOwner" index="/staff">
          <el-icon><Avatar /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/venue/list">
          <el-icon><Location /></el-icon>
          <span>我的场馆</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/cashier' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="merchant-info">
            {{ merchantInfo.merchantName }} · {{ displayName }}
            <el-tag size="small" style="margin-left: 8px">{{ roleLabel }}</el-tag>
          </span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessageBox } from "element-plus"
import {
  DataBoard, Location, User, ShoppingCart, Goods, TrendCharts,
  Avatar, UserFilled, DataAnalysis, Calendar, List, Trophy, Grid, Medal
} from "@element-plus/icons-vue"
import { authApi } from "@/api"

export default {
  name: "Layout",
  components: {
    DataBoard,
    Location,
    User,
    ShoppingCart,
    Goods,
    TrendCharts,
    Avatar,
    UserFilled,
    DataAnalysis,
    Calendar,
    List,
    Trophy,
    Grid,
    Medal
  },
  setup() {
    const router = useRouter()

    const merchantInfo = computed(() => {
      return JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    })

    const isOwner = computed(() => {
      const info = merchantInfo.value
      return info.role === "OWNER" || info.userType === "B_MERCHANT"
    })

    const displayName = computed(() => {
      return merchantInfo.value.realName || merchantInfo.value.username || "-"
    })

    const roleLabel = computed(() => (isOwner.value ? "老板" : "店员"))

    const handleCommand = async (command) => {
      if (command === "logout") {
        try {
          await ElMessageBox.confirm("确定要退出登录吗？", "确认退出", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          })
          try {
            await authApi.logout()
          } catch (e) {
            // ignore
          }
          localStorage.removeItem("merchantInfo")
          router.push("/login")
        } catch (error) {
          // cancel
        }
      }
    }

    return {
      merchantInfo,
      isOwner,
      displayName,
      roleLabel,
      handleCommand
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  color: #bfcbd9;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.menu {
  border: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.merchant-info {
  color: #606266;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  padding: 8px;
  border-radius: 4px;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
