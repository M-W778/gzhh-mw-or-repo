<template>
  <div class="profile-page">
    <section class="hero-panel">
      <div class="hero-main">
        <p class="hero-kicker">PERSONAL CENTER</p>
        <h1>个人中心</h1>
        <p class="hero-desc">查看账号信息并快速进入常用就诊功能。</p>
        <div class="hero-meta">
          <el-tag :type="roleTagType" size="large">{{ roleText }}</el-tag>
          <span>账号：{{ displayUsername }}</span>
        </div>
      </div>

      <div class="hero-stats">
        <article class="stat-card">
          <span>资料完整度</span>
          <strong>{{ profileCompletion }}%</strong>
        </article>
        <article class="stat-card">
          <span>登录状态</span>
          <strong>已登录</strong>
        </article>
      </div>
    </section>

    <section class="content-grid">
      <article class="profile-card">
        <div class="profile-header">
          <el-avatar :size="96" :icon="UserFilled" />
          <div class="profile-info">
            <h3>{{ displayUsername }}</h3>
            <p>欢迎回来</p>
          </div>
        </div>

        <div class="info-list">
          <div class="info-row">
            <label>用户名</label>
            <span>{{ displayUsername }}</span>
          </div>
          <div class="info-row">
            <label>手机号</label>
            <span>{{ displayPhone }}</span>
          </div>
        </div>
      </article>

      <article class="action-card">
        <h3>快捷操作</h3>
        <p>从这里快速进入高频功能页面。</p>

        <div class="action-list">
          <el-button type="primary" size="large" @click="goToPatients">管理就诊人</el-button>
          <el-button size="large" @click="goToAppointments">查看我的预约</el-button>
          <el-button size="large" @click="goHome">返回首页</el-button>
          <el-button type="danger" plain size="large" @click="handleLogout">退出登录</el-button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const displayUsername = computed(() => userStore.username || '未命名用户')

const displayPhone = computed(() => {
  const phone = String(userStore.userInfo?.phone || '').trim()
  if (!phone) {
    return '未绑定'
  }
  if (phone.length < 7) {
    return phone
  }
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
})

const roleText = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isDoctor) return '医生'
  if (userStore.isPatient) return '患者'
  return '用户'
})

const roleTagType = computed(() => {
  if (userStore.isAdmin) return 'danger'
  if (userStore.isDoctor) return 'warning'
  if (userStore.isPatient) return 'success'
  return 'info'
})

const profileCompletion = computed(() => {
  let completed = 0
  if (displayUsername.value && displayUsername.value !== '未命名用户') completed += 1
  if (displayPhone.value !== '未绑定') completed += 1
  return Math.round((completed / 2) * 100)
})

const goToPatients = () => {
  router.push('/patients')
}

const goToAppointments = () => {
  router.push('/appointments')
}

const goHome = () => {
  router.push('/')
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
</script>

<style scoped>
.profile-page {
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
.hero-stats {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(125deg, #eff6ff 0%, #dbeafe 62%, #e0f2fe 100%);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
}

.hero-main {
  padding: 16px 20px 20px;
}

.hero-kicker {
  margin: 0 0 6px;
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
  margin: 8px 0 12px;
  color: #334155;
  font-size: 14px;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-size: 13px;
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

.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
}

.profile-card,
.action-card {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.08);
  padding: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 14px;
}

.profile-info h3 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.profile-info p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.info-list {
  display: grid;
  gap: 10px;
}

.info-row {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.info-row label {
  color: #64748b;
  font-size: 13px;
}

.info-row span {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.action-card h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.action-card p {
  margin: 8px 0 14px;
  color: #64748b;
  font-size: 13px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: stretch;
}

.action-list :deep(.el-button) {
  width: 100%;
  margin: 0;
  justify-content: center;
}

@media (max-width: 980px) {
  .hero-panel,
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-main h1 {
    font-size: 26px;
  }

  .profile-header {
    align-items: flex-start;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
