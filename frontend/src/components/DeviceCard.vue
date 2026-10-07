<script setup>
defineProps({
  device: { type: Object, required: true }
})

defineEmits(['reserve'])
</script>

<template>
  <el-card class="device-card" shadow="hover">
    <div class="device-name">{{ device.name }}</div>
    <p class="device-desc">{{ device.description }}</p>
    <div class="device-foot">
      <el-tag size="small" effect="plain">教学器材</el-tag>
      <el-button type="primary" @click="$emit('reserve', device)">预约</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.device-card {
  display: flex;
  flex-direction: column;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
}

.device-desc {
  color: #909399;
  font-size: 13px;
  min-height: 40px;
  margin: 8px 0 16px;
}

.device-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
