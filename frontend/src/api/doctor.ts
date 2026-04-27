import request from './request'

export const getDoctors = () => {
  return request.get('/doctor')
}

export const getDoctorById = (id: number) => {
  return request.get(`/doctor/${id}`)
}

export const getDoctorsByDepartment = (departmentId: number) => {
  return request.get(`/doctor/department/${departmentId}`)
}

export const searchDoctors = (keyword: string) => {
  return request.get('/doctor/search', { params: { keyword } })
}

export const getDoctorDetail = (id: number) => {
  return request.get(`/doctor/detail/${id}`)
}
