import request from '../utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function submitVerification(imageUrl) {
  return request({
    url: '/user/verify/submit',
    method: 'post',
    params: { imageUrl }
  })
}

export function getPendingVerifications() {
  return request({
    url: '/user/admin/verify/list',
    method: 'get'
  })
}

export function processVerification(userId, status) {
  return request({
    url: '/user/admin/verify/process',
    method: 'post',
    params: { userId, status }
  })
}

export function becomeMerchant() {
  return request({
    url: '/user/become-merchant',
    method: 'post'
  })
}

export function getPendingMerchants() {
  return request({
    url: '/user/admin/merchant/list',
    method: 'get'
  })
}

export function processMerchant(userId, status) {
  return request({
    url: '/user/admin/merchant/process',
    method: 'post',
    params: { userId, status }
  })
}

export function getUserList(current = 1, size = 10) {
  return request({
    url: '/user/admin/list',
    method: 'get',
    params: { current, size }
  })
}

export function updateUserStatus(userId, status) {
  return request({
    url: '/user/admin/status',
    method: 'post',
    params: { userId, status }
  })
}

export function getAdminUserStats() {
  return request({
    url: '/user/admin/stats',
    method: 'get'
  })
}
