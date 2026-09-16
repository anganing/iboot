<script lang="ts" setup>
import type { MesWipTxnApi } from '#/api/mes/wiptxn';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getWipTxn } from '#/api/mes/wiptxn';

/**
 * 在制流转流水只读详情。
 *
 * 流水是过账留痕的证据链，账表只查询与导出、没有修改弹窗，双击事务编号是唯一看全字段的入口
 * （幂等键、来源/目标工序任务只在详情里看，追溯"这道工序怎么跳过去的"要用到）。
 */
defineOptions({ name: 'MesWipTxnDetail' });

const wipTxn = ref<MesWipTxnApi.WipTxn>();

const drawerTitle = computed(() =>
  wipTxn.value?.txnNo ? `在制流转流水 ${wipTxn.value.txnNo}` : '在制流转流水详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      wipTxn.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      wipTxn.value = await getWipTxn(id);
    } catch {
      // 列表刷新后记录可能已被清理，后端会抛“不存在”，此时保持空态即可
      wipTxn.value = undefined;
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
      <ElDescriptions v-if="wipTxn" :column="2" border size="small">
        <ElDescriptionsItem label="事务编号">
          {{ text(wipTxn.txnNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="事务类型">
          {{
            getDictLabel(DICT_TYPE.MES_WIP_TXN_TYPE, wipTxn.txnType) ||
            text(wipTxn.txnType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ text(wipTxn.prodOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(wipTxn.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序任务号">
          {{ text(wipTxn.opTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序名称">
          {{ text(wipTxn.opName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="事务数量">
          {{ text(wipTxn.qty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{
            getDictLabel(DICT_TYPE.MES_BIZ_TYPE, wipTxn.bizType) ||
            text(wipTxn.bizType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">
          {{ text(wipTxn.bizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="事务时间">
          {{ time(wipTxn.txnTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="变更前状态">
          {{
            getDictLabel(DICT_TYPE.MES_OP_TASK_STATUS, wipTxn.fromStatus) ||
            text(wipTxn.fromStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="变更后状态">
          {{
            getDictLabel(DICT_TYPE.MES_OP_TASK_STATUS, wipTxn.toStatus) ||
            text(wipTxn.toStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源工序任务">
          {{ text(wipTxn.fromOpTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标工序任务">
          {{ text(wipTxn.toOpTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键" :span="2">
          {{ text(wipTxn.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(wipTxn.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
