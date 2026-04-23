<template>
  <div class="shop-detail-container">
    <el-skeleton :rows="10" animated v-if="loadingShop" />
    
    <template v-else-if="shop">
      <!-- 商铺信息头部 -->
      <div class="shop-header glass-panel">
        <div class="shop-image">
          <el-image 
            :src="shop.image || 'https://via.placeholder.com/600x400?text=No+Image'" 
            fit="cover" 
          />
        </div>
        <div class="shop-info">
          <h1 class="shop-name">{{ shop.name }}</h1>
          
          <div class="rating-box">
            <div class="score">{{ shop.avgRating || '0.0' }}</div>
            <div class="stars-and-count">
              <el-rate 
                :model-value="Number(shop.avgRating || 0)" 
                disabled 
                show-score 
                text-color="#ff9900" 
                score-template=""
              />
              <span class="count">{{ shop.commentsCount || 0 }} 条评价</span>
            </div>
          </div>
          
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ shop.address || '暂无地址信息' }}</span>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="reviews-section">
        <div class="section-title">
          <h2>用户评价</h2>
          <el-button type="primary" @click="showReviewForm = true" v-if="userStore.token">
            <el-icon><EditPen /></el-icon> 写点评
          </el-button>
          <el-button type="primary" plain @click="$router.push('/login')" v-else>
            登录后评价
          </el-button>
        </div>

        <!-- 写点评表单 -->
        <transition name="el-zoom-in-top">
          <div v-if="showReviewForm" class="review-form glass-panel">
            <h3>发表您的评价</h3>
            <el-rate v-model="newReview.rating" size="large" />
            <el-input
              v-model="newReview.content"
              type="textarea"
              :rows="4"
              placeholder="分享您的体验，帮助更多人..."
              class="review-textarea"
            />
            <div class="form-actions">
              <el-button @click="showReviewForm = false">取消</el-button>
              <el-button type="primary" :loading="submitting" @click="submitReview">发布</el-button>
            </div>
          </div>
        </transition>

        <!-- 评论列表 -->
        <el-skeleton :rows="5" animated v-if="loadingReviews" />
        <template v-else>
          <div v-if="mainReviews.length === 0" class="empty-reviews">
            <el-empty description="暂无评价，快来抢沙发吧！" />
          </div>
          <div v-else class="review-list">
            <div v-for="review in mainReviews" :key="review.id" class="review-item-container">
              <!-- 主评论 -->
              <div class="review-item main-review">
                <div class="user-avatar">
                  <el-avatar :src="review.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                </div>
                <div class="review-content-box">
                  <div class="review-meta">
                    <span class="reviewer-name">{{ review.nickname || review.username || '未命名用户' }}</span>
                    <span class="review-time">{{ formatDate(review.createTime) }}</span>
                    <div class="review-actions" v-if="userStore.token">
                      <el-button link type="primary" size="small" @click="openReplyForm(review.id)">回复</el-button>
                      <el-button link type="danger" size="small" v-if="review.userId === userStore.userInfo?.id" @click="handleDelete(review.id)">删除</el-button>
                    </div>
                  </div>
                  <el-rate :model-value="review.rating" disabled size="small" />
                  <p class="review-text">{{ review.content }}</p>
                </div>
              </div>

              <!-- 回复表单 (针对该主评论) -->
              <transition name="el-zoom-in-top">
                <div v-if="replyingTo === review.id" class="reply-form">
                  <el-input
                    v-model="replyContent"
                    type="textarea"
                    :rows="2"
                    placeholder="写下您的回复..."
                    class="reply-textarea"
                  />
                  <div class="form-actions">
                    <el-button size="small" @click="replyingTo = null">取消</el-button>
                    <el-button type="primary" size="small" :loading="submitting" @click="submitReply(review.id)">回复</el-button>
                  </div>
                </div>
              </transition>

              <!-- 子评论 (回复) -->
              <div v-if="review.replies && review.replies.length > 0" class="replies-list">
                <div v-for="reply in review.replies" :key="reply.id" class="review-item reply-item">
                  <div class="user-avatar">
                    <el-avatar :size="32" :src="reply.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  </div>
                  <div class="review-content-box">
                    <div class="review-meta">
                      <span class="reviewer-name">{{ reply.nickname || reply.username || '未命名用户' }}</span>
                      <span class="review-time">{{ formatDate(reply.createTime) }}</span>
                      <div class="review-actions" v-if="userStore.token && reply.userId === userStore.userInfo?.id">
                        <el-button link type="danger" size="small" @click="handleDelete(reply.id)">删除</el-button>
                      </div>
                    </div>
                    <p class="review-text">{{ reply.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getShopDetail } from '../api/shop'
import { getReviewsByShop, addReview, deleteReview } from '../api/review'
import { Location, EditPen } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const shopId = route.params.id
const shop = ref(null)
const loadingShop = ref(true)

const rawReviews = ref([])
const loadingReviews = ref(true)

// 将扁平评价转换为树形结构（主评论和它们的回复）
const mainReviews = computed(() => {
  const parents = rawReviews.value.filter(r => !r.parentId)
  return parents.map(parent => {
    return {
      ...parent,
      replies: rawReviews.value.filter(child => child.parentId === parent.id).sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
    }
  })
})

const showReviewForm = ref(false)
const submitting = ref(false)
const newReview = ref({
  rating: 0,
  content: ''
})

const replyingTo = ref(null)
const replyContent = ref('')

const fetchShopDetail = async () => {
  loadingShop.value = true
  try {
    const res = await getShopDetail(shopId)
    shop.value = res
  } catch (error) {
    ElMessage.error('获取商铺信息失败')
  } finally {
    loadingShop.value = false
  }
}

const fetchReviews = async () => {
  loadingReviews.value = true
  try {
    const res = await getReviewsByShop(shopId)
    rawReviews.value = Array.isArray(res) ? res : (res.records || [])
  } catch (error) {
    console.error('Failed to fetch reviews', error)
  } finally {
    loadingReviews.value = false
  }
}

const submitReview = async () => {
  if (newReview.value.rating === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!newReview.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  submitting.value = true
  try {
    await addReview({
      shopId: shopId,
      rating: newReview.value.rating,
      content: newReview.value.content,
      parentId: null
    })
    ElMessage.success('评价发布成功！')
    showReviewForm.value = false
    newReview.value = { rating: 0, content: '' }
    setTimeout(() => {
      fetchReviews()
      fetchShopDetail()
    }, 500)
  } catch (error) {
    // 错误在拦截器处理
  } finally {
    submitting.value = false
  }
}

const openReplyForm = (parentId) => {
  replyingTo.value = parentId
  replyContent.value = ''
}

const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await addReview({
      shopId: shopId,
      rating: null,
      content: replyContent.value,
      parentId: parentId
    })
    ElMessage.success('回复发布成功！')
    replyingTo.value = null
    replyContent.value = ''
    fetchReviews()
  } catch (error) {
    //
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (reviewId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })
    await deleteReview(reviewId)
    ElMessage.success('删除成功')
    setTimeout(() => {
      fetchReviews()
      fetchShopDetail()
    }, 500)
  } catch (error) {
    // cancelled or failed
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchShopDetail()
  fetchReviews()
})
</script>

<style scoped>
.shop-detail-container {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.shop-header {
  display: flex;
  gap: 40px;
  padding: 32px;
  background: var(--surface-color);
}

.shop-image {
  flex: 0 0 40%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.shop-image .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.shop-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.shop-name {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  background: var(--bg-color);
  padding: 16px 24px;
  border-radius: var(--radius-lg);
}

.score {
  font-size: 3rem;
  font-weight: 700;
  color: #ff9900;
  line-height: 1;
}

.stars-and-count {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.count {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.125rem;
  color: var(--text-secondary);
}

.reviews-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.section-title h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.review-form {
  padding: 24px;
  margin-bottom: 24px;
}

.review-form h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
}

.review-textarea {
  margin: 16px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.review-item-container {
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid var(--border-color);
  padding: 24px 0;
}

.review-item {
  display: flex;
  gap: 16px;
}

.main-review {
  /* margin-bottom: 12px; */
}

.review-content-box {
  flex: 1;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.reviewer-name {
  font-weight: 600;
  color: var(--text-primary);
}

.review-time {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.review-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.review-text {
  margin-top: 12px;
  color: var(--text-primary);
  line-height: 1.6;
}

.reply-form {
  margin-left: 48px;
  margin-top: 12px;
  background: var(--bg-color);
  padding: 16px;
  border-radius: var(--radius-md);
}

.reply-textarea {
  margin-bottom: 12px;
}

.replies-list {
  margin-top: 16px;
  margin-left: 48px;
  background: rgba(0, 0, 0, 0.02);
  padding: 16px;
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--border-color);
}
.reply-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

@media (max-width: 768px) {
  .shop-header {
    flex-direction: column;
    padding: 16px;
  }
  .shop-image {
    flex: none;
    height: 250px;
  }
}
</style>
