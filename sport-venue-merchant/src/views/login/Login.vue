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
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入商户账号"
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
        <p>请使用已绑定商户的 B 端账号登录</p>
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
      username: "",
      password: ""
    })

    const loginRules = {
      username: [{ required: true, message: "请输入商户账号", trigger: "blur" }],
      password: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
      ]
    }

    const handleLogin = async () => {
      try {
        await loginFormRef.value.validate()
        loading.value = true
        const data = await authApi.login({
          username: loginForm.username,
          password: loginForm.password
        })

        const merchantInfo = {
          token: data.token,
          merchantId: data.merchantId,
          merchantName: data.merchantName || `商户${data.merchantId}`,
          userId: data.userId,
          username: data.username,
          userType: data.userType,
          role: data.role || (data.userType === "B_MERCHANT" ? "OWNER" : "STAFF"),
          realName: data.realName || data.username
        }
        localStorage.setItem("merchantInfo", JSON.stringify(merchantInfo))
        ElMessage.success("登录成功")
        router.push("/cashier")
      } catch (error) {
        console.error("登录失败:", error)
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
  background: linear-gradient(135deg, #1f6f5b 0%, #163a5f 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-header h2 {
  margin: 0 0 8px;
  color: #1f2d3d;
}

.login-header p {
  margin: 0;
  color: #909399;
}

.login-button {
  width: 100%;
}

.login-tips {
  margin-top: 12px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
