<template>
  <div class="scheduleDO-manage">
    <div class="page-header">
      <el-page-header @back="goBack" title="排班管理" />
    </div>
    
    <!-- 医生选择 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="选择医生">
          <el-select v-model="filterForm.doctorId" placeholder="请选择医生" clearable @change="handleDoctorChange">
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name + ' - ' + getTitleText(doctor.title)"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="showCreateDialog">新建排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排班列表 -->
    <el-card class="scheduleDO-list">
      <el-table :data="schedules" v-loading="loading" stripe>
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="100">
          <template #default="{ row }">
            <el-tag :type="row.timeSlot === 'MORNING' ? 'success' : 'warning'">
              {{ row.timeSlot === 'MORNING' ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuota" label="总号源" width="100" />
        <el-table-column prop="remainingQuota" label="剩余号源" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑排班对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="医生" prop="doctorId" v-if="!isEdit">
          <el-select v-model="form.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name + ' - ' + getTitleText(doctor.title)"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate" v-if="!isEdit">
          <el-date-picker
            v-model="form.scheduleDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot" v-if="!isEdit">
          <el-radio-group v-model="form.timeSlot">
            <el-radio label="MORNING">上午</el-radio>
            <el-radio label="AFTERNOON">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="总号源数" prop="totalQuota">
          <el-input-number v-model="form.totalQuota" :min="1" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="AVAILABLE">可预约</el-radio>
            <el-radio label="FULL">已约满</el-radio>
            <el-radio label="SUSPENDED">停诊</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctors } from '@/api/doctor'
import { getSchedulesByDoctorFlat, createSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'

const router = useRouter()
const doctors = ref<any[]>([])
const schedules = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新建排班')
const currentScheduleId = ref<number>()

const filterForm = reactive({
  doctorId: undefined as number | undefined
})

const form = reactive({
  doctorId: undefined as number | undefined,
  scheduleDate: '',
  timeSlot: 'MORNING',
  totalQuota: 15,
  status: 'AVAILABLE'
})

const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalQuota: [{ required: true, message: '请输入总号源数', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const formRef = ref()

const loadDoctors = async () => {
  try {
    const res = await getDoctors()
    doctors.value = res
  } catch (error) {
    ElMessage.error('获取医生列表失败')
  }
}

const loadSchedules = async () => {
  if (!filterForm.doctorId) {
    schedules.value = []
    return
  }
  loading.value = true
  try {
    const res = await getSchedulesByDoctorFlat(filterForm.doctorId)
    schedules.value = res
  } catch (error) {
    ElMessage.error('获取排班列表失败')
  } finally {
    loading.value = false
  }
}

const handleDoctorChange = () => {
  loadSchedules()
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新建排班'
  form.doctorId = filterForm.doctorId
  form.scheduleDate = ''
  form.timeSlot = 'MORNING'
  form.totalQuota = 15
  form.status = 'AVAILABLE'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑排班'
  currentScheduleId.value = row.id
  form.totalQuota = row.totalQuota
  form.status = row.status
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (error) {
    // 取消删除
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateSchedule(currentScheduleId.value!, {
            totalQuota: form.totalQuota,
            status: form.status
          })
          ElMessage.success('更新成功')
        } else {
          if (form.doctorId === undefined) {
            ElMessage.error('请选择医生')
            return
          }
          await createSchedule({
            doctorId: form.doctorId,
            scheduleDate: form.scheduleDate,
            timeSlot: form.timeSlot,
            totalQuota: form.totalQuota
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadSchedules()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const getTitleText = (title: string) => {
  const map: Record<string, string> = {
    'CHIEF_PHYSICIAN': '主任医师',
    'ASSOCIATE_CHIEF': '副主任医师',
    'ATTENDING': '主治医师',
    'RESIDENT': '住院医师',
    'INTERN': '实习医师'
  }
  return map[title] || title
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': 'success',
    'FULL': 'danger',
    'SUSPENDED': 'info'
  }
  return map[status] || ''
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': '可预约',
    'FULL': '已约满',
    'SUSPENDED': '停诊'
  }
  return map[status] || status
}

const goBack = () => {
  router.push('/admin/statistics')
}

onMounted(() => {
  loadDoctors()
})
</script>

<style scoped lang="scss">
.scheduleDO-manage {
  padding: 20px;

  h2 {
    margin-bottom: 20px;
    color: #303133;
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .scheduleDO-list {
    margin-bottom: 20px;
  }
}
</style>
