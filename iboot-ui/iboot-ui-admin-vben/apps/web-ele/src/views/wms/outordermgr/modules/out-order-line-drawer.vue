<script lang="ts" setup>
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import OutOrderLineList from './out-order-line-list.vue';

const summary = ref<WmsOutOrderApi.OutOrder>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<WmsOutOrderApi.OutOrder>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.outNo ? `出库单 ${summary.value.outNo}` : '出库明细',
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
        <ElDescriptionsItem label="出库单号">
          {{ summary.outNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="业务类型">
          {{ getDictLabel(DICT_TYPE.WMS_OUT_BIZ_TYPE, summary.bizType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源仓库">
          {{ summary.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, summary.status) || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <OutOrderLineList
        v-if="summary"
        :order-id="summary.id"
        :order-no="summary.outNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
