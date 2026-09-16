<script lang="ts" setup>
import type { WmsInvTxnApi } from '#/api/wms/invtxn';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getInvTxn } from '#/api/wms/invtxn';

/**
 * 库存流水只读详情。
 *
 * 流水列表 19 列放不下全部字段（幂等键、来源业务、项目维度只在详情里看），
 * 且账表没有修改弹窗，双击编码列开本抽屉是唯一看全字段的入口。
 */
defineOptions({ name: 'WmsInvTxnDetail' });

const txn = ref<WmsInvTxnApi.InvTxn>();

/** 来源业务类型字典随收发方向切换，入库/出库是两套编码 */
const bizTypeDict = computed(() =>
  txn.value?.ioType === 'OUT'
    ? DICT_TYPE.WMS_OUT_BIZ_TYPE
    : DICT_TYPE.WMS_IN_BIZ_TYPE,
);

const drawerTitle = computed(() =>
  txn.value?.txnNo ? `库存流水 ${txn.value.txnNo}` : '库存流水详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      txn.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      txn.value = await getInvTxn(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      txn.value = undefined;
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
      <ElDescriptions v-if="txn" :column="2" border size="small">
        <ElDescriptionsItem label="事务编号">{{ text(txn.txnNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="事务类型">
          {{ getDictLabel(DICT_TYPE.WMS_TXN_TYPE, txn.txnType) || text(txn.txnType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="收发方向">
          {{ getDictLabel(DICT_TYPE.WMS_IO_TYPE, txn.ioType) || text(txn.ioType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{ getDictLabel(bizTypeDict, txn.bizType) || text(txn.bizType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">{{ text(txn.bizNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务行号">{{ text(txn.bizLineNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">{{ text(txn.matCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">{{ text(txn.matName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="批次号">{{ text(txn.lotNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="基准单位">{{ text(txn.uomCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="来源库位">{{ text(txn.fromLocCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标库位">{{ text(txn.toLocCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="事务数量">{{ text(txn.qty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="过账前数量">{{ text(txn.beforeQty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="过账后数量">{{ text(txn.afterQty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="变更前质量状态">
          {{ getDictLabel(DICT_TYPE.WMS_QUALITY_STATUS, txn.qualityStatusFrom) || text(txn.qualityStatusFrom) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="变更后质量状态">
          {{ getDictLabel(DICT_TYPE.WMS_QUALITY_STATUS, txn.qualityStatusTo) || text(txn.qualityStatusTo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="过账时间">
          {{ txn.createTime ? formatDateTime(txn.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键">{{ text(txn.idempotencyKey) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(txn.remark) }}</ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
