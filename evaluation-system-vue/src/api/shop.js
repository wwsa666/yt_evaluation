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
