<script lang="ts" setup>
import type { MesWorkOrderApi } from '#/api/mes/workorder';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getWorkOrder } from '#/api/mes/workorder';

/**
 * 作业工单只读详情。
 *
 * 作业工单由生产工单下达时自动展开，本页没有新增/修改入口（后端只有 get/page/export-excel），
 * 16 列列表又放不下实际开工/完工时间与项目任务，双击作业工单号是看全字段的唯一入口。
 */
defineOptions({ name: 'MesWorkOrderDetail' });

const workOrder = ref<MesWorkOrderApi.WorkOrder>();

const drawerTitle = computed(() =>
  workOrder.value?.workOrderNo
    ? `作业工单 ${workOrder.value.workOrderNo}`
    : '作业工单详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      workOrder.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      workOrder.value = await getWorkOrder(id);
    } catch {
      // 列表刷新后记录可能已被删除（取消生产工单会级联），此时保持空态即可
      workOrder.value = undefined;
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
      <ElDescriptions v-if="workOrder" :column="2" border size="small">
        <ElDescriptionsItem label="作业工单号">
          {{ text(workOrder.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ text(workOrder.prodOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">
          {{ text(workOrder.matCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">
          {{ text(workOrder.matName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工厂">
          {{ text(workOrder.plantName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="车间">
          {{ text(workOrder.workshopName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工作中心">
          {{ text(workOrder.wcName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划数量">
          {{ text(workOrder.plannedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="已完成数量">
          {{ text(workOrder.completedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="齐套状态">
          {{
            getDictLabel(DICT_TYPE.MES_KIT_STATUS, workOrder.kitStatus) ||
            text(workOrder.kitStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{
            getDictLabel(DICT_TYPE.MES_WORK_ORDER_STATUS, workOrder.status) ||
            text(workOrder.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="乐观锁版本">
          {{ text(workOrder.version) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划开始时间">
          {{ time(workOrder.plannedStartTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划结束时间">
          {{ time(workOrder.plannedEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际开工时间">
          {{ time(workOrder.actualStartTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际完工时间">
          {{ time(workOrder.actualEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="归属项目">
          {{ text(workOrder.projectName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="项目任务">
          {{ text(workOrder.projectTaskName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(workOrder.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(workOrder.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
