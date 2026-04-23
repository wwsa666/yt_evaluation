import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/shop/:id',
        name: 'ShopDetail',
        component: () => import('../views/ShopDetail.vue'),
        meta: { title: '商铺详情' }
      },
      {
        path: '/my-profile',
        name: 'MyProfile',
        component: () => import('../views/MyProfile.vue'),
        meta: { title: '我的主页', requiresAuth: true }
      },
      // ======= 商家路由 =======
      {
        path: '/merchant',
        name: 'MerchantDashboard',
        component: () => import('../views/MerchantDashboard.vue'),
        meta: { title: '商家控制台', requiresAuth: true, requiresMerchant: true }
      },
      {
        path: '/merchant/shops',
        name: 'MerchantShops',
        component: () => import('../views/MerchantShops.vue'),
        meta: { title: '我的店铺', requiresAuth: true, requiresMerchant: true }
      },
      {
        path: '/merchant/shop/create',
        name: 'MerchantShopForm',
        component: () => import('../views/MerchantShopForm.vue'),
        meta: { title: '创建店铺', requiresAuth: true, requiresMerchant: true }
      },
      {
        path: '/merchant/shop/:id/reviews',
        name: 'MerchantReviews',
        component: () => import('../views/MerchantReviews.vue'),
        meta: { title: '评价监控', requiresAuth: true, requiresMerchant: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录注册' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：标题设置 & 权限拦截
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 随便点评` : '随便点评系统'
  
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.requiresMerchant && userStore.userInfo?.role !== 1) {
    next('/home')
  } else {
    next()
  }
})

export default router
