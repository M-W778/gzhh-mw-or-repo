<template>
  <div class="doctor-detail-page">
    <section class="top-bar">
      <el-page-header @back="goBack" title="返回医生列表" />
    </section>

    <section class="profile-panel" v-loading="detailLoading">
      <template v-if="doctorDetail">
        <div class="profile-main">
          <div class="avatar-wrap">
            <el-avatar :size="104" :icon="UserFilled" />
          </div>

          <div class="identity-wrap">
            <p class="identity-kicker">DOCTOR PROFILE</p>
            <h1>{{ doctorDetail.name }}</h1>

            <div class="tags-row">
              <el-tag :type="getTitleType(doctorDetail.title)" size="large">
                {{ doctorDetail.titleText }}
              </el-tag>
              <el-tag type="info" size="large" effect="plain">
                {{ doctorDetail.departmentName }}
              </el-tag>
            </div>

            <p class="fee">挂号费：<strong>¥{{ formatFee(doctorDetail.registrationFee) }}</strong></p>
          </div>

          <div class="action-wrap">
            <el-button type="primary" size="large" @click="bookAppointment">
              立即预约
            </el-button>
          </div>
        </div>

        <div class="profile-metrics">
          <article class="metric-card">
            <span>可预约天数</span>
            <strong>{{ scheduleEntries.length }}</strong>
          </article>
          <article class="metric-card">
            <span>可预约号源</span>
            <strong>{{ availableSlotCount }}</strong>
          </article>
        </div>
      </template>

      <el-empty
        v-else
        description="未获取到医生详情信息"
        :image-size="92"
      />
    </section>

    <section class="content-grid">
      <article class="bio-panel" v-loading="detailLoading">
        <div class="panel-header">
          <h2>医生信息</h2>
        </div>

        <div class="info-block">
          <label>医生专长</label>
          <p>{{ doctorDetail?.specialty || '暂无专长信息' }}</p>
        </div>

        <div class="info-block">
          <label>医生简介</label>
          <p>{{ doctorDetail?.introduction || '暂无简介信息' }}</p>
        </div>
      </article>

      <article class="schedule-panel" v-loading="scheduleLoading">
        <div class="panel-header">
          <h2>近期排班</h2>
          <span>未来 7 天可预约时段</span>
        </div>

        <el-empty
          v-if="!hasSchedules"
          description="暂无排班信息"
          :image-size="86"
        />

        <div v-else class="schedule-list">
          <div
            v-for="[date, slots] in scheduleEntries"
            :key="date"
            class="schedule-day"
          >
            <div class="day-label">
              <p>{{ formatDate(date) }}</p>
              <small>{{ date }}</small>
            </div>

            <div class="slot-group">
              <el-tag
                v-for="slot in slots"
                :key="slot.id"
                :type="getScheduleTagType(slot)"
                effect="light"
                class="slot-tag"
              >
                {{ slot.timeSlotText }} · 余 {{ slot.remainingQuota }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="legend" v-if="hasSchedules">
          <span><i class="dot success"></i>充足</span>
          <span><i class="dot warning"></i>紧张</span>
          <span><i class="dot danger"></i>约满</span>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getDoctorDetail } from '@/api/doctor'
import { getSchedulesByDoctor } from '@/api/schedule'
import dayjs from 'dayjs'

interface DoctorDetailView {
  id: number
  name: string
  title: string
  titleText: string
  specialty: string
  introduction: string
  registrationFee: number
  departmentName: string
}

const route = useRoute()
const router = useRouter()

const doctorId = ref(0)
const detailLoading = ref(false)
const scheduleLoading = ref(false)

const doctorDetail = ref<DoctorDetailView | null>(null)
const schedules = ref<Record<string, any[]>>({})

const pickString = (...values: unknown[]) => {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }
  return ''
}

const parseDoctorId = (value: unknown) => {
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : 0
}

const hasSchedules = computed(() => {
  return Object.keys(schedules.value).length > 0
})

const scheduleEntries = computed(() => {
  return Object.entries(schedules.value)
    .sort((a, b) => dayjs(a[0]).valueOf() - dayjs(b[0]).valueOf())
})

const availableSlotCount = computed(() => {
  return scheduleEntries.value.reduce((sum, [, slots]) => {
    return sum + (slots as any[]).reduce((n, slot) => {
      const remaining = Number(slot?.remainingQuota ?? 0)
      return n + (Number.isFinite(remaining) && remaining > 0 ? remaining : 0)
    }, 0)
  }, 0)
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

const normalizeDoctorDetail = (raw: any): DoctorDetailView => {
  const title = pickString(raw?.title)
  const fee = Number(raw?.registrationFee ?? raw?.registration_fee ?? 0)
  return {
    id: Number(raw?.id ?? doctorId.value),
    name: pickString(raw?.name) || '未命名医生',
    title,
    titleText: pickString(raw?.titleText) || getTitleText(title),
    specialty: pickString(raw?.specialty),
    introduction: pickString(raw?.introduction),
    registrationFee: Number.isFinite(fee) ? fee : 0,
    departmentName: pickString(
      raw?.department?.name,
      raw?.department?.deptName,
      raw?.department?.dept_name,
      raw?.departmentName
    ) || '未分配科室'
  }
}

const fetchDoctorDetail = async () => {
  detailLoading.value = true
  try {
    const res = await getDoctorDetail(doctorId.value)
    doctorDetail.value = normalizeDoctorDetail(res)
  } catch (error) {
    doctorDetail.value = null
    ElMessage.error('获取医生详情失败')
  } finally {
    detailLoading.value = false
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

const initPage = async () => {
  doctorId.value = parseDoctorId(route.params.id)
  if (!doctorId.value) {
    doctorDetail.value = null
    schedules.value = {}
    ElMessage.error('医生参数无效')
    return
  }
  await Promise.all([fetchDoctorDetail(), fetchSchedules()])
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

const getScheduleTagType = (slot: any) => {
  const status = pickString(slot?.status)
  const remaining = Number(slot?.remainingQuota ?? 0)
  if (status === 'FULL' || remaining <= 0) return 'danger'
  if (remaining < 5) return 'warning'
  return 'success'
}

const formatDate = (dateStr: string) => {
  const date = dayjs(dateStr)
  const today = dayjs()
  const tomorrow = today.add(1, 'day')

  if (date.isSame(today, 'day')) {
    return `今天 (${date.format('MM月DD日')})`
  }
  if (date.isSame(tomorrow, 'day')) {
    return `明天 (${date.format('MM月DD日')})`
  }
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${weekDays[date.day()]} (${date.format('MM月DD日')})`
}

const formatFee = (value: number) => {
  return Number.isFinite(value) ? value.toFixed(2) : '0.00'
}

const goBack = () => {
  router.back()
}

const bookAppointment = () => {
  router.push(`/appointment/${doctorId.value}`)
}

watch(
  () => route.params.id,
  () => {
    initPage()
  }
)

onMounted(() => {
  initPage()
})
</script>

<style scoped>
.doctor-detail-page {
  max-width: 1100px;
  margin: 0 auto;
}

.top-bar {
  margin-bottom: 14px;
}

.profile-panel {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(125deg, #eff6ff 0%, #dbeafe 62%, #e0f2fe 100%);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
  padding: 16px;
  margin-bottom: 14px;
}

.profile-main {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
}

.avatar-wrap {
  width: 120px;
  display: flex;
  justify-content: center;
}

.identity-kicker {
  margin: 0 0 6px;
  color: #1d4ed8;
  font-size: 12px;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.identity-wrap h1 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
}

.tags-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.fee {
  margin: 10px 0 0;
  color: #334155;
  font-size: 14px;
}

.fee strong {
  color: #dc2626;
  font-size: 20px;
}

.action-wrap {
  display: flex;
  align-items: center;
}

.profile-metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.92);
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.metric-card span {
  color: #64748b;
  font-size: 13px;
}

.metric-card strong {
  color: #1e3a8a;
  font-size: 22px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 14px;
}

.bio-panel,
.schedule-panel {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
  padding: 14px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
}

.panel-header h2 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.panel-header span {
  color: #64748b;
  font-size: 12px;
}

.info-block + .info-block {
  margin-top: 14px;
}

.info-block label {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
}

.info-block p {
  margin: 0;
  color: #334155;
  line-height: 1.7;
  font-size: 14px;
}

.schedule-list {
  display: grid;
  gap: 10px;
}

.schedule-day {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px;
  display: grid;
  grid-template-columns: 170px 1fr;
  gap: 10px;
}

.day-label p {
  margin: 0;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.day-label small {
  display: inline-block;
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
}

.slot-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.slot-tag {
  margin: 0;
}

.legend {
  margin-top: 12px;
  display: flex;
  gap: 14px;
  color: #64748b;
  font-size: 12px;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  margin-right: 6px;
}

.dot.success {
  background: #16a34a;
}

.dot.warning {
  background: #f59e0b;
}

.dot.danger {
  background: #dc2626;
}

@media (max-width: 980px) {
  .profile-main {
    grid-template-columns: auto 1fr;
  }

  .action-wrap {
    grid-column: 1 / -1;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .identity-wrap h1 {
    font-size: 24px;
  }

  .profile-metrics {
    grid-template-columns: 1fr;
  }

  .schedule-day {
    grid-template-columns: 1fr;
  }
}
</style>