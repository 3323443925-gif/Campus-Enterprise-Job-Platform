<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElDatePicker, ElMessage, ElSelect, ElOption, ElPagination } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const form = reactive({
  deliverId: null,
  jobId: null,
  studentUserId: null,
  studentName: '',
  jobName: '',
  interviewTime: '',
  interviewAddress: '',
  interviewContent: ''
})

const jobOptions = ref([])
const selectedJobId = ref(null)

const fetchJobs = async () => {
  const data = await request.get('/hr/job/page', { params: { page: 1, pageSize: 100 } })
  jobOptions.value = data.list || []
}

const fetchDelivers = async () => {
  const params = {
    page: pageInfo.page,
    pageSize: pageInfo.pageSize,
    deliverStatus: 1
  }
  if (selectedJobId.value) {
    params.jobId = selectedJobId.value
  }
  const data = await request.get('/hr/deliver/page', { params })
  tableData.value = data.list
  pageInfo.total = data.total
}

const openSendDialog = (row) => {
  Object.assign(form, {
    deliverId: row.id,
    jobId: row.jobId,
    studentUserId: row.studentUserId,
    studentName: row.studentName || '',
    jobName: row.jobName || '',
    interviewTime: '',
    interviewAddress: '',
    interviewContent: ''
  })
  dialogVisible.value = true
}

const handleSend = async () => {
  if (!form.interviewTime) {
    ElMessage.warning('请选择面试时间')
    return
  }
  if (!form.interviewAddress) {
    ElMessage.warning('请填写面试地点')
    return
  }

  const sendData = {
    deliverId: form.deliverId,
    interviewTime: form.interviewTime,
    interviewAddress: form.interviewAddress,
    interviewContent: form.interviewContent
  }

  await request.post('/hr/interview/send', sendData)
  ElMessage.success('面试通知发送成功')
  dialogVisible.value = false
  fetchDelivers()
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchDelivers()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchDelivers()
}

const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '待面试'
    case 2: return '面试完成'
    case 3: return '不合适'
    case 4: return '已录用'
    case 5: return '已报到'
    default: return '未知'
  }
}

onMounted(() => {
  fetchJobs()
  fetchDelivers()
})
</script>

<template>
  <div class="send-interview">
    <div class="search-bar">
      <ElSelect v-model="selectedJobId" placeholder="请选择岗位" style="width: 250px;" clearable @change="() => { pageInfo.page = 1; fetchDelivers() }">
        <ElOption v-for="job in jobOptions" :key="job.id" :label="job.jobName" :value="job.id" />
      </ElSelect>
    </div>

    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="投递ID" width="100" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="studentName" label="学生姓名" />
      <ElTableColumn prop="studentNo" label="学号" />
      <ElTableColumn prop="majorName" label="专业" />
      <ElTableColumn prop="deliverStatus" label="状态" width="100">
        <template #default="{ row }">
          <span>{{ getStatusLabel(row.deliverStatus) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="deliverTime" label="投递时间" width="180" />
      <ElTableColumn label="操作" width="150">
        <template #default="{ row }">
          <ElButton size="small" type="primary" @click="openSendDialog(row)">发布面试</ElButton>
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

    <ElDialog v-model="dialogVisible" title="发布面试通知" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="学生姓名">
          <ElInput :value="form.studentName" disabled />
        </ElFormItem>
        <ElFormItem label="岗位名称">
          <ElInput :value="form.jobName" disabled />
        </ElFormItem>
        <ElFormItem label="面试时间" required>
          <ElDatePicker v-model="form.interviewTime" type="datetime" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem label="面试地点" required>
          <ElInput v-model="form.interviewAddress" placeholder="请填写面试地点或线上链接" />
        </ElFormItem>
        <ElFormItem label="面试须知">
          <ElInput v-model="form.interviewContent" type="textarea" :rows="4" placeholder="请填写面试须知、注意事项等" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSend">发布面试通知</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.send-interview {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>