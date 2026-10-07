<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelReservation, fetchReservationsPage, getUser } from '../api/reservation'

// 分页状态：页码从 1 开始，与后端契约一致
const page = ref(1)
const size = ref(10)
const total = ref(0)
const records = ref([])
const loading = ref(false)

// 当前用户（无登录场景，姓名存 localStorage）
const userName = ref(getUser())

async function loadPage() {
  const name = userName.value.trim()
  if (!name) {
    records.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const data = await fetchReservationsPage(name, page.value, size.value)
    records.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 提交时间格式化：YYYY-MM-DD HH:mm
function formatTime(value) {
  return (value || '').slice(0, 16).replace('T', ' ')
}

// 翻页直接重查；改每页条数先回到第 1 页，避免落在不存在的页码上
watch(page, loadPage)
watch(size, () => {
  if (page.value !== 1) {
    page.value = 1 // 重置页码会触发 page 的 watch 完成加载
  } else {
    loadPage()
  }
})

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
  // 若删掉的是本页最后一条，先回退一页避免停留在空页
  if (records.value.length === 1 && page.value > 1) {
    page.value -= 1 // 触发 page 的 watch 重新加载
  } else {
    loadPage()
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="page">
    <header class="header">
      <h1 class="title">预约历史（分页）</h1>
    </header>

    <!-- 姓名为空时持久提示，不闪 toast（§4 约定） -->
    <el-alert
      v-if="!userName.trim()"
      title="请先在「器材预约」页填写并保存姓名，再查看预约历史"
      type="info"
      show-icon
      :closable="false"
      class="tip"
    />

    <el-card v-else shadow="never">
      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="deviceName" label="器材" min-width="120" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="period" label="时段" width="80" />
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

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.title {
  margin: 0;
  font-size: 22px;
}

.tip {
  margin-bottom: 16px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
