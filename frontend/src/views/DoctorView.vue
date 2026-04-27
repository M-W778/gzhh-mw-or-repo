<template>
  <div class="doctors-page">
    <section class="hero">
      <div class="hero-main">
        <el-page-header @back="goBack" title="返回科室列表" />
        <p class="hero-kicker">DEPARTMENT DOCTORS</p>
        <h1>{{ departmentName || '医生列表' }}</h1>
        <p class="hero-desc">请选择医生查看详情，或直接进入预约挂号流程。</p>
      </div>
      <div class="hero-stats">
        <article class="stat-card">
          <span>医生人数</span>
          <strong>{{ doctorList.length }}</strong>
        </article>
      </div>
    </section>

    <section class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="按姓名/专长/职称筛选医生"
        clearable
        @clear="handleClear"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button @click="handleClear">重置筛选</el-button>
      <p class="toolbar-tip">当前显示 {{ filteredDoctors.length }} 位医生</p>
    </section>

    <section class="list-panel" v-loading="loading">
      <div class="result-header">
        <h2>出诊医生</h2>
        <span>{{ filteredDoctors.length }} / {{ doctorList.length }}</span>
      </div>

      <div class="doctor-grid" v-if="filteredDoctors.length > 0">
        <article
          v-for="doctor in filteredDoctors"
          :key="doctor.id"
          class="doctor-card"
        >
          <div class="card-head">
            <el-avatar :size="68" :icon="UserFilled" />
            <div class="doctor-basic">
              <h3>{{ doctor.name }}</h3>
              <div class="title-row">
                <el-tag :type="getTitleType(doctor.title)" size="small">
                  {{ getTitleText(doctor.title) }}
                </el-tag>
              </div>
              <p class="department-line">{{ departmentName || '所属科室' }}</p>
            </div>
            <div class="fee-box">
              <span>挂号费</span>
              <strong>¥{{ formatFee(doctor.registrationFee) }}</strong>
            </div>
          </div>

          <div class="doctor-content">
            <div class="info-block">
              <label>医生专长</label>
              <p>{{ doctor.specialty || '暂无专长信息' }}</p>
            </div>
            <div class="info-block">
              <label>医生简介</label>
              <p class="intro">{{ doctor.introduction || '暂无简介信息' }}</p>
            </div>
          </div>

          <div class="card-actions">
            <el-button plain @click="viewDetail(doctor.id)">查看详情</el-button>
            <el-button type="primary" @click="bookAppointment(doctor.id)">立即预约</el-button>
          </div>
        </article>
      </div>

      <el-empty
        v-else
        description="未找到匹配医生，请更换关键词后重试"
        :image-size="92"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, UserFilled } from '@element-plus/icons-vue'
import { getDoctorsByDepartment } from '@/api/doctor'
import { getDepartmentById } from '@/api/department'

interface DoctorView {
  id: number
  departmentId: number
  name: string
  title: string
  specialty: string
  introduction: string
  registrationFee: number
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const departmentId = ref(0)
const departmentName = ref('')
const doctorList = ref<DoctorView[]>([])
const searchKeyword = ref('')

const parseDepartmentId = (value: unknown) => {
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

const normalizeDoctor = (doctor: any): DoctorView => {
  const rawFee = Number(doctor?.registrationFee ?? doctor?.registration_fee ?? 0)
  return {
    id: Number(doctor?.id ?? 0),
    departmentId: Number(doctor?.departmentId ?? doctor?.department_id ?? 0),
    name: pickString(doctor?.name, doctor?.doctorName) || '未命名医生',
    title: pickString(doctor?.title) || 'ATTENDING',
    specialty: pickString(doctor?.specialty),
    introduction: pickString(doctor?.introduction),
    registrationFee: Number.isFinite(rawFee) ? rawFee : 0
  }
}

const filteredDoctors = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return doctorList.value
  }
  return doctorList.value.filter((doctor) => {
    const titleText = getTitleText(doctor.title)
    return [doctor.name, doctor.specialty, doctor.introduction, titleText]
      .join(' ')
      .toLowerCase()
      .includes(keyword)
  })
})

const fetchDepartment = async () => {
  if (!departmentId.value) {
    departmentName.value = ''
    return
  }
  try {
    const res = await getDepartmentById(departmentId.value)
    departmentName.value = pickString(res?.name, res?.deptName, res?.dept_name) || `科室 ${departmentId.value}`
  } catch (error) {
    departmentName.value = `科室 ${departmentId.value}`
  }
}

const fetchDoctors = async () => {
  if (!departmentId.value) {
    doctorList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getDoctorsByDepartment(departmentId.value)
    const source = Array.isArray(res) ? res : []
    doctorList.value = source
      .map(normalizeDoctor)
      .filter((doctor) => doctor.id > 0)
  } catch (error) {
    doctorList.value = []
    ElMessage.error('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

const initPage = async () => {
  departmentId.value = parseDepartmentId(route.params.departmentId)
  if (!departmentId.value) {
    doctorList.value = []
    departmentName.value = ''
    ElMessage.error('科室参数无效')
    return
  }
  await Promise.all([fetchDepartment(), fetchDoctors()])
}

const handleClear = () => {
  searchKeyword.value = ''
}

const getTitleText = (title: string) => {
  const titleMap: Record<string, string> = {
    CHIEF_PHYSICIAN: '主任医师',
    ASSOCIATE_CHIEF: '副主任医师',
    ATTENDING: '主治医师',
    RESIDENT: '住院医师',
    INTERN: '实习医师'
  }
  return titleMap[title] || title
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

const formatFee = (value: number) => {
  return Number.isFinite(value) ? value.toFixed(2) : '0.00'
}

const goBack = () => {
  router.push('/departments')
}

const viewDetail = (doctorId: number) => {
  router.push(`/doctor/${doctorId}`)
}

const bookAppointment = (doctorId: number) => {
  router.push(`/appointment/${doctorId}`)
}

watch(
  () => route.params.departmentId,
  () => {
    initPage()
  }
)

onMounted(() => {
  initPage()
})
</script>

<style scoped>
.doctors-page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}

.hero-main,
.hero-stats {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(120deg, #eff6ff 0%, #dbeafe 65%, #e0f2fe 100%);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
}

.hero-main {
  padding: 16px 20px 20px;
}

.hero-kicker {
  margin: 14px 0 6px;
  color: #1d4ed8;
  font-size: 12px;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.hero-main h1 {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 32px;
}

.hero-desc {
  margin: 0;
  color: #334155;
  font-size: 14px;
}

.hero-stats {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  padding: 14px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.92);
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.stat-card span {
  color: #64748b;
  font-size: 13px;
}

.stat-card strong {
  color: #1e3a8a;
  font-size: 24px;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(250px, 420px) auto 1fr;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.95);
}

.toolbar-tip {
  justify-self: end;
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.list-panel {
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.95);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}

.result-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 21px;
}

.result-header span {
  color: #64748b;
  font-size: 13px;
}

.doctor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.doctor-card {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: #fff;
  padding: 14px;
  display: flex;
  flex-direction: column;
  min-height: 260px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.doctor-card:hover {
  transform: translateY(-3px);
  border-color: #93c5fd;
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.12);
}

.card-head {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.doctor-basic h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #0f172a;
}

.title-row {
  margin-bottom: 6px;
}

.department-line {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.fee-box {
  text-align: right;
}

.fee-box span {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.fee-box strong {
  font-size: 18px;
  color: #dc2626;
}

.doctor-content {
  display: grid;
  gap: 10px;
}

.info-block label {
  display: inline-block;
  margin-bottom: 4px;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
}

.info-block p {
  margin: 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

.intro {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
  padding-top: 12px;
}

.card-actions .el-button {
  flex: 1;
}

@media (max-width: 1024px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .doctor-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-main h1 {
    font-size: 26px;
  }

  .toolbar {
    grid-template-columns: 1fr;
  }

  .toolbar-tip {
    justify-self: start;
  }

  .card-head {
    grid-template-columns: auto 1fr;
    grid-template-areas:
      'avatar basic'
      'fee fee';
  }

  .card-head :deep(.el-avatar) {
    grid-area: avatar;
  }

  .doctor-basic {
    grid-area: basic;
  }

  .fee-box {
    grid-area: fee;
    text-align: left;
  }
}
</style>
