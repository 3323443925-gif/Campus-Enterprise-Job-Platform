<template>
  <div class="hr-resume-ai">
    <div class="ai-chat-panel" :class="{ fullscreen: isFullscreen, 'panel-enter': panelEnter, 'panel-leave': panelLeave }">
      <div class="panel-header">
        <div class="header-avatar">
          <el-icon :size="18" color="white"><Avatar /></el-icon>
        </div>
        <div class="header-info">
          <span class="header-title">AI 简历分析</span>
          <span class="header-sub"><i class="online-dot" />{{ currentPhase === 'config' ? '选择简历' : currentPhase === 'chat' ? '分析进行中' : '分析完成' }}</span>
        </div>
        <div class="header-actions">
          <el-icon class="action-icon" @click="resetAnalysis" v-if="currentPhase !== 'config'"><RefreshRight /></el-icon>
          <el-icon class="action-icon" @click="toggleFullscreen">
            <FullScreen v-if="!isFullscreen" />
            <Aim v-else />
          </el-icon>
        </div>
      </div>

      <div class="config-area" v-if="currentPhase === 'config'">
        <div class="config-title">选择简历进行 AI 分析</div>
        <div class="config-form">
          <el-form-item label="选择简历">
            <el-select v-model="selectedDeliverId" placeholder="请选择要分析的简历" style="width: 100%" filterable>
              <el-option v-for="item in deliverList" :key="item.id" :label="`${item.studentName} - ${item.jobName}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="startAnalysis" :disabled="!selectedDeliverId">
              开始分析
            </el-button>
          </el-form-item>
        </div>
        <div v-if="selectedDeliver" class="resume-preview">
          <h4>简历预览</h4>
          <div class="preview-item">
            <span class="label">候选人：</span>
            <span>{{ selectedDeliver.studentName }}</span>
          </div>
          <div class="preview-item">
            <span class="label">学号：</span>
            <span>{{ selectedDeliver.studentNo }}</span>
          </div>
          <div class="preview-item">
            <span class="label">专业：</span>
            <span>{{ selectedDeliver.majorName }}</span>
          </div>
          <div class="preview-item">
            <span class="label">投递岗位：</span>
            <span>{{ selectedDeliver.jobName }}</span>
          </div>
          <div class="preview-item">
            <span class="label">期望城市：</span>
            <span>{{ selectedDeliver.expectCity }}</span>
          </div>
          <div class="preview-item">
            <span class="label">期望薪资：</span>
            <span>{{ selectedDeliver.expectSalary }}</span>
          </div>
          <div class="preview-item">
            <span class="label">自我评价：</span>
            <span>{{ selectedDeliver.selfIntro }}</span>
          </div>
          <div class="preview-item">
            <span class="label">教育经历：</span>
            <span>{{ selectedDeliver.educationExp }}</span>
          </div>
          <div class="preview-item">
            <span class="label">实习经历：</span>
            <span>{{ selectedDeliver.practiceExp }}</span>
          </div>
          <div class="preview-item">
            <span class="label">技能证书：</span>
            <span>{{ selectedDeliver.certs }}</span>
          </div>
          <div class="preview-item" v-if="selectedDeliver.resumeFile">
            <span class="label">PDF简历：</span>
            <a :href="selectedDeliver.resumeFile" target="_blank" class="pdf-link">点击查看</a>
          </div>
        </div>
      </div>

      <div class="chat-area" v-else-if="currentPhase === 'chat'">
        <div class="message-list" ref="messageListRef">
          <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
            <div class="msg-avatar">
              <el-icon v-if="msg.role === 'assistant'"><Cpu /></el-icon>
              <el-icon v-else><User /></el-icon>
            </div>
            <div v-if="msg.role === 'assistant'" class="bubble assistant">
              <span v-html="renderMd(msg.content)"></span>
              <span v-if="index === messages.length - 1 && streaming" class="cursor-blink">|</span>
            </div>
            <div v-else class="bubble user">
              {{ msg.content }}
            </div>
          </div>
          <div class="msg-row assistant" v-if="waiting">
            <div class="msg-avatar"><el-icon><Cpu /></el-icon></div>
            <div class="bubble assistant typing-bubble">
              <span class="dot" /><span class="dot" /><span class="dot" />
            </div>
          </div>
        </div>

        <div class="input-area">
          <el-input
              v-model="inputText"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入问题，Enter 发送"
              resize="none"
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="streaming || waiting"
              class="chat-input"
          />
          <el-button
              type="danger"
              :disabled="streaming || waiting"
              @click="finishAnalysis"
              class="finish-btn"
          >
            生成报告
          </el-button>
          <el-button
              type="primary"
              :icon="Promotion"
              circle
              :disabled="!inputText.trim() || streaming || waiting"
              @click="sendMessage"
              class="send-btn"
          />
        </div>
      </div>

      <div class="report-area" v-else-if="currentPhase === 'report'">
        <div class="report-content" v-html="renderMd(reportContent)"></div>
        <div class="report-actions">
          <el-button type="primary" @click="resetAnalysis">重新分析</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Avatar, Close, Cpu, User, Promotion, FullScreen, Aim, RefreshRight } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const API_STREAM_URL = '/api/resume-ai/stream'
const API_SUMMARY_URL = '/resume-ai/summary'

const currentPhase = ref('config')
const selectedDeliverId = ref(null)
const deliverList = ref([])
const selectedDeliver = ref(null)
const sessionId = ref(crypto.randomUUID())

const isFullscreen = ref(false)
const panelEnter = ref(false)
const panelLeave = ref(false)
const inputText = ref('')
const streaming = ref(false)
const waiting = ref(false)
const messageListRef = ref(null)
const messages = ref([])
const reportContent = ref('')
const md = new MarkdownIt()

const fetchDeliverList = async () => {
  const data = await request.get('/hr/deliver/page', {
    params: { page: 1, pageSize: 100 }
  })
  deliverList.value = data.list || []
}

const getSelectedDeliver = computed(() => {
  return deliverList.value.find(item => item.id === selectedDeliverId.value)
})

onMounted(() => {
  fetchDeliverList()
  nextTick(() => {
    panelEnter.value = true
  })

  const deliverId = route.query.deliverId || route.query.resumeId
  if (deliverId) {
    selectedDeliverId.value = parseInt(deliverId)
  }
})

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  nextTick(scrollToBottom)
}

function resetAnalysis() {
  currentPhase.value = 'config'
  sessionId.value = crypto.randomUUID()
  messages.value = []
  reportContent.value = ''
  inputText.value = ''
  fetchDeliverList()
}

function parseSSEData(text) {
  if (!text) return ''
  let result = text.replace(/^data:\s*/gm, '')
  result = result.replace(/\n/g, '')
  return result
}

async function startAnalysis() {
  if (!selectedDeliverId.value) return

  selectedDeliver.value = getSelectedDeliver.value

  currentPhase.value = 'chat'
  messages.value = []
  waiting.value = true

  const baseUrl = import.meta.env.VITE_BASE_URL || ''
  const url = `${baseUrl}${API_STREAM_URL}?message=${encodeURIComponent('请帮我分析这份简历')}&sessionId=${sessionId.value}&deliverId=${selectedDeliverId.value}`

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': localStorage.getItem('token') || ''
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    waiting.value = false
    streaming.value = true
    messages.value.push({ role: 'assistant', content: '' })
    const lastIdx = messages.value.length - 1

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const cleanChunk = parseSSEData(chunk)
      if (cleanChunk) {
        messages.value[lastIdx].content += cleanChunk
        await nextTick(scrollToBottom)
      }
    }

    streaming.value = false
  } catch (e) {
    waiting.value = false
    streaming.value = false
    console.error('简历分析连接失败:', e)
    messages.value.push({ role: 'assistant', content: `分析连接失败: ${e.message || '请稍后重试'}` })
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value || waiting.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  waiting.value = true
  await nextTick(scrollToBottom)

  const baseUrl = import.meta.env.VITE_BASE_URL || ''
  const url = `${baseUrl}${API_STREAM_URL}?message=${encodeURIComponent(text)}&sessionId=${sessionId.value}&deliverId=${selectedDeliverId.value}`

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': localStorage.getItem('token') || ''
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    waiting.value = false
    streaming.value = true
    messages.value.push({ role: 'assistant', content: '' })
    const lastIdx = messages.value.length - 1

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const cleanChunk = parseSSEData(chunk)
      if (cleanChunk) {
        messages.value[lastIdx].content += cleanChunk
        await nextTick(scrollToBottom)
      }
    }

    streaming.value = false
  } catch (e) {
    waiting.value = false
    streaming.value = false
    console.error('简历分析连接失败:', e)
    messages.value.push({ role: 'assistant', content: `分析连接失败: ${e.message || '请稍后重试'}` })
  }
}

async function finishAnalysis() {
  if (streaming.value || waiting.value) return

  waiting.value = true
  messages.value.push({ role: 'user', content: '请生成完整的简历分析报告' })

  try {
    const baseUrl = import.meta.env.VITE_BASE_URL || ''
    const url = `${baseUrl}${API_SUMMARY_URL}?sessionId=${sessionId.value}&deliverId=${selectedDeliverId.value}`
    const report = await request.post(url)

    waiting.value = false
    reportContent.value = report
    currentPhase.value = 'report'
  } catch (e) {
    waiting.value = false
    messages.value.push({ role: 'assistant', content: '生成报告失败，请稍后重试' })
  }
}

function scrollToBottom() {
  if (messageListRef.value)
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
}

function renderMd(content) {
  return md.render(content || '')
}
</script>

<style scoped>
.hr-resume-ai {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
}

.ai-chat-panel {
  width: 100%;
  max-width: 800px;
  height: calc(100vh - 80px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 48px rgba(0, 0, 0, .18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform: scale(0.95);
  opacity: 0;
  transition: transform .3s ease, opacity .3s ease;
}

.ai-chat-panel.panel-enter {
  transform: scale(1);
  opacity: 1;
}

.ai-chat-panel.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  border-radius: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.header-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, .75);
  display: flex;
  align-items: center;
  gap: 4px;
}

.online-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 5px rgba(255, 255, 255, .8);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-icon {
  color: rgba(255, 255, 255, .8);
  cursor: pointer;
  font-size: 16px;
  transition: color .15s;
}

.action-icon:hover {
  color: white;
}

.config-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.config-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  text-align: center;
}

.config-form {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.config-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.config-form :deep(.el-form-item__label) {
  font-size: 13px;
}

.resume-preview {
  background: white;
  padding: 16px;
  border-radius: 8px;
}

.resume-preview h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.preview-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
  line-height: 1.6;
}

.preview-item .label {
  color: #909399;
  flex-shrink: 0;
  width: 80px;
}

.pdf-link {
  color: #67c23a;
  text-decoration: underline;
  cursor: pointer;
}

.pdf-link:hover {
  color: #85ce61;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8f9fa;
}

.message-list::-webkit-scrollbar {
  width: 4px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #e8f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  color: #67c23a;
}

.bubble {
  max-width: min(600px, 70%);
  padding: 10px 13px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.bubble.assistant {
  background: white;
  color: #303133;
  border-radius: 2px 12px 12px 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, .08);
}

.bubble.user {
  background: #67c23a;
  color: white;
  border-radius: 12px 2px 12px 12px;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 12px 16px;
}

.dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: dotBounce 1.4s infinite ease-in-out;
}

.dot:nth-child(2) { animation-delay: .16s; }
.dot:nth-child(3) { animation-delay: .32s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: translateY(0); background: #c0c4cc; }
  40% { transform: translateY(-7px); background: #67c23a; }
}

.cursor-blink {
  color: #67c23a;
  animation: cursorBlink .7s infinite;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid #ebeef5;
  background: white;
  flex-shrink: 0;
}

.chat-input { flex: 1; }

.chat-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  padding: 8px 10px;
  resize: none;
}

.finish-btn {
  flex-shrink: 0;
}

.send-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
}

.report-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.report-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  font-size: 14px;
  line-height: 1.8;
}

.report-content :deep(h1), .report-content :deep(h2), .report-content :deep(h3) {
  margin: 16px 0 8px 0;
  color: #303133;
}

.report-content :deep(p) {
  margin: 8px 0;
}

.report-actions {
  padding: 12px 20px;
  border-top: 1px solid #ebeef5;
  background: white;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.bubble.assistant :deep(p) {
  margin: 0;
}

.report-content :deep(ul), .report-content :deep(ol) {
  padding-left: 20px;
}

.report-content :deep(li) {
  margin: 4px 0;
}
</style>