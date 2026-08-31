<script lang="ts" setup>
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import InOrderLineList from './in-order-line-list.vue';

const summary = ref<WmsInOrderApi.InOrder>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<WmsInOrderApi.InOrder>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.inNo ? `入库单 ${summary.value.inNo}` : '入库明细',
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
        <ElDescriptionsItem label="入库单号">
          {{ summary.inNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="业务类型">
          {{ getDictLabel(DICT_TYPE.WMS_IN_BIZ_TYPE, summary.bizType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库">
          {{ summary.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, summary.status) || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <InOrderLineList
        v-if="summary"
        :order-id="summary.id"
        :order-no="summary.inNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
