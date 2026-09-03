<script lang="ts" setup>
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { getConstraintMatch } from '#/api/aps/solversnapshot';
import ConstraintMatchView from '#/views/aps/solversnapshot/modules/constraint-match-view.vue';

/**
 * 排程版本的约束命中分析。
 *
 * <p>版本列表上只有硬分、软分两个总数，回答不了"这分数是怎么来的"。这个入口按 versionId
 * 直接取该版本最新一条约束命中快照，省掉去快照列表里按编号翻的一步。
 *
 * <p>没有命中快照时不报错：求解中的版本、求解前生成的历史版本都属于正常情况，
 * 由 ConstraintMatchView 显示空态。
 */
const constraintMatch = ref<ApsSolverSnapshotApi.ConstraintMatch | null>();
const title = ref('约束命中分析');

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      constraintMatch.value = undefined;
      return;
    }
    const data = modalApi.getData<{ title?: string; versionId?: number }>();
    title.value = data?.title
      ? `约束命中分析 - ${data.title}`
      : '约束命中分析';
    if (!data?.versionId) {
      return;
    }
    modalApi.lock();
    try {
      constraintMatch.value = await getConstraintMatch(data.versionId);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-[1080px]" :title="title">
    <ConstraintMatchView :data="constraintMatch" />
  </Modal>
</template>
