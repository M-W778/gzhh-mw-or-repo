import request from './request'

export interface DoctorProfile {
  id: number
  name: string
  title: string
  specialty: string
  introduction: string
  department: {
    id: number
    name: string
  }
}

export interface DoctorAppointment {
  id: number
  appointmentNo: string
  patientName: string
  patientIdCard: string
  patientPhone: string
  appointmentDate: string
  timeSlot: string
  status: string
  remarks: string
  createdAt: string
}

export interface DoctorStatistics {
  total: number
  pending: number
  completed: number
  cancelled: number
  todayCount: number
}

// 获取医生个人信息
export const getDoctorProfile = () => {
  return request.get<DoctorProfile>('/doctor/profile')
}

// 获取医生的所有预约
export const getDoctorAppointments = () => {
  return request.get<DoctorAppointment[]>('/doctor/appointments')
}

// 获取今日预约
export const getTodayAppointments = () => {
  return request.get<DoctorAppointment[]>('/doctor/appointments/today')
}

// 根据状态获取预约
export const getAppointmentsByStatus = (status: string) => {
  return request.get<DoctorAppointment[]>(`/doctor/appointments/status/${status}`)
}

// 获取医生统计
export const getDoctorStatistics = () => {
  return request.get<DoctorStatistics>('/doctor/statistics')
}

// 完成就诊
export const completeAppointment = (id: number) => {
  return request.post(`/doctor/appointments/${id}/complete`)
}
