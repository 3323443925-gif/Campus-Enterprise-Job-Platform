<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage, ElPopconfirm } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增专业')
const form = reactive({
  id: null,
  majorName: '',
  majorCode: ''
})

const fetchMajors = async () => {
  tableData.value = await request.get('/admin/major/list')
}

const openAddDialog = () => {
  dialogTitle.value = '新增专业'
  Object.assign(form, { id: null, majorName: '', majorCode: '' })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑专业'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.id) {
    await request.put('/admin/major', form)
    ElMessage.success('更新成功')
  } else {
    await request.post('/admin/major', form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchMajors()
}

const handleDelete = async (id) => {
  await request.delete(`/admin/major/${id}`)
  ElMessage.success('删除成功')
  fetchMajors()
}

onMounted(() => {
  fetchMajors()
})
</script>

<template>
  <div class="major-list">
    <div class="search-bar">
      <ElButton type="primary" @click="openAddDialog">新增专业</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="majorCode" label="专业编码" />
      <ElTableColumn prop="majorName" label="专业名称" />
      <ElTableColumn prop="createTime" label="创建时间" width="180" />
      <ElTableColumn label="操作" width="150">
        <template #default="{ row }">
          <ElButton size="small" @click="openEditDialog(row)">编辑</ElButton>
          <ElPopconfirm title="确定删除该专业？" @confirm="handleDelete(row.id)">
            <ElButton size="small" type="danger">删除</ElButton>
          </ElPopconfirm>
        </template>
      </ElTableColumn>
    </ElTable>
    <ElDialog v-model="dialogVisible" :title="dialogTitle">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="专业编码">
          <ElInput v-model="form.majorCode" :disabled="!!form.id" />
        </ElFormItem>
        <ElFormItem label="专业名称">
          <ElInput v-model="form.majorName" />
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
.major-list {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>