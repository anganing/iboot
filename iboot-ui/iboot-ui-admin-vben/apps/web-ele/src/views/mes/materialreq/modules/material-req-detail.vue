<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getMaterialReq } from '#/api/mes/materialreq';

/**
 * 领退补料申请只读详情。
 *
 * 列表 15 列放不下幂等键、库存组织、项目任务、审批完成时间这些字段，
 * 而“明细”抽屉里的子表列表只关心行数据，两者都不足以看全一张单据。
 * 字段清单与嵌入审批流用的 material-req-process-detail.vue 保持一致，
 * 只是容器换成独立抽屉——审批侧那个组件由 bpm 按名挂载，不能反过来复用容器。
 */
defineOptions({ name: 'MesMaterialReqDetail' });

const materialReq = ref<MesMaterialReqApi.MaterialReq>();

const drawerTitle = computed(() =>
  materialReq.value?.reqNo
    ? `领退补料申请 ${materialReq.value.reqNo}`
    : '领退补料申请详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      materialReq.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      materialReq.value = await getMaterialReq(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      materialReq.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="materialReq" :column="2" border size="small">
        <ElDescriptionsItem label="申请单号">
          {{ text(materialReq.reqNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请类型">
          {{
            getDictLabel(DICT_TYPE.MES_MATERIAL_REQ_TYPE, materialReq.reqType) ||
            text(materialReq.reqType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ text(materialReq.prodOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(materialReq.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序任务号">
          {{ text(materialReq.opTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">
          {{ text(materialReq.whName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="归属项目">
          {{ text(materialReq.projectName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="WMS 单号">
          {{ text(materialReq.wmsBizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{
            getDictLabel(DICT_TYPE.MES_MATERIAL_REQ_STATUS, materialReq.status) ||
            text(materialReq.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="流程状态">
          <!-- approvalStatus 为空表示该单据未走过流程，不该渲染一个「0=未跑流程」的假标签 -->
          <template v-if="materialReq.approvalStatus !== undefined && materialReq.approvalStatus !== null">
            {{
              getDictLabel(
                DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
                materialReq.approvalStatus,
              ) || materialReq.approvalStatus
            }}
          </template>
          <template v-else>-</template>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批完成时间">
          {{ materialReq.approvalTime ? formatDate(materialReq.approvalTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ materialReq.createTime ? formatDate(materialReq.createTime) : '-' }}
        </ElDescriptionsItem>
        <!-- 幂等键是自动触发建单的去重证据，列表没这列，只有在这里可查；
             invOrgId / projectTaskId 是裸外键，列表和详情都不展示 -->
        <ElDescriptionsItem label="幂等键" :span="2">
          {{ text(materialReq.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批意见" :span="2">
          {{ text(materialReq.approvalReason) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(materialReq.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
