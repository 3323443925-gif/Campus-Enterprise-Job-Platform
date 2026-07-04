<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElTable, ElTableColumn, ElSelect, ElOption, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({
  jobId: '',
  deliverStatus: ''
})

const deliverStatusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 0 },
  { label: '待面试', value: 1 },
  { label: '面试完成', value: 2 },
  { label: '不合适', value: 3 },
  { label: '已录用', value: 4 },
  { label: '已报到', value: 5 }
]

const changeStatusOptions = [
  { label: '待面试', value: 1 },
  { label: '面试完成', value: 2 },
  { label: '不合适', value: 3 },
  { label: '已录用', value: 4 },
  { label: '已报到', value: 5 }
]

const detailDialogVisible = ref(false)
const detailData = ref({})

const statusDialogVisible = ref(false)
const statusForm = reactive({
  id: null,
  deliverStatus: 0,
  hrRemark: ''
})

const fetchDelivers = async () => {
  const data = await request.get('/hr/deliver/page', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize,
      jobId: searchForm.jobId || undefined,
      deliverStatus: searchForm.deliverStatus === '' ? undefined : searchForm.deliverStatus
    }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handleSearch = () => {
  pageInfo.page = 1
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

const handleDetail = async (row) => {
  detailData.value = row
  detailDialogVisible.value = true
}

const handleAIanalysis = (row) => {
  router.push({ path: '/hr/resume-ai', query: { deliverId: row.id } })
}

const openStatusDialog = (row) => {
  Object.assign(statusForm, { id: row.id, deliverStatus: row.deliverStatus, hrRemark: '' })
  statusDialogVisible.value = true
}

const handleUpdateStatus = async () => {
  await request.put(`/hr/deliver/${statusForm.id}/status`, statusForm)
  ElMessage.success('状态更新成功')
  statusDialogVisible.value = false
  fetchDelivers()
}

const getStatusLabel = (status) => {
  const found = deliverStatusOptions.find(o => o.value === status)
  return found ? found.label : '未知'
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-interview'
    case 2: return 'status-done'
    case 3: return 'status-reject'
    case 4: return 'status-employed'
    case 5: return 'status-report'
    default: return ''
  }
}

onMounted(() => {
  fetchDelivers()
})
</script>

<template>
  <div class="deliver-box">
    <div class="search-bar">
      <ElSelect v-model="searchForm.deliverStatus" placeholder="投递状态" style="width: 150px; margin-right: 10px">
        <ElOption v-for="option in deliverStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
      </ElSelect>
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="studentName" label="学生姓名" />
      <ElTableColumn prop="studentNo" label="学号" />
      <ElTableColumn prop="majorName" label="专业" />
      <ElTableColumn prop="deliverStatus" label="状态" width="100">
        <template #default="{ row }">
          <span :class="getStatusClass(row.deliverStatus)">{{ getStatusLabel(row.deliverStatus) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="hrRemark" label="HR备注" />
      <ElTableColumn prop="deliverTime" label="投递时间" width="180" />
      <ElTableColumn label="操作" width="320">
        <template #default="{ row }">
          <ElButton size="small" @click="handleDetail(row)">查看简历</ElButton>
          <ElButton size="small" type="success" @click="handleAIanalysis(row)">AI分析</ElButton>
          <ElButton v-if="row.deliverStatus !== 3 && row.deliverStatus !== 5" size="small" type="primary" @click="openStatusDialog(row)">修改状态</ElButton>
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
    <ElDialog v-model="detailDialogVisible" title="简历详情" width="800px">
      <div v-if="detailData">
        <h4>学生信息</h4>
        <p>姓名：{{ detailData.studentName }}</p>
        <p>学号：{{ detailData.studentNo }}</p>
        <p>专业：{{ detailData.majorName }}</p>
        <h4>简历信息</h4>
        <p>期望城市：{{ detailData.expectCity }}</p>
        <p>期望薪资：{{ detailData.expectSalary }}</p>
        <p>自我评价：{{ detailData.selfIntro }}</p>
        <p>教育经历：{{ detailData.educationExp }}</p>
        <p>实习经历：{{ detailData.practiceExp }}</p>
        <p>技能证书：{{ detailData.certs }}</p>
        <div v-if="detailData.resumeFile">
          <h4>PDF简历</h4>
          <a :href="detailData.resumeFile" target="_blank" class="resume-link">
            <i class="el-icon-document"></i> 查看PDF简历
          </a>
        </div>
        <div v-else style="color: #909399; font-size: 14px;">
          未上传PDF简历
        </div>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>
    <ElDialog v-model="statusDialogVisible" title="修改状态">
      <ElForm :model="statusForm" label-width="100px">
        <ElFormItem label="投递状态">
          <ElSelect v-model="statusForm.deliverStatus" placeholder="请选择状态">
            <ElOption v-for="option in changeStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="HR备注">
          <ElInput v-model="statusForm.hrRemark" type="textarea" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="statusDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleUpdateStatus">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.deliver-box {
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

.status-pending { color: #e6a23c; }
.status-interview { color: #409eff; }
.status-done { color: #909399; }
.status-reject { color: #f56c6c; }
.status-employed { color: #67c23a; }
.status-report { color: #8f5cf6; }

.resume-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.resume-link:hover {
  text-decoration: underline;
}
</style>