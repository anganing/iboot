<script lang="ts" setup>
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

import { computed, ref } from 'vue';

import { JsonViewer, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElSkeleton,
} from 'element-plus';

import { getSolverSnapshot } from '#/api/aps/solversnapshot';

import ConstraintMatchView from './constraint-match-view.vue';

/**
 * 求解快照详情。
 *
 * <p>快照分四类，只有 `CONSTRAINT_MATCH` 值得渲染成表格：它是 Timefold 评分分析的产出，
 * 结构固定且是计划员唯一会反复看的一类。其余三类（输入指纹、结果分派、异常）结构各不相同，
 * 用 JSON 视图原样展示，比为每一类各写一套字段映射更耐改。
 */
const snapshot = ref<ApsSolverSnapshotApi.SolverSnapshot>();

/**
 * 快照类型的中文名。
 *
 * 走 computed 而不是在模板里直接调 `getDictLabel`：那个函数内部对入参做了 `value.toString()`，
 * 数据还没到位时传 undefined 会直接抛错，把整个弹窗渲染打断。
 */
const snapshotTypeLabel = computed(() =>
  snapshot.value?.snapshotType
    ? getDictLabel(DICT_TYPE.APS_SNAPSHOT_TYPE, snapshot.value.snapshotType) ||
      snapshot.value.snapshotType
    : '-',
);

/**
 * 约束命中且解析出了约束才渲染对照表，其余一律走 JSON 视图。
 *
 * 早期版本写的约束命中快照是另一套结构，后端解析不出来。这种情况回落到原始 JSON 而不是
 * 显示一张空表：快照的价值在于追溯，让人看到原文比看到"无数据"有用。
 */
const showConstraintTable = computed(
  () =>
    snapshot.value?.snapshotType === 'CONSTRAINT_MATCH' &&
    (snapshot.value?.constraintMatch?.constraints?.length ?? 0) > 0,
);

/** JSON 解析失败时回落成原始字符串，快照读不动也要让人看到原文 */
const snapshotContent = computed(() => {
  const json = snapshot.value?.snapshotJson;
  if (!json) {
    return {};
  }
  try {
    return JSON.parse(json);
  } catch {
    return { raw: json };
  }
});

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      snapshot.value = undefined;
      return;
    }
    const data = modalApi.getData<{ id?: number }>();
    if (!data?.id) {
      return;
    }
    modalApi.lock();
    try {
      snapshot.value = await getSolverSnapshot(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-[1080px]" title="求解快照详情">
    <!-- 数据到位后才渲染：字典标签在 snapshotType 还是 undefined 时会取不到值 -->
    <div v-if="snapshot" class="flex flex-col gap-3">
      <ElDescriptions :column="3" border size="small">
        <ElDescriptionsItem label="排程场景">
          {{
            [snapshot.scenarioCode, snapshot.scenarioName]
              .filter(Boolean)
              .join(' · ') || '-'
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本号">
          {{ snapshot.versionNo == null ? '-' : `V${snapshot.versionNo}` }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="快照类型">
          {{ snapshotTypeLabel }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="校验值" :span="3">
          <span class="break-all font-mono text-xs">
            {{ snapshot.checksum || '-' }}
          </span>
        </ElDescriptionsItem>
      </ElDescriptions>

      <ConstraintMatchView
        v-if="showConstraintTable"
        :data="snapshot.constraintMatch"
        :show-context="false"
      />
      <div v-else class="max-h-[480px] overflow-auto">
        <JsonViewer
          :expand-depth="3"
          :value="snapshotContent"
          boxed
          copyable
        />
      </div>
    </div>
    <ElSkeleton v-else :rows="6" animated />
  </Modal>
</template>
