<script lang="ts" setup>
import type { MesRgvTaskApi } from '#/api/mes/rgvtask';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getRgvTask } from '#/api/mes/rgvtask';

/**
 * RGV 流转任务只读详情。
 *
 * 15 列列表按展示裁剪，排查"外部系统没回执"这类问题要看幂等键、外部任务号与乐观锁版本，
 * 编辑弹窗是给可改字段用的、不承担只读留痕，所以单独做只读抽屉，由双击本地任务号打开。
 */
defineOptions({ name: 'MesRgvTaskDetail' });

const rgvTask = ref<MesRgvTaskApi.RgvTask>();

const drawerTitle = computed(() =>
  rgvTask.value?.taskNo ? `RGV 流转任务 ${rgvTask.value.taskNo}` : 'RGV 任务详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      rgvTask.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      rgvTask.value = await getRgvTask(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      rgvTask.value = undefined;
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
      <ElDescriptions v-if="rgvTask" :column="2" border size="small">
        <ElDescriptionsItem label="本地任务号">
          {{ text(rgvTask.taskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="任务类型">
          {{
            getDictLabel(DICT_TYPE.MES_RGV_TASK_TYPE, rgvTask.taskType) ||
            text(rgvTask.taskType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="外部任务号">
          {{ text(rgvTask.externalTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="外部调度系统">
          {{ text(rgvTask.externalSystemCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(rgvTask.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="载具编号">
          {{ text(rgvTask.carrierNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源工序任务">
          {{ text(rgvTask.opTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标工序任务">
          {{ text(rgvTask.targetOpTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源工位">
          {{ text(rgvTask.fromStationName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标工位">
          {{ text(rgvTask.toStationName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="下发状态">
          {{
            getDictLabel(
              DICT_TYPE.MES_RGV_DISPATCH_STATUS,
              rgvTask.dispatchStatus,
            ) || text(rgvTask.dispatchStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="执行状态">
          {{
            getDictLabel(DICT_TYPE.MES_RGV_TASK_STATUS, rgvTask.status) ||
            text(rgvTask.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="完成时间">
          {{ time(rgvTask.completedTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="乐观锁版本">
          {{ text(rgvTask.version) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(rgvTask.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键" :span="2">
          {{ text(rgvTask.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(rgvTask.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
