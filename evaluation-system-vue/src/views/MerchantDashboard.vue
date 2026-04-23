<template>
  <div class="merchant-dashboard">
    <div class="page-header">
      <h1>商家控制台</h1>
      <p>管理您的店铺，监控经营数据</p>
    </div>

    <!-- 概览统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card glass-panel" v-for="stat in summaryStats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button type="primary" size="large" @click="$router.push('/merchant/shop/create')">
        <el-icon><Plus /></el-icon> 添加新店铺
      </el-button>
      <el-button size="large" @click="$router.push('/merchant/shops')">
        <el-icon><Shop /></el-icon> 管理我的店铺
      </el-button>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section" v-if="myShops.length > 0">
      <!-- 店铺选择器 -->
      <div class="chart-controls glass-panel">
        <span class="control-label">选择店铺：</span>
        <el-select v-model="selectedShopId" placeholder="选择查看统计的店铺" size="large">
          <el-option v-for="shop in myShops" :key="shop.id" :label="shop.name" :value="shop.id" />
        </el-select>
      </div>

      <div class="chart-grid">
        <!-- 评价趋势折线图 -->
        <div class="chart-container glass-panel">
          <h3>📈 近30天评价趋势</h3>
          <div ref="lineChartRef" class="chart-canvas"></div>
          <div v-if="statsData.length === 0" class="chart-empty">
            <el-empty description="暂无统计数据" :image-size="80" />
          </div>
        </div>

        <!-- 各店铺评分对比柱状图 -->
        <div class="chart-container glass-panel">
          <h3>⭐ 各店铺评分对比</h3>
          <div ref="barChartRef" class="chart-canvas"></div>
        </div>
      </div>
    </div>

    <div v-else class="empty-merchant glass-panel">
      <el-empty description="您还没有创建任何店铺">
        <el-button type="primary" @click="$router.push('/merchant/shop/create')">立即创建第一家店</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { Shop, Plus, TrendCharts, ChatDotSquare, Star, Document } from '@element-plus/icons-vue'
import { getMyShops, getShopStats } from '../api/merchant'
import * as echarts from 'echarts'

const myShops = ref([])
const selectedShopId = ref(null)
const statsData = ref([])

let lineChart = null
let barChart = null
const lineChartRef = ref(null)
const barChartRef = ref(null)

const summaryStats = computed(() => {
  const totalShops = myShops.value.length
  const totalReviews = myShops.value.reduce((sum, s) => sum + (s.commentsCount || 0), 0)
  const avgRating = totalShops > 0
    ? (myShops.value.reduce((sum, s) => sum + parseFloat(s.avgRating || 0), 0) / totalShops).toFixed(1)
    : '0.0'
  return [
    { label: '我的店铺', value: totalShops, icon: 'Shop', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
    { label: '总评价数', value: totalReviews, icon: 'ChatDotSquare', gradient: 'linear-gradient(135deg, #f093fb, #f5576c)' },
    { label: '平均评分', value: avgRating, icon: 'Star', gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
    { label: '营业中', value: myShops.value.filter(s => s.status === 1).length, icon: 'Document', gradient: 'linear-gradient(135deg, #43e97b, #38f9d7)' },
  ]
})

const fetchMyShops = async () => {
  try {
    const res = await getMyShops()
    myShops.value = Array.isArray(res) ? res : []
    if (myShops.value.length > 0 && !selectedShopId.value) {
      selectedShopId.value = myShops.value[0].id
    }
  } catch (e) {
    console.error('获取店铺失败', e)
  }
}

const fetchStats = async () => {
  if (!selectedShopId.value) return
  try {
    const res = await getShopStats(selectedShopId.value, 30)
    statsData.value = Array.isArray(res) ? res : []
    await nextTick()
    renderLineChart()
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

const renderLineChart = () => {
  if (!lineChartRef.value) return
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  const dates = statsData.value.map(s => s.statDate)
  const reviews = statsData.value.map(s => s.newReviews)
  const ratings = statsData.value.map(s => parseFloat(s.avgRating))

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增评价', '平均评分'], top: 0 },
    grid: { left: 50, right: 30, bottom: 30, top: 40 },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '评价数', min: 0 },
      { type: 'value', name: '评分', min: 0, max: 5 }
    ],
    series: [
      {
        name: '新增评价', type: 'line', smooth: true, data: reviews,
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79,70,229,0.3)' }, { offset: 1, color: 'rgba(79,70,229,0.02)' }
        ]) },
        lineStyle: { color: '#4f46e5', width: 3 },
        itemStyle: { color: '#4f46e5' }
      },
      {
        name: '平均评分', type: 'line', smooth: true, yAxisIndex: 1, data: ratings,
        lineStyle: { color: '#f59e0b', width: 3, type: 'dashed' },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  })
}

const renderBarChart = () => {
  if (!barChartRef.value || myShops.value.length === 0) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }
  const names = myShops.value.map(s => s.name)
  const ratings = myShops.value.map(s => parseFloat(s.avgRating || 0))
  const counts = myShops.value.map(s => s.commentsCount || 0)

  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['评分', '评价数'], top: 0 },
    grid: { left: 50, right: 50, bottom: 30, top: 40 },
    xAxis: { type: 'category', data: names },
    yAxis: [
      { type: 'value', name: '评分', min: 0, max: 5 },
      { type: 'value', name: '评价数', min: 0 }
    ],
    series: [
      {
        name: '评分', type: 'bar', data: ratings,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        barWidth: 40
      },
      {
        name: '评价数', type: 'bar', yAxisIndex: 1, data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#43e97b' }, { offset: 1, color: '#38f9d7' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        barWidth: 40
      }
    ]
  })
}

watch(selectedShopId, () => {
  fetchStats()
})

const handleResize = () => {
  lineChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  await fetchMyShops()
  await nextTick()
  renderBarChart()
  if (selectedShopId.value) {
    fetchStats()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.merchant-dashboard {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-top: 4px;
}

.quick-actions {
  display: flex;
  gap: 16px;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
}

.control-label {
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 20px;
}

.chart-container {
  padding: 24px;
  position: relative;
}

.chart-container h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.chart-canvas {
  width: 100%;
  height: 320px;
}

.chart-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.empty-merchant {
  padding: 60px;
  text-align: center;
}

@media (max-width: 1024px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .stats-cards { grid-template-columns: 1fr; }
}
</style>
