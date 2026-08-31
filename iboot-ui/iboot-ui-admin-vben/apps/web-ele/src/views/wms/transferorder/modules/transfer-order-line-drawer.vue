<script lang="ts" setup>
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import TransferOrderLineList from './transfer-order-line-list.vue';

const summary = ref<WmsTransferOrderApi.TransferOrder>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<WmsTransferOrderApi.TransferOrder>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.transferNo ? `移库调拨单 ${summary.value.transferNo}` : '调拨明细',
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
        <ElDescriptionsItem label="调拨单号">
          {{ summary.transferNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="转移类型">
          {{ getDictLabel(DICT_TYPE.WMS_TRANSFER_TYPE, summary.transferType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源仓库">
          {{ summary.fromWhName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库">
          {{ summary.toWhName || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <TransferOrderLineList
        v-if="summary"
        :order-id="summary.id"
        :order-no="summary.transferNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
