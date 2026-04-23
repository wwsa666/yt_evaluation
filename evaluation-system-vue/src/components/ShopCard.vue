<template>
  <div class="shop-card" @click="goToDetail">
    <div class="image-wrapper">
      <el-image 
        :src="shop.image || 'https://via.placeholder.com/400x300?text=No+Image'" 
        fit="cover"
        lazy
      >
        <template #error>
          <div class="image-slot">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div class="rating-badge">
        <el-icon color="#fadb14"><StarFilled /></el-icon>
        <span>{{ shop.avgRating || '0.0' }}</span>
      </div>
    </div>
    
    <div class="content">
      <h3 class="title line-clamp-1">{{ shop.name }}</h3>
      <div class="meta">
        <span class="comments">
          <el-icon><ChatLineRound /></el-icon>
          {{ shop.commentsCount || 0 }} 条点评
        </span>
      </div>
      <p class="address line-clamp-1">
        <el-icon><Location /></el-icon>
        {{ shop.address || '暂无地址信息' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Picture, StarFilled, ChatLineRound, Location } from '@element-plus/icons-vue'

const props = defineProps({
  shop: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const goToDetail = () => {
  router.push(`/shop/${props.shop.id}`)
}
</script>

<style scoped>
.shop-card {
  background: var(--surface-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-color);
}

.shop-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.image-wrapper {
  position: relative;
  padding-top: 66.66%; /* 3:2 Aspect Ratio */
  overflow: hidden;
}

.image-wrapper .el-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.shop-card:hover .image-wrapper .el-image {
  transform: scale(1.05);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.rating-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: white;
  padding: 4px 8px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  font-size: 0.875rem;
}

.content {
  padding: 16px;
}

.title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.meta {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.comments {
  display: flex;
  align-items: center;
  gap: 4px;
}

.address {
  font-size: 0.875rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
