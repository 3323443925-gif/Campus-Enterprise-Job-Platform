<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElCard, ElSelect, ElOption } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

const auditStatus = ref({})
const isEditing = ref(false)
const form = reactive({
  companyName: '',
  unifiedSocialCode: '',
  address: '',
  industry: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  cooperationLevel: 1,
  businessLicenseImg: ''
})

const fetchAuditStatus = async () => {
  auditStatus.value = await request.get('/hr/audit/status')
}

const fetchEnterpriseInfo = async () => {
  try {
    const enterprise = await request.get('/hr/enterprise')
    form.companyName = enterprise.companyName || ''
    form.unifiedSocialCode = enterprise.unifiedSocialCode || ''
    form.address = enterprise.address || ''
    form.industry = enterprise.industry || ''
    form.contactName = enterprise.contactName || ''
    form.contactPhone = enterprise.contactPhone || ''
    form.contactEmail = enterprise.contactEmail || ''
    form.cooperationLevel = enterprise.cooperationLevel || 1
  } catch (e) {
    ElMessage.error('获取企业信息失败')
  }
}

const handleSubmit = async () => {
  if (!form.businessLicenseImg && !auditStatus.value.auditStatus) {
    ElMessage.error('请上传营业执照')
    return
  }
  await request.post('/hr/audit/submit', form)
  ElMessage.success('提交成功')
  fetchAuditStatus()
}

const handleUpdate = async () => {
  await request.put('/hr/enterprise', form)
  ElMessage.success('企业信息更新成功')
  isEditing.value = false
}

const startEdit = async () => {
  isEditing.value = true
  await fetchEnterpriseInfo()
}

const cancelEdit = () => {
  isEditing.value = false
}

onMounted(() => {
  fetchAuditStatus()
})
</script>

<template>
  <div class="audit-apply">
    <ElCard title="企业认证申请">
      <div v-if="auditStatus.auditStatus === 1 && !isEditing" class="success-message">
        <i class="el-icon-check-circle" style="color: #67c23a; font-size: 48px"></i>
        <p>企业认证已通过</p>
        <ElButton type="primary" @click="startEdit">修改企业信息</ElButton>
      </div>
      <div v-else>
        <ElForm :model="form" label-width="150px">
          <ElFormItem label="企业名称" required>
            <ElInput v-model="form.companyName" placeholder="请输入企业名称" />
          </ElFormItem>
          <ElFormItem label="统一社会信用代码" required>
            <ElInput v-model="form.unifiedSocialCode" placeholder="请输入统一社会信用代码" />
          </ElFormItem>
          <ElFormItem label="企业地址" required>
            <ElInput v-model="form.address" placeholder="请输入企业地址" />
          </ElFormItem>
          <ElFormItem label="所属行业" required>
            <ElInput v-model="form.industry" placeholder="请输入所属行业" />
          </ElFormItem>
          <ElFormItem label="联系人姓名" required>
            <ElInput v-model="form.contactName" placeholder="请输入联系人姓名" />
          </ElFormItem>
          <ElFormItem label="联系电话" required>
            <ElInput v-model="form.contactPhone" placeholder="请输入联系电话" />
          </ElFormItem>
          <ElFormItem label="联系邮箱">
            <ElInput v-model="form.contactEmail" placeholder="请输入联系邮箱" />
          </ElFormItem>
          <ElFormItem label="合作等级">
            <ElSelect v-model="form.cooperationLevel" style="width: 100%" placeholder="请选择合作等级">
              <ElOption label="普通" :value="1" />
              <ElOption label="良好" :value="2" />
              <ElOption label="核心" :value="3" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="营业执照" v-if="!auditStatus.auditStatus" required>
            <UploadFile v-model="form.businessLicenseImg" accept="image/*" buttonText="上传营业执照" />
          </ElFormItem>
          <ElFormItem>
            <template v-if="isEditing">
              <ElButton type="primary" @click="handleUpdate">保存修改</ElButton>
              <ElButton @click="cancelEdit" style="margin-left: 10px">取消</ElButton>
            </template>
            <template v-else>
              <ElButton type="primary" @click="handleSubmit">提交认证</ElButton>
            </template>
          </ElFormItem>
        </ElForm>
        <div v-if="auditStatus.statusText && !isEditing" class="status-info">
          当前状态：{{ auditStatus.statusText }}
          <span v-if="auditStatus.auditRemark">，备注：{{ auditStatus.auditRemark }}</span>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<style scoped>
.audit-apply {
  padding: 20px;
}

.success-message {
  text-align: center;
  padding: 40px;
}

.success-message p {
  margin-top: 20px;
  font-size: 18px;
  color: #67c23a;
}

.status-info {
  margin-top: 20px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}
</style>