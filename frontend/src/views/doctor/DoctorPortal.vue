<template>
  <div class="doctorDO-portal">
    <div class="page-header">
      <el-page-header @back="goBack" title="医生工作台" />
    </div>
    
    <!-- 医生信息头部 -->
    <el-card class="doctorDO-header">
      <div class="doctorDO-info">
        <el-avatar :size="80" :icon="UserFilled" class="doctorDO-avatar" />
        <div class="doctorDO-details">
          <h2>{{ doctorInfo?.name }}</h2>
          <el-tag :type="getTitleType(doctorInfo?.title)" size="large">
            {{ getTitleText(doctorInfo?.title) }}
          </el-tag>
          <p class="departmentDO">{{ doctorInfo?.department?.name }}</p>
          <p class="specialty">专长：{{ doctorInfo?.specialty || '暂无' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ statistics?.todayCount || 0 }}</div>
          <div class="stat-label">今日预约</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending">
          <div class="stat-value">{{ statistics?.pending || 0 }}</div>
          <div class="stat-label">待就诊</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card completed">
          <div class="stat-value">{{ statistics?.completed || 0 }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card total">
          <div class="stat-value">{{ statistics?.total || 0 }}</div>
          <div class="stat-label">总预约</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约列表 -->
    <el-card class="appointmentDO-list">
      <template #header>
        <div class="list-header">
          <span>预约管理</span>
          <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="PENDING">待就诊</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="appointments" v-loading="loading" stripe>
        <el-table-column prop="appointmentNo" label="预约号" width="150" />
        <el-table-column label="就诊人" width="120">
          <template #default="{ row }">
            <div>{{ row.patientName }}</div>
            <div class="id-card">{{ maskIdCard(row.patientIdCard) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="150">
          <template #default="{ row }">
            <div>{{ row.appointmentDate }}</div>
            <el-tag size="small" :type="row.timeSlot === 'MORNING' ? 'success' : 'warning'">
              {{ row.timeSlot === 'MORNING' ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleComplete(row)"
            >
              完成就诊
            </el-button>
            <el-tag v-else type="info" size="small">已处理</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import {
  getDoctorProfile,
  getDoctorAppointments,
  getTodayAppointments,
  getAppointmentsByStatus,
  getDoctorStatistics,
  completeAppointment,
  type DoctorProfile,
  type DoctorAppointment,
  type DoctorStatistics
} from '@/api/doctorPortal'

const router = useRouter()
const doctorInfo = ref<DoctorProfile>()
const appointments = ref<DoctorAppointment[]>([])
const statistics = ref<DoctorStatistics>()
const loading = ref(false)
const filterStatus = ref('today')

const loadDoctorInfo = async () => {
  try {
    const res = await getDoctorProfile()
    doctorInfo.value = res
  } catch (error) {
    ElMessage.error('获取医生信息失败')
  }
}

const loadStatistics = async () => {
  try {
    const res = await getDoctorStatistics()
    statistics.value = res
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const loadAppointments = async () => {
  loading.value = true
  try {
    let res
    switch (filterStatus.value) {
      case 'today':
        res = await getTodayAppointments()
        break
      case '':
        res = await getDoctorAppointments()
        break
      default:
        res = await getAppointmentsByStatus(filterStatus.value)
    }
    appointments.value = res
  } catch (error) {
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  loadAppointments()
}

const handleComplete = async (row: DoctorAppointment) => {
  try {
    await ElMessageBox.confirm('确认完成该患者的就诊？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await completeAppointment(row.id)
    ElMessage.success('就诊完成')
    loadAppointments()
    loadStatistics()
  } catch (error) {
    // 取消操作
  }
}

const getTitleText = (title?: string) => {
  const map: Record<string, string> = {
    'CHIEF_PHYSICIAN': '主任医师',
    'ASSOCIATE_CHIEF': '副主任医师',
    'ATTENDING': '主治医师',
    'RESIDENT': '住院医师',
    'INTERN': '实习医师'
  }
  return map[title || ''] || title
}

const getTitleType = (title?: string) => {
  const map: Record<string, string> = {
    'CHIEF_PHYSICIAN': 'danger',
    'ASSOCIATE_CHIEF': 'warning',
    'ATTENDING': 'success',
    'RESIDENT': 'info',
    'INTERN': ''
  }
  return map[title || ''] || ''
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'NO_SHOW': 'danger'
  }
  return map[status] || ''
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待就诊',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'NO_SHOW': '爽约'
  }
  return map[status] || status
}

const maskIdCard = (idCard?: string) => {
  if (!idCard || idCard.length < 8)
    return idCard
  return idCard.substring(0, 6) + '********' + idCard.substring(idCard.length - 4)
}

const goBack = () => {
  router.push('/departments')
}

onMounted(() => {
  loadDoctorInfo()
  loadStatistics()
  loadAppointments()
})
</script>

<style scoped lang="scss">
.doctorDO-portal {
  padding: 20px;

  h2 {
    margin-bottom: 20px;
    color: #303133;
  }

  .stat-cards {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;

        .stat-label {
          color: rgba(255, 255, 255, 0.9);
        }
      }

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: inherit;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .charts-row {
    .chart-card {
      .chart {
        height: 300px;
      }
    }
  }
}
</style>
