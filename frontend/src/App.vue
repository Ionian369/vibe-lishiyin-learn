<script setup>
import { ref } from 'vue'
import ReservationView from './views/ReservationView.vue'
import HistoryView from './views/HistoryView.vue'

// 顶部页签切换：不引入路由依赖，页面按需挂载（切到历史页时才加载最新数据）
const activeTab = ref('reserve')
const tabs = [
  { name: 'reserve', label: '器材预约' },
  { name: 'history', label: '预约历史' }
]
</script>

<template>
  <div class="shell">
    <nav class="tabs">
      <button
        v-for="t in tabs"
        :key="t.name"
        class="tab"
        :class="{ active: activeTab === t.name }"
        @click="activeTab = t.name"
      >
        {{ t.label }}
      </button>
    </nav>
    <ReservationView v-if="activeTab === 'reserve'" />
    <HistoryView v-else />
  </div>
</template>

<style>
body {
  margin: 0;
  background: #f5f7fa;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-top: 16px;
}

.tab {
  border: 1px solid #dcdfe6;
  border-bottom: none;
  background: #eef1f6;
  color: #606266;
  font-size: 14px;
  padding: 8px 24px;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
}

.tab.active {
  background: #fff;
  color: #409eff;
  font-weight: 600;
}
</style>
