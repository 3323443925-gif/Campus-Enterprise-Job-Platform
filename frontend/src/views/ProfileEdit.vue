<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElSelect, ElOption, ElDialog, ElCard } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

const router = useRouter()
const store = useStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  majorId: ''
})

const majorOptions = ref([])

const passwordDialogVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

const fetchProfile = async () => {
  try {
    const data = await request.get('/auth/profile')
    form.username = data.username || ''
    form.realName = data.realName || ''
    form.phone = data.phone || ''
    form.email = data.email || ''
    form.avatar = data.avatar || ''
    form.majorId = data.majorId || ''
  } catch (e) {
    console.error('获取用户信息失败', e)
    ElMessage.error('获取用户信息失败')
  }
}

const fetchMajors = async () => {
  try {
    const data = await request.get('/auth/majors')
    majorOptions.value = data.map(m => ({ label: m.majorName, value: m.id }))
  } catch (e) {
    console.error('获取专业列表失败', e)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await request.put('/auth/profile', {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar,
      majorId: form.majorId
    })
    
    ElMessage.success('更新成功')
    
    const userInfo = store.getters.userInfo
    store.commit('updateUserInfo', {
      ...userInfo,
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar,
      majorId: form.majorId
    })
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.error('请输入旧密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  
  try {
    await request.put('/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    await store.dispatch('logout')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  }
}

onMounted(() => {
  fetchProfile()
  fetchMajors()
})
</script>

<template>
  <div class="profile-edit">
    <ElCard title="个人资料" class="profile-card">
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="用户名" prop="username">
          <ElInput v-model="form.username" placeholder="请输入用户名" />
        </ElFormItem>
        
        <ElFormItem label="姓名" prop="realName">
          <ElInput v-model="form.realName" placeholder="请输入姓名" />
        </ElFormItem>
        
        <ElFormItem label="手机号" prop="phone">
          <ElInput v-model="form.phone" placeholder="请输入手机号" />
        </ElFormItem>
        
        <ElFormItem label="邮箱" prop="email">
          <ElInput v-model="form.email" placeholder="请输入邮箱" />
        </ElFormItem>
        
        <ElFormItem label="头像">
          <UploadFile v-model="form.avatar" accept="image/*" buttonText="上传头像" />
        </ElFormItem>
        
        <ElFormItem label="专业">
          <ElSelect v-model="form.majorId" placeholder="请选择专业" style="width: 100%">
            <ElOption v-for="option in majorOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleSubmit">保存修改</ElButton>
          <ElButton style="margin-left: 10px" @click="passwordDialogVisible = true">修改密码</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
    
    <ElDialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <ElForm label-width="80px">
        <ElFormItem label="旧密码">
          <ElInput v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </ElFormItem>
        <ElFormItem label="新密码">
          <ElInput v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </ElFormItem>
        <ElFormItem label="确认密码">
          <ElInput v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="passwordDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleChangePassword">确认修改</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.profile-edit {
  padding: 20px;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}
</style>