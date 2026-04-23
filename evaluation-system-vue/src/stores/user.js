import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  
  let initialUserInfo = null
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored && stored !== 'undefined') {
      initialUserInfo = JSON.parse(stored)
    }
  } catch (e) {
    console.error('Failed to parse userInfo:', e)
  }
  const userInfo = ref(initialUserInfo)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    if (info) {
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      userInfo.value = null
      localStorage.removeItem('userInfo')
    }
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const { getUserInfo } = await import('../api/user')
      const res = await getUserInfo()
      setUserInfo(res)
    } catch (error) {
      console.error('Failed to fetch user info', error)
    }
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    clearToken,
    fetchUserInfo
  }
})
