import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const hasRole = (role: string) => {
    const roles = userInfo.value.roles || []
    return roles.includes(role) || roles.includes(role.replace('ROLE_', ''))
  }

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const userId = computed(() => userInfo.value.id || null)
  const isAdmin = computed(() => hasRole('ROLE_ADMIN'))
  const isDoctor = computed(() => hasRole('ROLE_DOCTOR'))
  const isPatient = computed(() => hasRole('ROLE_PATIENT'))

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: any) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    userId,
    isAdmin,
    isDoctor,
    isPatient,
    setToken,
    setUserInfo,
    logout
  }
})
