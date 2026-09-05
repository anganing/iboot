<script lang="ts" setup>
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import ArrivalLineList from './arrival-line-list.vue';

/** 明细里"按选中明细生成入库单"会把到货单推进到已完成，要把刷新信号转给主列表 */
const emit = defineEmits(['success']);
const arrivalSummary = ref<WmsArrivalApi.Arrival>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    arrivalSummary.value = isOpen
      ? drawerApi.getData<WmsArrivalApi.Arrival>()
      : undefined;
  },
});

const drawerTitle = computed(() => {
  const arrival = arrivalSummary.value;
  return arrival
    ? [arrival.arrivalNo, arrival.supplierName].filter(Boolean).join(' · ')
    : '到货登记明细';
});
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-4/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-hidden px-4 pb-4">
      <ElDescriptions
        v-if="arrivalSummary"
        :column="4"
        border
        size="small"
        class="mb-3 shrink-0"
      >
        <ElDescriptionsItem label="到货单号">
          {{ arrivalSummary.arrivalNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源单号">
          {{ arrivalSummary.sourceNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="供应商">
          {{ arrivalSummary.supplierName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="到货状态">
          {{
            getDictLabel(DICT_TYPE.WMS_ARRIVAL_STATUS, arrivalSummary.status) ||
            '-'
          }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <ArrivalLineList
        v-if="arrivalSummary"
        :arrival-id="arrivalSummary.id"
        :arrival-no="arrivalSummary.arrivalNo"
        :arrival-status="arrivalSummary.status"
        class="min-h-0 flex-1"
        @success="emit('success')"
      />
    </div>
  </Drawer>
</template>
