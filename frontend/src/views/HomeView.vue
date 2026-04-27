<template>
  <section class="home-page">
    <div class="hero">
      <p class="eyebrow">Online Registration Service</p>
      <h1>预约挂号服务入口</h1>
      <p class="summary">
        欢迎使用在线预约挂号系统，请先选择服务入口。
      </p>
    </div>

    <div class="entry-grid">
      <article class="entry-card appointment">
        <h2>预约挂号</h2>
        <p>进入科室与医生列表，按流程完成预约。</p>
        <el-button type="primary" size="large" @click="goToAppointment">进入预约</el-button>
      </article>

      <article class="entry-card login">
        <h2>用户登录</h2>
        <p>登录后可管理就诊人、查看预约记录与个人信息。</p>
        <el-button plain size="large" @click="goToLogin">去登录</el-button>
      </article>
    </div>

    <div class="tips">
      <p>温馨提示：首次使用请先注册账号，再进行预约挂号。</p>
    </div>

    <section class="notice-board">
      <article class="notice-panel">
        <div class="panel-header">
          <h3>医院公告</h3>
          <el-link type="primary" :underline="false" @click="goToAppointment">更多公告</el-link>
        </div>

        <div class="notice-scroll" v-loading="noticeLoading">
          <ul class="notice-list" v-if="notices.length > 0">
            <li v-for="item in notices" :key="item.id" class="notice-item">
              <div class="notice-meta">
                <div class="notice-tags">
                  <el-tag size="small" :type="item.tagType">{{ item.tag }}</el-tag>
                  <el-tag v-if="item.pinned" size="small" type="danger" effect="plain">置顶</el-tag>
                </div>
                <span class="notice-date">{{ item.publishDate }}</span>
              </div>
              <el-link type="default" :underline="false" @click="openNotice(item.path || '/departments')">
                {{ item.title }}
              </el-link>
              <p class="notice-summary" v-if="item.summary">{{ item.summary }}</p>
            </li>
          </ul>
          <el-empty v-else description="暂无公告" :image-size="60" />
        </div>
        <div class="notice-pagination">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="noticeTotal"
            :page-size="noticePageSize"
            :current-page="noticePage"
            @current-change="handleNoticePageChange"
          />
        </div>
      </article>

      <article class="carousel-panel">
        <div class="panel-header">
          <h3>服务导览</h3>
        </div>
        <el-carousel :interval="5000" indicator-position="outside" height="260px" arrow="always">
          <el-carousel-item v-for="item in banners" :key="item.title">
            <div class="banner-item" :class="item.className">
              <div class="banner-content">
                <p class="banner-kicker">{{ item.kicker }}</p>
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticePage, type NoticeItem } from '@/api/notice'

const router = useRouter()

const goToAppointment = () => {
  router.push('/departments')
}

const goToLogin = () => {
  router.push('/login')
}

const openNotice = (path: string) => {
  router.push(path)
}

const noticeLoading = ref(false)
const notices = ref<NoticeItem[]>([])
const noticePage = ref(1)
const noticePageSize = ref(4)
const noticeTotal = ref(0)

const fallbackNotices: NoticeItem[] = [
  {
    id: 1001,
    tag: '公告',
    tagType: 'primary',
    publishDate: '2026-04-27',
    title: '五一假期门诊排班已发布，请提前预约就诊时段。',
    summary: '节假日期间部分门诊时段有调整，请以预约页面实时号源为准。',
    pinned: true,
    path: '/departments'
  },
  {
    id: 1002,
    tag: '通知',
    tagType: 'success',
    publishDate: '2026-04-25',
    title: '线上预约支持“我的预约”内改签与取消操作。',
    summary: '已支持在就诊前进行预约取消与状态查询。',
    pinned: true,
    path: '/appointments'
  },
  {
    id: 1003,
    tag: '提醒',
    tagType: 'warning',
    publishDate: '2026-04-24',
    title: '首次挂号请先完善就诊人实名信息并绑定手机号。',
    summary: '为保障就诊准确核验，请在预约前补全实名信息。',
    pinned: false,
    path: '/patients'
  },
  {
    id: 1004,
    tag: '就医',
    tagType: 'info',
    publishDate: '2026-04-22',
    title: '请按预约时间提前15分钟到院签到，避免过号。',
    summary: '建议预留交通时间，错峰就诊。',
    pinned: false,
    path: '/departments'
  }
]

const loadNotices = async (page = noticePage.value) => {
  noticeLoading.value = true
  try {
    const res = await getNoticePage(page, noticePageSize.value)
    noticePage.value = res.page || page
    noticePageSize.value = res.size || noticePageSize.value
    noticeTotal.value = res.total || 0
    notices.value = res.records || []
  } catch (error) {
    notices.value = fallbackNotices
    noticeTotal.value = fallbackNotices.length
  } finally {
    noticeLoading.value = false
  }
}

const handleNoticePageChange = (page: number) => {
  noticePage.value = page
  loadNotices(page)
}

onMounted(() => {
  loadNotices(1)
})

const banners = [
  {
    className: 'banner-a',
    kicker: '01 / 流程引导',
    title: '三步完成在线挂号',
    desc: '登录后选择科室、医生与时段，即可提交预约。'
  },
  {
    className: 'banner-b',
    kicker: '02 / 就诊提醒',
    title: '预约记录一键查询',
    desc: '可在我的预约中查看状态、取消预约及后续就诊信息。'
  },
  {
    className: 'banner-c',
    kicker: '03 / 服务提示',
    title: '就诊人信息请如实填写',
    desc: '姓名、身份证与手机号信息会用于到院核验。'
  }
]
</script>

<style scoped>
.home-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 24px 0;
  position: relative;
}

.home-page::before,
.home-page::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  z-index: 0;
}

.home-page::before {
  width: 380px;
  height: 380px;
  top: -80px;
  right: -120px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, rgba(59, 130, 246, 0) 70%);
}

.home-page::after {
  width: 300px;
  height: 300px;
  bottom: 50px;
  left: -120px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.18) 0%, rgba(14, 165, 233, 0) 70%);
}

.hero {
  padding: 32px;
  border-radius: 20px;
  background: linear-gradient(125deg, rgba(219, 234, 254, 0.95) 0%, rgba(224, 242, 254, 0.95) 55%, rgba(236, 254, 255, 0.95) 100%);
  border: 1px solid #bfdbfe;
  box-shadow: 0 18px 40px rgba(30, 64, 175, 0.08);
  position: relative;
  z-index: 1;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0f766e;
  margin-bottom: 8px;
}

.hero h1 {
  font-size: 34px;
  line-height: 1.2;
  color: #0f172a;
  margin-bottom: 10px;
}

.summary {
  color: #334155;
  font-size: 15px;
}

.entry-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  position: relative;
  z-index: 1;
}

.entry-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #dbeafe;
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.08);
}

.entry-card h2 {
  color: #0f172a;
  margin-bottom: 8px;
}

.entry-card p {
  color: #475569;
  margin-bottom: 18px;
}

.appointment {
  border-top: 4px solid #0ea5e9;
}

.login {
  border-top: 4px solid #22c55e;
}

.tips {
  margin-top: 16px;
  background: rgba(239, 246, 255, 0.96);
  border: 1px solid #bfdbfe;
  color: #1e3a8a;
  border-radius: 12px;
  padding: 12px 16px;
  position: relative;
  z-index: 1;
}

.notice-board {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1.1fr 1.4fr;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.notice-panel,
.carousel-panel {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.07);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-scroll {
  max-height: 285px;
  overflow-y: auto;
  padding-right: 4px;
}

.notice-item {
  padding: 10px 6px;
  border-bottom: 1px dashed #dbeafe;
}

.notice-item:last-child {
  border-bottom: 0;
}

.notice-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notice-tags {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notice-date {
  font-size: 12px;
  color: #64748b;
}

.notice-summary {
  margin: 8px 0 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.45;
}

.notice-pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.banner-item {
  height: 260px;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(191, 219, 254, 0.9);
}

.banner-item::after {
  content: '';
  position: absolute;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  right: -70px;
  top: -80px;
  background: rgba(255, 255, 255, 0.28);
}

.banner-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 26px;
  color: #0b2547;
}

.banner-kicker {
  font-size: 12px;
  letter-spacing: 0.06em;
  color: rgba(11, 37, 71, 0.75);
  margin-bottom: 8px;
}

.banner-content h4 {
  margin: 0 0 8px;
  font-size: 28px;
  line-height: 1.2;
}

.banner-content p {
  margin: 0;
  max-width: 560px;
  font-size: 15px;
  color: rgba(15, 23, 42, 0.82);
}

.banner-a {
  background: linear-gradient(120deg, #dbeafe 0%, #bfdbfe 48%, #e0f2fe 100%);
}

.banner-b {
  background: linear-gradient(120deg, #e0f2fe 0%, #bae6fd 50%, #dbeafe 100%);
}

.banner-c {
  background: linear-gradient(120deg, #e6f4ff 0%, #cfe8ff 52%, #d8f3ff 100%);
}

@media (max-width: 900px) {
  .notice-board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 20px;
  }

  .hero h1 {
    font-size: 28px;
  }

  .entry-grid {
    grid-template-columns: 1fr;
  }

  .banner-item {
    height: 220px;
  }

  .banner-content {
    padding: 20px;
  }

  .banner-content h4 {
    font-size: 22px;
  }
}
</style>
