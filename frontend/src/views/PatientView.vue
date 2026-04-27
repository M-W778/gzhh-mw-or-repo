<template>
  <div class="patients-page">
    <section class="hero-panel">
      <div class="hero-main">
        <el-page-header @back="goBack" title="就诊人管理" />
        <p class="hero-kicker">PATIENT PROFILES</p>
        <h1>添加与管理就诊人</h1>
        <p class="hero-desc">完善就诊人信息后，可在预约挂号页面快速选择。</p>
      </div>

      <div class="hero-stats">
        <article class="stat-card">
          <span>就诊人数</span>
          <strong>{{ patientCount }}</strong>
        </article>
        <article class="stat-card">
          <span>默认就诊人</span>
          <strong>{{ defaultPatientName }}</strong>
        </article>
      </div>
    </section>

    <section class="toolbar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加就诊人
      </el-button>
    </section>

    <section class="list-panel" v-loading="loading">
      <div class="panel-head">
        <h2>就诊人列表</h2>
        <span>{{ patientCount }} 条记录</span>
      </div>

      <el-empty v-if="patientDOS.length === 0" description="暂无就诊人，请先添加">
        <el-button type="primary" @click="showAddDialog">立即添加</el-button>
      </el-empty>

      <div v-else class="patient-grid">
        <article
          v-for="patientDO in patientDOS"
          :key="patientDO.id"
          class="patient-card"
        >
          <header class="patient-head">
            <div class="identity-wrap">
              <el-avatar :size="58" :icon="UserFilled" />
              <div class="patient-main">
                <h3>{{ patientDO.fullName }}</h3>
                <p>{{ getGenderText(patientDO.gender) }} · 常用就诊档案</p>
              </div>
            </div>
            <el-tag v-if="patientDO.isDefault" type="success" size="small" effect="dark">默认</el-tag>
          </header>

          <div class="patient-info-grid">
            <div class="info-item">
              <label>身份证</label>
              <span>{{ maskIdCard(patientDO.idCard) }}</span>
            </div>
            <div class="info-item">
              <label>手机号</label>
              <span>{{ patientDO.phone || '暂无' }}</span>
            </div>
            <div class="info-item">
              <label>出生日期</label>
              <span>{{ patientDO.birthDate || '暂无' }}</span>
            </div>
          </div>

          <div class="patient-actions">
            <div class="action-left">
              <el-button size="small" @click="showEditDialog(patientDO)">编辑</el-button>
              <el-button
                v-if="!patientDO.isDefault"
                size="small"
                type="success"
                @click="setAsDefault(patientDO)"
              >
                设为默认
              </el-button>
            </div>
            <el-button size="small" type="danger" plain @click="handleDelete(patientDO)">删除</el-button>
          </div>
        </article>
      </div>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑就诊人' : '添加就诊人'"
      width="680px"
      class="patient-form-dialog"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="patient-form"
      >
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="fullName">
              <el-input v-model="form.fullName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择出生日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" class="default-col">
            <el-form-item label="默认设置">
              <el-checkbox v-model="form.isDefault">设为默认就诊人</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UserFilled } from '@element-plus/icons-vue'
import { getPatients, addPatient, updatePatient, deletePatient, type PatientFormData } from '@/api/patient'

const router = useRouter()

const patientDOS = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref<number | null>(null)

const form = reactive<PatientFormData>({
  fullName: '',
  idCard: '',
  gender: 'MALE',
  phone: '',
  birthDate: '',
  isDefault: false
})

const rules = {
  fullName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

const patientCount = computed(() => patientDOS.value.length)

const defaultPatientName = computed(() => {
  const defaultPatient = patientDOS.value.find((patient) => patient.isDefault)
  return defaultPatient?.fullName || '未设置'
})

const fetchPatients = async () => {
  loading.value = true
  try {
    const res = await getPatients()
    patientDOS.value = Array.isArray(res) ? res : []
  } catch (error) {
    ElMessage.error('获取就诊人列表失败')
  } finally {
    loading.value = false
  }
}

const getGenderText = (gender: string) => {
  if (gender === 'MALE') return '男'
  if (gender === 'FEMALE') return '女'
  return '其他'
}

const maskIdCard = (idCard: string) => {
  if (!idCard || idCard.length < 8) return idCard
  return `${idCard.substring(0, 4)}********${idCard.substring(idCard.length - 4)}`
}

const resetForm = () => {
  form.fullName = ''
  form.idCard = ''
  form.gender = 'MALE'
  form.phone = ''
  form.birthDate = ''
  form.isDefault = false
  currentId.value = null
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (patientDO: any) => {
  isEdit.value = true
  currentId.value = patientDO.id
  form.fullName = patientDO.fullName
  form.idCard = patientDO.idCard
  form.gender = patientDO.gender === 'FEMALE' ? 'FEMALE' : 'MALE'
  form.phone = patientDO.phone || ''
  form.birthDate = patientDO.birthDate || ''
  form.isDefault = !!patientDO.isDefault
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value && currentId.value) {
        await updatePatient(currentId.value, form)
        ElMessage.success('更新成功')
      } else {
        await addPatient(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchPatients()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
    } finally {
      submitting.value = false
    }
  })
}

const setAsDefault = async (patientDO: any) => {
  try {
    await updatePatient(patientDO.id, { ...patientDO, isDefault: true })
    ElMessage.success('设置成功')
    fetchPatients()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const handleDelete = (patientDO: any) => {
  ElMessageBox.confirm(`确定要删除就诊人 "${patientDO.fullName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePatient(patientDO.id)
      ElMessage.success('删除成功')
      fetchPatients()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchPatients()
})
</script>

<style scoped>
.patients-page {
  max-width: 1100px;
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

.toolbar,
.list-panel {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
}

.toolbar {
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.toolbar-tip {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.list-panel {
  padding: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 21px;
}

.panel-head span {
  color: #64748b;
  font-size: 13px;
}

.patient-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.patient-card {
  border-radius: 14px;
  border: 1px solid #cfe1ff;
  background: #fff;
  padding: 14px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.patient-card:hover {
  transform: translateY(-2px);
  border-color: #93c5fd;
  box-shadow: 0 14px 24px rgba(37, 99, 235, 0.12);
}

.patient-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}

.identity-wrap {
  display: flex;
  gap: 10px;
  align-items: center;
}

.patient-main h3 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 18px;
}

.patient-main p {
  margin: 0;
  color: #475569;
  font-size: 12px;
}

.patient-info-grid {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.info-item {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.info-item label {
  color: #64748b;
  font-size: 12px;
}

.info-item span {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.action-left {
  display: flex;
  gap: 8px;
  flex: 1;
}

.action-left :deep(.el-button) {
  min-width: 88px;
}

.patient-actions :deep(.el-button) {
  margin: 0;
}

.patient-form {
  padding-top: 4px;
}

.default-col {
  display: flex;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 980px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .patient-grid {
    grid-template-columns: 1fr;
  }

  .patient-info-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .hero-main h1 {
    font-size: 26px;
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .patient-info-grid {
    grid-template-columns: 1fr;
  }

  .patient-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-left {
    width: 100%;
  }

  .action-left :deep(.el-button),
  .patient-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
