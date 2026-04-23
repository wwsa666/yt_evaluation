<template>
  <div class="shop-form-container">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑店铺' : '创建新店铺' }}</h1>
    </div>

    <div class="form-card glass-panel">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="top" size="large">
        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="form.name" placeholder="输入店铺名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="店铺分类" prop="typeId">
          <el-select v-model="form.typeId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="t in shopTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="form.address" placeholder="输入店铺地址" />
        </el-form-item>

        <el-form-item label="店铺图片 URL" prop="image">
          <el-input v-model="form.image" placeholder="输入图片链接（建议 Unsplash 等免费图片链接）" />
          <div v-if="form.image" class="image-preview">
            <el-image :src="form.image" fit="cover" style="width: 200px; height: 140px; border-radius: 8px; margin-top: 12px" />
          </div>
        </el-form-item>

        <el-form-item label="店铺简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简单介绍您的店铺特色" maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '创建店铺' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShopTypes, getShopDetail } from '../api/shop'
import { createShop, updateShop } from '../api/merchant'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const editId = computed(() => route.query.id)
const isEdit = computed(() => !!editId.value)

const formRef = ref(null)
const submitting = ref(false)
const shopTypes = ref([])

const form = reactive({
  name: '',
  typeId: null,
  address: '',
  image: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  image: [{ required: true, message: '请输入图片链接', trigger: 'blur' }],
}

const fetchTypes = async () => {
  try {
    const res = await getShopTypes()
    shopTypes.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const loadShopData = async () => {
  if (!editId.value) return
  try {
    const res = await getShopDetail(editId.value)
    if (res) {
      form.name = res.name
      form.typeId = res.typeId
      form.address = res.address
      form.image = res.image
      form.description = res.description || ''
    }
  } catch (e) {
    ElMessage.error('加载店铺信息失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateShop({ id: editId.value, ...form })
      ElMessage.success('修改成功')
    } else {
      await createShop(form)
      ElMessage.success('创建成功')
    }
    router.push('/merchant/shops')
  } catch (e) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTypes()
  loadShopData()
})
</script>

<style scoped>
.shop-form-container {
  max-width: 700px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header h1 {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-primary);
}

.form-card {
  padding: 40px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  width: 100%;
}
</style>
