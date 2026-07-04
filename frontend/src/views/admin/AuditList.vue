<script setup>import { ref, reactive, onMounted } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElSelect, ElOption, ElMessage, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus';
import request from '@/utils/request';
const tableData = ref([]);
const pageInfo = reactive({ page: 1, pageSize: 10, total: 0 });
const searchForm = reactive({ auditStatus: '' });
const auditStatusOptions = [
 { label: '全部', value: '' },
 { label: '待审核', value: 0 },
 { label: '已通过', value: 1 },
 { label: '已驳回', value: 2 }
];
const dialogVisible = ref(false);
const dialogTitle = ref('审核操作');
const auditForm = reactive({
 id: null,
 auditRemark: ''
});
const currentType = ref('pass');
const fetchAudits = async () => {
 const data = await request.get('/admin/audit/page', {
 params: {
 page: pageInfo.page,
 pageSize: pageInfo.pageSize,
 auditStatus: searchForm.auditStatus || undefined
 }
 });
 tableData.value = data.list;
 pageInfo.total = data.total;
};
const handleSearch = () => {
 pageInfo.page = 1;
 fetchAudits();
};
const handlePageChange = (page) => {
 pageInfo.page = page;
 fetchAudits();
};
const handleSizeChange = (size) => {
 pageInfo.pageSize = size;
 pageInfo.page = 1;
 fetchAudits();
};
const openAuditDialog = (row, type) => {
 currentType.value = type;
 dialogTitle.value = type === 'pass' ? '审核通过' : '审核驳回';
 Object.assign(auditForm, { id: row.id, auditRemark: '' });
 dialogVisible.value = true;
};
const handleAudit = async () => {
 const url = currentType.value === 'pass'
 ? `/admin/audit/${auditForm.id}/pass`
 : `/admin/audit/${auditForm.id}/reject`;
 await request.put(url, {}, {
 params: { auditRemark: auditForm.auditRemark }
 });
 ElMessage.success(currentType.value === 'pass' ? '审核通过' : '审核驳回');
 dialogVisible.value = false;
 fetchAudits();
};
const getStatusLabel = (status) => {
 const found = auditStatusOptions.find(o => o.value === status);
 return found ? found.label : '未知';
};
const getStatusClass = (status) => {
 switch (status) {
 case 0: return 'status-pending';
 case 1: return 'status-pass';
 case 2: return 'status-reject';
 default: return '';
 }
};
onMounted(() => {
 fetchAudits();
});
</script>

<template>
  <div class="audit-list">
    <div class="search-bar">
      <ElSelect v-model="searchForm.auditStatus" placeholder="审核状态" style="width: 150px; margin-right: 10px">
        <ElOption v-for="option in auditStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
      </ElSelect>
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
    </div>
    <ElTable :data="tableData" border>
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="enterpriseId" label="企业ID" width="100" />
      <ElTableColumn prop="hrUserId" label="HR用户ID" width="100" />
      <ElTableColumn prop="businessLicenseImg" label="营业执照" width="200">
        <template #default="{ row }">
          <a :href="row.businessLicenseImg" target="_blank">查看图片</a>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="auditStatus" label="状态" width="100">
        <template #default="{ row }">
          <span :class="getStatusClass(row.auditStatus)">{{ getStatusLabel(row.auditStatus) }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="auditRemark" label="审核备注" />
      <ElTableColumn prop="auditTime" label="审核时间" width="180" />
      <ElTableColumn prop="createTime" label="提交时间" width="180" />
      <ElTableColumn label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 0">
            <ElButton size="small" type="success" @click="openAuditDialog(row, 'pass')">通过</ElButton>
            <ElButton size="small" type="danger" @click="openAuditDialog(row, 'reject')">驳回</ElButton>
          </template>
          <span v-else>-</span>
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
.audit-list {
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

.status-pending {
  color: #e6a23c;
}

.status-pass {
  color: #67c23a;
}

.status-reject {
  color: #f56c6c;
}
</style>