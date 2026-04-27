import request from './request'

export const getAppointments = () => {
  return request.get('/appointment')
}

export const getAppointmentById = (id: number) => {
  return request.get(`/appointment/${id}`)
}

export const createAppointment = (data: {
  doctorId: number
  scheduleId: number
  patientId: number
  remarks?: string
}) => {
  return request.post('/appointment', data)
}

export const cancelAppointment = (id: number, reason: string) => {
  return request.post(`/appointment/${id}/cancel`, { reason })
}

export const canCancelAppointment = (id: number) => {
  return request.get(`/appointment/${id}/can-cancel`)
}

export const sendAppointmentNotification = (id: number) => {
  return request.post(`/appointment/${id}/notify`)
}
