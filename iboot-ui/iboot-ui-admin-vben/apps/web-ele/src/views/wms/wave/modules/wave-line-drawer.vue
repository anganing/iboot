<script lang="ts" setup>
import type { WmsWaveApi } from '#/api/wms/wave';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import WaveLineList from './wave-line-list.vue';

const summary = ref<WmsWaveApi.Wave>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<WmsWaveApi.Wave>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.waveNo ? `出库波次 ${summary.value.waveNo}` : '波次明细',
);
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-4/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-hidden px-4 pb-4">
      <ElDescriptions
        v-if="summary"
        :column="4"
        border
        size="small"
        class="mb-3 shrink-0"
      >
        <ElDescriptionsItem label="波次编号">
          {{ summary.waveNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="波次类型">
          {{ getDictLabel(DICT_TYPE.WMS_WAVE_TYPE, summary.waveType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属仓库">
          {{ summary.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="波次状态">
          {{ getDictLabel(DICT_TYPE.WMS_WAVE_STATUS, summary.status) || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <WaveLineList
        v-if="summary"
        :wave-id="summary.id"
        :wave-no="summary.waveNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
