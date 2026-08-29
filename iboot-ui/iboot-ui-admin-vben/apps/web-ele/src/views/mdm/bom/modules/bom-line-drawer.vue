<script lang="ts" setup>
import type { MdmBomApi } from '#/api/mdm/bom';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import BomLineList from './bom-line-list.vue';

const bomSummary = ref<MdmBomApi.Bom>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    bomSummary.value = isOpen
      ? drawerApi.getData<MdmBomApi.Bom>()
      : undefined;
  },
});

const drawerTitle = computed(() => {
  const bom = bomSummary.value;
  return bom
    ? [bom.matCode, bom.matName, bom.version].filter(Boolean).join(' · ')
    : '物料清单明细';
});
</script>

<template>
  <Drawer :title="drawerTitle" class="w-full">
    <div class="flex h-full min-h-0 flex-col px-4 pb-4">
      <ElDescriptions
        v-if="bomSummary"
        :column="4"
        border
        size="small"
        class="mb-3 shrink-0"
      >
        <ElDescriptionsItem label="物料编码">
          {{ bomSummary.matCode || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">
          {{ bomSummary.matName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本号">
          {{ bomSummary.version || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{
            getDictLabel(DICT_TYPE.COMMON_STATUS, bomSummary.status) || '-'
          }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <BomLineList
        v-if="bomSummary"
        :bom-id="bomSummary.id"
        class="min-h-0 flex-1"
      />
    </div>
  </Drawer>
</template>
