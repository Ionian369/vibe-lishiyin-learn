<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelReservation, fetchDevices, fetchMyReservations, fetchStats, getUser, setUser } from '../api/reservation'
import DeviceCard from '../components/DeviceCard.vue'
import ReservationDialog from '../components/ReservationDialog.vue'

// 器材列表、预约成功次数与我的预约历史
const devices = ref([])
const count = ref(0)
const myReservations = ref([])

// 当前用户（无登录场景，姓名存 localStorage）
const userName = ref(getUser())

// 预约弹窗
const dialogVisible = ref(false)
const currentDevice = ref(null)

async function refreshStats() {
  const name = userName.value.trim()
  if (!name) {
    count.value = 0
    myReservations.value = []
    return
  }
  // 成功次数与历史列表一起刷新
  const [stats, list] = await Promise.all([fetchStats(name), fetchMyReservations(name)])
  count.value = stats.count
  myReservations.value = list
}

// 提交时间格式化：YYYY-MM-DD HH:mm
function formatTime(value) {
  return (value || '').slice(0, 16).replace('T', ' ')
}

function saveUser() {
  const name = userName.value.trim()
  if (!name) {
    ElMessage.warning('请填写姓名')
    return
  }
  setUser(name)
  refreshStats()
  ElMessage.success('姓名已保存')
}

function openReserve(device) {
  if (!userName.value.trim()) {
    ElMessage.warning('请先在右上角填写并保存姓名')
    return
  }
  currentDevice.value = device
  dialogVisible.value = true
}

// 取消预约（仅本人）：二次确认后调用，失败文案由后端经拦截器弹出
async function cancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定取消「${row.deviceName} ${row.date} ${row.period}」的预约吗？`,
      '取消预约',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' }
    )
  } catch {
    return // 用户点了"再想想"，不打扰
  }
  await cancelReservation(row.id, userName.value.trim())
  ElMessage.success('已取消该预约')
  refreshStats()
}

onMounted(async () => {
  devices.value = await fetchDevices()
  refreshStats()
})
</script>

<template>
  <div class="page">
    <header class="header">
      <div class="brand">
        <h1 class="title">教务器材预约系统</h1>
      </div>
      <div class="toolbar">
        <el-input
          v-model="userName"
          placeholder="你的姓名"
          class="user-input"
          clearable
          @keyup.enter="saveUser"
        />
        <el-button @click="saveUser">保存姓名</el-button>
        <el-tag type="success" size="large" effect="light" class="count-tag">
          累计预约成功 {{ count }} 次
        </el-tag>
      </div>
    </header>

    <main class="cards">
      <DeviceCard
        v-for="device in devices"
        :key="device.id"
        :device="device"
        @reserve="openReserve"
      />
    </main>

    <section class="history">
      <el-card shadow="never">
        <template #header>
          <span class="history-title">我的预约历史</span>
        </template>
        <el-table :data="myReservations" stripe>
          <el-table-column prop="deviceName" label="器材" min-width="120" />
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="period" label="时段" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '待使用' ? 'success' : 'info'" effect="light">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" min-width="150">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" type="danger" plain @click="cancel(row)">取消</el-button>
            </template>
          </el-table-column>
          <template #empty>
            暂无预约记录
          </template>
        </el-table>
      </el-card>
    </section>

    <ReservationDialog
      v-model="dialogVisible"
      :device="currentDevice"
      @success="refreshStats"
    />
  </div>
</template>

<style scoped>
.page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}

.title {
  margin: 0;
  font-size: 22px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-input {
  width: 140px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.history {
  margin-top: 24px;
}

.history-title {
  font-weight: 600;
}
</style>
