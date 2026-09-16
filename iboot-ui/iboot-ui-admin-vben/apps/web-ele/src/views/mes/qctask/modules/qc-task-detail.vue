<script lang="ts" setup>
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getQcTask } from '#/api/mes/qctask';

/**
 * 质检任务只读详情。
 *
 * 列表 20 列只放了业务动作要看的字段：命中的规则、锁定的方案、来源业务链路、幂等键与乐观锁版本
 * 都在列表之外。质检任务由规则触发、由检验结果汇总，本页没有编辑弹窗，双击任务号是看全字段的唯一入口。
 */
defineOptions({ name: 'MesQcTaskDetail' });

const task = ref<MesQcTaskApi.QcTask>();

const drawerTitle = computed(() =>
  task.value?.taskNo ? `质检任务 ${task.value.taskNo}` : '质检任务详情',
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
      // 列表 RespVO 是裁剪过的展示字段，详情要回查拿 JOIN 出的规则与方案名称
      task.value = await getQcTask(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端抛"不存在"时保持空态即可
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
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="task" :column="2" border size="small">
        <ElDescriptionsItem label="质检任务号">{{ text(task.taskNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ getDictLabel(DICT_TYPE.MES_QC_TASK_STATUS, task.status) || text(task.status) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="触发点">
          {{ getDictLabel(DICT_TYPE.MES_QC_TRIGGER_POINT, task.triggerPoint) || text(task.triggerPoint) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{ getDictLabel(DICT_TYPE.MES_BIZ_TYPE, task.bizType) || text(task.bizType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">{{ text(task.bizNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务行号">{{ text(task.bizLineId) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="质检规则">
          {{ [task.ruleCode, task.ruleName].filter(Boolean).join(' · ') || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="检验方案">
          {{ [task.qcPlanCode, task.qcPlanName].filter(Boolean).join(' · ') || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="方案版本">{{ text(task.qcPlanVersion) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物料">
          {{ [task.matCode, task.matName].filter(Boolean).join(' · ') || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="规格型号">{{ text(task.matSpec) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="批次号">{{ text(task.lotNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="送检数量">{{ text(task.qty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="抽样数量">{{ text(task.sampleQty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="计量单位">{{ text(task.uomCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="检验员">{{ text(task.assignedInspectorName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="检验开始时间">
          {{ task.inspectStartTime ? formatDate(task.inspectStartTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="检验结束时间">
          {{ task.inspectEndTime ? formatDate(task.inspectEndTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="检验结论">
          {{ getDictLabel(DICT_TYPE.MES_QC_CONCLUSION, task.conclusion) || text(task.conclusion) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="合格数量">{{ text(task.qtyQualified) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="不合格数量">{{ text(task.qtyRejected) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="创建幂等键">{{ text(task.idempotencyKey) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="乐观锁版本">{{ text(task.version) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ task.createTime ? formatDate(task.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(task.remark) }}</ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
