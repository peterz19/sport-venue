<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h2>场馆管理系统</h2>
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
        <el-sub-menu index="/venue">
          <template #title>
            <el-icon><Location /></el-icon>
            <span>场馆管理</span>
          </template>
          <el-menu-item index="/venue/list">场馆列表</el-menu-item>
          <el-menu-item index="/venue/add">添加场馆</el-menu-item>
        </el-sub-menu>
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
          <span class="welcome-text">欢迎，{{ userInfo.realName || userInfo.username }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar">
                {{ (userInfo.realName || userInfo.username || 'A').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo.realName || userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
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
import { computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessageBox, ElMessage } from "element-plus"
import { DataBoard, Location, ArrowDown, User, SwitchButton } from "@element-plus/icons-vue"
import request from "@/utils/request"

export default {
  name: "Layout",
  components: {
    DataBoard,
    Location,
    ArrowDown,
    User,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    
    const userInfo = computed(() => {
      return JSON.parse(localStorage.getItem("userInfo") || "{}")
    })

    const handleCommand = async (command) => {
      if (command === "logout") {
        try {
          await ElMessageBox.confirm("确定要退出登录吗？", "确认退出", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          })
          
          // 调用登出接口
          try {
            await request({
              url: "/auth/logout",
              method: "post"
            })
          } catch (error) {
            console.error("登出接口调用失败:", error)
          }
          
          // 清除本地存储
          localStorage.removeItem("token")
          localStorage.removeItem("userInfo")
          
          ElMessage.success("已退出登录")
          router.push("/login")
        } catch (error) {
          // 用户取消
        }
      } else if (command === "profile") {
        router.push("/profile")
      }
    }

    return {
      userInfo,
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

.welcome-text {
  color: #606266;
  font-size: 14px;
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

.username {
  margin: 0 8px;
  font-weight: 500;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
