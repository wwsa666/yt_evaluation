<template>
  <div class="login-container">
    <div class="login-box glass-panel">
      <div class="logo-area">
        <el-icon :size="48" color="var(--primary-color)"><Shop /></el-icon>
        <h2>随便点评</h2>
        <p class="subtitle">{{ isLogin ? '欢迎回来，请登录' : '注册新账号，发现好店' }}</p>
      </div>

      <transition name="fade" mode="out-in">
        <!-- 登录表单 -->
        <el-form 
          v-if="isLogin"
          key="login"
          :model="loginForm" 
          :rules="loginRules" 
          ref="loginFormRef"
          size="large"
          class="auth-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登录</el-button>
          <div class="toggle-text">
            还没有账号？ <a href="javascript:void(0)" @click="toggleMode">立即注册</a>
          </div>
        </el-form>

        <!-- 注册表单 -->
        <el-form 
          v-else
          key="register"
          :model="registerForm" 
          :rules="registerRules" 
          ref="registerFormRef"
          size="large"
          class="auth-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次确认密码" 
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input 
              v-model="registerForm.nickname" 
              placeholder="请输入昵称 (可选)" 
              prefix-icon="Edit"
            />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注册</el-button>
          <div class="toggle-text">
            已有账号？ <a href="javascript:void(0)" @click="toggleMode">直接登录</a>
          </div>
        </el-form>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const token = await login(loginForm)
        // 因为后端直接在 data 字段返回了 token 字符串，拦截器里直接 return res.data，所以这里拿到的就是 token 字符串
        userStore.setToken(token)
        ElMessage.success('登录成功')
        router.push('/home')
      } catch (error) {
        // 请求内部拦截器已处理错误提示
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname
        })
        ElMessage.success('注册成功，请登录')
        isLogin.value = true
      } catch (error) {
        // Error handled in interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f6f8fb 0%, #e2e8f0 100%);
  /* 可选添加背景图 */
  /* background-image: url('...'); background-size: cover; */
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  text-align: center;
}

.logo-area {
  margin-bottom: 32px;
}

.logo-area h2 {
  margin-top: 16px;
  font-size: 1.5rem;
  color: var(--text-primary);
  font-weight: 700;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  margin-top: 16px;
  height: 44px;
  font-size: 1rem;
}

.toggle-text {
  margin-top: 24px;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.toggle-text a {
  font-weight: 600;
}
</style>
