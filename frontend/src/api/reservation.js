import axios from 'axios'
import { ElMessage } from 'element-plus'

const USER_KEY = 'reservation-user'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 响应拦截：成功直接解包 Result.data；失败只弹出后端返回的 message，不自行编造文案
api.interceptors.response.use(
  (response) => {
    const body = response.data
    return body.code === 0 ? body.data : Promise.reject(body)
  },
  (error) => {
    const message = error.response?.data?.message
    // 冲突(409)等失败原因由后端给出，这里只负责展示
    if (message) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export function getUser() {
  return localStorage.getItem(USER_KEY) || ''
}

export function setUser(name) {
  localStorage.setItem(USER_KEY, name)
}

/** 器材卡片列表 */
export function fetchDevices() {
  return api.get('/devices')
}

/** 提交预约：{ deviceId, date: 'YYYY-MM-DD', period: '上午'|'下午', user } */
export function createReservation(payload) {
  return api.post('/reservations', payload)
}

/** 我的预约成功次数 */
export function fetchStats(user) {
  return api.get('/reservations/stats', { params: { user } })
}

/** 我的预约历史列表（按提交时间倒序） */
export function fetchMyReservations(user) {
  return api.get('/reservations', { params: { user } })
}

/** 我的预约历史（分页，按预约日期倒序）：{ list, total, page, size } */
export function fetchReservationsPage(user, page, size) {
  return api.get('/reservations/page', { params: { user, page, size } })
}

/** 取消预约（仅本人）：404 记录不存在 / 403 非本人，文案由后端下发 */
export function cancelReservation(id, user) {
  return api.delete(`/reservations/${id}`, { params: { user } })
}
