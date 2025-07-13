<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>管理员登录</h2>
        <p>运动场馆管理系统</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>测试账号：</p>
        <p>用户名: admin</p>
        <p>密码: 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import request from "@/utils/request"

export default {
  name: "Login",
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)

    const loginForm = reactive({
      username: "",
      password: ""
    })

    const loginRules = {
      username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
        { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" }
      ],
      password: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
      ]
    }

    const handleLogin = async () => {
      try {
        await loginFormRef.value.validate()
        loading.value = true
        
        const response = await request({
          url: "/auth/login",
          method: "post",
          data: {
            username: loginForm.username,
            password: loginForm.password
          }
        })
        
        // 保存token和用户信息
        localStorage.setItem("token", response.token)
        localStorage.setItem("userInfo", JSON.stringify(response.user))
        
        ElMessage.success("登录成功")
        router.push("/")
      } catch (error) {
        console.error("登录失败:", error)
        ElMessage.error(error.message || "登录失败，请检查用户名和密码")
      } finally {
        loading.value = false
      }
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #303133;
  margin-bottom: 10px;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-tips {
  text-align: center;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
}

.login-tips p {
  margin: 5px 0;
}

.login-tips p:first-child {
  font-weight: 500;
  color: #606266;
}
</style> 