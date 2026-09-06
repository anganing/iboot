<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispatchTaskApi } from '#/api/mes/dispatchtask';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { computed, ref } from 'vue';

import { ColPage } from '@vben/common-ui';
import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { createDispatchTask } from '#/api/mes/dispatchtask';
import { getOpTaskPage } from '#/api/mes/optask';

import {
  useDispatchFormSchema,
  useTaskGridColumns,
  useTaskGridFormSchema,
} from './data';

const checkedTasks = ref<MesOpTaskApi.OpTask[]>([]);
const selectedCount = computed(() => checkedTasks.value.length);

const [DispatchForm, dispatchFormApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelClass: 'justify-start',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useDispatchFormSchema(),
  showDefaultActions: false,
});

const [TaskGrid, taskGridApi] = useVbenVxeGrid({
  formOptions: { schema: useTaskGridFormSchema(), wrapperClass: 'grid-cols-3' },
  gridOptions: {
    columns: useTaskGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getOpTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesOpTaskApi.OpTask>,
  gridEvents: {
    checkboxAll: ({ records }: { records: MesOpTaskApi.OpTask[] }) => {
      checkedTasks.value = records;
    },
    checkboxChange: ({ records }: { records: MesOpTaskApi.OpTask[] }) => {
      checkedTasks.value = records;
    },
  },
});

async function handleDispatch() {
  const { valid } = await dispatchFormApi.validate();
  if (!valid || selectedCount.value === 0) return;
  const values = await dispatchFormApi.getValues();
  const loading = ElLoading.service({
    text: `正在派发 ${selectedCount.value} 个工序任务...`,
  });
  try {
    await Promise.all(
      checkedTasks.value.map((task) =>
        createDispatchTask({
          opTaskId: task.id,
          resId: values.resId,
          operatorId: values.operatorId || 0,
          teamId: values.teamId || 0,
          shiftId: values.shiftId || 0,
          plannedStartTime: task.plannedStartTime,
          plannedEndTime: task.plannedEndTime,
          status: 'PENDING',
          remark: values.remark,
        } as MesDispatchTaskApi.DispatchTask),
      ),
    );
    ElMessage.success(`已派工 ${selectedCount.value} 个工序任务`);
    checkedTasks.value = [];
    await taskGridApi.query();
  } finally {
    loading.close();
  }
}
</script>

<template>
  <ColPage
    auto-content-height
    content-class="overflow-hidden p-0"
    :left-width="68"
    :right-width="32"
    :left-min-width="50"
    :right-min-width="24"
    resizable
    split-line
    split-handle
  >
    <template #left>
      <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
        <TaskGrid
          class="w-full min-w-0 overflow-hidden"
          table-title="待派工工序任务"
        />
      </section>
    </template>
    <section class="flex h-full min-h-0 w-full flex-col overflow-hidden p-4">
      <ElAlert
        :closable="false"
        show-icon
        type="info"
        :title="
          selectedCount
            ? `已选择 ${selectedCount} 个工序任务`
            : '请在左侧勾选待派工工序任务'
        "
      />
      <ElDescriptions
        v-if="selectedCount"
        class="my-3 shrink-0"
        :column="1"
        border
        size="small"
      >
        <ElDescriptionsItem label="首个任务"
          >{{ checkedTasks[0]?.taskNo }} -
          {{ checkedTasks[0]?.opName }}</ElDescriptionsItem
        >
        <ElDescriptionsItem label="批量数量">{{
          selectedCount
        }}</ElDescriptionsItem>
      </ElDescriptions>
      <DispatchForm class="mt-4" />
      <div class="mt-4 shrink-0">
        <TableAction
          :actions="[
            {
              label: '确认派工',
              type: 'primary',
              icon: 'lucide:user-round-check',
              auth: ['mes:dispatch-task:create'],
              disabled: selectedCount === 0,
              onClick: handleDispatch,
            },
          ]"
        />
      </div>
    </section>
  </ColPage>
</template>
