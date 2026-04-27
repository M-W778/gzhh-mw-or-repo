import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

interface BackendResponse<T> {
  code: number
  message?: string
  data: T
}

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers = config.headers ?? {}
      ;(config.headers as Record<string, string>).Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data as BackendResponse<unknown>
    if (res.code !== 0) {
      ElMessage.error(res.message || 'Request failed')
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return response
  },
  (error) => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401: {
          ElMessage.error('Login expired, please sign in again')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        }
        case 403:
          ElMessage.error('Permission denied')
          break
        case 404:
          ElMessage.error('Resource not found')
          break
        case 500:
          ElMessage.error('Internal server error')
          break
        default:
          ElMessage.error(response.data?.message || 'Request failed')
      }
    } else {
      ElMessage.error('Network error, please check your connection')
    }
    return Promise.reject(error)
  }
)

const unwrap = <T>(promise: Promise<{ data: BackendResponse<T> }>) =>
  promise.then((res) => res.data.data)

const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.get<BackendResponse<T>>(url, config))
  },
  post<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(service.post<BackendResponse<T>>(url, data, config))
  },
  put<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(service.put<BackendResponse<T>>(url, data, config))
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.delete<BackendResponse<T>>(url, config))
  }
}

export default request
