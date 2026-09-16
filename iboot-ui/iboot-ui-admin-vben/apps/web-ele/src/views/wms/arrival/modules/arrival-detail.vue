<script lang="ts" setup>
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate, formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getArrival } from '#/api/wms/arrival';

/**
 * 到货通知单只读详情。
 *
 * 30+ 列的宽表一屏放不下全部字段（来源单号、库存组织、预计到货日期只在详情里看），
 * 双击「到货单号」链接列打开，与「明细」按钮的子表抽屉分工不同：这里看单头全字段。
 */
defineOptions({ name: 'WmsArrivalDetail' });

const arrival = ref<WmsArrivalApi.Arrival>();

const drawerTitle = computed(() =>
  arrival.value?.arrivalNo ? `到货通知 ${arrival.value.arrivalNo}` : '到货通知详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      arrival.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 回查 get：列表行是裁剪过的展示字段，详情要拿完整数据
      arrival.value = await getArrival(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，保持空态即可
      arrival.value = undefined;
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
      <ElDescriptions v-if="arrival" :column="2" border size="small">
        <ElDescriptionsItem label="到货单号">{{ text(arrival.arrivalNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ getDictLabel(DICT_TYPE.WMS_ARRIVAL_STATUS, arrival.status) || text(arrival.status) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源类型">
          {{ getDictLabel(DICT_TYPE.WMS_ARRIVAL_SOURCE_TYPE, arrival.sourceType) || text(arrival.sourceType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源单号">{{ text(arrival.sourceNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="供应商编码">{{ text(arrival.supplierCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="供应商名称">{{ text(arrival.supplierName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库编码">{{ text(arrival.whCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标仓库名称">{{ text(arrival.whName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="预计到货日期">
          {{ arrival.expectDate ? formatDate(arrival.expectDate) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际到货时间">
          {{ arrival.arrivalTime ? formatDateTime(arrival.arrivalTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ arrival.createTime ? formatDateTime(arrival.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(arrival.remark) }}</ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
