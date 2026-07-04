<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import {
  ElMenu,
  ElMenuItem,
  ElSubMenu,
  ElHeader,
  ElAside,
  ElContainer,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElBadge,
  ElIcon
} from 'element-plus'
import AiChat from '@/components/AiChat.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const collapsed = ref(false)

const isStudent = computed(() => store.getters.userType === 'student')

const menus = computed(() => {
  const userType = store.getters.userType
  const menuConfig = {
    admin: [
      { path: '/admin/dashboard', name: '首页', icon: 'el-icon-house' },
      { path: '/admin/users', name: '用户管理', icon: 'el-icon-user' },
      { path: '/admin/user-audit', name: '用户审核', icon: 'el-icon-circle-check' },
      { path: '/admin/majors', name: '专业管理', icon: 'el-icon-school' },
      { path: '/admin/audit', name: '企业审核', icon: 'el-icon-check-circle' },
      { path: '/stat', name: '数据统计', icon: 'el-icon-data-analysis' },
      { path: '/notice', name: '消息通知', icon: 'el-icon-bell' }
    ],
    job_office: [
      { path: '/teacher/index', name: '首页', icon: 'el-icon-house' },
      { path: '/teacher/enterprise', name: '企业管理', icon: 'el-icon-office-building' },
      { path: '/teacher/jobs', name: '岗位管理', icon: 'el-icon-suitcase' },
      { path: '/admin/audit', name: '企业审核', icon: 'el-icon-check-circle' },
      { path: '/stat', name: '数据统计', icon: 'el-icon-data-analysis' },
      { path: '/notice', name: '消息通知', icon: 'el-icon-bell' }
    ],
    teacher: [
      { path: '/teacher/index', name: '首页', icon: 'el-icon-house' },
      { path: '/teacher/enterprise', name: '企业管理', icon: 'el-icon-office-building' },
      { path: '/teacher/jobs', name: '岗位管理', icon: 'el-icon-suitcase' },
      { path: '/teacher/deliver', name: '投递统计', icon: 'el-icon-document' },
      { path: '/notice', name: '消息通知', icon: 'el-icon-bell' }
    ],
    hr: [
      { path: '/hr/recruit', name: '首页', icon: 'el-icon-house' },
      { path: '/hr/audit', name: '企业认证', icon: 'el-icon-certificate' },
      { path: '/hr/jobs', name: '岗位管理', icon: 'el-icon-suitcase' },
      { path: '/hr/deliver', name: '简历收件箱', icon: 'el-icon-inbox' },
      { path: '/hr/resume-ai', name: 'AI简历分析', icon: 'el-icon-magic-stick' },
      { path: '/hr/interview', name: '面试通知', icon: 'el-icon-chat-line-square' },
      { path: '/notice', name: '消息通知', icon: 'el-icon-bell' }
    ],
    student: [
      { path: '/student/jobs', name: '岗位浏览', icon: 'el-icon-suitcase' },
      { path: '/student/resume', name: '我的简历', icon: 'el-icon-document' },
      { path: '/student/deliver', name: '投递记录', icon: 'el-icon-tickets' },
      { path: '/student/interview', name: '面试通知', icon: 'el-icon-chat-line-square' },
      { path: '/notice', name: '消息通知', icon: 'el-icon-bell' }
    ]
  }
  return menuConfig[userType] || []
})

const activeMenu = computed(() => route.path)

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleLogout = async () => {
  await store.dispatch('logout')
  router.push('/login')
}

const goHome = () => {
  const userType = store.getters.userType
  const homePath = {
    admin: '/admin/dashboard',
    job_office: '/teacher/enterprise',
    teacher: '/teacher/index',
    student: '/student/jobs',
    hr: '/hr/recruit'
  }[userType] || '/login'
  router.push(homePath)
}

onMounted(() => {
  store.dispatch('updateUnreadCount')
})
</script>

<template>
  <ElContainer class="layout-container">
    <ElAside :width="collapsed ? '64px' : '200px'" class="aside">
      <div class="logo" @click="goHome">
        <span v-if="!collapsed" class="logo-text">就业管理系统</span>
        <span v-else class="logo-icon">📋</span>
      </div>
      <ElMenu
        :default-active="activeMenu"
        :collapse="collapsed"
        mode="vertical"
        background-color="#212529"
        text-color="#9d9d9d"
        active-text-color="#fff"
        @select="handleMenuSelect"
      >
        <ElMenuItem
          v-for="item in menus"
          :key="item.path"
          :index="item.path"
        >
          <i :class="item.icon"></i>
          <template #title>{{ item.name }}</template>
        </ElMenuItem>
      </ElMenu>
    </ElAside>
    <ElContainer>
      <ElHeader class="header">
        <div class="header-left">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <i :class="collapsed ? 'el-icon-s-fold' : 'el-icon-s-unfold'"></i>
          </button>
        </div>
        <div class="header-right">
          <ElDropdown>
            <span class="user-info">
              <i class="el-icon-user"></i>
              {{ store.getters.userInfo.realName || store.getters.userInfo.username }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem @click="router.push('/profile')">个人资料</ElDropdownItem>
                <ElDropdownItem @click="handleLogout">退出登录</ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
        </div>
      </ElHeader>
      <main class="main-content">
        <router-view />
      </main>
    </ElContainer>
    <!-- AI模拟面试组件 - 仅学生角色可见 -->
    <AiChat v-if="isStudent" />
  </ElContainer>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #212529;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  border-bottom: 1px solid #333;
}

.logo-text {
  white-space: nowrap;
}

.logo-icon {
  font-size: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.collapse-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
  padding: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f5f5;
}
</style>