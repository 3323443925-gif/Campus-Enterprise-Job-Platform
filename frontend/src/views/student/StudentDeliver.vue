<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElSelect, ElOption, ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({
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

const detailDialogVisible = ref(false)
const detailData = ref({})

const fetchDelivers = async () => {
  const data = await request.get('/student/deliver/page', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize,
      deliverStatus: searchForm.deliverStatus || undefined
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

const handleDetail = (row) => {
  detailData.value = row
  detailDialogVisible.value = true
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
  <div class="student-deliver">
    <div class="search-bar">
      <ElSelect v-model="searchForm.deliverStatus" placeholder="投递状态" style="width: 150px; margin-right: 10px">
        <ElOption v-for="option in deliverStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
      </ElSelect>
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="jobName" label="岗位名称" />
      <ElTableColumn prop="companyName" label="企业名称" />
      <ElTableColumn prop="deliverStatus" label="状态" width="100">
        <template #default="{ row }">
          <span :class="getStatusClass(row.deliverStatus)">{{ getStatusLabel(row.deliverStatus) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="hrRemark" label="HR备注" />
      <ElTableColumn prop="deliverTime" label="投递时间" width="180" />
      <ElTableColumn label="操作" width="100">
        <template #default="{ row }">
          <ElButton size="small" @click="handleDetail(row)">详情</ElButton>
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
    <ElDialog v-model="detailDialogVisible" title="投递详情" width="600px">
      <div v-if="detailData">
        <p><strong>岗位名称：</strong>{{ detailData.jobName }}</p>
        <p><strong>企业名称：</strong>{{ detailData.companyName }}</p>
        <p><strong>投递时间：</strong>{{ detailData.deliverTime }}</p>
        <p><strong>当前状态：</strong><span :class="getStatusClass(detailData.deliverStatus)">{{ getStatusLabel(detailData.deliverStatus) }}</span></p>
        <p><strong>HR备注：</strong>{{ detailData.hrRemark || '暂无' }}</p>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.student-deliver {
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
</style>