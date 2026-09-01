<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import MaterialReqLineList from './material-req-line-list.vue';

const summary = ref<MesMaterialReqApi.MaterialReq>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    summary.value = isOpen
      ? drawerApi.getData<MesMaterialReqApi.MaterialReq>()
      : undefined;
  },
});

const drawerTitle = computed(() =>
  summary.value?.reqNo ? `领退补料申请 ${summary.value.reqNo}` : '申请明细',
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
        <ElDescriptionsItem label="申请单号">
          {{ summary.reqNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请类型">
          {{
            getDictLabel(DICT_TYPE.MES_MATERIAL_REQ_TYPE, summary.reqType) || '-'
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ summary.prodOrderNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">
          {{ summary.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{
            getDictLabel(DICT_TYPE.MES_MATERIAL_REQ_STATUS, summary.status) ||
            '-'
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="WMS 单号">
          {{ summary.wmsBizNo || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <MaterialReqLineList
        v-if="summary"
        :req-id="summary.id"
        :req-no="summary.reqNo"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
