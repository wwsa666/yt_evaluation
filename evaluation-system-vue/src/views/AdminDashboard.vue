<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="admin-sidebar">
      <div class="logo">
        <h2>系统管理</h2>
      </div>
      <el-menu :default-active="activeMenu" class="admin-menu" @select="handleSelectMenu">
        <el-menu-item index="stats">
          <el-icon><DataLine /></el-icon>
          <span>数据大盘</span>
        </el-menu-item>
        <el-menu-item index="student">
          <el-icon><Avatar /></el-icon>
          <span>学生认证审批</span>
        </el-menu-item>
        <el-menu-item index="merchant">
          <el-icon><Shop /></el-icon>
          <span>商家入驻审批</span>
        </el-menu-item>
        <el-menu-item index="shop">
          <el-icon><List /></el-icon>
          <span>全站商铺管理</span>
        </el-menu-item>
        <el-menu-item index="review">
          <el-icon><Comment /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="user">
          <el-icon><User /></el-icon>
          <span>账号封禁管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主容器 -->
    <el-container>
      <el-header class="admin-header">
        <div class="breadcrumb">校园美食雷达后台管理系统</div>
        <div class="user-info">
          <span>管理员：{{ userStore.userInfo?.nickname }}</span>
          <el-button size="small" type="danger" plain @click="handleLogout" style="margin-left: 15px">退出</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <!-- 1. 数据大盘 -->
        <div v-if="activeMenu === 'stats'">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-title">总用户数</div>
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-title">总商铺数</div>
                <div class="stat-value">{{ stats.totalShops || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-title">总评价数</div>
                <div class="stat-value">{{ stats.totalReviews || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-title">全站总浏览量</div>
                <div class="stat-value">{{ stats.totalViews || 0 }}</div>
              </el-card>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
               <el-card shadow="hover">
                  <div class="stat-title" style="color: #ff6b6b">待处理学生认证</div>
                  <div class="stat-value">{{ stats.pendingVerifications || 0 }}</div>
                  <el-button style="margin-top:10px" @click="activeMenu='student'">去处理</el-button>
               </el-card>
            </el-col>
            <el-col :span="12">
               <el-card shadow="hover">
                  <div class="stat-title" style="color: #e6a23c">待处理商家申请</div>
                  <div class="stat-value">{{ stats.pendingMerchants || 0 }}</div>
                  <el-button style="margin-top:10px" @click="activeMenu='merchant'">去处理</el-button>
               </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 2. 学生认证审批 -->
        <div v-if="activeMenu === 'student'">
          <el-card>
            <template #header>学生认证审批</template>
            <el-table :data="studentList" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column label="学生证照片">
                <template #default="{ row }">
                  <el-image 
                    style="width: 100px; height: 60px" 
                    :src="row.verifyImage" 
                    :preview-src-list="[row.verifyImage]" 
                    :preview-teleported="true"
                    fit="cover"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button type="success" size="small" @click="processStudent(row.id, 2)">通过</el-button>
                  <el-button type="danger" size="small" @click="processStudent(row.id, 3)">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 3. 商家入驻审批 -->
        <div v-if="activeMenu === 'merchant'">
          <el-card>
            <template #header>商家入驻审批</template>
            <el-table :data="merchantList" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="nickname" label="昵称" />
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button type="success" size="small" @click="processMerchantApply(row.id, 2)">批准开店</el-button>
                  <el-button type="danger" size="small" @click="processMerchantApply(row.id, 3)">驳回申请</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 4. 全站商铺管理 -->
        <div v-if="activeMenu === 'shop'">
          <el-card>
            <template #header>商铺列表</template>
            <el-table :data="shopList" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="商铺名称" />
              <el-table-column prop="rating" label="综合评分" width="100" />
              <el-table-column prop="viewCount" label="浏览量" width="100" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '营业中' : '已下架' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 5. 评价管理 -->
        <div v-if="activeMenu === 'review'">
          <el-card>
            <template #header>全站评价</template>
            <el-table :data="reviewList" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="发布者" width="120" />
              <el-table-column prop="content" label="评价内容" />
              <el-table-column prop="rating" label="评分" width="80" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="danger" size="small" @click="deleteReviewItem(row.id)">强制删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 6. 账号管理 -->
        <div v-if="activeMenu === 'user'">
          <el-card>
            <template #header>账号管理</template>
            <el-table :data="userList" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column label="角色">
                <template #default="{ row }">
                  <el-tag v-if="row.role===2" type="danger">管理员</el-tag>
                  <el-tag v-else-if="row.role===1" type="warning">商家</el-tag>
                  <el-tag v-else>学生</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '已封禁' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button v-if="row.role !== 2 && row.status === 1" type="danger" size="small" @click="banUser(row.id, 0)">封禁</el-button>
                  <el-button v-if="row.role !== 2 && row.status === 0" type="success" size="small" @click="banUser(row.id, 1)">解封</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataLine, Avatar, Shop, List, Comment, User } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getAdminUserStats, getPendingVerifications, processVerification, getPendingMerchants, processMerchant, getUserList, updateUserStatus } from '../api/user'
import { getAdminShopList, getAdminShopStats } from '../api/shop'
import { getAdminReviewList, adminDeleteReview, getAdminReviewStats } from '../api/review'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('stats')
const loading = ref(false)

const stats = ref({})

const studentList = ref([])
const merchantList = ref([])
const shopList = ref([])
const reviewList = ref([])
const userList = ref([])

const handleSelectMenu = (index) => {
  activeMenu.value = index
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    if (activeMenu.value === 'stats') {
      const uStats = await getAdminUserStats()
      const sStats = await getAdminShopStats()
      const rStats = await getAdminReviewStats()
      stats.value = {
        ...(uStats || {}),
        ...(sStats || {}),
        ...(rStats || {})
      }
    } else if (activeMenu.value === 'student') {
      const res = await getPendingVerifications()
      studentList.value = res || []
    } else if (activeMenu.value === 'merchant') {
      const res = await getPendingMerchants()
      merchantList.value = res || []
    } else if (activeMenu.value === 'shop') {
      const res = await getAdminShopList()
      shopList.value = res.records || []
    } else if (activeMenu.value === 'review') {
      const res = await getAdminReviewList()
      reviewList.value = res.records || []
    } else if (activeMenu.value === 'user') {
      const res = await getUserList()
      userList.value = res.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const processStudent = async (id, status) => {
  try {
    await processVerification(id, status)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const processMerchantApply = async (id, status) => {
  try {
    await processMerchant(id, status)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const deleteReviewItem = async (id) => {
  ElMessageBox.confirm('强制删除该评论？', '警告').then(async () => {
    try {
      await adminDeleteReview(id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {}
  }).catch(() => {})
}

const banUser = async (id, status) => {
  try {
    await updateUserStatus(id, status)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {}
}

const handleLogout = () => {
  userStore.clearToken()
  router.push('/admin/login')
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  if (userStore.userInfo?.role !== 2) {
    ElMessage.error('无权限访问')
    router.push('/')
    return
  }
  loadData()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-sidebar {
  background-color: #304156;
  color: white;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3643;
}
.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}
.admin-menu {
  border-right: none;
  background-color: #304156;
}
.admin-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}
.admin-menu :deep(.el-menu-item.is-active) {
  color: #409EFF;
  background-color: #263445;
}
.admin-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
.stat-card {
  text-align: center;
  padding: 20px 0;
}
.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>
