<template>
  <div class="ai-chat-fab" @click="openPanel" v-show="!visible">
    <el-icon :size="24" color="white"><ChatDotRound /></el-icon>
    <span v-if="unread > 0" class="fab-badge">{{ unread }}</span>
  </div>

  <!-- 面板 -->
  <div :class="['ai-chat-panel', { fullscreen: isFullscreen, 'panel-enter': panelEnter, 'panel-leave': panelLeave }]"
       v-show="panelVisible">
    <div class="panel-header">
      <div class="header-avatar">
        <el-icon :size="18" color="white"><Avatar /></el-icon>
      </div>
      <div class="header-info">
        <span class="header-title">AI 模拟面试</span>
        <span class="header-sub"><i class="online-dot" />{{ interviewPhase === 'config' ? '配置面试' : interviewPhase === 'chat' ? '面试进行中' : '面试结束' }}</span>
      </div>
      <div class="header-actions">
        <el-icon class="action-icon" @click="resetInterview" v-if="interviewPhase !== 'config'"><RefreshRight /></el-icon>
        <el-icon class="action-icon" @click="toggleFullscreen">
          <FullScreen v-if="!isFullscreen" />
          <Aim v-else />
        </el-icon>
        <el-icon class="action-icon" @click.stop="closePanel"><Close /></el-icon>
      </div>
    </div>

    <!-- 面试配置阶段 -->
    <div class="config-area" v-if="interviewPhase === 'config'">
      <div class="config-title">开始模拟面试</div>
      <el-form :model="interviewConfig" label-width="90px" class="config-form">
        <el-form-item label="面试岗位">
          <el-input v-model="interviewConfig.jobPosition" placeholder="如：Java开发工程师" />
        </el-form-item>
        <el-form-item label="技术分类">
          <el-select v-model="interviewConfig.jobCategory" placeholder="选择技术方向或输入自定义" style="width: 100%" filterable allow-create default-first-option append-to-body popper-class="ai-chat-dropdown">
            <el-option label="Java后端" value="Java后端" />
            <el-option label="前端开发" value="前端开发" />
            <el-option label="Python" value="Python" />
            <el-option label="Go语言" value="Go语言" />
            <el-option label="数据库" value="数据库" />
            <el-option label="算法" value="算法" />
            <el-option label="运维/测试" value="运维/测试" />
            <el-option label="全栈开发" value="全栈开发" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="interviewConfig.difficultyLevel" placeholder="选择难度" style="width: 100%" append-to-body popper-class="ai-chat-dropdown">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
            <el-option label="专家" value="专家" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试风格">
          <el-select v-model="interviewConfig.interviewStyle" placeholder="选择风格" style="width: 100%" append-to-body popper-class="ai-chat-dropdown">
            <el-option label="温和引导" value="温和" />
            <el-option label="标准面试" value="标准" />
            <el-option label="高压严厉" value="高压严厉" />
            <el-option label="反问刁难" value="反问刁难" />
          </el-select>
        </el-form-item>
        <el-form-item label="压力面试">
          <el-switch v-model="interviewConfig.openPressureTest" />
        </el-form-item>
        <el-form-item label="面试时长">
          <el-select v-model="interviewConfig.totalTimeMinute" placeholder="选择时长" style="width: 100%" append-to-body popper-class="ai-chat-dropdown">
            <el-option label="15分钟" :value="15" />
            <el-option label="30分钟" :value="30" />
            <el-option label="45分钟" :value="45" />
            <el-option label="60分钟" :value="60" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型要求">
          <el-select 
            v-model="interviewConfig.questionTypes" 
            multiple 
            filterable 
            allow-create 
            default-first-option
            placeholder="选择题型或输入自定义题型" 
            style="width: 100%"
            append-to-body
            popper-class="ai-chat-dropdown"
          >
            <el-option label="基础概念" value="基础概念" />
            <el-option label="项目经验" value="项目经验" />
            <el-option label="情景题" value="情景题" />
            <el-option label="代码实现" value="代码实现" />
            <el-option label="系统设计" value="系统设计" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="startInterview" :disabled="!isConfigValid">
            开始面试
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 面试进行阶段 -->
    <div class="chat-area" v-else-if="interviewPhase === 'chat'">
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
            placeholder="输入回答，Enter 发送"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="streaming || waiting"
            class="chat-input"
        />
        <el-button
            type="danger"
            :disabled="streaming || waiting"
            @click="finishInterview"
            class="finish-btn"
        >
          结束面试
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

    <!-- 面试报告阶段 -->
    <div class="report-area" v-else-if="interviewPhase === 'report'">
      <div class="report-content" v-html="renderMd(reportContent)"></div>
      <div class="report-actions">
        <el-button type="primary" @click="resetInterview">重新面试</el-button>
        <el-button @click="closePanel">关闭</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { ChatDotRound, Avatar, Close, Cpu, User, Promotion, FullScreen, Aim, RefreshRight } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import request from '@/utils/request'

const API_STREAM_URL = '/api/interview/stream'
const API_FINISH_URL = '/interview/finish'

// 面试阶段：config(配置), chat(进行), report(结束)
const interviewPhase = ref('config')

// 面试配置
const interviewConfig = ref({
  jobPosition: '',
  jobCategory: '',
  difficultyLevel: '中级',
  interviewStyle: '标准',
  openPressureTest: false,
  totalTimeMinute: 30,
  questionTypes: []
})

// 会话ID
const sessionId = ref(crypto.randomUUID())

// 验证配置是否有效
const isConfigValid = computed(() => {
  return interviewConfig.value.jobPosition && 
         interviewConfig.value.jobCategory && 
         interviewConfig.value.difficultyLevel
})

// UI状态
const visible = ref(false)
const panelVisible = ref(false)
const panelEnter = ref(false)
const panelLeave = ref(false)
const isFullscreen = ref(false)
const inputText = ref('')
const streaming = ref(false)
const waiting = ref(false)
const unread = ref(0)
const messageListRef = ref(null)
const messages = ref([])
const reportContent = ref('')
const md = new MarkdownIt()

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  nextTick(scrollToBottom)
}

function openPanel() {
  visible.value = true
  panelVisible.value = true
  unread.value = 0
  nextTick(() => {
    panelEnter.value = true
    panelLeave.value = false
    nextTick(scrollToBottom)
  })
}

function closePanel() {
  visible.value = false
  panelEnter.value = false
  panelLeave.value = true
  setTimeout(() => {
    panelVisible.value = false
    panelLeave.value = false
    isFullscreen.value = false
    visible.value = false
  }, 200)
}

function resetInterview() {
  interviewPhase.value = 'config'
  sessionId.value = crypto.randomUUID()
  messages.value = []
  reportContent.value = ''
  inputText.value = ''
}

// 解析SSE数据，去掉 "data:" 前缀和换行
function parseSSEData(text) {
  if (!text) return ''
  // 去掉 "data:" 前缀
  let result = text.replace(/^data:\s*/gm, '')
  // 去掉换行符
  result = result.replace(/\n/g, '')
  return result
}

async function startInterview() {
  if (!isConfigValid.value) return
  
  interviewPhase.value = 'chat'
  messages.value = []
  waiting.value = true
  
  // 发送开场白，启动面试
  const config = { ...interviewConfig.value }
  
  // 使用fetch发送POST请求，接收SSE流
  // 注意：VITE_BASE_URL可能未定义，使用相对路径让nginx/vite代理处理
  const baseUrl = import.meta.env.VITE_BASE_URL || ''
  const url = `${baseUrl}${API_STREAM_URL}?message=${encodeURIComponent('我准备好了，请开始面试')}&sessionId=${sessionId.value}`
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': localStorage.getItem('token') || ''
      },
      body: JSON.stringify(config)
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
    console.error('面试连接失败:', e)
    messages.value.push({ role: 'assistant', content: `面试官连接失败: ${e.message || '请稍后重试'}` })
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value || waiting.value) return
  
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  waiting.value = true
  await nextTick(scrollToBottom)
  
  const config = { ...interviewConfig.value }
  const baseUrl = import.meta.env.VITE_BASE_URL || ''
  const url = `${baseUrl}${API_STREAM_URL}?message=${encodeURIComponent(text)}&sessionId=${sessionId.value}`
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': localStorage.getItem('token') || ''
      },
      body: JSON.stringify(config)
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
    console.error('面试连接失败:', e)
    messages.value.push({ role: 'assistant', content: `面试官连接失败: ${e.message || '请稍后重试'}` })
  }
}

async function finishInterview() {
  if (streaming.value || waiting.value) return
  
  waiting.value = true
  messages.value.push({ role: 'user', content: '面试结束，请生成评测报告' })
  
  try {
    const config = { ...interviewConfig.value }
    const report = await request.post(`${API_FINISH_URL}?sessionId=${sessionId.value}`, config)
    
    waiting.value = false
    reportContent.value = report
    interviewPhase.value = 'report'
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
.ai-chat-fab {
  position: fixed;
  bottom: 28px;
  right: 28px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #1d7fe4);
  box-shadow: 0 4px 20px rgba(64, 158, 255, .5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 9997;
  transition: transform .2s, box-shadow .2s;
}

.ai-chat-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(64, 158, 255, .65);
}

.fab-badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  background: #f56c6c;
  border: 2px solid white;
  font-size: 11px;
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-chat-panel {
  position: fixed;
  bottom: 96px;
  right: 28px;
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 48px rgba(0, 0, 0, .18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9998;
  transform: scale(0.6) translateY(30px);
  opacity: 0;
  transform-origin: bottom right;
  pointer-events: none;
  transition: width .3s ease, height .3s ease, bottom .3s ease,
  right .3s ease, border-radius .3s ease;
}

.ai-chat-panel.panel-enter {
  animation: chatPopIn .28s cubic-bezier(.34, 1.56, .64, 1) forwards;
  pointer-events: auto;
}

.ai-chat-panel.panel-leave {
  animation: chatPopOut .2s ease-in forwards;
  pointer-events: none;
}

.ai-chat-panel.fullscreen {
  bottom: 0;
  right: 0;
  width: 100vw;
  height: 100vh;
  border-radius: 0;
}

@keyframes chatPopIn {
  from {
    transform: scale(.6) translateY(30px);
    opacity: 0;
    transform-origin: bottom right;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
    transform-origin: bottom right;
  }
}

@keyframes chatPopOut {
  from {
    transform: scale(1) translateY(0);
    opacity: 1;
    transform-origin: bottom right;
  }
  to {
    transform: scale(.6) translateY(20px);
    opacity: 0;
    transform-origin: bottom right;
  }
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #409eff, #1d7fe4);
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
  background: #67c23a;
  box-shadow: 0 0 5px #67c23a;
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

/* 配置区域 */
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
}

.config-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.config-form :deep(.el-form-item__label) {
  font-size: 13px;
}

:global(.ai-chat-dropdown) {
  z-index: 10001 !important;
}

/* 聊天区域 */
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
  color: #409eff;
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
  background: #409eff;
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
  40% { transform: translateY(-7px); background: #409eff; }
}

.cursor-blink {
  color: #409eff;
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

/* 报告区域 */
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

@media (max-width: 480px) {
  .ai-chat-panel {
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 70vh;
    border-radius: 16px 16px 0 0;
  }
  .ai-chat-fab {
    bottom: 20px;
    right: 20px;
  }
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