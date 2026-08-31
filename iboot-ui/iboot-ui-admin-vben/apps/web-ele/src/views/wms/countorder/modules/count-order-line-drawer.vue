<script lang="ts" setup>
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import CountOrderLineList from './count-order-line-list.vue';

const summary = ref<WmsCountOrderApi.CountOrder>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<WmsCountOrderApi.CountOrder>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.countNo ? `盘点单 ${summary.value.countNo}` : '盘点明细',
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
        <ElDescriptionsItem label="盘点单号">
          {{ summary.countNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点类型">
          {{ getDictLabel(DICT_TYPE.WMS_COUNT_TYPE, summary.countType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点仓库">
          {{ summary.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, summary.status) || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <CountOrderLineList
        v-if="summary"
        :order-id="summary.id"
        :order-no="summary.countNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
