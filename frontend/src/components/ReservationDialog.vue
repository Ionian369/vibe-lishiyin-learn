<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createReservation, getUser } from '../api/reservation'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  device: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const form = ref({ date: '', period: '上午' })
const submitting = ref(false)
// 后端返回的失败原因（如 409 冲突），在弹窗内持久展示，重新提交时清除
const lastError = ref('')

// 每次打开弹窗都重置表单
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      form.value = { date: '', period: '上午' }
      lastError.value = ''
    }
  }
)

// 禁选今天之前、以及今天 +14 天之后的日期（与后端窗口校验同口径，后端为准）
const MAX_ADVANCE_DAYS = 14
function disabledDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const latest = new Date(today)
  latest.setDate(latest.getDate() + MAX_ADVANCE_DAYS + 1)
  return date.getTime() < today.getTime() || date.getTime() >= latest.getTime()
}

function close() {
  emit('update:modelValue', false)
}

async function submit() {
  if (!form.value.date) {
    ElMessage.warning('请选择预约日期')
    return
  }
  submitting.value = true
  try {
    await createReservation({
      deviceId: props.device.id,
      date: form.value.date,
      period: form.value.period,
      user: getUser()
    })
    ElMessage.success(`预约成功：${props.device.name} ${form.value.date} ${form.value.period}`)
    close()
    emit('success')
  } catch (error) {
    // 记录后端返回的原因（如 409 冲突文案），在弹窗内持久展示；
    // 拦截器里也会弹出全局 toast，弹窗保持打开方便直接改选重试
    lastError.value = error?.response?.data?.message || ''
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="device ? `预约：${device.name}` : '预约'"
    width="420px"
    @update:model-value="close"
    @closed="close"
  >
    <el-alert
      v-if="lastError"
      :title="lastError"
      type="error"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    />
    <el-form label-width="72px">
      <el-form-item label="预约日期">
        <el-date-picker
          v-model="form.date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          :disabled-date="disabledDate"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="预约时段">
        <el-radio-group v-model="form.period">
          <el-radio-button value="上午">上午</el-radio-button>
          <el-radio-button value="下午">下午</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
    </template>
  </el-dialog>
</template>
