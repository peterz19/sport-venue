<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>商户登录</h2>
        <p>运动场馆管理系统</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="merchantId">
          <el-input
            v-model="loginForm.merchantId"
            placeholder="请输入商户ID"
            size="large"
            prefix-icon="User"
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
        <p>商户ID: merchant001</p>
        <p>密码: 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { authApi } from "@/api"

export default {
  name: "Login",
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)

    const loginForm = reactive({
      merchantId: "",
      password: ""
    })

    const loginRules = {
      merchantId: [
        { required: true, message: "请输入商户ID", trigger: "blur" }
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
        
        // 模拟登录成功
        const merchantInfo = {
          merchantId: loginForm.merchantId,
          merchantName: `商户${loginForm.merchantId}`,
          token: "mock-token-" + Date.now()
        }
        
        localStorage.setItem("merchantInfo", JSON.stringify(merchantInfo))
        ElMessage.success("登录成功")
        router.push("/")
      } catch (error) {
        console.error("登录失败:", error)
        ElMessage.error("登录失败，请检查商户ID和密码")
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
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
}

.login-tips {
  text-align: center;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style> 