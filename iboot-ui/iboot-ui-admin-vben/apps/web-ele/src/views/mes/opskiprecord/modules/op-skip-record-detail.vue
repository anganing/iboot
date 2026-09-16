<script lang="ts" setup>
import type { MesOpSkipRecordApi } from '#/api/mes/opskiprecord';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getOpSkipRecord } from '#/api/mes/opskiprecord';

/**
 * 工序跳过记录只读详情。
 *
 * 跳序记录是"绕过工艺"的留痕，判断责任要看跳过原因、影响说明、操作人、审批人与两套审批状态，
 * 15 列列表放不下。本行没有唯一业务编码，所以详情只由操作列"详情"按钮打开，不做双击链接列。
 * 字段清单与审批嵌入用的 op-skip-record-process-detail.vue 对齐，容器换成独立抽屉。
 */
defineOptions({ name: 'MesOpSkipRecordDetail' });

const skipRecord = ref<MesOpSkipRecordApi.OpSkipRecord>();

const drawerTitle = computed(() =>
  skipRecord.value?.opTaskNo
    ? `工序跳过记录 ${skipRecord.value.opTaskNo}`
    : '工序跳过记录详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      skipRecord.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      skipRecord.value = await getOpSkipRecord(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      skipRecord.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}

/** 时间空值给短横线：formatDate 传空会渲染成 Invalid Date */
function time(value: number | string | undefined) {
  return value ? formatDate(value) : '-';
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="skipRecord" :column="2" border size="small">
        <ElDescriptionsItem label="工序任务号">
          {{ text(skipRecord.opTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序名称">
          {{ text(skipRecord.opName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(skipRecord.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="跳过原因类型">
          {{
            getDictLabel(
              DICT_TYPE.MES_SKIP_REASON_TYPE,
              skipRecord.skipReasonType,
            ) || text(skipRecord.skipReasonType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="跳过原因" :span="2">
          {{ text(skipRecord.skipReason) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="影响说明" :span="2">
          {{ text(skipRecord.effectRemark) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作人员">
          {{ text(skipRecord.operatorName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批人员">
          {{ text(skipRecord.approvedByName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="跳序审批状态">
          {{
            getDictLabel(
              DICT_TYPE.MES_SKIP_APPROVE_STATUS,
              skipRecord.approveStatus,
            ) || text(skipRecord.approveStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="记录状态">
          {{
            getDictLabel(DICT_TYPE.MES_SKIP_RECORD_STATUS, skipRecord.status) ||
            text(skipRecord.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="流程状态">
          <!-- approvalStatus 为空表示按跳序策略免审，不该渲染成“0=未跑流程”的假标签 -->
          <template
            v-if="
              skipRecord.approvalStatus !== undefined &&
              skipRecord.approvalStatus !== null
            "
          >
            {{
              getDictLabel(
                DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
                skipRecord.approvalStatus,
              ) || skipRecord.approvalStatus
            }}
          </template>
          <template v-else>-</template>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="跳过时间">
          {{ time(skipRecord.skipTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批完成时间">
          {{ time(skipRecord.approvalTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(skipRecord.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批意见" :span="2">
          {{ text(skipRecord.approvalReason) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(skipRecord.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
