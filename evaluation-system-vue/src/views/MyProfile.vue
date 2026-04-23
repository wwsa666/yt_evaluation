<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <div class="user-card glass-panel">
      <div class="user-avatar">
        <el-avatar :size="100" :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
      </div>
      <div class="user-details">
        <h2>{{ userInfo?.nickname || userInfo?.username || '未命名用户' }}</h2>
        <p class="join-date">已加入随便点评</p>
        <div class="user-role-actions" style="margin-top: 12px">
          <el-button v-if="userInfo?.role === 1" type="success" plain size="small" @click="$router.push('/merchant')">
            🏪 进入商家中心
          </el-button>
          <el-button v-else type="warning" size="small" :loading="applyingMerchant" @click="handleBecomeMerchant">
            🚀 申请成为商家
          </el-button>
        </div>
      </div>
    </div>

    <!-- 历史评价列表 -->
    <div class="history-reviews">
      <div class="section-title">
        <h2>我的历史评价</h2>
        <span class="count-badge">共 {{ reviews.length }} 条</span>
      </div>

      <el-skeleton :rows="5" animated v-if="loading" />
      
      <template v-else>
        <div v-if="reviews.length === 0" class="empty-state">
          <el-empty description="您还没有发表过评价哦，快去探索好店吧！">
            <el-button type="primary" @click="$router.push('/home')">去发现</el-button>
          </el-empty>
        </div>
        
        <div v-else class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-item glass-panel">
            <div class="shop-info" @click="$router.push(`/shop/${review.shopId}`)">
              <el-icon><Shop /></el-icon>
              <span>{{ review.shopName || '未知商铺' }}</span>
            </div>
            <div class="review-content-box">
              <el-rate :model-value="review.rating" disabled size="small" />
              <p class="review-text">{{ review.content }}</p>
              <div class="review-time">{{ formatDate(review.createTime) }}</div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { getUserReviews } from '../api/review'
import { becomeMerchant } from '../api/merchant'
import { Shop } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userInfo = userStore.userInfo

const reviews = ref([])
const loading = ref(true)
const applyingMerchant = ref(false)

const handleBecomeMerchant = async () => {
  applyingMerchant.value = true
  try {
    await becomeMerchant()
    ElMessage.success('恭喜，您已成功成为商家！')
    // 刷新用户信息
    await userStore.fetchUserInfo()
  } catch (e) {
    // handled by interceptor
  } finally {
    applyingMerchant.value = false
  }
}

const fetchMyReviews = async () => {
  loading.value = true
  try {
    const res = await getUserReviews()
    reviews.value = Array.isArray(res) ? res : (res.records || [])
  } catch (error) {
    console.error('Failed to fetch user reviews')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchMyReviews()
})
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-width: 800px;
  margin: 0 auto;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 40px;
  background: linear-gradient(135deg, var(--surface-color) 0%, #f8fafc 100%);
}

.user-avatar {
  border: 4px solid white;
  border-radius: 50%;
  box-shadow: var(--shadow-md);
}

.user-details h2 {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.join-date {
  color: var(--text-secondary);
  font-size: 1rem;
}

.history-reviews {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.section-title h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.count-badge {
  background: var(--primary-color);
  color: white;
  padding: 2px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 24px;
  transition: transform 0.3s ease;
}

.review-item:hover {
  transform: translateX(8px);
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 16px;
  cursor: pointer;
  width: fit-content;
}

.shop-info:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.review-content-box {
  background: var(--bg-color);
  padding: 16px;
  border-radius: var(--radius-md);
}

.review-text {
  margin: 12px 0;
  color: var(--text-primary);
  line-height: 1.6;
}

.review-time {
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-align: right;
}
</style>
