<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const overview = ref({})
const charts = {}

const initCharts = () => {
  const majorChartEl = document.getElementById('majorChart')
  const salaryChartEl = document.getElementById('salaryChart')
  const jobChartEl = document.getElementById('jobChart')
  const enterpriseChartEl = document.getElementById('enterpriseChart')
  if (majorChartEl) charts.majorChart = echarts.init(majorChartEl)
  if (salaryChartEl) charts.salaryChart = echarts.init(salaryChartEl)
  if (jobChartEl) charts.jobChart = echarts.init(jobChartEl)
  if (enterpriseChartEl) charts.enterpriseChart = echarts.init(enterpriseChartEl)
}

const fetchData = async () => {
  try {
    const data = await request.get('/stat/overview')
    overview.value = data
    renderCharts(data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const renderCharts = (data) => {
  if (data.majorStats && Array.isArray(data.majorStats)) {
    charts.majorChart.setOption({
      title: { text: '分专业就业率', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.majorStats.map(item => item.majorName || '未知专业'),
        axisLabel: { interval: 0, rotate: 30 }
      },
      yAxis: {
        type: 'value',
        axisLabel: { formatter: '{value}%' }
      },
      series: [{
        type: 'bar',
        data: data.majorStats.map(item => item.employmentRate || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#667eea' }
          ])
        }
      }]
    }, true)
  }

  if (data.salaryStats && Array.isArray(data.salaryStats)) {
    charts.salaryChart.setOption({
      title: { text: '薪资分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.salaryStats.map(item => ({
          name: item.name || '未知',
          value: item.value || 0
        }))
      }]
    }, true)
  }

  if (data.jobStats && Array.isArray(data.jobStats)) {
    charts.jobChart.setOption({
      title: { text: '热门岗位排行', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: data.jobStats.map(item => item.name || '未知岗位').reverse()
      },
      series: [{
        type: 'bar',
        data: data.jobStats.map(item => item.value || 0).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#85ce61' }
          ])
        }
      }]
    }, true)
  }

  if (data.enterpriseStats && Array.isArray(data.enterpriseStats)) {
    charts.enterpriseChart.setOption({
      title: { text: '企业行业分布', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.enterpriseStats.map(item => item.industry || '未知行业'),
        axisLabel: { rotate: 30 }
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        type: 'bar',
        data: data.enterpriseStats.map(item => item.count || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e6a23c' },
            { offset: 1, color: '#ebb563' }
          ])
        }
      }]
    }, true)
  }
}

const handleResize = () => {
  Object.values(charts).forEach(chart => chart && chart.resize())
}

onMounted(() => {
  initCharts()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(charts).forEach(chart => chart && chart.dispose())
})
</script>

<template>
  <div class="stat-screen">
    <div class="screen-header">
      <h1>就业数据大屏</h1>
    </div>
    <ElRow :gutter="20" style="margin-bottom: 20px">
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="总就业率" :value="overview.employmentRate || 0" suffix="%" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="毕业生总数" :value="overview.totalStudents || 0" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="已就业人数" :value="overview.employedStudents || 0" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="企业总数" :value="overview.totalEnterprises || 0" />
        </ElCard>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="12">
        <ElCard>
          <div id="majorChart" style="height: 350px"></div>
        </ElCard>
      </ElCol>
      <ElCol :span="12">
        <ElCard>
          <div id="salaryChart" style="height: 350px"></div>
        </ElCard>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20" style="margin-top: 20px">
      <ElCol :span="12">
        <ElCard>
          <div id="jobChart" style="height: 350px"></div>
        </ElCard>
      </ElCol>
      <ElCol :span="12">
        <ElCard>
          <div id="enterpriseChart" style="height: 350px"></div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.stat-screen {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.screen-header {
  text-align: center;
  margin-bottom: 30px;
}

.screen-header h1 {
  font-size: 32px;
  color: #303133;
}

:deep(.el-card) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>