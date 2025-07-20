<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
          <el-button type="primary" @click="handleEdit" v-if="!isEditing">
            <el-icon><Edit /></el-icon>
            编辑信息
          </el-button>
          <div v-else>
            <el-button type="success" @click="handleSave" :loading="saving">
              <el-icon><Check /></el-icon>
              保存
            </el-button>
            <el-button @click="handleCancel">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        :disabled="!isEditing"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="头像" prop="avatar">
              <div class="avatar-upload">
                <el-avatar
                  :size="80"
                  :src="form.avatar"
                  @error="handleAvatarError"
                >
                  {{ (form.realName || form.username || 'A').charAt(0) }}
                </el-avatar>
                <div class="avatar-actions" v-if="isEditing">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :on-success="handleAvatarSuccess"
                    action="#"
                    :http-request="uploadAvatar"
                  >
                    <el-button size="small" type="primary">更换头像</el-button>
                  </el-upload>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户ID" prop="id">
              <el-input v-model="form.id" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
                <el-option label="保密" value="secret" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择生日"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>

        <el-divider content-position="left">安全设置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="form.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="form.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账户信息卡片 -->
    <el-card class="account-card">
      <template #header>
        <h3>账户信息</h3>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="注册时间">
          {{ formatDate(form.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">
          {{ formatDate(form.lastLoginTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag :type="form.status === 'active' ? 'success' : 'danger'">
            {{ form.status === 'active' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户角色">
          <el-tag type="primary">{{ form.role || '管理员' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Check, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'Profile',
  components: {
    Edit,
    Check,
    Close
  },
  setup() {
    const formRef = ref()
    const isEditing = ref(false)
    const saving = ref(false)
    
    // 表单数据
    const form = reactive({
      id: '',
      username: '',
      realName: '',
      email: '',
      phone: '',
      gender: '',
      birthday: '',
      bio: '',
      address: '',
      avatar: '',
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
      createTime: '',
      lastLoginTime: '',
      status: 'active',
      role: '管理员'
    })

    // 表单验证规则
    const rules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      newPassword: [
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        {
          validator: (rule, value, callback) => {
            if (form.newPassword && value !== form.newPassword) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 初始化默认用户信息
    const getDefaultUserInfo = () => {
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return {
        id: localUserInfo.id || '1',
        username: localUserInfo.username || 'admin',
        realName: localUserInfo.realName || '管理员',
        email: localUserInfo.email || 'admin@example.com',
        phone: localUserInfo.phone || '13800138000',
        gender: localUserInfo.gender || 'male',
        birthday: localUserInfo.birthday || '',
        bio: localUserInfo.bio || '',
        address: localUserInfo.address || '',
        avatar: localUserInfo.avatar || '',
        createTime: localUserInfo.createTime || new Date().toISOString(),
        lastLoginTime: localUserInfo.lastLoginTime || new Date().toISOString(),
        status: localUserInfo.status || 'active',
        role: localUserInfo.role || '管理员'
      }
    }

    // 加载用户信息
    const loadUserInfo = async () => {
      // 首先从本地存储加载用户信息作为默认值
      const defaultInfo = getDefaultUserInfo()
      Object.assign(form, defaultInfo)

      // 然后尝试从后端API获取最新信息
      try {
        console.log('开始调用后端API获取用户信息')
        const response = await request({
          url: '/users/profile',
          method: 'get'
        })
        
        // 如果后端返回了数据，更新表单和本地存储
        if (response) {
          // 转换后端数据格式为前端格式
          const userData = {
            id: response.id,
            username: response.username,
            realName: response.realName || '',
            email: response.email || '',
            phone: response.phone || '',
            gender: response.gender || 'male',
            birthday: response.birthday || '',
            bio: response.remark || '',
            address: response.address || '',
            avatar: response.avatar || '',
            createTime: response.createTime,
            lastLoginTime: response.lastLoginTime,
            status: response.status?.toLowerCase() || 'active',
            role: response.userType?.description || '管理员'
          }
          
          Object.assign(form, userData)
          localStorage.setItem('userInfo', JSON.stringify(userData))
        }
      } catch (error) {
        // 后端API不可用，使用本地存储的数据
        console.log('后端API不可用，使用本地存储的用户信息')
      }
    }

    // 开始编辑
    const handleEdit = () => {
      isEditing.value = true
      // 清空密码字段
      form.currentPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
    }

    // 保存信息
    const handleSave = async () => {
      try {
        await formRef.value.validate()
        
        saving.value = true
        
        // 构建保存的数据
        const saveData = {
          realName: form.realName,
          email: form.email,
          phone: form.phone,
          gender: form.gender,
          birthday: form.birthday,
          bio: form.bio,
          address: form.address
        }

        // 如果输入了新密码，需要验证当前密码
        if (form.newPassword) {
          if (!form.currentPassword) {
            ElMessage.error('请输入当前密码')
            return
          }
          saveData.currentPassword = form.currentPassword
          saveData.newPassword = form.newPassword
        }

        try {
          // 构建后端API需要的用户数据格式
          const userUpdateData = {
            id: form.id,
            username: form.username,
            realName: saveData.realName,
            email: saveData.email,
            phone: saveData.phone,
            avatar: form.avatar,
            remark: saveData.bio,
            // 其他字段保持不变
            userType: form.userType,
            status: form.status,
            points: form.points,
            memberLevel: form.memberLevel
          }
          
          const response = await request({
            url: '/users/profile',
            method: 'put',
            data: userUpdateData
          })
          
          // 保存成功
          ElMessage.success('保存成功')
          isEditing.value = false
          
          // 更新本地存储和表单数据
          updateLocalData(saveData)
          
          // 重新加载用户信息以确保数据同步
          await loadUserInfo()
          
        } catch (error) {
          // 后端API不可用，保存到本地存储
          console.log('后端API不可用，保存到本地存储')
          ElMessage.success('保存成功（本地存储）')
          isEditing.value = false
          
          // 更新本地存储和表单数据
          updateLocalData(saveData)
        }
      } catch (error) {
        console.error('表单验证失败:', error)
      } finally {
        saving.value = false
      }
    }

    // 更新本地数据
    const updateLocalData = (saveData) => {
      // 更新本地存储
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      Object.assign(userInfo, saveData)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      // 同时更新表单数据，确保页面显示正确
      Object.assign(form, saveData)
      
      // 清空密码字段
      form.currentPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
    }

    // 取消编辑
    const handleCancel = async () => {
      try {
        await ElMessageBox.confirm('确定要取消编辑吗？未保存的更改将丢失。', '确认取消', {
          confirmButtonText: '确定',
          cancelButtonText: '继续编辑',
          type: 'warning'
        })
        
        // 重新加载用户信息
        await loadUserInfo()
        isEditing.value = false
        
        // 清空密码字段
        form.currentPassword = ''
        form.newPassword = ''
        form.confirmPassword = ''
      } catch (error) {
        // 用户选择继续编辑
      }
    }

    // 头像上传前的验证
    const beforeAvatarUpload = (file) => {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        ElMessage.error('头像只能是 JPG 或 PNG 格式!')
      }
      if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    }

    // 头像上传成功
    const handleAvatarSuccess = (response, file) => {
      form.avatar = URL.createObjectURL(file.raw)
    }

    // 自定义上传
    const uploadAvatar = async (options) => {
      try {
        // 这里应该调用实际的上传接口
        // 目前使用本地URL作为演示
        const file = options.file
        form.avatar = URL.createObjectURL(file)
        
        // 更新本地存储
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        userInfo.avatar = form.avatar
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
        ElMessage.success('头像上传成功')
      } catch (error) {
        ElMessage.error('头像上传失败')
      }
    }

    // 头像加载失败
    const handleAvatarError = () => {
      console.log('头像加载失败')
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }

    onMounted(() => {
      loadUserInfo()
    })

    return {
      formRef,
      form,
      rules,
      isEditing,
      saving,
      handleEdit,
      handleSave,
      handleCancel,
      beforeAvatarUpload,
      handleAvatarSuccess,
      uploadAvatar,
      handleAvatarError,
      formatDate
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-actions {
  display: flex;
  gap: 10px;
}

.account-card {
  margin-top: 20px;
}

.account-card :deep(.el-card__header) {
  padding: 15px 20px;
}

.account-card h3 {
  margin: 0;
  color: #303133;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style> 