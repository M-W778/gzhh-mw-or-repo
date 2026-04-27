<template>
  <div class="departments-page">
    <section class="hero">
      <div class="hero-main">
        <p class="hero-kicker">SMART TRIAGE CENTER</p>
        <h1>选择就诊科室</h1>
        <p class="hero-desc">
          支持按科室名称快速检索，也可直接使用下方导航卡片进入对应科室。
        </p>
      </div>
      <div class="hero-side">
        <div class="stat-card">
          <span class="label">科室总数</span>
          <strong>{{ viewDepartmentList.length }}</strong>
        </div>
        <div class="stat-card">
          <span class="label">当前日期</span>
          <strong>{{ todayText }}</strong>
        </div>
      </div>
    </section>

    <section class="nav-section" v-loading="loading && viewDepartmentList.length === 0">
      <div class="section-header">
        <h2>科室导航</h2>
      </div>
      <div class="nav-grid" v-if="viewDepartmentList.length > 0">
        <button
          v-for="(dept, index) in viewDepartmentList"
          :key="dept.id"
          type="button"
          class="nav-card"
          :class="getCardTheme(index)"
          @click="goToDoctors(dept.id)"
        >
          <span class="nav-icon">
            <el-icon><component :is="getDepartmentIcon(dept.deptName, index)" /></el-icon>
          </span>
          <span class="nav-meta">
            <strong>{{ dept.deptName }}</strong>
            <small>进入{{ dept.deptName }}挂号</small>
          </span>
        </button>
      </div>
      <el-empty
        v-else
        description="暂无科室数据"
        :image-size="90"
      />
    </section>

    <section class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入科室名称进行筛选"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClear"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询科室</el-button>
      <el-button @click="handleClear">重置</el-button>
      <p class="toolbar-tip">共 {{ viewDepartmentList.length }} 个科室</p>
    </section>

    <section class="department-section" v-loading="loading">
      <div class="section-header">
        <h2>科室列表</h2>
        <p>请根据就诊需求选择科室，随后查看医生排班并完成预约。</p>
      </div>

      <div class="department-grid" v-if="viewDepartmentList.length > 0">
        <article
          v-for="(dept, index) in viewDepartmentList"
          :key="dept.id"
          class="department-card"
          :class="getCardTheme(index)"
          @click="goToDoctors(dept.id)"
        >
          <div class="card-top">
            <div class="card-title">
              <span class="icon-wrap">
                <el-icon><component :is="getDepartmentIcon(dept.deptName, index)" /></el-icon>
              </span>
              <div>
                <h3>{{ dept.deptName }}</h3>
                <p class="card-subtitle">导航标注：{{ dept.deptName }}</p>
              </div>
            </div>
            <el-icon class="go-icon"><ArrowRightBold /></el-icon>
          </div>
          <p class="card-desc">
            {{ dept.description || '该科室暂无简介，点击查看医生与排班信息。' }}
          </p>
          <div class="card-footer">
            <el-tag size="small" type="info" effect="plain">科室名称: {{ dept.deptName }}</el-tag>
            <span class="enter-text">进入挂号</span>
          </div>
        </article>
      </div>

      <el-empty
        v-else
        description="未找到相关科室，请更换关键词重试"
        :image-size="96"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRightBold,
  CollectionTag,
  Female,
  FirstAidKit,
  Management,
  Notebook,
  Opportunity,
  Postcard,
  School,
  Search,
  Service,
  Suitcase,
  View
} from '@element-plus/icons-vue'
import { getDepartments, searchDepartments } from '@/api/department'

const router = useRouter()
const departmentList = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')

const todayText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
})

const fetchDepartments = async () => {
  loading.value = true
  try {
    const res = await getDepartments()
    departmentList.value = res || []
  } catch (error) {
    ElMessage.error('获取科室列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    fetchDepartments()
    return
  }
  loading.value = true
  try {
    const res = await searchDepartments(keyword)
    departmentList.value = res || []
  } catch (error) {
    ElMessage.error('科室搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleClear = () => {
  searchKeyword.value = ''
  fetchDepartments()
}

const getCardTheme = (index: number) => {
  const themes = ['theme-a', 'theme-b', 'theme-c', 'theme-d']
  return themes[index % themes.length]
}

const pickString = (...values: unknown[]) => {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }
  return ''
}

const normalizeDepartment = (dept: any) => {
  const deptId = Number(
    dept?.id ??
    dept?.deptId ??
    dept?.departmentId
  )
  const deptName = pickString(
    dept?.dept_name,
    dept?.deptName,
    dept?.name,
    dept?.departmentName,
    dept?.label,
    dept?.title
  )
  const description = pickString(
    dept?.description,
    dept?.deptDescription,
    dept?.desc,
    dept?.introduction
  )
  return {
    id: Number.isFinite(deptId) ? deptId : 0,
    deptName: deptName || '未命名科室',
    description
  }
}

const viewDepartmentList = computed(() => {
  const source = Array.isArray(departmentList.value) ? departmentList.value : []
  return source.map(normalizeDepartment).filter(item => item.id > 0)
})

const fallbackIcons: Component[] = [
  FirstAidKit,
  Management,
  Service,
  View,
  Female,
  School,
  Notebook,
  Postcard,
  Opportunity,
  CollectionTag,
  Suitcase
]

const getDepartmentIcon = (departmentName: string, index: number): Component => {
  const name = (departmentName || '').toLowerCase()
  if (name.includes('内') || name.includes('心') || name.includes('呼吸')) {
    return FirstAidKit
  }
  if (name.includes('外') || name.includes('骨')) {
    return Management
  }
  if (name.includes('儿')) {
    return Female
  }
  if (name.includes('妇') || name.includes('产')) {
    return School
  }
  if (name.includes('眼')) {
    return View
  }
  if (name.includes('急诊')) {
    return Opportunity
  }
  if (name.includes('中医')) {
    return Notebook
  }
  if (name.includes('口腔')) {
    return CollectionTag
  }
  if (name.includes('体检')) {
    return Postcard
  }
  if (name.includes('康复')) {
    return Service
  }
  return fallbackIcons[index % fallbackIcons.length]
}

const goToDoctors = (departmentId: number) => {
  router.push(`/doctors/${departmentId}`)
}

onMounted(() => {
  fetchDepartments()
})
</script>

<style scoped>
.departments-page {
  max-width: 1220px;
  margin: 0 auto;
  padding-bottom: 10px;
}

.hero {
  display: grid;
  grid-template-columns: 1.45fr 1fr;
  gap: 16px;
  margin-bottom: 14px;
}

.hero-main,
.hero-side {
  border-radius: 16px;
  border: 1px solid #bfdbfe;
  background: linear-gradient(124deg, #eff6ff 0%, #dbeafe 62%, #e0f2fe 100%);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
}

.hero-main {
  padding: 24px;
}

.hero-kicker {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 12px;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.hero-main h1 {
  margin: 0 0 10px;
  font-size: 32px;
  line-height: 1.2;
  color: #0f172a;
}

.hero-desc {
  margin: 0;
  color: #334155;
  font-size: 15px;
}

.hero-side {
  padding: 14px;
  display: grid;
  grid-template-rows: 1fr 1fr;
  gap: 10px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card .label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-card strong {
  font-size: 22px;
  color: #1e3a8a;
}

.nav-section,
.department-section {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.96);
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-end;
  margin-bottom: 12px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.section-header p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.nav-card {
  text-align: left;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #fff;
  min-height: 84px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.nav-card:hover {
  transform: translateY(-2px);
  border-color: #93c5fd;
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.11);
}

.nav-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 21px;
}

.nav-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.nav-meta strong {
  font-size: 16px;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-meta small {
  color: #64748b;
  font-size: 12px;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 430px) auto auto 1fr;
  gap: 10px;
  align-items: center;
  margin: 14px 0;
  padding: 14px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.95);
}

.toolbar-tip {
  margin: 0;
  justify-self: end;
  color: #475569;
  font-size: 13px;
}

.department-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.department-card {
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: #fff;
  padding: 14px;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  min-height: 170px;
  display: flex;
  flex-direction: column;
}

.department-card:hover {
  transform: translateY(-3px);
  border-color: #93c5fd;
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.12);
}

.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  display: flex;
  gap: 10px;
}

.icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrap .el-icon {
  color: #fff;
  font-size: 25px;
}

.card-title h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.2;
}

.card-subtitle {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.go-icon {
  margin-top: 2px;
  font-size: 18px;
  color: #94a3b8;
}

.card-desc {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.enter-text {
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.theme-a .icon-wrap,
.theme-a .nav-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #0ea5e9 100%);
}

.theme-b .icon-wrap,
.theme-b .nav-icon {
  background: linear-gradient(135deg, #2563eb 0%, #38bdf8 100%);
}

.theme-c .icon-wrap,
.theme-c .nav-icon {
  background: linear-gradient(135deg, #1d4ed8 0%, #60a5fa 100%);
}

.theme-d .icon-wrap,
.theme-d .nav-icon {
  background: linear-gradient(135deg, #0369a1 0%, #0ea5e9 100%);
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .toolbar {
    grid-template-columns: 1fr 1fr;
  }

  .toolbar-tip {
    grid-column: 1 / -1;
    justify-self: start;
  }

  .department-grid {
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

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
