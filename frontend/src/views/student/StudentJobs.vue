<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElInput, ElSelect, ElOption, ElMessage, ElCard } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({
  jobName: '',
  jobCategory: '',
  salaryMin: '',
  salaryMax: '',
  workAddress: ''
})

const jobCategories = ['技术类', '销售类', '运营类', '产品类', '设计类', '其他']

const detailDialogVisible = ref(false)
const jobDetail = ref({})

const fetchJobs = async () => {
  const data = await request.get('/student/job/page', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize,
      jobName: searchForm.jobName || undefined,
      jobCategory: searchForm.jobCategory || undefined,
      salaryMin: searchForm.salaryMin || undefined,
      salaryMax: searchForm.salaryMax || undefined,
      workAddress: searchForm.workAddress || undefined
    }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handleSearch = () => {
  pageInfo.page = 1
  fetchJobs()
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchJobs()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchJobs()
}

const handleDetail = async (id) => {
  jobDetail.value = await request.get(`/student/job/${id}`)
  detailDialogVisible.value = true
}

const handleDeliver = async (id) => {
  await request.post(`/student/deliver/${id}`)
  ElMessage.success('投递成功')
  fetchJobs()
}

const getStatusLabel = (status) => {
  switch (status) {
    case 0: return '草稿'
    case 1: return '招聘中'
    case 2: return '暂停招聘'
    case 3: return '已关闭'
    default: return '未知'
  }
}

onMounted(() => {
  fetchJobs()
})
</script>

<template>
  <div class="student-jobs">
    <div class="search-bar">
      <ElInput v-model="searchForm.jobName" placeholder="岗位名称" style="width: 150px; margin-right: 10px" />
      <ElSelect v-model="searchForm.jobCategory" placeholder="岗位类别" style="width: 120px; margin-right: 10px">
        <ElOption label="全部" value="" />
        <ElOption v-for="cat in jobCategories" :key="cat" :label="cat" :value="cat" />
      </ElSelect>
      <ElInput v-model="searchForm.workAddress" placeholder="工作地点" style="width: 150px; margin-right: 10px" />
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="jobCategory" label="岗位类别" />
      <ElTableColumn prop="salaryMin" label="薪资范围">
        <template #default="{ row }">{{ row.salaryMin }}-{{ row.salaryMax }}/月</template>
      </ElTableColumn>
      <ElTableColumn prop="workAddress" label="工作地点" />
      <ElTableColumn prop="educationRequire" label="学历要求" />
      <ElTableColumn prop="recruitNum" label="招聘人数" width="100" />
      <ElTableColumn prop="status" label="状态" width="100">
        <template #default="{ row }">{{ getStatusLabel(row.status) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="viewCount" label="浏览量" width="100" />
      <ElTableColumn prop="deliverCount" label="投递数" width="100" />
      <ElTableColumn prop="deadline" label="截止日期" width="120" />
      <ElTableColumn label="操作" width="150">
        <template #default="{ row }">
          <ElButton size="small" @click="handleDetail(row.id)">详情</ElButton>
          <ElButton size="small" type="primary" @click="handleDeliver(row.id)" :disabled="row.status !== 1">投递</ElButton>
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
    <ElDialog v-model="detailDialogVisible" title="岗位详情" width="700px">
      <ElCard v-if="jobDetail.enterprise" style="margin-bottom: 20px">
        <template #header>企业信息</template>
        <p>企业名称：{{ jobDetail.enterprise.companyName }}</p>
        <p>行业：{{ jobDetail.enterprise.industry }}</p>
        <p>地址：{{ jobDetail.enterprise.address }}</p>
      </ElCard>
      <div>
        <h4>岗位信息</h4>
        <p><strong>岗位名称：</strong>{{ jobDetail.jobName }}</p>
        <p><strong>岗位类别：</strong>{{ jobDetail.jobCategory }}</p>
        <p><strong>薪资范围：</strong>{{ jobDetail.salaryMin }}-{{ jobDetail.salaryMax }}/月</p>
        <p><strong>工作地点：</strong>{{ jobDetail.workAddress }}</p>
        <p><strong>学历要求：</strong>{{ jobDetail.educationRequire }}</p>
        <p><strong>招聘人数：</strong>{{ jobDetail.recruitNum }}</p>
        <p><strong>岗位要求：</strong>{{ jobDetail.jobRequire }}</p>
        <p><strong>岗位描述：</strong>{{ jobDetail.jobDesc }}</p>
        <p><strong>截止日期：</strong>{{ jobDetail.deadline }}</p>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
        <ElButton type="primary" @click="() => { handleDeliver(jobDetail.id); detailDialogVisible = false }" :disabled="jobDetail.status !== 1">投递简历</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.student-jobs {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>