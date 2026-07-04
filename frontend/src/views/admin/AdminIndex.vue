<script setup>
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic } from 'element-plus'
import request from '@/utils/request'
import EchartsBox from '@/components/EchartsBox.vue'

const overview = ref({})
const majorStats = ref([])

const fetchOverview = async () => {
  const data = await request.get('/stat/overview')
  overview.value = data
  majorStats.value = data.majorStats || []
}

const chartData = ref({
  xData: [],
  seriesData: []
})

onMounted(() => {
  fetchOverview()
})
</script>

<template>
  <div class="admin-index">
    <ElRow :gutter="20">
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="总学生数" :value="overview.totalStudents || 0" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="已就业" :value="overview.employedStudents || 0" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="就业率" :value="overview.employmentRate || 0" suffix="%" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="总岗位数" :value="overview.totalJobs || 0" />
        </ElCard>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20" style="margin-top: 20px">
      <ElCol :span="12">
        <ElCard title="分专业就业率">
          <EchartsBox 
            type="bar" 
            :data="{
              xData: majorStats.map(item => item.majorName),
              seriesData: majorStats.map(item => item.employmentRate)
            }"
          />
        </ElCard>
      </ElCol>
      <ElCol :span="12">
        <ElCard title="专业就业分布">
          <EchartsBox 
            type="pie" 
            :data="{
              seriesData: majorStats.map(item => ({
                name: item.majorName,
                value: item.employedStudents || 0
              }))
            }"
          />
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.admin-index {
  padding: 20px 0;
}
</style>