<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })

const detailDialogVisible = ref(false)
const detailData = ref({})

const fetchInterviews = async () => {
  const data = await request.get('/student/interview/page', {
    params: { page: pageInfo.page, pageSize: pageInfo.pageSize }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchInterviews()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchInterviews()
}

const handleDetail = (row) => {
  detailData.value = row
  detailDialogVisible.value = true
}

const handleAccept = async (id) => {
  await request.put(`/student/interview/${id}/accept`)
  ElMessage.success('已接受面试')
  fetchInterviews()
}

const handleReject = async (id) => {
  await request.put(`/student/interview/${id}/reject`)
  ElMessage.success('已拒绝面试')
  fetchInterviews()
}

const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '待确认'
    case 1: return '已接受'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-accept'
    case 2: return 'status-reject'
    default: return ''
  }
}

onMounted(() => {
  fetchInterviews()
})
</script>

<template>
  <div class="student-interview">
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="companyName" label="企业名称" />
      <ElTableColumn prop="interviewTime" label="面试时间" width="180" />
      <ElTableColumn prop="interviewAddress" label="面试地点" />
      <ElTableColumn prop="interviewStatus" label="状态" width="100">
        <template #default="{ row }">
          <span :class="getStatusClass(row.interviewStatus)">{{ getStatusLabel(row.interviewStatus) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createTime" label="通知时间" width="180" />
      <ElTableColumn label="操作" width="200">
        <template #default="{ row }">
          <ElButton size="small" @click="handleDetail(row)">详情</ElButton>
          <ElButton v-if="row.interviewStatus === 0" size="small" type="primary" @click="handleAccept(row.id)">接受</ElButton>
          <ElButton v-if="row.interviewStatus === 0" size="small" type="danger" @click="handleReject(row.id)">拒绝</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
    <div class="pagination">
      <ElPagination
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="pageInfo.page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageInfo.pageSize"
        :total="pageInfo.total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
    <ElDialog v-model="detailDialogVisible" title="面试通知详情" width="600px">
      <div v-if="detailData">
        <p><strong>岗位名称：</strong>{{ detailData.jobName }}</p>
        <p><strong>企业名称：</strong>{{ detailData.companyName }}</p>
        <p><strong>面试时间：</strong>{{ detailData.interviewTime }}</p>
        <p><strong>面试地点：</strong>{{ detailData.interviewAddress }}</p>
        <p><strong>面试须知：</strong>{{ detailData.interviewContent || '暂无' }}</p>
        <p><strong>当前状态：</strong><span :class="getStatusClass(detailData.interviewStatus)">{{ getStatusLabel(detailData.interviewStatus) }}</span></p>
        <p><strong>通知时间：</strong>{{ detailData.createTime }}</p>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
        <ElButton v-if="detailData.interviewStatus === 0" type="primary" @click="() => { handleAccept(detailData.id); detailDialogVisible = false }">接受面试</ElButton>
        <ElButton v-if="detailData.interviewStatus === 0" type="danger" @click="() => { handleReject(detailData.id); detailDialogVisible = false }">拒绝面试</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.student-interview {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.status-pending { color: #e6a23c; }
.status-accept { color: #67c23a; }
.status-reject { color: #f56c6c; }
</style>