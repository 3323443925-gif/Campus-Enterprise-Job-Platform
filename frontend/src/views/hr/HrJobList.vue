<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage, ElDatePicker, ElSelect, ElOption } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const form = reactive({
  id: null,
  jobName: '',
  jobCategory: '',
  salaryMin: null,
  salaryMax: null,
  workAddress: '',
  educationRequire: '',
  jobRequire: '',
  jobDesc: '',
  recruitNum: null,
  deadline: ''
})

const jobCategories = ['技术类', '销售类', '运营类', '产品类', '设计类', '其他']

const fetchJobs = async () => {
  const data = await request.get('/hr/job/page', {
    params: { page: pageInfo.page, pageSize: pageInfo.pageSize }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const openAddDialog = () => {
  dialogTitle.value = '新增岗位'
  Object.assign(form, {
    id: null,
    jobName: '',
    jobCategory: '',
    salaryMin: null,
    salaryMax: null,
    workAddress: '',
    educationRequire: '',
    jobRequire: '',
    jobDesc: '',
    recruitNum: null,
    deadline: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑岗位'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.id) {
    await request.put('/hr/job', form)
    ElMessage.success('更新成功')
  } else {
    await request.post('/hr/job', form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchJobs()
}

const handlePublish = async (id) => {
  await request.put(`/hr/job/${id}/publish`)
  ElMessage.success('发布成功')
  fetchJobs()
}

const handlePause = async (id) => {
  await request.put(`/hr/job/${id}/pause`)
  ElMessage.success('已暂停')
  fetchJobs()
}

const handleClose = async (id) => {
  await request.put(`/hr/job/${id}/close`)
  ElMessage.success('已关闭')
  fetchJobs()
}

const handleDelete = async (id) => {
  await request.delete(`/hr/job/${id}`)
  ElMessage.success('删除成功')
  fetchJobs()
}

const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '草稿'
    case 1: return '已发布'
    case 2: return '暂停招聘'
    case 3: return '已关闭'
    default: return '未知'
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-draft'
    case 1: return 'status-published'
    case 2: return 'status-paused'
    case 3: return 'status-closed'
    default: return ''
  }
}

onMounted(() => {
  fetchJobs()
})
</script>

<template>
  <div class="hr-job-list">
    <div class="search-bar">
      <ElButton type="primary" @click="openAddDialog">新增岗位</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="jobCategory" label="岗位类别" />
      <ElTableColumn prop="salaryMin" label="薪资范围">
        <template #default="{ row }">{{ row.salaryMin }}-{{ row.salaryMax }}/月</template>
      </ElTableColumn>
      <ElTableColumn prop="workAddress" label="工作地点" />
      <ElTableColumn prop="recruitNum" label="招聘人数" width="100" />
      <ElTableColumn prop="status" label="状态" width="100">
        <template #default="{ row }">
          <span :class="getStatusClass(row.status)">{{ getStatusLabel(row.status) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="viewCount" label="浏览量" width="100" />
      <ElTableColumn prop="deliverCount" label="投递数" width="100" />
      <ElTableColumn prop="deadline" label="截止日期" width="120" />
      <ElTableColumn label="操作" width="300">
        <template #default="{ row }">
          <ElButton v-if="row.status === 0" size="small" @click="openEditDialog(row)">编辑</ElButton>
          <ElButton v-if="row.status === 0" size="small" type="primary" @click="handlePublish(row.id)">发布</ElButton>
          <ElButton v-if="row.status === 1" size="small" @click="handlePause(row.id)">暂停</ElButton>
          <ElButton v-if="row.status === 2" size="small" type="primary" @click="handlePublish(row.id)">重新发布</ElButton>
          <ElButton v-if="row.status === 1 || row.status === 2" size="small" type="danger" @click="handleClose(row.id)">关闭</ElButton>
          <ElButton v-if="row.status === 3" size="small" type="danger" @click="handleDelete(row.id)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
    <div class="pagination">
      <ElPagination
        @size-change="(size) => { pageInfo.pageSize = size; pageInfo.page = 1; fetchJobs() }"
        @current-change="(page) => { pageInfo.page = page; fetchJobs() }"
        :current-page="pageInfo.page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageInfo.pageSize"
        :total="pageInfo.total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="岗位名称" required>
          <ElInput v-model="form.jobName" />
        </ElFormItem>
        <ElFormItem label="岗位类别">
          <ElSelect v-model="form.jobCategory">
            <ElOption v-for="cat in jobCategories" :key="cat" :label="cat" :value="cat" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="最低薪资">
          <ElInput v-model="form.salaryMin" type="number" />
        </ElFormItem>
        <ElFormItem label="最高薪资">
          <ElInput v-model="form.salaryMax" type="number" />
        </ElFormItem>
        <ElFormItem label="工作地点">
          <ElInput v-model="form.workAddress" />
        </ElFormItem>
        <ElFormItem label="学历要求">
          <ElInput v-model="form.educationRequire" />
        </ElFormItem>
        <ElFormItem label="岗位要求">
          <ElInput v-model="form.jobRequire" type="textarea" />
        </ElFormItem>
        <ElFormItem label="岗位描述">
          <ElInput v-model="form.jobDesc" type="textarea" />
        </ElFormItem>
        <ElFormItem label="招聘人数">
          <ElInput v-model="form.recruitNum" type="number" />
        </ElFormItem>
        <ElFormItem label="截止日期">
          <ElDatePicker v-model="form.deadline" type="date" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.hr-job-list {
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

.status-draft { color: #909399; }
.status-published { color: #67c23a; }
.status-paused { color: #e6a23c; }
.status-closed { color: #f56c6c; }
</style>