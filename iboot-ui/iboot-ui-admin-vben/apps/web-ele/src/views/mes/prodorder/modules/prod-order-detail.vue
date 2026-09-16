<script lang="ts" setup>
import type { MesProdOrderApi } from '#/api/mes/prodorder';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getProdOrder } from '#/api/mes/prodorder';

/**
 * 生产工单只读详情。
 *
 * 列表 20 列只到“备注”，实际开工/完工时间、归属项目与项目任务、乐观锁版本这些
 * 追根因要用的字段只在详情里看。外键一律用 JOIN 出的名称，不暴露 bomId/routeId 等主键。
 */
defineOptions({ name: 'MesProdOrderDetail' });

const prodOrder = ref<MesProdOrderApi.ProdOrder>();

const drawerTitle = computed(() =>
  prodOrder.value?.orderNo
    ? `生产工单 ${prodOrder.value.orderNo}`
    : '生产工单详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      prodOrder.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      prodOrder.value = await getProdOrder(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      prodOrder.value = undefined;
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
      <ElDescriptions v-if="prodOrder" :column="2" border size="small">
        <ElDescriptionsItem label="生产工单号">
          {{ text(prodOrder.orderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产需求单号">
          {{ text(prodOrder.demandNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工厂">
          {{ text(prodOrder.plantName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="车间">
          {{ text(prodOrder.workshopName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">
          {{ text(prodOrder.matCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">
          {{ text(prodOrder.matName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="规格型号">
          {{ text(prodOrder.matSpec) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单位">
          {{ text(prodOrder.uomCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划数量">
          {{ text(prodOrder.qty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="已完成数量">
          {{ text(prodOrder.completedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报废数量">
          {{ text(prodOrder.scrapQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="优先级">
          {{ text(prodOrder.priority) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="锁定 BOM 版本">
          {{ text(prodOrder.bomVersion) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="锁定路线版本">
          {{ text(prodOrder.routeVersion) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="归属项目">
          {{ text(prodOrder.projectName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="项目任务">
          {{ text(prodOrder.projectTaskName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工单交期">
          {{ time(prodOrder.dueDate) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="齐套状态">
          {{
            getDictLabel(DICT_TYPE.MES_KIT_STATUS, prodOrder.kitStatus) ||
            text(prodOrder.kitStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{
            getDictLabel(DICT_TYPE.MES_PROD_ORDER_STATUS, prodOrder.status) ||
            text(prodOrder.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="乐观锁版本">
          {{ text(prodOrder.version) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="下达时间">
          {{ time(prodOrder.releaseTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(prodOrder.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际开工时间">
          {{ time(prodOrder.actualStartTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际完工时间">
          {{ time(prodOrder.actualEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(prodOrder.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
