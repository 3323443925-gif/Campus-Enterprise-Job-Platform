<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElSelect, ElOption } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

const formRef = ref(null)

const userType = ref('student')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  majorId: '',
  enterpriseId: ''
})

const userTypeOptions = [
  { label: '学生', value: 'student' },
  { label: '企业HR', value: 'hr' },
  { label: '教师', value: 'teacher' }
]

const majorOptions = ref([])
const enterpriseOptions = ref([])

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ]
}

function validateConfirmPassword(rule, value, callback) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
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

const fetchEnterprises = async () => {
  try {
    const data = await request.get('/auth/enterprises')
    enterpriseOptions.value = data.map(e => ({ label: e.enterpriseName, value: e.id }))
  } catch (e) {
    console.error('获取企业列表失败', e)
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    let url = ''
    let params = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      email: form.email
    }
    
    if (userType.value === 'student') {
      url = '/auth/register/student'
      params.majorId = parseInt(form.majorId)
    } else if (userType.value === 'hr') {
      url = '/auth/register/hr'
    } else if (userType.value === 'teacher') {
      url = '/auth/register/teacher'
      params.majorId = parseInt(form.majorId)
      params.userType = 'teacher'
    }
    
    await request.post(url, params)
    
    ElMessage.success(userType.value === 'student' ? '注册成功，请登录' : '注册成功，请等待管理员审核')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

watch(userType, (val) => {
  form.majorId = ''
  form.enterpriseId = ''
  if (val === 'student' || val === 'teacher') {
    fetchMajors()
  }
})

onMounted(() => {
  fetchMajors()
  fetchEnterprises()
})
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>就业管理系统</h2>
        <p>用户注册</p>
      </div>
      
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="用户类型" prop="userType">
          <ElSelect v-model="userType" placeholder="请选择用户类型" style="width: 100%">
            <ElOption v-for="option in userTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </ElSelect>
        </ElFormItem>
        
        <template v-if="userType === 'student'">
          <ElFormItem label="学号" prop="username">
            <ElInput v-model="form.username" placeholder="请输入学号" />
          </ElFormItem>
        </template>
        <template v-else-if="userType === 'hr'">
          <ElFormItem label="手机号" prop="username">
            <ElInput v-model="form.username" placeholder="请输入手机号" />
          </ElFormItem>
        </template>
        <template v-else>
          <ElFormItem label="工号" prop="username">
            <ElInput v-model="form.username" placeholder="请输入工号" />
          </ElFormItem>
        </template>
        
        <ElFormItem label="密码" prop="password">
          <ElInput v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </ElFormItem>
        
        <ElFormItem label="确认密码" prop="confirmPassword">
          <ElInput v-model="form.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </ElFormItem>
        
        <ElFormItem label="姓名" prop="realName">
          <ElInput v-model="form.realName" placeholder="请输入姓名" />
        </ElFormItem>
        
        <ElFormItem label="手机号" prop="phone">
          <ElInput v-model="form.phone" placeholder="请输入手机号" />
        </ElFormItem>
        
        <ElFormItem label="邮箱">
          <ElInput v-model="form.email" placeholder="请输入邮箱（选填）" />
        </ElFormItem>
        
        <template v-if="userType === 'student' || userType === 'teacher'">
          <ElFormItem label="专业" prop="majorId">
            <ElSelect v-model="form.majorId" placeholder="请选择专业" style="width: 100%">
              <ElOption v-for="option in majorOptions" :key="option.value" :label="option.label" :value="option.value" />
            </ElSelect>
          </ElFormItem>
        </template>
        
        <template v-if="userType === 'hr'">
        </template>
        
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            注册
          </ElButton>
        </ElFormItem>
      </ElForm>
      
      <div class="register-tips">
        <p>已有账号？<a href="#" @click.prevent="router.push('/login')">立即登录</a></p>
      </div>
      
      <div class="register-rules">
        <h4>注册须知</h4>
        <ul>
          <li>学生：学号作为登录账号，注册后可直接登录</li>
          <li>企业HR：手机号作为登录账号，需管理员审核通过后登录</li>
          <li>教师：工号作为登录账号，需管理员审核通过后登录</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
}

.register-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-tips {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.register-tips p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-tips a {
  color: #409eff;
  text-decoration: none;
}

.register-tips a:hover {
  text-decoration: underline;
}

.register-rules {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.register-rules h4 {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.register-rules ul {
  margin: 0;
  padding-left: 20px;
}

.register-rules li {
  margin: 5px 0;
  color: #909399;
  font-size: 12px;
}
</style>
