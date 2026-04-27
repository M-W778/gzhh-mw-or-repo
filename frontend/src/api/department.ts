import request from './request'

export const getDepartments = () => {
  return request.get('/department')
}

export const getDepartmentById = (id: number) => {
  return request.get(`/department/${id}`)
}

export const searchDepartments = (keyword: string) => {
  return request.get('/department/search', { params: { keyword } })
}
