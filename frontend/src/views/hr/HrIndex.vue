<script setup>
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic } from 'element-plus'
import request from '@/utils/request'

const stats = ref({
  jobCount: 0,
  deliverCount: 0,
  interviewCount: 0,
  auditStatus: -1
})

const fetchStats = async () => {
  const [jobs, delivers, interviews, auditStatus] = await Promise.all([
    request.get('/hr/job/page', { params: { page: 1, pageSize: 1 } }),
    request.get('/hr/deliver/page', { params: { page: 1, pageSize: 1 } }),
    request.get('/hr/interview/page', { params: { page: 1, pageSize: 1 } }),
    request.get('/hr/audit/status')
  ])
  stats.value.jobCount = jobs.total || 0
  stats.value.deliverCount = delivers.total || 0
  stats.value.interviewCount = interviews.total || 0
  stats.value.auditStatus = auditStatus.auditStatus || -1
}

const getAuditStatusText = () => {
  switch (stats.value.auditStatus) {
    case -1: return '未提交认证'
    case 0: return '审核中'
    case 1: return '已通过'
    case 2: return '已驳回'
    default: return '未知'
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="hr-index">
    <ElRow :gutter="20">
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="发布岗位数" :value="stats.jobCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="收到简历数" :value="stats.deliverCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="面试通知数" :value="stats.interviewCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <ElStatistic title="认证状态" :value="getAuditStatusText()" />
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.hr-index {
  padding: 20px 0;
}
</style>