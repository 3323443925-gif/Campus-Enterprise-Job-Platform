import { createStore } from 'vuex'
import request from '@/utils/request'

const state = {
  token: localStorage.getItem('token') || '',
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
  roles: [],
  unreadCount: 0
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  SET_ROLES(state, roles) {
    state.roles = roles
  },
  SET_UNREAD_COUNT(state, count) {
    state.unreadCount = count
  },
  CLEAR_STATE(state) {
    state.token = ''
    state.userInfo = {}
    state.roles = []
    state.unreadCount = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  async login({ commit }, user) {
    const data = await request.post('/auth/login', user)
    commit('SET_TOKEN', data.userInfo.token)
    commit('SET_USER_INFO', data.userInfo)
    commit('SET_ROLES', [data.userType])
    return data
  },
  async getUserInfo({ commit }) {
    const data = await request.get('/auth/info')
    commit('SET_USER_INFO', data)
    commit('SET_ROLES', [data.userType])
    return data
  },
  async logout({ commit }) {
    try {
      await request.post('/auth/logout')
    } finally {
      commit('CLEAR_STATE')
    }
  },
  async updateUnreadCount({ commit }) {
    const data = await request.get('/notice/list', { params: { page: 1, pageSize: 1, isRead: 0 } })
    commit('SET_UNREAD_COUNT', data.total || 0)
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo,
  roles: state => state.roles,
  userId: state => state.userInfo.id,
  userType: state => state.userInfo.userType,
  unreadCount: state => state.unreadCount,
  isLoggedIn: state => !!state.token
}

export default createStore({
  state,
  mutations,
  actions,
  getters
})