<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增企业')
const form = reactive({
  id: null,
  companyName: '',
  unifiedSocialCode: '',
  address: '',
  industry: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  businessLicenseImg: ''
})

const fetchEnterprises = async () => {
  const data = await request.get('/teacher/enterprise/page', {
    params: { page: pageInfo.page, pageSize: pageInfo.pageSize }
  })
  tableData.value = data.list
  pageInfo.total = data.total
}

const openAddDialog = () => {
  dialogTitle.value = '新增企业'
  Object.assign(form, {
    id: null,
    companyName: '',
    unifiedSocialCode: '',
    address: '',
    industry: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    businessLicenseImg: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑企业'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.companyName) {
    ElMessage.error('请输入企业名称')
    return
  }
  if (form.id) {
    await request.put('/teacher/enterprise', form)
    ElMessage.success('更新成功')
  } else {
    await request.post('/teacher/enterprise', form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchEnterprises()
}

const handleDelete = async (id) => {
  await request.delete(`/teacher/enterprise/${id}`)
  ElMessage.success('删除成功')
  fetchEnterprises()
}

const handlePageChange = (page) => {
  pageInfo.page = page
  fetchEnterprises()
}

const handleSizeChange = (size) => {
  pageInfo.pageSize = size
  pageInfo.page = 1
  fetchEnterprises()
}

onMounted(() => {
  fetchEnterprises()
})
</script>

<template>
  <div class="my-enterprise">
    <div class="search-bar">
      <ElButton type="primary" @click="openAddDialog">新增企业</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="companyName" label="企业名称" />
      <ElTableColumn prop="unifiedSocialCode" label="统一社会信用代码" />
      <ElTableColumn prop="industry" label="行业" />
      <ElTableColumn prop="address" label="地址" />
      <ElTableColumn prop="contactName" label="联系人" />
      <ElTableColumn prop="contactPhone" label="联系电话" />
      <ElTableColumn label="操作" width="150">
        <template #default="{ row }">
          <ElButton size="small" @click="openEditDialog(row)">编辑</ElButton>
          <ElButton size="small" type="danger" @click="handleDelete(row.id)">删除</ElButton>
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
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <ElForm :model="form" label-width="120px">
        <ElFormItem label="企业名称" required>
          <ElInput v-model="form.companyName" />
        </ElFormItem>
        <ElFormItem label="统一社会信用代码">
          <ElInput v-model="form.unifiedSocialCode" />
        </ElFormItem>
        <ElFormItem label="企业地址">
          <ElInput v-model="form.address" />
        </ElFormItem>
        <ElFormItem label="所属行业">
          <ElInput v-model="form.industry" />
        </ElFormItem>
        <ElFormItem label="联系人姓名">
          <ElInput v-model="form.contactName" />
        </ElFormItem>
        <ElFormItem label="联系电话">
          <ElInput v-model="form.contactPhone" />
        </ElFormItem>
        <ElFormItem label="联系邮箱">
          <ElInput v-model="form.contactEmail" />
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
.my-enterprise {
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