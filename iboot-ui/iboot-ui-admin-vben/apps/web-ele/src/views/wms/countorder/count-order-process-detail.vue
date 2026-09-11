<script lang="ts" setup>
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getCountOrder } from '#/api/wms/countorder';
import { DictTag } from '#/components/dict-tag';

defineOptions({ name: 'WmsCountOrderProcessDetail' });

const props = defineProps<{ id: string }>();
const loading = ref(false);
const countOrder = ref<WmsCountOrderApi.CountOrder>();

async function loadCountOrder() {
  const id = Number(props.id);
  if (!id) return;
  loading.value = true;
  try {
    countOrder.value = await getCountOrder(id);
  } finally {
    loading.value = false;
  }
}

watch(() => props.id, loadCountOrder, { immediate: true });
</script>

<template>
  <div v-loading="loading" class="min-h-[220px] py-1 pb-5">
    <div class="mb-3 text-base font-semibold">盘点差异调整申请</div>
    <ElDescriptions v-if="countOrder" :column="2" border size="small">
      <ElDescriptionsItem label="盘点单号">{{ countOrder.countNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="盘点类型"><DictTag :type="DICT_TYPE.WMS_COUNT_TYPE" :value="countOrder.countType" /></ElDescriptionsItem>
      <ElDescriptionsItem label="仓库">{{ countOrder.whName || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="库区">{{ countOrder.zoneName || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="盘点状态"><DictTag :type="DICT_TYPE.WMS_ORDER_STATUS" :value="countOrder.status" /></ElDescriptionsItem>
      <ElDescriptionsItem label="流程状态"><DictTag v-if="countOrder.approvalStatus !== undefined" :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS" :value="countOrder.approvalStatus" /><span v-else>-</span></ElDescriptionsItem>
      <ElDescriptionsItem label="账面快照时间">{{ formatDate(countOrder.snapshotTime) || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批完成时间">{{ formatDate(countOrder.approvalTime) || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批意见" :span="2">{{ countOrder.approvalReason || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="备注" :span="2">{{ countOrder.remark || '-' }}</ElDescriptionsItem>
    </ElDescriptions>
  </div>
</template>
