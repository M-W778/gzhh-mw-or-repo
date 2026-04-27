import request from './request'

interface BackendSchedule {
  id: number
  doctorId: number
  workDate: string
  workPeriod: 'MORNING' | 'AFTERNOON'
  workPeriodText?: string
  totalNum: number
  remainingNum: number
  status: string
  statusText?: string
}

export interface ScheduleItem {
  id: number
  doctorId: number
  scheduleDate: string
  timeSlot: 'MORNING' | 'AFTERNOON'
  timeSlotText: string
  totalQuota: number
  remainingQuota: number
  status: string
  statusText: string
}

const getWorkPeriodText = (workPeriod: 'MORNING' | 'AFTERNOON') => {
  return workPeriod === 'MORNING' ? '上午' : '下午'
}

const mapScheduleItem = (item: BackendSchedule): ScheduleItem => ({
  id: item.id,
  doctorId: item.doctorId,
  scheduleDate: item.workDate,
  timeSlot: item.workPeriod,
  timeSlotText: item.workPeriodText || getWorkPeriodText(item.workPeriod),
  totalQuota: item.totalNum,
  remainingQuota: item.remainingNum,
  status: item.status,
  statusText: item.statusText || item.status
})

const groupByDate = (items: ScheduleItem[]) => {
  return items.reduce<Record<string, ScheduleItem[]>>((acc, item) => {
    if (!acc[item.scheduleDate]) {
      acc[item.scheduleDate] = []
    }
    acc[item.scheduleDate].push(item)
    return acc
  }, {})
}

export const getSchedulesByDoctor = async (doctorId: number) => {
  const res = await request.get<BackendSchedule[]>(`/schedule/${doctorId}`)
  return groupByDate(res.map(mapScheduleItem))
}

export const getSchedulesByDoctorFlat = async (doctorId: number) => {
  const res = await request.get<BackendSchedule[]>(`/schedule/future/${doctorId}`)
  return res.map(mapScheduleItem)
}

export const getFutureSchedulesByDoctor = (doctorId: number) => {
  return getSchedulesByDoctorFlat(doctorId)
}

export const getScheduleById = async (id: number) => {
  const res = await request.get<BackendSchedule>(`/schedule/detail/${id}`)
  return mapScheduleItem(res)
}

// 管理员排班管理接口
export const createSchedule = (data: {
  doctorId: number
  scheduleDate: string
  timeSlot: string
  totalQuota: number
}) => {
  return request.post('/admin/schedule', {
    doctorId: data.doctorId,
    workDate: data.scheduleDate,
    workPeriod: data.timeSlot,
    totalNum: data.totalQuota
  })
}

export const updateSchedule = (id: number, data: {
  totalQuota?: number
  status?: string
}) => {
  return request.put(`/admin/schedule/${id}`, {
    totalNum: data.totalQuota,
    status: data.status
  })
}

export const deleteSchedule = (id: number) => {
  return request.delete(`/admin/schedule/${id}`)
}
