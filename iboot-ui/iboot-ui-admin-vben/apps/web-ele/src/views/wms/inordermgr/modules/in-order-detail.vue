<script lang="ts" setup>
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getInOrder } from '#/api/wms/inordermgr';

/**
 * 入库单只读详情。
 *
 * 30+ 列宽表放不下全部字段（原始来源、质检策略、幂等键只在详情里看），
 * 双击「入库单号」链接列打开，与「明细」按钮的子表抽屉分工不同：这里看单头全字段。
 */
defineOptions({ name: 'WmsInOrderDetail' });

const order = ref<WmsInOrderApi.InOrder>();

const drawerTitle = computed(() =>
  order.value?.inNo ? `入库单 ${order.value.inNo}` : '入库单详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      order.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 回查 get：列表行是裁剪过的展示字段，详情要拿完整数据
      order.value = await getInOrder(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，保持空态即可
      order.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: boolean | null | number | string | undefined) {
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
      <ElDescriptions v-if="order" :column="2" border size="small">
        <ElDescriptionsItem label="入库单号">{{ text(order.inNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, order.status) || text(order.status) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="入库业务类型">
          {{ getDictLabel(DICT_TYPE.WMS_IN_BIZ_TYPE, order.bizType) || text(order.bizType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="直接来源类型">
          {{ getDictLabel(DICT_TYPE.WMS_SOURCE_BIZ_TYPE, order.sourceType) || text(order.sourceType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="直接来源单号">{{ text(order.sourceBizNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="原始来源类型">
          {{ getDictLabel(DICT_TYPE.WMS_SOURCE_BIZ_TYPE, order.originType) || text(order.originType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原始来源单号">{{ text(order.originNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">{{ text(order.prodOrderNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">{{ text(order.workOrderNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库编码">{{ text(order.whCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库名称">{{ text(order.whName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="质量状态">
          {{ getDictLabel(DICT_TYPE.WMS_QUALITY_STATUS, order.qualityStatus) || text(order.qualityStatus) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否需要质检">
          {{ getDictLabel(DICT_TYPE.INFRA_BOOLEAN_STRING, order.needQc) || text(order.needQc) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来料检验模式">
          {{ getDictLabel(DICT_TYPE.MES_QC_MODE, order.iqcMode) || text(order.iqcMode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="外部设备触发方式">{{ text(order.hookMode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ order.createTime ? formatDateTime(order.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键">{{ text(order.idempotencyKey) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(order.remark) }}</ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
