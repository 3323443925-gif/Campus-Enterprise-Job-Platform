<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElSelect, ElOption, ElMessage, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('审核操作')
const auditForm = reactive({
  id: null,
  auditRemark: ''
})
const currentType = ref('pass')

const fetchAudits = async () => {
  const data = await request.get('/admin/user/audit/page', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize
    }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchAudits()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchAudits()
}

const openAuditDialog = (row, type) => {
  currentType.value = type
  dialogTitle.value = type === 'pass' ? '审核通过' : '审核驳回'
  Object.assign(auditForm, { id: row.id, auditRemark: '' })
  dialogVisible.value = true
}

const handleAudit = async () => {
  try {
    if (currentType.value === 'pass') {
      await request.put(`/admin/user/audit/${auditForm.id}/pass`)
      ElMessage.success('审核通过')
    } else {
      await request.delete(`/admin/user/audit/${auditForm.id}/reject`)
      ElMessage.success('审核驳回')
    }
    dialogVisible.value = false
    fetchAudits()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const getUserTypeLabel = (userType) => {
  const map = {
    'student': '学生',
    'hr': '企业HR',
    'teacher': '教师',
    'job_office': '就业办',
    'admin': '管理员'
  }
  return map[userType] || userType
}

const getUserTypeClass = (userType) => {
  const map = {
    'student': 'type-student',
    'hr': 'type-hr',
    'teacher': 'type-teacher',
    'job_office': 'type-office',
    'admin': 'type-admin'
  }
  return map[userType] || ''
}

onMounted(() => {
  fetchAudits()
})
</script>

<template>
  <div class="user-audit-list">
    <div class="page-header">
      <h2>用户审核</h2>
      <p>待审核的HR和教师账号</p>
    </div>
    
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="username" label="账号" width="150" />
      <ElTableColumn prop="realName" label="姓名" width="100" />
      <ElTableColumn prop="userType" label="用户类型" width="100">
        <template #default="{ row }">
          <span :class="getUserTypeClass(row.userType)">{{ getUserTypeLabel(row.userType) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="phone" label="手机号" width="150" />
      <ElTableColumn prop="email" label="邮箱" />
      <ElTableColumn prop="createTime" label="注册时间" width="180" />
      <ElTableColumn label="操作" width="180">
        <template #default="{ row }">
          <ElButton size="small" type="success" @click="openAuditDialog(row, 'pass')">通过</ElButton>
          <ElButton size="small" type="danger" @click="openAuditDialog(row, 'reject')">驳回</ElButton>
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
    
    <ElDialog v-model="dialogVisible" :title="dialogTitle">
      <ElForm :model="auditForm" label-width="100px">
        <ElFormItem label="审核备注">
          <ElInput v-model="auditForm.auditRemark" type="textarea" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleAudit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.user-audit-list {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.type-student { color: #409eff; }
.type-hr { color: #e6a23c; }
.type-teacher { color: #67c23a; }
.type-office { color: #909399; }
.type-admin { color: #f56c6c; }
</style>
