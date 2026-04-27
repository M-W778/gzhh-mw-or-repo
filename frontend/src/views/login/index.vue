<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <section class="hero-panel">
        <div class="hero-brand">
          <div class="hero-logo">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <span>智航科技大学附属第二医院预约挂号系统</span>
        </div>

        <div class="hero-copy">
          <h1>欢迎使用在线挂号平台</h1>
          <p>
            支持患者账号注册、登录、就诊人管理与预约挂号流程。请先登录或快速注册后开始使用。
          </p>
        </div>

        <div class="hero-points">
          <div class="point-item">实时查看科室与医生排班信息</div>
          <div class="point-item">统一管理就诊人档案与预约记录</div>
          <div class="point-item">预约确认后可一键发送通知提醒</div>
        </div>

        <div class="hero-switch">
          <button
            type="button"
            class="switch-btn"
            :class="{ active: currentAction === 'login' }"
            @click="switchAction('login')"
          >
            账号登录
          </button>
          <button
            type="button"
            class="switch-btn"
            :class="{ active: currentAction === 'register' }"
            @click="switchAction('register')"
          >
            快速注册
          </button>
        </div>
      </section>

      <section class="form-panel">
        <div class="panel-badge">
          {{ currentAction === 'login' ? 'WELCOME BACK' : 'CREATE ACCOUNT' }}
        </div>
        <h2 class="panel-title">
          {{ currentAction === 'login' ? '登录账户' : '注册账户' }}
        </h2>

        <el-form
          v-if="currentAction === 'login'"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <div class="action-row">
            <span class="helper-text">登录后可进入预约挂号主流程</span>
            <el-button type="primary" size="large" :loading="loginLoading" @click="handleLogin">
              登录
            </el-button>
          </div>
        </el-form>

        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20个字符）"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              :prefix-icon="Phone"
              size="large"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20个字符）"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <div class="action-row">
            <span class="helper-text">注册成功后自动切换到登录表单</span>
            <el-button type="primary" size="large" :loading="registerLoading" @click="handleRegister">
              注册
            </el-button>
          </div>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, OfficeBuilding, Phone, User } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth.ts'
import { useUserStore } from '@/stores/user.ts'

type ActionMode = 'login' | 'register'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentAction = ref<ActionMode>('login')

const loginFormRef = ref()
const registerFormRef = ref()

const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const switchAction = (mode: ActionMode) => {
  currentAction.value = mode
  const targetPath = mode === 'register' ? '/register' : '/login'
  if (route.path !== targetPath) {
    router.replace(targetPath)
  }
}

const normalizeRoles = (roles: unknown) => {
  if (!Array.isArray(roles)) {
    return []
  }
  return roles.map((item) => String(item || '').toUpperCase())
}

const hasRole = (roles: string[], role: string) => {
  const upperRole = role.toUpperCase()
  return roles.includes(upperRole) || roles.includes(`ROLE_${upperRole}`)
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true
    const res = await login(loginForm)
    const token = res.accessToken || res.token
    const userId = res.userId || res.id
    const roles = normalizeRoles(res.roles)

    if (!token) {
      ElMessage.error('登录响应缺少 token')
      return
    }

    userStore.setToken(token)
    userStore.setUserInfo({
      id: userId,
      username: res.username,
      phone: res.phone,
      roles
    })
    ElMessage.success('登录成功')

    if (hasRole(roles, 'DOCTOR')) {
      router.push('/doctor/portal')
      return
    }
    if (hasRole(roles, 'ADMIN')) {
      router.push('/admin/statistics')
      return
    }
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    await register({
      username: registerForm.username,
      password: registerForm.password,
      checkPassword: registerForm.confirmPassword,
      phone: registerForm.phone,
      email: registerForm.email
    })
    ElMessage.success('注册成功，请登录')
    currentAction.value = 'login'
    if (route.path !== '/login') {
      router.replace('/login')
    }
    loginForm.username = registerForm.username
    loginForm.password = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
  } finally {
    registerLoading.value = false
  }
}

watch(
  () => route.path,
  (path) => {
    if (path === '/register') {
      currentAction.value = 'register'
      return
    }
    currentAction.value = 'login'
  },
  { immediate: true }
)
</script>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  padding: 104px 24px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top left, rgba(72, 148, 235, 0.24), transparent 32%),
    radial-gradient(circle at bottom right, rgba(18, 101, 191, 0.14), transparent 28%),
    linear-gradient(135deg, #f7fbff 0%, #eaf2fb 100%);
}

.auth-card {
  width: min(1120px, 100%);
  min-height: 680px;
  border-radius: 22px;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(320px, 1.05fr) minmax(360px, 0.95fr);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(16, 73, 134, 0.14);
  border: 1px solid rgba(215, 230, 247, 0.9);
}

.hero-panel {
  padding: 42px 42px 36px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  background:
    linear-gradient(145deg, rgba(9, 79, 152, 0.96) 0%, rgba(27, 121, 214, 0.92) 100%),
    radial-gradient(circle at 20% 10%, rgba(255, 255, 255, 0.16), transparent 34%),
    radial-gradient(circle at 86% 100%, rgba(255, 255, 255, 0.12), transparent 30%);
}

.hero-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  letter-spacing: 0.04em;
}

.hero-logo {
  width: 48px;
  height: 48px;
  border-radius: 11px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 26px;
}

.hero-copy {
  margin-top: 40px;
}

.hero-copy h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.15;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.hero-copy p {
  margin: 18px 0 0;
  max-width: 420px;
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.78);
}

.hero-points {
  margin-top: 26px;
  display: grid;
  gap: 12px;
}

.point-item {
  padding: 14px 16px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.hero-switch {
  margin-top: 28px;
  display: flex;
  gap: 12px;
}

.switch-btn {
  flex: 1;
  height: 44px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  transition: all 0.2s ease;
}

.switch-btn.active {
  color: #0b5cac;
  background: #fff;
  border-color: transparent;
}

.form-panel {
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.96);
}

.panel-badge {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  color: #166fc9;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: #edf5ff;
}

.panel-title {
  margin: 18px 0 20px;
  font-size: 30px;
  line-height: 1.2;
  color: #1f2d3d;
}

.action-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.helper-text {
  color: #7a8ca3;
  font-size: 13px;
}

.action-row :deep(.el-button) {
  min-width: 120px;
}

@media (max-width: 960px) {
  .auth-wrapper {
    padding: 96px 16px 24px;
  }

  .auth-card {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .hero-panel,
  .form-panel {
    padding: 28px 22px;
  }

  .hero-copy h1 {
    font-size: 30px;
  }

  .action-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .action-row :deep(.el-button) {
    width: 100%;
  }
}
</style>
