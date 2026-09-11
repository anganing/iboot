<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getMaterialReq } from '#/api/mes/materialreq';
import { DictTag } from '#/components/dict-tag';

defineOptions({ name: 'MesMaterialReqProcessDetail' });

const props = defineProps<{ id: string }>();
const loading = ref(false);
const materialReq = ref<MesMaterialReqApi.MaterialReq>();

async function loadMaterialReq() {
  const id = Number(props.id);
  if (!id) return;
  loading.value = true;
  try {
    materialReq.value = await getMaterialReq(id);
  } finally {
    loading.value = false;
  }
}

watch(() => props.id, loadMaterialReq, { immediate: true });
</script>

<template>
  <div v-loading="loading" class="min-h-[220px] py-1 pb-5">
    <div class="mb-3 text-base font-semibold">领退补料申请</div>
    <ElDescriptions v-if="materialReq" :column="2" border size="small">
      <ElDescriptionsItem label="申请单号">{{ materialReq.reqNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="申请类型"><DictTag :type="DICT_TYPE.MES_MATERIAL_REQ_TYPE" :value="materialReq.reqType" /></ElDescriptionsItem>
      <ElDescriptionsItem label="生产工单号">{{ materialReq.prodOrderNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="作业工单号">{{ materialReq.workOrderNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="工序任务号">{{ materialReq.opTaskNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="仓库">{{ materialReq.whName || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="单据状态"><DictTag :type="DICT_TYPE.MES_MATERIAL_REQ_STATUS" :value="materialReq.status" /></ElDescriptionsItem>
      <ElDescriptionsItem label="流程状态"><DictTag v-if="materialReq.approvalStatus !== undefined" :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS" :value="materialReq.approvalStatus" /><span v-else>-</span></ElDescriptionsItem>
      <ElDescriptionsItem label="审批完成时间">{{ formatDate(materialReq.approvalTime) || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="WMS 单号">{{ materialReq.wmsBizNo || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="审批意见" :span="2">{{ materialReq.approvalReason || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="备注" :span="2">{{ materialReq.remark || '-' }}</ElDescriptionsItem>
    </ElDescriptions>
  </div>
</template>
