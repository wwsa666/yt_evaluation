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
