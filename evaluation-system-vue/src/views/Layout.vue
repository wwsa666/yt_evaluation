<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="header glass-panel">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="28" color="var(--primary-color)"><Shop /></el-icon>
          <span class="logo-text">随便点评</span>
        </div>
        
        <nav class="nav-links">
          <router-link to="/home" class="nav-item">首页</router-link>
          <router-link v-if="userStore.userInfo?.role === 1" to="/merchant" class="nav-item">商家中心</router-link>
        </nav>

        <div class="user-actions">
          <template v-if="userStore.token">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-profile">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '默认用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">我的主页</el-dropdown-item>
                  <el-dropdown-item command="merchant" v-if="userStore.userInfo?.role === 1">商家中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" plain @click="$router.push('/login')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主体内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <footer class="footer">
      <p>&copy; 2026 随便点评. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { Shop, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.clearToken()
    ElMessage.success('已退出登录')
    router.push('/home')
  } else if (command === 'profile') {
    router.push('/my-profile')
  } else if (command === 'merchant') {
    router.push('/merchant')
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  border-radius: 0;
  border-left: none;
  border-right: none;
  border-top: none;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: var(--text-secondary);
  font-weight: 500;
  position: relative;
  padding: 8px 0;
}

.nav-item.router-link-active {
  color: var(--primary-color);
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--primary-color);
  border-radius: 2px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background-color 0.3s;
}

.user-profile:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 20px;
}

.footer {
  text-align: center;
  padding: 24px;
  color: var(--text-secondary);
  font-size: 0.875rem;
  border-top: 1px solid var(--border-color);
}
</style>
