<script setup>
import { ref, computed } from 'vue'
import { ElUpload, ElButton, ElMessage, ElIcon } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  accept: {
    type: String,
    default: 'image/*'
  },
  buttonText: {
    type: String,
    default: '点击上传'
  },
  showPreview: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error'])

const fileList = computed(() => {
  if (props.modelValue) {
    return [{ name: props.modelValue.split('/').pop() || '已上传文件', url: props.modelValue }]
  }
  return []
})

const isImage = computed(() => {
  return props.accept.includes('image') || props.modelValue?.match(/\.(jpg|jpeg|png|gif|webp)$/i)
})

const beforeUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const url = await request.post('/common/upload', formData)
    ElMessage.success('上传成功')
    emit('update:modelValue', url)
    emit('upload-success', url)
  } catch (error) {
    ElMessage.error('上传失败')
    emit('upload-error', error)
  }
  return false
}

const handleRemove = () => {
  emit('update:modelValue', '')
  emit('upload-success', '')
}
</script>

<template>
  <div class="upload-file-wrapper">
    <ElUpload
      class="upload-file"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :accept="accept"
      :limit="1"
    >
      <ElButton type="primary">{{ buttonText }}</ElButton>
    </ElUpload>
    <div v-if="showPreview && modelValue" class="file-preview">
      <img v-if="isImage" :src="modelValue" alt="预览" class="preview-image" />
      <a v-else :href="modelValue" target="_blank" class="preview-link">
        {{ modelValue.split('/').pop() || '查看文件' }}
      </a>
    </div>
  </div>
</template>

<style scoped>
.upload-file-wrapper {
  display: inline-block;
}

.upload-file {
  display: inline-block;
}

.file-preview {
  margin-top: 10px;
}

.preview-image {
  max-width: 200px;
  max-height: 150px;
  border-radius: 4px;
  object-fit: cover;
}

.preview-link {
  color: #409eff;
  text-decoration: none;
}

.preview-link:hover {
  text-decoration: underline;
}
</style>