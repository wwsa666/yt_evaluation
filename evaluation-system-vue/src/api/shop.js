import request from '../utils/request'

export function getShopTypes() {
  return request({
    url: '/shop/type/list',
    method: 'get'
  })
}

export function getShopList(params) {
  return request({
    url: '/shop/list',
    method: 'get',
    params
  })
}

export function getShopDetail(id) {
  return request({
    url: `/shop/${id}`,
    method: 'get'
  })
}

export function incrementShopViewCount(id) {
  return request({
    url: `/shop/${id}/view`,
    method: 'post'
  })
}

export function getAdminShopList(current = 1, size = 10) {
  return request({
    url: '/shop/admin/list',
    method: 'get',
    params: { current, size }
  })
}

export function getAdminShopStats() {
  return request({
    url: '/shop/admin/stats',
    method: 'get'
  })
}
