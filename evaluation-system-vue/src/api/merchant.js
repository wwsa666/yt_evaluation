import request from '../utils/request'

// 创建店铺
export function createShop(data) {
  return request({
    url: '/shop/create',
    method: 'post',
    data
  })
}

// 编辑店铺
export function updateShop(data) {
  return request({
    url: '/shop/update',
    method: 'put',
    data
  })
}

// 获取我的店铺列表
export function getMyShops() {
  return request({
    url: '/shop/my-shops',
    method: 'get'
  })
}

// 获取店铺统计数据
export function getShopStats(shopId, days = 30) {
  return request({
    url: `/shop/stats/${shopId}`,
    method: 'get',
    params: { days }
  })
}

// 切换店铺上下架状态
export function toggleShopStatus(shopId) {
  return request({
    url: `/shop/toggle-status/${shopId}`,
    method: 'put'
  })
}

// 成为商家
export function becomeMerchant() {
  return request({
    url: '/user/become-merchant',
    method: 'post'
  })
}
