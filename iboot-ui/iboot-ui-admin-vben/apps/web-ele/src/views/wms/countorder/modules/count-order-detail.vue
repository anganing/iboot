<script lang="ts" setup>
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getCountOrder } from '#/api/wms/countorder';

/**
 * 盘点单只读详情（列表入口）。
 *
 * 与 `count-order-process-detail.vue`（bpm 按组件名挂载的审批详情）字段取并集但不共用文件：
 * 那份带自己的 v-loading 容器与外层标题，塞进抽屉会出现双标题。
 * 时间精度按列表 `formatDateTime` 统一（审批详情那份是 formatDate，属旧的独立口径）。
 */
defineOptions({ name: 'WmsCountOrderDetail' });

const order = ref<WmsCountOrderApi.CountOrder>();

const drawerTitle = computed(() =>
  order.value?.countNo ? `盘点单 ${order.value.countNo}` : '盘点单详情',
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
      order.value = await getCountOrder(id);
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
        <ElDescriptionsItem label="盘点单号">{{ text(order.countNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="盘点类型">
          {{ getDictLabel(DICT_TYPE.WMS_COUNT_TYPE, order.countType) || text(order.countType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库编码">{{ text(order.whCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="仓库名称">{{ text(order.whName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="库区编码">{{ text(order.zoneCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="库区名称">{{ text(order.zoneName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="盘点状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, order.status) || text(order.status) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否盲盘">
          {{ getDictLabel(DICT_TYPE.INFRA_BOOLEAN_STRING, order.blindCountFlag) || text(order.blindCountFlag) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="流程状态">
          {{ order.approvalStatus === undefined ? '-' : (getDictLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, order.approvalStatus) || text(order.approvalStatus)) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="账面快照时间">
          {{ order.snapshotTime ? formatDateTime(order.snapshotTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批完成时间">
          {{ order.approvalTime ? formatDateTime(order.approvalTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ order.createTime ? formatDateTime(order.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批意见" :span="2">{{ text(order.approvalReason) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键">{{ text(order.idempotencyKey) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(order.remark) }}</ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
