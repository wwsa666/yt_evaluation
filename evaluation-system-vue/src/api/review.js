import request from '../utils/request'

export function addReview(data) {
  return request({
    url: '/review/save',
    method: 'post',
    data
  })
}

export function getReviewsByShop(shopId, params) {
  return request({
    url: `/review/shop/${shopId}`,
    method: 'get',
    params
  })
}

export function getUserReviews(params) {
  return request({
    url: '/review/user',
    method: 'get',
    params
  })
}

export function deleteReview(id) {
  return request({
    url: `/review/delete/${id}`,
    method: 'delete'
  })
}

export function likeReview(id) {
  return request({
    url: `/review/like/${id}`,
    method: 'post'
  })
}

export function getAdminReviewList(current = 1, size = 10) {
  return request({
    url: '/review/admin/list',
    method: 'get',
    params: { current, size }
  })
}

export function adminDeleteReview(id) {
  return request({
    url: `/review/admin/delete/${id}`,
    method: 'delete'
  })
}

export function getAdminReviewStats() {
  return request({
    url: '/review/admin/stats',
    method: 'get'
  })
}
