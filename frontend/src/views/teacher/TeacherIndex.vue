<script setup>
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic, ElButton } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const stats = ref({
  jobCount: 0,
  deliverCount: 0,
  employedCount: 0
})

const fetchStats = async () => {
  const [jobs, delivers] = await Promise.all([
    request.get('/teacher/job/page', { params: { page: 1, pageSize: 1 } }),
    request.get('/teacher/deliver/page', { params: { page: 1, pageSize: 1 } })
  ])
  stats.value.jobCount = jobs.total || 0
  stats.value.deliverCount = delivers.total || 0
  stats.value.employedCount = (delivers.list || []).filter(d => d.deliverStatus === 4).length
}

const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="teacher-index">
    <ElRow :gutter="20" style="margin-bottom: 30px">
      <ElCol :span="8">
        <ElCard>
          <ElStatistic title="发布岗位总数" :value="stats.jobCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard>
          <ElStatistic title="总投递数" :value="stats.deliverCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard>
          <ElStatistic title="录用人数" :value="stats.employedCount" />
        </ElCard>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="8">
        <ElCard class="action-card" @click="goTo('/teacher/jobs')">
          <div class="action-icon">📝</div>
          <h3>招聘管理</h3>
          <p>发布和管理招聘岗位</p>
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard class="action-card" @click="goTo('/teacher/deliver')">
          <div class="action-icon">📋</div>
          <h3>投递管理</h3>
          <p>查看学生投递记录</p>
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard class="action-card" @click="goTo('/teacher/enterprise')">
          <div class="action-icon">🏢</div>
          <h3>企业管理</h3>
          <p>管理合作企业档案</p>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.teacher-index {
  padding: 20px 0;
}

.action-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: translateY(-5px);
}

.action-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.action-card h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #303133;
}

.action-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
</style>