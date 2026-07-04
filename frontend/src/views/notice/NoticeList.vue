<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElInput, ElMessage, ElSelect, ElOption, ElPagination } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ isRead: '' })

const detailDialogVisible = ref(false)
const detailData = ref({})

const fetchNotices = async () => {
  const data = await request.get('/notice/list', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize,
      isRead: searchForm.isRead || undefined
    }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handleSearch = () => {
  pageInfo.page = 1
  fetchNotices()
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchNotices()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchNotices()
}

const handleDetail = async (notice) => {
  detailData.value = notice
  if (!notice.isRead) {
    await request.put(`/notice/${notice.id}/read`)
    notice.isRead = 1
  }
  detailDialogVisible.value = true
}

const handleMarkRead = async (id) => {
  await request.put(`/notice/${id}/read`)
  const notice = tableData.value.find(n => n.id === id)
  if (notice) notice.isRead = 1
  ElMessage.success('已标记为已读')
}

const handleMarkAllRead = async () => {
  await request.put('/notice/all/read')
  tableData.value.forEach(n => n.isRead = 1)
  ElMessage.success('全部已标记为已读')
}

const getStatusLabel = (isRead) => {
  return isRead === 1 ? '已读' : '未读'
}

const getStatusClass = (isRead) => {
  return isRead === 1 ? 'status-read' : 'status-unread'
}

onMounted(() => {
  fetchNotices()
})
</script>

<template>
  <div class="notice-list">
    <div class="search-bar">
      <ElSelect v-model="searchForm.isRead" placeholder="消息状态" style="width: 120px; margin-right: 10px">
        <ElOption label="全部" value="" />
        <ElOption label="未读" :value="0" />
        <ElOption label="已读" :value="1" />
      </ElSelect>
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
      <ElButton @click="handleMarkAllRead">一键全部已读</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="noticeTitle" label="标题" />
      <ElTableColumn prop="noticeType" label="类型" width="100" />
      <ElTableColumn prop="isRead" label="状态" width="80">
        <template #default="{ row }">
          <span :class="getStatusClass(row.isRead)">{{ getStatusLabel(row.isRead) }}</span>
          <span v-if="row.isRead === 0" class="red-dot"></span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createTime" label="发送时间" width="180" />
      <ElTableColumn label="操作" width="150">
        <template #default="{ row }">
          <ElButton size="small" @click="handleDetail(row)">查看</ElButton>
          <ElButton v-if="row.isRead === 0" size="small" @click="handleMarkRead(row.id)">标记已读</ElButton>
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
    <ElDialog v-model="detailDialogVisible" title="消息详情" width="600px">
      <div v-if="detailData">
        <h4>{{ detailData.noticeTitle }}</h4>
        <p class="notice-type">类型：{{ detailData.noticeType }}</p>
        <p class="notice-time">发送时间：{{ detailData.createTime }}</p>
        <div class="notice-content">{{ detailData.noticeContent }}</div>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.notice-list {
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

.status-read { color: #909399; }
.status-unread { color: #409eff; font-weight: bold; }

.red-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
  margin-left: 4px;
}

.notice-type {
  color: #666;
  margin: 10px 0;
}

.notice-time {
  color: #999;
  margin-bottom: 20px;
}

.notice-content {
  line-height: 1.8;
  color: #333;
}
</style>