<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
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

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="merchant-info">{{ merchantInfo.merchantName }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { ref, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessageBox } from "element-plus"
import { DataBoard, Location, User } from "@element-plus/icons-vue"

export default {
  name: "Layout",
  components: {
    DataBoard,
    Location,
    User
  },
  setup() {
    const router = useRouter()
    
    const merchantInfo = computed(() => {
      return JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    })

    const handleCommand = async (command) => {
      if (command === "logout") {
        try {
          await ElMessageBox.confirm("确定要退出登录吗？", "确认退出", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          })
          
          localStorage.removeItem("merchantInfo")
          router.push("/login")
        } catch (error) {
          // 用户取消
        }
      } else if (command === "profile") {
        // 处理个人信息
        console.log("个人信息")
      }
    }

    return {
      merchantInfo,
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
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style> 