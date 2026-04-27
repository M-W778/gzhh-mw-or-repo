import request from './request'

type GenderText = 'MALE' | 'FEMALE'

interface BackendPatient {
  id: number
  realName: string
  idCard: string
  phone: string
  gender: number
  birthDate?: string
  isDefault?: boolean
}

export interface PatientFormData {
  fullName: string
  idCard: string
  phone?: string
  gender: GenderText
  birthDate?: string
  isDefault?: boolean
  [key: string]: unknown
}

const toFrontendPatient = (patient: BackendPatient) => ({
  id: patient.id,
  fullName: patient.realName,
  idCard: patient.idCard,
  phone: patient.phone,
  gender: patient.gender === 0 ? 'FEMALE' : 'MALE',
  birthDate: patient.birthDate,
  isDefault: !!patient.isDefault
})

const toBackendPatient = (data: PatientFormData) => ({
  realName: data.fullName,
  idCard: data.idCard,
  phone: data.phone,
  gender: data.gender === 'FEMALE' ? 0 : 1,
  birthDate: data.birthDate,
  isDefault: data.isDefault
})

export const getPatients = async () => {
  const res = await request.get<BackendPatient[]>('/patient/query')
  return res.map(toFrontendPatient)
}

export const getPatientById = async (id: number) => {
  const res = await request.get<BackendPatient>(`/patient/${id}`)
  return toFrontendPatient(res)
}

export const addPatient = (data: PatientFormData) => {
  return request.post('/patient/save', toBackendPatient(data))
}

export const updatePatient = (id: number, data: PatientFormData) => {
  return request.put(`/patient/update/${id}`, toBackendPatient(data))
}

export const deletePatient = (id: number) => {
  return request.delete(`/patient/delete/${id}`)
}

export const getDefaultPatient = async () => {
  const res = await request.get<BackendPatient | null>('/patient/default')
  if (!res) {
    return null
  }
  return toFrontendPatient(res)
}
