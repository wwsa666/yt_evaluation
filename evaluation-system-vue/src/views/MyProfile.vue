<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <div class="user-card glass-panel">
      <div class="user-avatar">
        <el-avatar :size="100" :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
      </div>
      <div class="user-details">
        <h2>{{ userInfo?.nickname || userInfo?.username || '未命名用户' }}</h2>
        <p class="join-date">已加入校园美食雷达</p>
        <div class="user-role-actions" style="margin-top: 12px">
          <el-tag v-if="userInfo?.verifyStatus === 2" type="success" effect="dark" style="margin-right: 10px;">已认证学生</el-tag>
          <el-tag v-else-if="userInfo?.verifyStatus === 1" type="warning" effect="dark" style="margin-right: 10px;">认证审核中</el-tag>
          <template v-else>
            <el-tag type="info" effect="dark" style="margin-right: 10px;">未认证</el-tag>
            <el-button type="primary" size="small" plain @click="verifyDialogVisible = true" style="margin-right: 10px;">
              🎓 申请学生认证
            </el-button>
          </template>

          <el-button v-if="userInfo?.role === 1" type="success" plain size="small" @click="$router.push('/merchant')">
            🏪 进入商家中心
          </el-button>
          <el-button v-else-if="userInfo?.merchantStatus === 1" type="warning" size="small" disabled>
            🚀 商家申请审核中
          </el-button>
          <el-button v-else-if="userInfo?.merchantStatus === 3" type="danger" size="small" :loading="applyingMerchant" @click="handleBecomeMerchant">
            🚀 申请被驳回，重新申请商家
          </el-button>
          <el-button v-else type="warning" size="small" :loading="applyingMerchant" @click="handleBecomeMerchant">
            🚀 申请成为商家
          </el-button>
        </div>
      </div>
    </div>

    <!-- 学生入驻审核弹框 -->
    <el-dialog v-model="verifyDialogVisible" title="🎓 学生专属认证" width="450px" destroy-on-close>
      <p style="color: #64748b; margin-bottom: 20px;">认证为本校学生后，方可发布多维度真实评价，共建校园美食雷达！</p>
      
      <el-alert v-if="userInfo?.verifyStatus === 3" title="您的上一次认证申请被驳回，请重新上传清晰的学生证照片。" type="error" show-icon style="margin-bottom: 20px;" />

      <el-form label-position="top">
        <el-form-item label="上传真实有效学生证照片">
          <el-upload
            class="student-card-uploader"
            action="http://localhost:8080/api/user/upload"
            :headers="{ Authorization: userStore.token }"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="verifyImageUrl" :src="verifyImageUrl" class="uploaded-img" />
            <div v-else class="uploader-placeholder">
              <el-icon class="uploader-icon"><Plus /></el-icon>
              <div>点击选择图片</div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submittingVerify" @click="handleSubmitVerify" color="#ff6b6b">提交认证申请</el-button>
        </span>
      </template>
    </el-dialog>

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
import { becomeMerchant } from '../api/user'
import { submitVerification } from '../api/user'
import { Shop, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userInfo = userStore.userInfo

const reviews = ref([])
const loading = ref(true)
const applyingMerchant = ref(false)

const verifyImageUrl = ref('')
const submittingVerify = ref(false)
const verifyDialogVisible = ref(false)

const handleUploadSuccess = (response) => {
  if (response.success) {
    verifyImageUrl.value = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.errorMsg || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请检查网络连接')
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  return isImage
}

const handleSubmitVerify = async () => {
  if (!verifyImageUrl.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  submittingVerify.value = true
  try {
    await submitVerification(verifyImageUrl.value)
    ElMessage.success('提交成功，请等待管理员审核')
    verifyDialogVisible.value = false
    await userStore.fetchUserInfo()
  } catch (error) {
    // 拦截器已处理错误提示
  } finally {
    submittingVerify.value = false
  }
}

const handleBecomeMerchant = async () => {
  applyingMerchant.value = true
  try {
    const res = await becomeMerchant()
    if(res.success) {
      ElMessage.success('申请提交成功，请等待审核！')
      await userStore.fetchUserInfo()
    } else {
      ElMessage.error(res.errorMsg || '申请失败')
    }
  } catch (e) {
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

.verify-card {
  padding: 40px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
  border: 1px solid #ffe4e6;
}

.student-card-uploader {
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--transition-fast);
  width: 250px;
  height: 150px;
  background-color: #fafafa;
}

.student-card-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.student-card-uploader:hover {
  border-color: var(--primary-color);
}

.uploader-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #8c939d;
}

.uploader-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.uploaded-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
