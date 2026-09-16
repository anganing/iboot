<script lang="ts" setup>
import type { MesOpTaskApi } from '#/api/mes/optask';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getOpTask } from '#/api/mes/optask';

/**
 * 工序任务只读详情。
 *
 * 列表 19 列放不下计划/实际四类时间与并行组、跳序策略等工艺快照，
 * 双击工序任务号开本抽屉是看全字段的快捷入口；作业动作仍在列表上完成。
 */
defineOptions({ name: 'MesOpTaskDetail' });

const task = ref<MesOpTaskApi.OpTask>();

const drawerTitle = computed(() =>
  task.value?.taskNo ? `工序任务 ${task.value.taskNo}` : '工序任务详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      task.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情拿 JOIN 后的完整 RespVO
      task.value = await getOpTask(id);
    } catch {
      // 列表刷新后行可能已被删除，后端抛“不存在”，此时保持空态而不是白屏
      task.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}

/**
 * 时间一律到秒：工序的计划/实际起止时间决定报工与工时统计口径，
 * 只显示到日期会让“当天开工当天完工”与“跨天”看起来一样。
 */
function time(value: number | string | undefined) {
  return value ? formatDateTime(value) : '-';
}

/**
 * 字典取中文：值统一转字符串再查（字典里存的是 'true'/'false' 这类字符串，
 * keyFlag 是布尔，不转就查不到）；取不到退回原值，不留空白格。
 */
function dict(
  type: string,
  value: boolean | null | number | string | undefined,
) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  return getDictLabel(type, String(value)) || String(value);
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="task" :column="2" border size="small">
        <ElDescriptionsItem label="工序任务号">
          {{ text(task.taskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ dict(DICT_TYPE.MES_OP_TASK_STATUS, task.status) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(task.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ text(task.prodOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序顺序">
          {{ text(task.seq) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="并行组">
          {{ text(task.parallelGroup) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序编码">
          {{ text(task.opCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序名称">
          {{ text(task.opName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="执行模式">
          {{ dict(DICT_TYPE.MES_OP_EXEC_MODE, task.execMode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="关键工序">
          {{ dict(DICT_TYPE.INFRA_BOOLEAN_STRING, task.keyFlag) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="分配资源">
          {{ text(task.resName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="质检模式">
          {{ dict(DICT_TYPE.MES_QC_MODE, task.qcMode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="跳序策略">
          {{ dict(DICT_TYPE.MES_SKIP_POLICY, task.skipPolicy) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划数量">
          {{ text(task.plannedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="已完成数量">
          {{ text(task.completedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报废数量">
          {{ text(task.scrapQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划开始时间">
          {{ time(task.plannedStartTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划结束时间">
          {{ time(task.plannedEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际开始时间">
          {{ time(task.actualStartTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际结束时间">
          {{ time(task.actualEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注">
          {{ text(task.remark) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(task.createTime) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
