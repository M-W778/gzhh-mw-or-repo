import request from './request'

export const login = (data: { username: string; password: string }) => {
  return request.post('/user/login', data)
}

export const register = (data: { username: string; password: string; checkPassword: string; phone: string; email: string }) => {
  return request.post('/user/register', data)
}

export const checkUsername = (username: string) => {
  return request.get('/user/has-username', { params: { username } })
}
