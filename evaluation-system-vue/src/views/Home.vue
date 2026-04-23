<template>
  <div class="home-container">
    <!-- Hero Section -->
    <div class="hero-section glass-panel">
      <h1>发现城市里的<span class="highlight">好味道</span>与<span class="highlight">好时光</span></h1>
      <p>真实的用户点评，帮您找到最值得去的每一个地方</p>
      
      <!-- 搜索框暂不实现复杂功能，仅作占位或简单展示 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索商铺名称..."
          size="large"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <el-skeleton :rows="1" animated v-if="loadingTypes" />
      <el-tabs v-else v-model="activeTypeId" class="custom-tabs">
        <el-tab-pane label="全部" name="0" />
        <el-tab-pane 
          v-for="type in shopTypes" 
          :key="type.id" 
          :label="type.name" 
          :name="String(type.id)" 
        />
      </el-tabs>
    </div>

    <!-- 商铺列表 -->
    <div class="shop-list-section">
      <div class="section-header">
        <h2>{{ activeTypeName }}精选</h2>
      </div>

      <el-skeleton :rows="5" animated v-if="loadingShops" />
      
      <template v-else>
        <div v-if="shops.length === 0" class="empty-state">
          <el-empty description="暂无相关商铺数据" />
        </div>
        
        <div v-else class="shop-grid">
          <ShopCard 
            v-for="shop in shops" 
            :key="shop.id" 
            :shop="shop" 
          />
        </div>
        
        <!-- 分页组件 -->
        <div class="pagination-container" v-if="totalShops > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[9, 15, 21]"
            layout="prev, pager, next, jumper, sizes"
            :total="totalShops"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ShopCard from '../components/ShopCard.vue'
import { getShopTypes, getShopList } from '../api/shop'

const searchQuery = ref('')
const activeTypeId = ref('0')

const shopTypes = ref([])
const shops = ref([])
const loadingTypes = ref(false)
const loadingShops = ref(false)

const currentPage = ref(1)
const pageSize = ref(9)
const totalShops = ref(0)

const activeTypeName = computed(() => {
  if (activeTypeId.value === '0') return '全部'
  const type = shopTypes.value.find(t => String(t.id) === activeTypeId.value)
  return type ? type.name : '精选'
})

const fetchTypes = async () => {
  loadingTypes.value = true
  try {
    const res = await getShopTypes()
    shopTypes.value = res || []
  } catch (error) {
    console.error('Failed to fetch types')
  } finally {
    loadingTypes.value = false
  }
}

const fetchShops = async () => {
  loadingShops.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (activeTypeId.value !== '0') {
      params.typeId = activeTypeId.value
    }
    if (searchQuery.value) {
      params.name = searchQuery.value
    }
    const res = await getShopList(params)
    console.log('fetchShops 响应:', JSON.stringify(res))
    // res 可能是 { records: [], total: 0 } 或直接是数组
    if (res && res.records) {
      shops.value = res.records
      totalShops.value = res.total || 0
    } else {
      shops.value = Array.isArray(res) ? res : []
      totalShops.value = shops.value.length
    }
  } catch (error) {
    console.error('Failed to fetch shops')
  } finally {
    loadingShops.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchShops()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchShops()
}

// 使用 watch 监听分类切换，保证拿到的是最新值
watch(activeTypeId, () => {
  currentPage.value = 1
  fetchShops()
})

const handleSearch = () => {
  currentPage.value = 1
  fetchShops() 
}

onMounted(() => {
  fetchTypes()
  fetchShops()
})
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero-section {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%);
}

.hero-section h1 {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.highlight {
  color: var(--primary-color);
}

.hero-section p {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.search-input :deep(.el-input-group__append) {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  transition: background-color 0.3s;
}

.search-input :deep(.el-input-group__append):hover {
  background-color: var(--primary-hover);
}

.category-nav {
  padding: 0 10px;
}

/* Custom Element UI Tabs for a premium look */
.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--border-color);
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 1.125rem;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  color: var(--text-secondary);
  transition: all 0.3s;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  font-weight: 600;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
  background-color: var(--primary-color);
}

.section-header {
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.empty-state {
  padding: 60px 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px 0;
}
</style>
