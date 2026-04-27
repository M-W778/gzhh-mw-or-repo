<template>
  <div class="appointment-page">
    <section class="hero-panel">
      <div class="hero-main">
        <el-page-header @back="goBack" title="返回上一页" />
        <p class="hero-kicker">ONLINE APPOINTMENT</p>
        <h1>预约挂号</h1>
        <p class="hero-desc">请按步骤完成时段选择、就诊人确认与预约提交。</p>
      </div>

      <div class="doctor-summary" v-loading="doctorLoading">
        <template v-if="doctorInfo">
          <h3>{{ doctorName }}</h3>
          <p>{{ departmentName }} · {{ doctorTitleText }}</p>
          <strong>挂号费 ¥{{ doctorFeeText }}</strong>
        </template>
        <template v-else>
          <el-empty description="未获取到医生信息" :image-size="74" />
        </template>
      </div>
    </section>

    <section class="step-panel">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="选择时段" />
        <el-step title="选择就诊人" />
        <el-step title="确认预约" />
      </el-steps>
    </section>

    <section class="workflow-panel" v-loading="currentStepLoading" v-if="activeStep !== 3">
      <article v-if="activeStep === 0" class="step-card">
        <div class="card-head">
          <h2>选择预约时段</h2>
          <el-button class="head-next-btn" type="primary" @click="nextStep">下一步</el-button>
        </div>

        <el-empty v-if="!hasSchedules && !scheduleLoading" description="暂无可用排班" />

        <div v-else class="schedule-grid">
          <article
            v-for="[date, slots] in scheduleEntries"
            :key="date"
            class="date-card"
          >
            <header class="date-head">
              <h4>{{ formatDate(date) }}</h4>
              <small>{{ date }}</small>
            </header>

            <div class="slot-list">
              <button
                v-for="slot in slots"
                :key="slot.id"
                type="button"
                class="slot-item"
                :class="{
                  selected: selectedSchedule === slot.id,
                  disabled: isSlotFull(slot)
                }"
                @click="handleSelectSchedule(slot)"
              >
                <strong>{{ slot.timeSlotText }}</strong>
                <span>{{ isSlotFull(slot) ? '已约满' : `剩余${slot.remainingQuota}号` }}</span>
              </button>
            </div>
          </article>
        </div>

      </article>

      <article v-if="activeStep === 1" class="step-card">
        <div class="card-head">
          <h2>选择就诊人</h2>
          <span>支持选择已有就诊人或新增后返回</span>
        </div>

        <el-empty v-if="patients.length === 0 && !patientLoading" description="暂无就诊人，请先添加">
          <el-button type="primary" @click="goToAddPatient">添加就诊人</el-button>
        </el-empty>

        <div v-else class="patient-list">
          <button
            v-for="patient in patients"
            :key="patient.id"
            type="button"
            class="patient-card"
            :class="{ selected: selectedPatient === patient.id }"
            @click="selectedPatient = patient.id"
          >
            <div class="patient-main">
              <h4>{{ patient.fullName }}</h4>
              <el-tag v-if="patient.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <p>{{ getGenderText(patient.gender) }} · {{ maskIdCard(patient.idCard) }}</p>
            <small>{{ patient.phone || '暂无手机号' }}</small>
          </button>

          <div class="add-patient-entry">
            <el-link type="primary" @click="goToAddPatient">+ 添加新就诊人</el-link>
          </div>
        </div>

        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </div>
      </article>

      <article v-if="activeStep === 2" class="step-card">
        <div class="card-head">
          <h2>确认预约信息</h2>
          <span>确认无误后提交预约</span>
        </div>

        <div class="confirm-grid">
          <section class="confirm-block">
            <h4>医生信息</h4>
            <p><label>医生</label>{{ doctorName }}</p>
            <p><label>科室</label>{{ departmentName }}</p>
            <p><label>职称</label>{{ doctorTitleText }}</p>
            <p><label>挂号费</label>¥{{ doctorFeeText }}</p>
          </section>

          <section class="confirm-block">
            <h4>预约信息</h4>
            <p><label>日期</label>{{ selectedScheduleInfo?.dateLabel || '-' }}</p>
            <p><label>时段</label>{{ selectedScheduleInfo?.timeSlotText || '-' }}</p>
            <p><label>号源</label>{{ selectedScheduleInfo ? `剩余${selectedScheduleInfo.remainingQuota}号` : '-' }}</p>
          </section>

          <section class="confirm-block">
            <h4>就诊人信息</h4>
            <p><label>姓名</label>{{ selectedPatientInfo?.fullName || '-' }}</p>
            <p><label>身份证</label>{{ maskIdCard(selectedPatientInfo?.idCard || '') || '-' }}</p>
            <p><label>手机号</label>{{ selectedPatientInfo?.phone || '暂无' }}</p>
          </section>

          <section class="confirm-block">
            <h4>备注信息</h4>
            <el-input
              v-model="remarks"
              type="textarea"
              :rows="4"
              placeholder="请输入备注信息（选填）"
            />
          </section>
        </div>

        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :loading="submitting" @click="submitAppointment">
            确认预约
          </el-button>
        </div>
      </article>
    </section>

    <el-result
      v-if="activeStep === 3"
      class="success-panel"
      icon="success"
      title="预约成功"
      :sub-title="`您的预约号是：${appointmentNo}`"
    >
      <template #extra>
        <div class="success-actions">
          <div class="notification-area">
            <el-alert
              v-if="notificationSent"
              title="预约确认通知已发送"
              type="success"
              :closable="false"
              show-icon
            />
            <el-button
              v-else
              type="success"
              :loading="sendingNotification"
              @click="sendNotification"
            >
              发送预约确认通知
            </el-button>
          </div>

          <div class="action-row">
            <el-button type="primary" @click="goToAppointments">查看我的预约</el-button>
            <el-button @click="goHome">返回首页</el-button>
          </div>
        </div>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getDoctorDetail } from '@/api/doctor'
import { getSchedulesByDoctor } from '@/api/schedule'
import { getPatients } from '@/api/patient'
import { createAppointment, sendAppointmentNotification } from '@/api/appointment'

const route = useRoute()
const router = useRouter()

const doctorId = ref(0)
const activeStep = ref(0)

const schedules = ref<Record<string, any[]>>({})
const patients = ref<any[]>([])
const doctorInfo = ref<any>(null)

const selectedSchedule = ref<number | null>(null)
const selectedPatient = ref<number | null>(null)
const remarks = ref('')

const submitting = ref(false)
const appointmentNo = ref('')
const notificationSent = ref(false)
const sendingNotification = ref(false)
const appointmentId = ref<number | null>(null)

const doctorLoading = ref(false)
const scheduleLoading = ref(false)
const patientLoading = ref(false)

const normalizeId = (value: unknown) => {
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : 0
}

const pickString = (...values: unknown[]) => {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }
  return ''
}

const hasSchedules = computed(() => {
  return Object.keys(schedules.value).length > 0
})

const scheduleEntries = computed(() => {
  return Object.entries(schedules.value).sort((a, b) => dayjs(a[0]).valueOf() - dayjs(b[0]).valueOf())
})

const selectedScheduleInfo = computed(() => {
  for (const [date, slots] of scheduleEntries.value) {
    const slot = (slots as any[]).find((item) => item.id === selectedSchedule.value)
    if (slot) {
      return {
        ...slot,
        date,
        dateLabel: formatDate(date)
      }
    }
  }
  return null
})

const selectedPatientInfo = computed(() => {
  return patients.value.find((patient) => patient.id === selectedPatient.value) || null
})

const doctorName = computed(() => {
  return pickString(doctorInfo.value?.name) || '未命名医生'
})

const departmentName = computed(() => {
  return pickString(
    doctorInfo.value?.department?.name,
    doctorInfo.value?.department?.deptName,
    doctorInfo.value?.department?.dept_name,
    doctorInfo.value?.departmentName
  ) || '未分配科室'
})

const doctorTitleText = computed(() => {
  const title = pickString(doctorInfo.value?.title)
  return pickString(doctorInfo.value?.titleText) || getTitleText(title)
})

const doctorFeeText = computed(() => {
  const fee = Number(doctorInfo.value?.registrationFee ?? 0)
  return Number.isFinite(fee) ? fee.toFixed(2) : '0.00'
})

const currentStepLoading = computed(() => {
  if (activeStep.value === 0) return scheduleLoading.value
  if (activeStep.value === 1) return patientLoading.value
  if (activeStep.value === 2) return doctorLoading.value || scheduleLoading.value || patientLoading.value
  return false
})

const getTitleText = (title: string) => {
  const titleMap: Record<string, string> = {
    CHIEF_PHYSICIAN: '主任医师',
    ASSOCIATE_CHIEF: '副主任医师',
    ATTENDING: '主治医师',
    RESIDENT: '住院医师',
    INTERN: '实习医师'
  }
  return titleMap[title] || title || '医师'
}

const getGenderText = (gender: string) => {
  if (gender === 'MALE') return '男'
  if (gender === 'FEMALE') return '女'
  return '其他'
}

const getTitleType = (title: string) => {
  const typeMap: Record<string, string> = {
    CHIEF_PHYSICIAN: 'danger',
    ASSOCIATE_CHIEF: 'warning',
    ATTENDING: 'success',
    RESIDENT: 'info',
    INTERN: ''
  }
  return typeMap[title] || ''
}

const fetchDoctorInfo = async () => {
  doctorLoading.value = true
  try {
    const res = await getDoctorDetail(doctorId.value)
    doctorInfo.value = res
  } catch (error) {
    doctorInfo.value = null
    ElMessage.error('获取医生信息失败')
  } finally {
    doctorLoading.value = false
  }
}

const fetchSchedules = async () => {
  scheduleLoading.value = true
  try {
    const res = await getSchedulesByDoctor(doctorId.value)
    schedules.value = res || {}
  } catch (error) {
    schedules.value = {}
    ElMessage.error('获取排班信息失败')
  } finally {
    scheduleLoading.value = false
  }
}

const fetchPatients = async () => {
  patientLoading.value = true
  try {
    const res = await getPatients()
    const source = Array.isArray(res) ? res : []
    patients.value = source
    const defaultPatient = source.find((patient: any) => patient.isDefault)
    selectedPatient.value = defaultPatient ? defaultPatient.id : (source[0]?.id ?? null)
  } catch (error) {
    patients.value = []
    ElMessage.error('获取就诊人列表失败')
  } finally {
    patientLoading.value = false
  }
}

const initPage = async () => {
  doctorId.value = normalizeId(route.params.doctorId)
  if (!doctorId.value) {
    ElMessage.error('医生参数无效')
    return
  }

  activeStep.value = 0
  selectedSchedule.value = null
  remarks.value = ''
  appointmentNo.value = ''
  notificationSent.value = false
  appointmentId.value = null

  await Promise.all([fetchDoctorInfo(), fetchSchedules(), fetchPatients()])
}

const formatDate = (dateStr: string) => {
  const date = dayjs(dateStr)
  const today = dayjs()
  const tomorrow = today.add(1, 'day')

  if (date.isSame(today, 'day')) {
    return `今天 ${date.format('MM月DD日')}`
  }
  if (date.isSame(tomorrow, 'day')) {
    return `明天 ${date.format('MM月DD日')}`
  }
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${weekDays[date.day()]} ${date.format('MM月DD日')}`
}

const maskIdCard = (idCard: string) => {
  if (!idCard || idCard.length < 8) return idCard
  return `${idCard.substring(0, 4)}********${idCard.substring(idCard.length - 4)}`
}

const isSlotFull = (slot: any) => {
  const remaining = Number(slot?.remainingQuota ?? 0)
  return pickString(slot?.status) === 'FULL' || remaining <= 0
}

const handleSelectSchedule = (slot: any) => {
  if (isSlotFull(slot)) {
    return
  }
  selectedSchedule.value = slot.id
}

const nextStep = () => {
  if (activeStep.value === 0 && !selectedSchedule.value) {
    ElMessage.warning('请先选择预约时段')
    return
  }
  if (activeStep.value === 1 && !selectedPatient.value) {
    ElMessage.warning('请先选择就诊人')
    return
  }
  if (activeStep.value < 2) {
    activeStep.value += 1
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value -= 1
  }
}

const goToAddPatient = () => {
  router.push('/patients')
}

const submitAppointment = async () => {
  if (!selectedSchedule.value || !selectedPatient.value) {
    ElMessage.warning('预约信息不完整，请重新确认')
    return
  }

  submitting.value = true
  try {
    const res = await createAppointment({
      doctorId: doctorId.value,
      scheduleId: selectedSchedule.value,
      patientId: selectedPatient.value,
      remarks: remarks.value
    })
    appointmentNo.value = pickString(res?.appointmentNo) || '-'
    appointmentId.value = Number(res?.id ?? 0) || null
    notificationSent.value = !!res?.notificationSent
    activeStep.value = 3
    ElMessage.success('预约成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

const sendNotification = async () => {
  if (!appointmentId.value) {
    ElMessage.warning('未找到预约记录，无法发送通知')
    return
  }

  sendingNotification.value = true
  try {
    await sendAppointmentNotification(appointmentId.value)
    notificationSent.value = true
    ElMessage.success('预约确认通知已发送')
  } catch (error: any) {
    ElMessage.error(error?.message || '发送通知失败')
  } finally {
    sendingNotification.value = false
  }
}

const goToAppointments = () => {
  router.push('/appointments')
}

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}

watch(
  () => route.params.doctorId,
  () => {
    initPage()
  }
)

onMounted(() => {
  initPage()
})
</script>

<style scoped>
.appointment-page {
  max-width: 1040px;
  margin: 0 auto;
}

.hero-panel {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}

.hero-main,
.doctor-summary {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(122deg, #eff6ff 0%, #dbeafe 62%, #e0f2fe 100%);
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.1);
}

.hero-main {
  padding: 14px 18px 18px;
}

.hero-kicker {
  margin: 12px 0 6px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #1d4ed8;
  font-weight: 700;
}

.hero-main h1 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
}

.hero-desc {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
}

.doctor-summary {
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.doctor-summary h3 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.doctor-summary p {
  margin: 8px 0;
  color: #475569;
  font-size: 14px;
}

.doctor-summary strong {
  color: #dc2626;
  font-size: 24px;
}

.step-panel,
.workflow-panel,
.success-panel {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
}

.step-panel {
  padding: 16px;
  margin-bottom: 14px;
}

.workflow-panel {
  padding: 16px;
}

.step-card {
  min-height: 360px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}

.card-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.card-head span {
  color: #64748b;
  font-size: 12px;
}

.schedule-grid {
  display: grid;
  gap: 10px;
}

.date-card {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 12px;
}

.date-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
}

.date-head h4 {
  margin: 0;
  color: #0f172a;
  font-size: 15px;
}

.date-head small {
  color: #64748b;
  font-size: 12px;
}

.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.slot-item {
  min-width: 120px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.slot-item strong {
  color: #0f172a;
  font-size: 13px;
}

.slot-item span {
  font-size: 12px;
  color: #16a34a;
}

.slot-item.selected {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
  background: #eff6ff;
}

.slot-item.disabled {
  cursor: not-allowed;
  background: #f1f5f9;
  border-color: #e2e8f0;
}

.slot-item.disabled strong,
.slot-item.disabled span {
  color: #94a3b8;
}

.patient-list {
  display: grid;
  gap: 10px;
}

.patient-card {
  text-align: left;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.patient-card:hover,
.patient-card.selected {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.patient-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.patient-main h4 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.patient-card p {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.patient-card small {
  display: inline-block;
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.add-patient-entry {
  text-align: center;
  padding-top: 4px;
}

.confirm-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.confirm-block {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 12px;
}

.confirm-block h4 {
  margin: 0 0 10px;
  color: #0f172a;
  font-size: 15px;
}

.confirm-block p {
  margin: 6px 0;
  color: #334155;
  font-size: 13px;
}

.confirm-block p label {
  display: inline-block;
  width: 66px;
  color: #64748b;
}

.step-actions {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.success-panel {
  margin-top: 10px;
  padding: 12px;
}

.success-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.notification-area {
  min-height: 40px;
}

.action-row {
  display: flex;
  gap: 12px;
}

@media (max-width: 980px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .confirm-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-main h1 {
    font-size: 26px;
  }

  .card-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .action-row {
    flex-direction: column;
    width: 100%;
  }

  .action-row .el-button {
    width: 100%;
  }
}
</style>
