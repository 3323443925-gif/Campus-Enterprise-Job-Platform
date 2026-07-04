<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElCard, ElSelect, ElOption } from 'element-plus'
import request from '@/utils/request'
import UploadFile from '@/components/UploadFile.vue'

const form = reactive({
  id: null,
  majorId: '',
  studentNo: '',
  expectCity: '',
  expectSalary: '',
  selfIntro: '',
  educationExp: '',
  practiceExp: '',
  certs: '',
  projectExp: '',
  resumeFile: ''
})

const majors = ref([])

const fetchResume = async () => {
  try {
    const data = await request.get('/student/resume')
    Object.assign(form, data)
  } catch (e) {
  }
}

const fetchMajors = async () => {
  const data = await request.get('/admin/major/list')
  majors.value = data
}

const handleSubmit = async () => {
  if (!form.majorId) {
    ElMessage.error('请选择专业')
    return
  }
  if (!form.studentNo) {
    ElMessage.error('请输入学号')
    return
  }
  if (form.id) {
    await request.put('/student/resume', form)
    ElMessage.success('更新成功')
  } else {
    await request.post('/student/resume', form)
    ElMessage.success('保存成功')
  }
}

onMounted(() => {
  fetchResume()
  fetchMajors()
})
</script>

<template>
  <div class="student-resume">
    <ElCard title="我的简历">
      <ElForm :model="form" label-width="120px">
        <ElFormItem label="专业" required>
          <ElSelect v-model="form.majorId" placeholder="请选择专业">
            <ElOption v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="学号" required>
          <ElInput v-model="form.studentNo" placeholder="请输入学号" />
        </ElFormItem>
        <ElFormItem label="期望城市" required>
          <ElInput v-model="form.expectCity" placeholder="请输入期望工作城市" />
        </ElFormItem>
        <ElFormItem label="期望薪资" required>
          <ElInput v-model="form.expectSalary" placeholder="请输入期望薪资（如：5000-8000）" />
        </ElFormItem>
        <ElFormItem label="自我评价">
          <ElInput v-model="form.selfIntro" type="textarea" :rows="3" placeholder="请介绍一下自己" />
        </ElFormItem>
        <ElFormItem label="教育经历">
          <ElInput v-model="form.educationExp" type="textarea" :rows="3" placeholder="请填写教育经历" />
        </ElFormItem>
        <ElFormItem label="实习经历">
          <ElInput v-model="form.practiceExp" type="textarea" :rows="3" placeholder="请填写实习经历" />
        </ElFormItem>
        <ElFormItem label="项目经验">
          <ElInput v-model="form.projectExp" type="textarea" :rows="3" placeholder="请填写项目经验" />
        </ElFormItem>
        <ElFormItem label="技能证书">
          <ElInput v-model="form.certs" placeholder="请填写技能证书，用逗号分隔" />
        </ElFormItem>
        <ElFormItem label="PDF简历">
          <UploadFile v-model="form.resumeFile" accept=".pdf" buttonText="上传PDF简历" />
          <span class="upload-tip">（可选，上传 PDF 格式简历文件）</span>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSubmit">保存简历</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<style scoped>
.student-resume {
  padding: 20px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
</style>