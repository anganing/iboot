<script lang="ts" setup>
import type { ApsVersionApi } from '#/api/aps/version';

import { computed, onUnmounted, ref, watch, watchEffect } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';
import { formatDateTime } from '@vben/utils';

import { useWebSocket } from '@vueuse/core';
import {
  ElAlert,
  ElButton,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElMessage,
  ElProgress,
  ElTag,
  ElTimeline,
  ElTimelineItem,
  ElTooltip,
} from 'element-plus';

import { cancelSolve, getSolveProgress } from '#/api/aps/version';

/** 终态时通知列表页刷新 */
const emit = defineEmits(['finished']);

/** WebSocket 消息类型，与后端 ApsSolveProgressService.MESSAGE_TYPE 对齐 */
const MESSAGE_TYPE = 'aps-solve-progress';

/** 求解终态：收到后停止等待并刷新列表 */
const FINISHED_STATUS = new Set(['CANCELLED', 'FAILED', 'SUCCESS']);

const STATUS_TEXT: Record<string, string> = {
  CANCELLED: '已取消',
  FAILED: '求解失败',
  PREPARING: '准备输入',
  SAVING: '保存结果',
  SOLVING: '求解中',
  SUCCESS: '求解完成',
};

const versionId = ref<number>();
const progress = ref<ApsVersionApi.SolveProgress>();
const cancelling = ref(false);

const accessStore = useAccessStore();
// 用 refreshToken 而不是 accessToken：WebSocket 连接无法方便地刷新访问令牌
const socketUrl = computed(
  () =>
    `${`${import.meta.env.VITE_BASE_URL}/infra/ws`.replace('http', 'ws')}?token=${
      accessStore.refreshToken as string
    }`,
);

const finished = computed(() =>
  FINISHED_STATUS.has(progress.value?.status ?? ''),
);
const statusText = computed(
  () => STATUS_TEXT[progress.value?.status ?? ''] ?? '等待求解',
);
const statusType = computed(() => {
  const status = progress.value?.status;
  if (status === 'SUCCESS') {
    return 'success';
  }
  if (status === 'FAILED') {
    return 'danger';
  }
  return status === 'CANCELLED' ? 'info' : 'primary';
});
const progressStatus = computed(() => {
  const status = progress.value?.status;
  if (status === 'SUCCESS') {
    return 'success';
  }
  return status === 'FAILED' || status === 'CANCELLED' ? 'exception' : undefined;
});
const logs = computed(() => [...(progress.value?.logs ?? [])].reverse());

/** 得分为空表示还没拿到第一个可行解，显示"-"而不是 0 */
function formatScore(value?: number) {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '-';
}

/** WebSocket 只在抽屉打开期间连接，关闭即断开，不占着连接不放 */
const { data, close, open } = useWebSocket(socketUrl, {
  autoReconnect: true,
  heartbeat: true,
  immediate: false,
});

/** 监听推送：只处理属于当前版本的求解进度 */
watchEffect(() => {
  if (!data.value || data.value === 'pong') {
    return;
  }
  try {
    const message = JSON.parse(data.value as string);
    if (message.type !== MESSAGE_TYPE) {
      return;
    }
    const content = JSON.parse(message.content) as ApsVersionApi.SolveProgress;
    if (content.versionId !== versionId.value) {
      return;
    }
    progress.value = content;
  } catch {
    // 消息格式异常不该影响抽屉：进度是观测手段，静默跳过这一条即可
  }
});

/** 收到终态后刷新列表，让状态与得分同步过来 */
watch(finished, (value) => {
  if (value) {
    emit('finished');
  }
});

/** 拉一次进度：打开抽屉与断线重连后用它补齐已经发生的进度 */
async function loadProgress() {
  if (!versionId.value) {
    return;
  }
  progress.value = (await getSolveProgress(versionId.value)) ?? undefined;
}

/** 取消求解：已取得的最优解不落库，版本置为求解失败 */
async function handleCancel() {
  if (!versionId.value) {
    return;
  }
  cancelling.value = true;
  try {
    await cancelSolve(versionId.value);
    ElMessage.success('已发送取消指令');
    await loadProgress();
  } finally {
    cancelling.value = false;
  }
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      close();
      versionId.value = undefined;
      progress.value = undefined;
      return;
    }
    versionId.value = drawerApi.getData<{ versionId?: number }>()?.versionId;
    // 先补历史再开推送：反过来会漏掉打开抽屉前已经推过的进度
    await loadProgress();
    open();
  },
});

onUnmounted(() => close());
</script>

<template>
  <Drawer class="w-[720px]" title="排程求解进度">
    <div class="flex h-full min-h-0 flex-col gap-3 px-4 pb-4">
      <ElDescriptions :column="2" border size="small" class="shrink-0">
        <ElDescriptionsItem label="版本编号">
          {{ versionId ?? '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前状态">
          <ElTag :type="statusType" size="small">{{ statusText }}</ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="硬约束得分">
          <ElTooltip
            content="硬约束违反的罚分总和。0 表示当前最优解可行；小于 0 表示仍有无法回避的冲突，求解结束后版本会判为失败"
            placement="top"
          >
            <span class="cursor-help">{{
              formatScore(progress?.hardScore)
            }}</span>
          </ElTooltip>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="软约束得分">
          <ElTooltip
            content="各软约束按权重加权后的罚分总和，越接近 0 越好，求解过程中会持续变小"
            placement="top"
          >
            <span class="cursor-help">{{
              formatScore(progress?.softScore)
            }}</span>
          </ElTooltip>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开始时间">
          {{ progress?.startTime ? formatDateTime(progress.startTime) : '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElProgress
        :percentage="progress?.ratio ?? 0"
        :status="progressStatus"
        :stroke-width="16"
        :text-inside="true"
        class="shrink-0"
      />

      <ElAlert
        v-if="progress?.message"
        :closable="false"
        :title="progress.message"
        :type="progress?.status === 'FAILED' ? 'error' : 'info'"
        class="shrink-0"
      />

      <div class="flex shrink-0 items-center gap-2">
        <ElButton
          :disabled="!versionId || finished"
          :loading="cancelling"
          size="small"
          type="danger"
          @click="handleCancel"
        >
          取消求解
        </ElButton>
        <ElButton size="small" @click="loadProgress">刷新进度</ElButton>
      </div>

      <div class="min-h-0 flex-1 overflow-auto">
        <ElTimeline v-if="logs.length > 0">
          <ElTimelineItem
            v-for="(log, index) in logs"
            :key="index"
            :timestamp="log.occurTime ? formatDateTime(log.occurTime) : ''"
            placement="top"
          >
            {{ log.content }}
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else description="暂无进度日志" />
      </div>
    </div>
  </Drawer>
</template>
