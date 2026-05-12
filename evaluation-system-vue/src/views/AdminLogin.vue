<template>
  <div class="admin-login-container">
    <el-card class="admin-login-card glass-panel">
      <div class="logo-area">
        <el-icon :size="48" color="#ff6b6b"><Platform /></el-icon>
        <h2>校园美食雷达 - 管理后台</h2>
      </div>

      <el-form :model="loginForm" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="管理员账号">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入管理员账号" 
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="管理员密码">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            :prefix-icon="Lock"
            show-password 
          />
        </el-form-item>
        
        <el-button 
          type="primary" 
          native-type="submit" 
          class="submit-btn" 
          :loading="loading"
          color="#ff6b6b"
        >
          登录后台
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Platform } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { login } from '../api/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const token = await login(loginForm)
    if (token) {
      userStore.setToken(token)
      await userStore.fetchUserInfo()
      
      // 检查是否为管理员
      if (userStore.userInfo?.role === 2) {
        ElMessage.success('管理员登录成功')
        router.push('/admin/dashboard')
      } else {
        ElMessage.error('非管理员账号，拒绝访问')
        userStore.clearToken()
      }
    }
  } catch (error) {
    // 请求内部拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.admin-login-card {
  width: 400px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
}

.logo-area {
  text-align: center;
  margin-bottom: 30px;
}

.logo-area h2 {
  margin-top: 15px;
  color: #1e293b;
  font-size: 22px;
}

.submit-btn {
  width: 100%;
  margin-top: 20px;
  height: 44px;
  font-size: 16px;
}
</style>
