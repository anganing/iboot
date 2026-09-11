<script lang="ts" setup>
import type { MesOpSkipRecordApi } from '#/api/mes/opskiprecord';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getOpSkipRecord } from '#/api/mes/opskiprecord';
import { DictTag } from '#/components/dict-tag';

defineOptions({ name: 'MesOpSkipRecordProcessDetail' });

const props = defineProps<{ id: string }>();
const loading = ref(false);
const record = ref<MesOpSkipRecordApi.OpSkipRecord>();

async function loadRecord() {
  const id = Number(props.id);
  if (!id) return;
  loading.value = true;
  try {
    record.value = await getOpSkipRecord(id);
  } finally {
    loading.value = false;
  }
}

watch(() => props.id, loadRecord, { immediate: true });
</script>

<template>
  <div v-loading="loading" class="min-h-[220px] py-1 pb-5">
    <div class="mb-3 text-base font-semibold">工序跳序申请</div>
    <ElDescriptions v-if="record" :column="2" border size="small">
      <ElDescriptionsItem label="工序任务号">{{ record.opTaskNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="作业工单号">{{ record.workOrderNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="工序名称">{{ record.opName || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="跳过原因类型"><DictTag :type="DICT_TYPE.MES_SKIP_REASON_TYPE" :value="record.skipReasonType" /></ElDescriptionsItem>
      <ElDescriptionsItem label="记录状态"><DictTag :type="DICT_TYPE.MES_SKIP_RECORD_STATUS" :value="record.status" /></ElDescriptionsItem>
      <ElDescriptionsItem label="流程状态"><DictTag v-if="record.approvalStatus !== undefined" :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS" :value="record.approvalStatus" /><span v-else>-</span></ElDescriptionsItem>
      <ElDescriptionsItem label="跳过原因" :span="2">{{ record.skipReason || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="影响说明" :span="2">{{ record.effectRemark || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批完成时间">{{ formatDate(record.approvalTime) || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批意见">{{ record.approvalReason || '-' }}</ElDescriptionsItem>
    </ElDescriptions>
  </div>
</template>
