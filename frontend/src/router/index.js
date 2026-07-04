import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/main',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: '/admin/dashboard',
        name: 'AdminIndex',
        component: () => import('@/views/admin/AdminIndex.vue'),
        meta: { roles: ['admin'], title: '管理员首页' }
      },
      {
        path: '/admin/users',
        name: 'UserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { roles: ['admin'], title: '用户管理' }
      },
      {
        path: '/admin/user-audit',
        name: 'UserAudit',
        component: () => import('@/views/admin/UserAudit.vue'),
        meta: { roles: ['admin'], title: '用户审核' }
      },
      {
        path: '/admin/majors',
        name: 'MajorList',
        component: () => import('@/views/admin/MajorList.vue'),
        meta: { roles: ['admin'], title: '专业管理' }
      },
      {
        path: '/admin/audit',
        name: 'AuditList',
        component: () => import('@/views/admin/AuditList.vue'),
        meta: { roles: ['admin', 'job_office'], title: '企业审核' }
      },
      {
        path: '/hr/recruit',
        name: 'HrIndex',
        component: () => import('@/views/hr/HrIndex.vue'),
        meta: { roles: ['hr'], title: 'HR首页' }
      },
      {
        path: '/hr/audit',
        name: 'AuditApply',
        component: () => import('@/views/hr/AuditApply.vue'),
        meta: { roles: ['hr'], title: '企业认证' }
      },
      {
        path: '/hr/deliver',
        name: 'DeliverBox',
        component: () => import('@/views/hr/DeliverBox.vue'),
        meta: { roles: ['hr'], title: '简历收件箱' }
      },
      {
        path: '/hr/jobs',
        name: 'HrJobList',
        component: () => import('@/views/hr/HrJobList.vue'),
        meta: { roles: ['hr'], title: '岗位管理' }
      },
      {
        path: '/hr/interview',
        name: 'SendInterview',
        component: () => import('@/views/hr/SendInterview.vue'),
        meta: { roles: ['hr'], title: '面试通知' }
      },
      {
        path: '/hr/resume-ai',
        name: 'HrResumeAI',
        component: () => import('@/views/hr/HrResumeAI.vue'),
        meta: { roles: ['hr'], title: 'AI简历分析' }
      },
      {
        path: '/teacher/enterprise',
        name: 'MyEnterprise',
        component: () => import('@/views/teacher/MyEnterprise.vue'),
        meta: { roles: ['teacher', 'job_office'], title: '企业管理' }
      },
      {
        path: '/teacher/jobs',
        name: 'JobList',
        component: () => import('@/views/teacher/JobList.vue'),
        meta: { roles: ['teacher', 'job_office'], title: '岗位管理' }
      },
      {
        path: '/teacher/deliver',
        name: 'DeliverList',
        component: () => import('@/views/teacher/DeliverList.vue'),
        meta: { roles: ['teacher'], title: '投递统计' }
      },
      {
        path: '/teacher/index',
        name: 'TeacherIndex',
        component: () => import('@/views/teacher/TeacherIndex.vue'),
        meta: { roles: ['teacher', 'job_office'], title: '教师首页' }
      },
      {
        path: '/student/jobs',
        name: 'StudentJobs',
        component: () => import('@/views/student/StudentJobs.vue'),
        meta: { roles: ['student'], title: '岗位浏览' }
      },
      {
        path: '/student/resume',
        name: 'StudentResume',
        component: () => import('@/views/student/StudentResume.vue'),
        meta: { roles: ['student'], title: '我的简历' }
      },
      {
        path: '/student/deliver',
        name: 'StudentDeliver',
        component: () => import('@/views/student/StudentDeliver.vue'),
        meta: { roles: ['student'], title: '投递记录' }
      },
      {
        path: '/student/interview',
        name: 'StudentInterview',
        component: () => import('@/views/student/StudentInterview.vue'),
        meta: { roles: ['student'], title: '面试通知' }
      },
      {
        path: '/notice',
        name: 'NoticeList',
        component: () => import('@/views/notice/NoticeList.vue'),
        meta: { roles: ['admin', 'teacher', 'job_office', 'student', 'hr'], title: '消息通知' }
      },
      {
        path: '/profile',
        name: 'ProfileEdit',
        component: () => import('@/views/ProfileEdit.vue'),
        meta: { roles: ['admin', 'teacher', 'job_office', 'student', 'hr'], title: '个人资料' }
      },
      {
        path: '/stat',
        name: 'StatScreen',
        component: () => import('@/views/stat/StatScreen.vue'),
        meta: { roles: ['admin', 'job_office'], title: '数据统计' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title || '就业管理系统'
  
  const requiresAuth = to.meta.requiresAuth !== false
  const isLoggedIn = store.getters.isLoggedIn
  
  if (requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }
  
  if (to.path === '/login' && isLoggedIn) {
    const userType = store.getters.userType
    const homePath = getHomePath(userType)
    next(homePath)
    return
  }
  
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userType = store.getters.userType
    if (!to.meta.roles.includes(userType)) {
      next('/login')
      return
    }
  }
  
  next()
})

function getHomePath(userType) {
  switch (userType) {
    case 'admin':
      return '/admin/dashboard'
    case 'job_office':
      return '/teacher/enterprise'
    case 'teacher':
      return '/teacher/index'
    case 'student':
      return '/student/jobs'
    case 'hr':
      return '/hr/recruit'
    default:
      return '/login'
  }
}

export default router