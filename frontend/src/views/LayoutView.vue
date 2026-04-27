<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <el-icon class="logo-icon"><OfficeBuilding /></el-icon>
        <span class="title">示例综合医院</span>
      </div>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown @command="handleCommand" popper-class="header-user-dropdown">
            <span class="user-trigger">
              <span class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </span>
              <span class="user-meta">
                <strong>{{ userStore.username }}</strong>
                <small>{{ currentRoleText }}</small>
              </span>
              <el-icon class="trigger-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">主页面</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isDoctor" command="doctorPortal">医生工作台</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isDoctor" command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isDoctor" command="patientDOS">就诊人管理</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isDoctor" command="appointmentDOS">我的预约</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="scheduleDOS">排班管理</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="statistics">数据统计</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="goToLogin">登录</el-button>
          <el-button @click="goToRegister">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">
      <p>2026 示例综合医院 在线预约挂号系统</p>
      <p>本系统为教学与演示用途，页面与数据仅用于功能展示。</p>
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const currentRoleText = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isDoctor) return '医生'
  if (userStore.isPatient) return '患者'
  return '用户'
})

const handleCommand = (command: string) => {
  switch (command) {
    case 'home':
      router.push('/')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'patientDOS':
      router.push('/patients')
      break
    case 'appointmentDOS':
      router.push('/appointments')
      break
    case 'scheduleDOS':
      router.push('/admin/schedules')
      break
    case 'statistics':
      router.push('/admin/statistics')
      break
    case 'doctorPortal':
      router.push('/doctor/portal')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  background: linear-gradient(180deg, #f3f9ff 0%, #eaf4ff 100%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 88px;
  padding: 0 40px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 38px;
  color: #409eff;
}

.title {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
  font-family: 'STXingkai', '华文行楷', 'KaiTi', cursive;
  letter-spacing: 0.02em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-trigger {
  min-width: 186px;
  height: 52px;
  padding: 0 12px 0 8px;
  border-radius: 999px;
  border: 1px solid #dbeafe;
  background: linear-gradient(120deg, #f8fbff 0%, #eef6ff 100%);
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.user-trigger:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.14);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: linear-gradient(135deg, #3b82f6 0%, #0ea5e9 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  color: #1f2937;
}

.user-meta strong {
  font-size: 14px;
  font-weight: 600;
}

.user-meta small {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.trigger-arrow {
  margin-left: auto;
  color: #64748b;
}

:deep(.header-user-dropdown.el-popper) {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.12);
  padding: 4px;
}

:deep(.header-user-dropdown .el-dropdown-menu) {
  padding: 4px;
}

:deep(.header-user-dropdown .el-dropdown-menu__item) {
  border-radius: 8px;
  min-width: 156px;
  margin: 2px 0;
}

:deep(.header-user-dropdown .el-dropdown-menu__item:not(.is-disabled):hover) {
  background: #eff6ff;
  color: #1d4ed8;
}

.main {
  background-color: #f5f7fa;
  padding: 20px 40px;
  min-height: calc(100vh - 170px);
}

.footer {
  background-color: #fff;
  text-align: center;
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
  padding: 14px 20px 18px;
  border-top: 1px solid #eef2f7;
}

.footer p {
  margin: 0;
}
</style>
