<template>
  <div class="merchant-reviews">
    <div class="page-header">
      <div>
        <h1>{{ shopName }} - 评价监控</h1>
        <p class="subtitle">共 {{ reviews.length }} 条评价</p>
      </div>
      <el-button @click="$router.back()">← 返回</el-button>
    </div>

    <el-skeleton :rows="5" animated v-if="loading" />

    <template v-else>
      <div v-if="reviews.length === 0" class="empty-state glass-panel">
        <el-empty description="该店铺暂无评价" />
      </div>

      <div v-else class="review-list">
        <div v-for="review in reviews" :key="review.id" class="review-item glass-panel">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="40" :src="review.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <div>
                <span class="user-name">{{ review.nickname || review.username || '匿名用户' }}</span>
                <span class="review-time">{{ formatDate(review.createTime) }}</span>
              </div>
            </div>
            <el-rate :model-value="review.rating" disabled size="small" v-if="review.rating" />
            <el-tag v-else size="small" type="info">回复</el-tag>
          </div>
          <p class="review-content">{{ review.content }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getShopDetail } from '../api/shop'
import { getReviewsByShop } from '../api/review'

const route = useRoute()
const shopId = route.params.id

const shopName = ref('加载中...')
const reviews = ref([])
const loading = ref(true)

const fetchData = async () => {
  loading.value = true
  try {
    const shopRes = await getShopDetail(shopId)
    shopName.value = shopRes?.name || '未知店铺'

    const reviewRes = await getReviewsByShop(shopId)
    reviews.value = Array.isArray(reviewRes) ? reviewRes : (reviewRes?.records || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => fetchData())
</script>

<style scoped>
.merchant-reviews {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
}

.subtitle {
  color: var(--text-secondary);
  margin-top: 4px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 24px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  display: block;
  font-weight: 600;
  color: var(--text-primary);
}

.review-time {
  display: block;
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

.review-content {
  color: var(--text-primary);
  line-height: 1.6;
  margin: 0;
}

.empty-state {
  padding: 60px;
  text-align: center;
}
</style>
