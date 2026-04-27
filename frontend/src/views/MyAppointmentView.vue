<template>
  <div class="appointments-page">
    <section class="hero-panel">
      <div class="hero-main">
        <el-page-header @back="goBack" title="返回首页" />
        <p class="hero-kicker">MY APPOINTMENTS</p>
        <h1>我的预约</h1>
        <p class="hero-desc">查看预约进度、就诊信息与取消状态，便于统一管理。</p>
      </div>

      <div class="hero-stats">
        <article class="stat-card">
          <span>预约总数</span>
          <strong>{{ totalCount }}</strong>
        </article>
        <article class="stat-card pending">
          <span>待就诊</span>
          <strong>{{ pendingCount }}</strong>
        </article>
        <article class="stat-card success">
          <span>已完成</span>
          <strong>{{ completedCount }}</strong>
        </article>
        <article class="stat-card muted">
          <span>已取消</span>
          <strong>{{ cancelledCount }}</strong>
        </article>
      </div>
    </section>

    <section class="filter-panel">
      <el-radio-group v-model="activeStatus" size="small">
        <el-radio-button v-for="option in statusOptions" :key="option.value" :label="option.value">
          {{ option.label }}
          <span class="count">{{ getStatusCount(option.value) }}</span>
        </el-radio-button>
      </el-radio-group>
      <p class="filter-tip">当前显示 {{ filteredAppointments.length }} 条记录</p>
    </section>

    <section class="list-panel" v-loading="loading">
      <el-empty
        v-if="filteredAppointments.length === 0"
        :description="emptyDescription"
        :image-size="94"
      />

      <div v-else class="appointment-list">
        <article
          v-for="item in filteredAppointments"
          :key="item.id"
          class="appointment-card"
          :class="`status-${(item.status || '').toLowerCase()}`"
        >
          <header class="card-head">
            <div class="no-block">
              <label>预约号</label>
              <el-tag size="small" type="info">{{ item.appointmentNo || '-' }}</el-tag>
            </div>
            <el-tag :type="getStatusType(item.status)">{{ getStatusText(item) }}</el-tag>
          </header>

          <div class="card-grid">
            <section class="info-block">
              <h4>就诊信息</h4>
              <p><label>就诊人</label>{{ item.patientName || '-' }}</p>
              <p><label>身份证</label>{{ item.patientIdCard || '-' }}</p>
              <p><label>预约时间</label>{{ formatVisitTime(item) }}</p>
            </section>

            <section class="info-block">
              <h4>医生信息</h4>
              <p><label>医生</label>{{ item.doctorName || '-' }}</p>
              <p><label>职称</label>{{ item.doctorTitle || '-' }}</p>
              <p><label>科室</label>{{ item.departmentName || '-' }}</p>
            </section>
          </div>

          <div class="remark" v-if="item.remarks">
            <label>备注</label>
            <p>{{ item.remarks }}</p>
          </div>

          <footer class="card-actions" v-if="item.status === 'PENDING'">
            <el-button
              type="danger"
              size="small"
              :disabled="!item.canCancel"
              @click="handleCancel(item)"
            >
              {{ item.canCancel ? '取消预约' : '已超取消时限' }}
            </el-button>
          </footer>
        </article>
      </div>
    </section>

    <el-dialog v-model="cancelDialogVisible" title="取消预约" width="420px">
      <p class="cancel-tip">确定要取消预约吗？取消后号源将释放给其他患者。</p>
      <el-form>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cancelLoading" @click="confirmCancel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getAppointments, cancelAppointment } from '@/api/appointment'

type StatusFilter = 'ALL' | 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'

const router = useRouter()

const appointmentDOS = ref<any[]>([])
const loading = ref(false)
const cancelDialogVisible = ref(false)
const cancelLoading = ref(false)
const cancelReason = ref('')
const currentAppointment = ref<any>(null)
const activeStatus = ref<StatusFilter>('ALL')

const statusOptions: Array<{ value: StatusFilter; label: string }> = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待就诊' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'NO_SHOW', label: '爽约' }
]

const statusTextMap: Record<string, string> = {
  PENDING: '待就诊',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '爽约'
}

const sortedAppointments = computed(() => {
  const source = Array.isArray(appointmentDOS.value) ? [...appointmentDOS.value] : []
  return source.sort((a, b) => {
    const aTime = dayjs(a?.appointmentDate || '').valueOf() || 0
    const bTime = dayjs(b?.appointmentDate || '').valueOf() || 0
    if (aTime !== bTime) {
      return bTime - aTime
    }
    return Number(b?.id || 0) - Number(a?.id || 0)
  })
})

const filteredAppointments = computed(() => {
  if (activeStatus.value === 'ALL') {
    return sortedAppointments.value
  }
  return sortedAppointments.value.filter((item) => (item?.status || '') === activeStatus.value)
})

const totalCount = computed(() => appointmentDOS.value.length)
const pendingCount = computed(() => appointmentDOS.value.filter((item) => item.status === 'PENDING').length)
const completedCount = computed(() => appointmentDOS.value.filter((item) => item.status === 'COMPLETED').length)
const cancelledCount = computed(() => appointmentDOS.value.filter((item) => item.status === 'CANCELLED').length)

const emptyDescription = computed(() => {
  if (activeStatus.value === 'ALL') {
    return '暂无预约记录'
  }
  const current = statusOptions.find((option) => option.value === activeStatus.value)
  return `暂无${current?.label || ''}记录`
})

const fetchAppointments = async () => {
  loading.value = true
  try {
    const res = await getAppointments()
    appointmentDOS.value = Array.isArray(res) ? res : []
  } catch (error) {
    appointmentDOS.value = []
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info',
    NO_SHOW: 'warning'
  }
  return typeMap[status] || ''
}

const getStatusText = (item: any) => {
  return item?.statusText || statusTextMap[item?.status] || item?.status || '未知状态'
}

const getStatusCount = (status: StatusFilter) => {
  if (status === 'ALL') {
    return totalCount.value
  }
  return appointmentDOS.value.filter((item) => item.status === status).length
}

const formatVisitTime = (item: any) => {
  const date = item?.appointmentDate || '-'
  const slot = item?.timeSlotText || ''
  return `${date} ${slot}`.trim()
}

const handleCancel = (item: any) => {
  if (!item?.canCancel) {
    ElMessage.warning('预约已超过30分钟，无法取消')
    return
  }
  currentAppointment.value = item
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!currentAppointment.value?.id) {
    ElMessage.warning('未找到预约记录')
    return
  }
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  cancelLoading.value = true
  try {
    await cancelAppointment(currentAppointment.value.id, cancelReason.value)
    ElMessage.success('取消预约成功')
    cancelDialogVisible.value = false
    fetchAppointments()
  } catch (error: any) {
    ElMessage.error(error?.message || '取消预约失败')
  } finally {
    cancelLoading.value = false
  }
}

const goBack = () => {
  router.push('/departments')
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.appointments-page {
  max-width: 1040px;
  margin: 0 auto;
}

.hero-panel {
  display: grid;
  grid-template-columns: 1.45fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}

.hero-main,
.hero-stats {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(124deg, #eff6ff 0%, #dbeafe 62%, #e0f2fe 100%);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
}

.hero-main {
  padding: 14px 18px 18px;
}

.hero-kicker {
  margin: 12px 0 6px;
  color: #1d4ed8;
  font-size: 12px;
  letter-spacing: 0.08em;
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

.hero-stats {
  padding: 14px;
  display: grid;
  gap: 10px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.92);
  padding: 12px;
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

.stat-card.pending strong {
  color: #2563eb;
}

.stat-card.success strong {
  color: #059669;
}

.stat-card.muted strong {
  color: #64748b;
}

.filter-panel,
.list-panel {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
}

.filter-panel {
  padding: 14px;
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.filter-panel :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.count {
  font-size: 12px;
  color: #64748b;
}

.filter-tip {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.list-panel {
  padding: 16px;
}

.appointment-list {
  display: grid;
  gap: 12px;
}

.appointment-card {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #fff;
  padding: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.appointment-card:hover {
  transform: translateY(-2px);
  border-color: #93c5fd;
  box-shadow: 0 12px 20px rgba(37, 99, 235, 0.1);
}

.appointment-card.status-cancelled {
  opacity: 0.8;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.no-block {
  display: flex;
  align-items: center;
  gap: 8px;
}

.no-block label {
  color: #64748b;
  font-size: 13px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.info-block {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px;
}

.info-block h4 {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 15px;
}

.info-block p {
  margin: 5px 0;
  color: #334155;
  font-size: 13px;
}

.info-block p label {
  display: inline-block;
  width: 66px;
  color: #64748b;
}

.remark {
  margin-top: 10px;
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.remark label {
  color: #64748b;
  font-size: 12px;
}

.remark p {
  margin: 6px 0 0;
  color: #334155;
  font-size: 13px;
}

.card-actions {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.cancel-tip {
  margin: 0 0 14px;
  color: #475569;
  font-size: 14px;
}

@media (max-width: 980px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-main h1 {
    font-size: 26px;
  }

  .filter-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-panel :deep(.el-radio-group) {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .filter-panel :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner),
  .filter-panel :deep(.el-radio-button__inner) {
    width: 100%;
    justify-content: center;
  }

  .card-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>