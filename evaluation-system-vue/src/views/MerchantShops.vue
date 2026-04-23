<template>
  <div class="merchant-shops">
    <div class="page-header">
      <h1>我的店铺</h1>
      <el-button type="primary" @click="$router.push('/merchant/shop/create')">
        <el-icon><Plus /></el-icon> 添加新店铺
      </el-button>
    </div>

    <el-skeleton :rows="5" animated v-if="loading" />

    <template v-else>
      <div v-if="shops.length === 0" class="empty-state glass-panel">
        <el-empty description="还没有店铺，快去创建第一家吧！">
          <el-button type="primary" @click="$router.push('/merchant/shop/create')">创建店铺</el-button>
        </el-empty>
      </div>

      <el-table v-else :data="shops" style="width: 100%" stripe class="shop-table">
        <el-table-column label="店铺名称" prop="name" min-width="160">
          <template #default="{ row }">
            <div class="shop-name-cell">
              <el-avatar :size="40" shape="square" :src="row.image" />
              <div>
                <span class="name-text">{{ row.name }}</span>
                <span class="desc-text">{{ row.description || '暂无简介' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" prop="avgRating" width="100" align="center">
          <template #default="{ row }">
            <span class="rating-badge">⭐ {{ row.avgRating || '0.0' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评价数" prop="commentsCount" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '营业中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="$router.push(`/merchant/shop/${row.id}/reviews`)">查看评价</el-button>
            <el-button link type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="handleToggle(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getMyShops, toggleShopStatus } from '../api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const shops = ref([])
const loading = ref(true)

const fetchShops = async () => {
  loading.value = true
  try {
    const res = await getMyShops()
    shops.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  router.push({ path: '/merchant/shop/create', query: { id: row.id } })
}

const handleToggle = async (row) => {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}「${row.name}」吗？`, '提示', { type: 'warning' })
    await toggleShopStatus(row.id)
    ElMessage.success(`${action}成功`)
    fetchShops()
  } catch (e) {
    // cancelled
  }
}

onMounted(() => fetchShops())
</script>

<style scoped>
.merchant-shops {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h1 {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-primary);
}

.shop-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-text {
  display: block;
  font-weight: 600;
  color: var(--text-primary);
}

.desc-text {
  display: block;
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

.rating-badge {
  font-weight: 700;
  color: #f59e0b;
}

.empty-state {
  padding: 60px;
  text-align: center;
}
</style>
