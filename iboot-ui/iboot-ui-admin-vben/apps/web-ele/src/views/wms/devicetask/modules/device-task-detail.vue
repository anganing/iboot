<script lang="ts" setup>
import type { WmsDeviceTaskApi } from '#/api/wms/devicetask';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getDeviceTask } from '#/api/wms/devicetask';

/**
 * 设备任务只读详情。
 *
 * 列表 15 列放不下全部字段（来源业务类型、外部系统编码、下发/回调快照、幂等键只在详情里看），
 * 这三段快照是排查设备对接故障的唯一依据；列表行数据是裁剪过的，必须回查 get 才拿得到。
 */
defineOptions({ name: 'WmsDeviceTaskDetail' });

const task = ref<WmsDeviceTaskApi.DeviceTask>();

const drawerTitle = computed(() =>
  task.value?.taskNo ? `设备任务 ${task.value.taskNo}` : '设备任务详情',
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
      // 必须回查 get：详情要拿 JOIN 容器与库位编码后的完整数据，含两段快照
      task.value = await getDeviceTask(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端返回 null 或抛“不存在”，此时保持空态即可
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
        <ElDescriptionsItem label="任务号">{{ text(task.taskNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="外部任务号">
          {{ text(task.externalTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="任务类型">
          {{
            getDictLabel(DICT_TYPE.WMS_DEVICE_TASK_TYPE, task.taskType) ||
            text(task.taskType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="设备类型">
          {{
            getDictLabel(DICT_TYPE.WMS_DEVICE_TYPE, task.deviceType) ||
            text(task.deviceType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="外部系统编码">
          {{ text(task.externalSystemCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{
            getDictLabel(DICT_TYPE.WMS_SOURCE_BIZ_TYPE, task.sourceBizType) ||
            text(task.sourceBizType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">
          {{ text(task.sourceBizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="搬运容器">
          {{ text(task.containerNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源库位">
          {{ text(task.fromLocCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标库位">
          {{ text(task.toLocCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实际库位">
          {{ text(task.actualLocCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="下发状态">
          {{
            getDictLabel(DICT_TYPE.WMS_DISPATCH_STATUS, task.dispatchStatus) ||
            text(task.dispatchStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="执行状态">
          {{
            getDictLabel(DICT_TYPE.WMS_DEVICE_TASK_STATUS, task.status) ||
            text(task.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="过账状态">
          {{
            getDictLabel(DICT_TYPE.WMS_POST_STATUS, task.postStatus) ||
            text(task.postStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="完成时间">
          {{ task.completedTime ? formatDateTime(task.completedTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ task.createTime ? formatDateTime(task.createTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键" :span="2">
          {{ text(task.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(task.remark) }}
        </ElDescriptionsItem>
        <!-- 两段快照是设备对接的原始报文，排障时才看，独占整栏并允许换行 -->
        <ElDescriptionsItem label="下发业务快照" :span="2">
          <div class="max-h-40 overflow-y-auto break-all whitespace-pre-wrap">
            {{ text(task.requestSnapshotJson) }}
          </div>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="完成回调快照" :span="2">
          <div class="max-h-40 overflow-y-auto break-all whitespace-pre-wrap">
            {{ text(task.callbackSnapshotJson) }}
          </div>
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>
