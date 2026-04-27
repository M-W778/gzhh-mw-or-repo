import request from './request'

export interface DepartmentStat {
  departmentName: string
  count: number
}

export interface DailyStat {
  date: string
  count: number
}

export interface AppointmentStatistics {
  todayAppointments: number
  todayPending: number
  totalPending: number
  totalCompleted: number
  totalCancelled: number
  topDepartments: DepartmentStat[]
  dailyStats: DailyStat[]
}

export interface TodayStatistics {
  todayAppointments: number
  todayPending: number
}

export const getAppointmentStatistics = () => {
  return request.get<AppointmentStatistics>('/statistic/appointments')
}

export const getTodayStatistics = () => {
  return request.get<TodayStatistics>('/statistic/today')
}
