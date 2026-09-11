<script lang="ts" setup>
import type { MesDispositionApi } from '#/api/mes/disposition';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getDisposition } from '#/api/mes/disposition';
import { DictTag } from '#/components/dict-tag';

defineOptions({ name: 'MesDispositionProcessDetail' });

const props = defineProps<{ id: string }>();
const loading = ref(false);
const disposition = ref<MesDispositionApi.Disposition>();

async function loadDisposition() {
  const id = Number(props.id);
  if (!id) return;
  loading.value = true;
  try {
    disposition.value = await getDisposition(id);
  } finally {
    loading.value = false;
  }
}

watch(() => props.id, loadDisposition, { immediate: true });
</script>

<template>
  <div v-loading="loading" class="min-h-[220px] py-1 pb-5">
    <div class="mb-3 text-base font-semibold">不合格品处置单</div>
    <ElDescriptions v-if="disposition" :column="2" border size="small">
      <ElDescriptionsItem label="处置单号">{{ disposition.dispositionNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="质检任务号">{{ disposition.taskNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="物料">{{ disposition.matCode || '-' }} {{ disposition.matName || '' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="处置数量">{{ disposition.qty ?? '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="处置类型"><DictTag :type="DICT_TYPE.MES_DISPOSITION_TYPE" :value="disposition.dispositionType" /></ElDescriptionsItem>
      <ElDescriptionsItem label="单据状态"><DictTag :type="DICT_TYPE.MES_DISPOSITION_STATUS" :value="disposition.status" /></ElDescriptionsItem>
      <ElDescriptionsItem label="流程状态"><DictTag v-if="disposition.approvalStatus !== undefined" :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS" :value="disposition.approvalStatus" /><span v-else>-</span></ElDescriptionsItem>
      <ElDescriptionsItem label="审批完成时间">{{ formatDate(disposition.approvalTime) || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批意见" :span="2">{{ disposition.approvalReason || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="处置说明" :span="2">{{ disposition.remark || '-' }}</ElDescriptionsItem>
    </ElDescriptions>
  </div>
</template>
