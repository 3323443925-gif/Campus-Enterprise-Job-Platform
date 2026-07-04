<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus'

const router = useRouter()
const store = useStore()

const formRef = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const data = await store.dispatch('login', form)
    ElMessage.success('登录成功')
    const homePath = data.homePath || '/login'
    router.push(homePath)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>就业管理系统</h2>
        <p>请登录您的账号</p>
      </div>
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="80px">
        <ElFormItem label="账号" prop="username">
          <ElInput v-model="form.username" placeholder="请输入账号" />
        </ElFormItem>
        <ElFormItem label="密码" prop="password">
          <ElInput v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </ElButton>
        </ElFormItem>
        <ElFormItem>
          <ElButton @click="router.push('/register')" style="width: 100%">
            注册账号
          </ElButton>
        </ElFormItem>
      </ElForm>
      <div class="login-tips">
        <p>学生账号：学号</p>
        <p>企业账号：手机号</p>
        <p>教师账号：工号</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
}

.login-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-tips {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.login-tips p {
  margin: 5px 0;
  color: #909399;
  font-size: 12px;
  text-align: center;
}
</style>