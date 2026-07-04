<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessage, ElPopconfirm } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({
  userType: '',
  keyword: ''
})

const userTypes = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
  { label: '就业办', value: 'job_office' },
  { label: 'HR', value: 'hr' },
  { label: '管理员', value: 'admin' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  userType: 'student',
  phone: '',
  email: '',
  avatar: '',
  majorId: null,
  enterpriseId: null,
  status: 1
})

const fetchUsers = async () => {
  const data = await request.get('/admin/user/page', {
    params: {
      page: pageInfo.page,
      pageSize: pageInfo.pageSize,
      userType: searchForm.userType,
      keyword: searchForm.keyword
    }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const handleSearch = () => {
  pageInfo.page = 1
  fetchUsers()
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchUsers()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchUsers()
}

const openAddDialog = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    realName: '',
    userType: 'student',
    phone: '',
    email: '',
    avatar: '',
    majorId: null,
    enterpriseId: null,
    status: 1
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.id) {
    await request.put('/admin/user', form)
    ElMessage.success('更新成功')
  } else {
    await request.post('/admin/user', form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchUsers()
}

const handleDelete = async (id) => {
  await request.delete(`/admin/user/${id}`)
  ElMessage.success('删除成功')
  fetchUsers()
}

const getUserTypeLabel = (type) => {
  const found = userTypes.find(t => t.value === type)
  return found ? found.label : type
}

const getStatusLabel = (status) => {
  return status === 1 ? '正常' : '禁用'
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-list">
    <div class="search-bar">
      <ElSelect v-model="searchForm.userType" placeholder="用户类型" clearable style="width: 150px; margin-right: 10px">
        <ElOption v-for="type in userTypes" :key="type.value" :label="type.label" :value="type.value" />
      </ElSelect>
      <ElInput v-model="searchForm.keyword" placeholder="关键词" style="width: 200px; margin-right: 10px" />
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
      <ElButton @click="openAddDialog">新增用户</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="avatar" label="头像" width="80">
        <template #default="{ row }">
          <img v-if="row.avatar" :src="row.avatar" class="avatar-img" />
          <span v-else class="no-avatar">-</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="username" label="账号" />
      <ElTableColumn prop="realName" label="姓名" />
      <ElTableColumn prop="userType" label="类型">
        <template #default="{ row }">{{ getUserTypeLabel(row.userType) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="phone" label="手机号" />
      <ElTableColumn prop="email" label="邮箱" />
      <ElTableColumn prop="status" label="状态">
        <template #default="{ row }">{{ getStatusLabel(row.status) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="createTime" label="创建时间" width="180" />
      <ElTableColumn label="操作" width="200">
        <template #default="{ row }">
          <ElButton size="small" @click="openEditDialog(row)">编辑</ElButton>
          <ElPopconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
            <ElButton size="small" type="danger">删除</ElButton>
          </ElPopconfirm>
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
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="账号">
          <ElInput v-model="form.username" :disabled="!!form.id" />
        </ElFormItem>
        <ElFormItem label="密码" v-if="!form.id">
          <ElInput v-model="form.password" type="password" />
        </ElFormItem>
        <ElFormItem label="姓名">
          <ElInput v-model="form.realName" />
        </ElFormItem>
        <ElFormItem label="类型">
          <ElSelect v-model="form.userType">
            <ElOption v-for="type in userTypes" :key="type.value" :label="type.label" :value="type.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="手机号">
          <ElInput v-model="form.phone" />
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="form.email" />
        </ElFormItem>
        <ElFormItem label="头像">
          <UploadFile v-model="form.avatar" accept="image/*" buttonText="上传头像" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="form.status">
            <ElOption label="正常" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
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
.user-list {
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

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.no-avatar {
  color: #909399;
}
</style>