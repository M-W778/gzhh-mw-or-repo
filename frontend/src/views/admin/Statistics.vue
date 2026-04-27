<template>
  <div class="statistics-container">
    <div class="page-header">
      <el-page-header @back="goBack" title="数据统计面板" />
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayAppointments || 0 }}</div>
          <div class="stat-label">今日预约</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayPending || 0 }}</div>
          <div class="stat-label">今日待就诊</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalPending || 0 }}</div>
          <div class="stat-label">总待就诊</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalCompleted || 0 }}</div>
          <div class="stat-label">总已完成</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalCancelled || 0 }}</div>
          <div class="stat-label">总已取消</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card total">
          <div class="stat-value">{{ totalAppointments || 0 }}</div>
          <div class="stat-label">预约总数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loading">
          <template #header>
            <span>最近7天预约趋势</span>
          </template>
          <div v-if="!hasDailyData" class="empty-chart">
            <el-empty description="暂无数据" />
          </div>
          <div v-else ref="dailyChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" v-loading="loading">
          <template #header>
            <span>热门科室TOP5（最近7天）</span>
          </template>
          <div v-if="!hasDepartmentData" class="empty-chart">
            <el-empty description="暂无数据" />
          </div>
          <div v-else ref="departmentChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getAppointmentStatistics, type AppointmentStatistics } from '@/api/statistics'

const router = useRouter()
const loading = ref(false)
const stats = ref<AppointmentStatistics>({
  todayAppointments: 0,
  todayPending: 0,
  totalPending: 0,
  totalCompleted: 0,
  totalCancelled: 0,
  topDepartments: [],
  dailyStats: []
})

const hasDailyData = computed(() => {
  return stats.value.dailyStats && stats.value.dailyStats.length > 0
})

const hasDepartmentData = computed(() => {
  return stats.value.topDepartments && stats.value.topDepartments.length > 0
})

const totalAppointments = computed(() => {
  return (stats.value.totalPending || 0) + (stats.value.totalCompleted || 0) + (stats.value.totalCancelled || 0)
})

const dailyChartRef = ref<HTMLElement>()
const departmentChartRef = ref<HTMLElement>()
let dailyChart: echarts.ECharts | null = null
let departmentChart: echarts.ECharts | null = null

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getAppointmentStatistics()
    console.log('Statistics data:', res)
    // 响应拦截器已经提取了 res.data，直接使用 res
    if (res) {
      stats.value = {
        todayAppointments: res.todayAppointments || 0,
        todayPending: res.todayPending || 0,
        totalPending: res.totalPending || 0,
        totalCompleted: res.totalCompleted || 0,
        totalCancelled: res.totalCancelled || 0,
        topDepartments: res.topDepartments || [],
        dailyStats: res.dailyStats || []
      }
      // 等待DOM更新后再初始化图表
      await nextTick()
      initCharts()
    }
  } catch (error: any) {
    console.error('Load statistics error:', error)
    ElMessage.error('加载统计数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  // 销毁旧图表实例
  if (dailyChart) {
    dailyChart.dispose()
    dailyChart = null
  }
  if (departmentChart) {
    departmentChart.dispose()
    departmentChart = null
  }

  // 初始化每日预约趋势图
  if (dailyChartRef.value && hasDailyData.value) {
    try {
      dailyChart = echarts.init(dailyChartRef.value)
      const dailyData = stats.value.dailyStats
      const dailyOption = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dailyData.map(item => item.date),
          axisLabel: {
            formatter: (value: string) => {
              if (!value) return ''
              const date = new Date(value)
              return `${date.getMonth() + 1}/${date.getDate()}`
            }
          }
        },
        yAxis: {
          type: 'value',
          minInterval: 1
        },
        series: [{
          data: dailyData.map(item => item.count || 0),
          type: 'line',
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ]
            }
          },
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }
      dailyChart.setOption(dailyOption)
    } catch (error) {
      console.error('Init daily chart error:', error)
    }
  }

  // 初始化热门科室图
  if (departmentChartRef.value && hasDepartmentData.value) {
    try {
      departmentChart = echarts.init(departmentChartRef.value)
      const deptData = stats.value.topDepartments
      const departmentOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          minInterval: 1
        },
        yAxis: {
          type: 'category',
          data: deptData.map(item => item.departmentName || '未知').reverse()
        },
        series: [{
          data: deptData.map(item => item.count || 0).reverse(),
          type: 'bar',
          itemStyle: {
            color: '#67C23A'
          }
        }]
      }
      departmentChart.setOption(departmentOption)
    } catch (error) {
      console.error('Init department chart error:', error)
    }
  }
}

const goBack = () => {
  router.push('/departments')
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', () => {
    dailyChart?.resize()
    departmentChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.statistics-container {
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
      
      .empty-chart {
        height: 300px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
