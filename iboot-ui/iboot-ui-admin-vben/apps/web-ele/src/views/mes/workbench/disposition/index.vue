<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispositionApi } from '#/api/mes/disposition';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { ref } from 'vue';

import { ColPage, Page } from '@vben/common-ui';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
} from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createDisposition,
  executeDisposition,
  getDispositionPage,
  submitDisposition,
} from '#/api/mes/disposition';
import { getQcTaskPage } from '#/api/mes/qctask';

const selectedTask = ref<MesQcTaskApi.QcTask>();

const [TaskGrid] = useVbenVxeGrid({
  formOptions: { wrapperClass: 'grid-cols-2' },
  gridOptions: {
    columns: [
      { type: 'seq', title: '序号', width: 60 },
      { field: 'taskNo', title: '质检任务号', minWidth: 160 },
      { field: 'matCode', title: '物料编码', minWidth: 130 },
      { field: 'matName', title: '物料名称', minWidth: 150 },
      { field: 'qtyRejected', title: '不合格数量', width: 110 },
      { field: 'triggerPoint', title: '触发点', minWidth: 110 },
    ],
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getQcTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            conclusion: 'REJECTED',
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesQcTaskApi.QcTask>,
});

const [DispositionGrid, dispositionGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { type: 'seq', title: '序号', width: 60 },
      { field: 'dispositionNo', title: '处置单号', minWidth: 160 },
      { field: 'dispositionType', title: '处置类型', minWidth: 120 },
      { field: 'qty', title: '处置数量', width: 100 },
      { field: 'targetBizType', title: '联动类型', minWidth: 110 },
      { field: 'targetBizNo', title: '联动单号', minWidth: 150 },
      { field: 'status', title: '状态', width: 100 },
    ],
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getDispositionPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesDispositionApi.Disposition>,
});

const selectedDisposition = ref<MesDispositionApi.Disposition>();

function handleTaskClick({ row }: { row: MesQcTaskApi.QcTask }) {
  selectedTask.value = row;
}

function handleDispositionClick({ row }: { row: MesDispositionApi.Disposition }) {
  selectedDisposition.value = row;
}

async function handleSubmitDisposition() {
  if (!selectedDisposition.value) return;
  await submitDisposition(selectedDisposition.value.id!);
  ElMessage.success('处置单已提交');
  dispositionGridApi.query();
}

async function handleExecuteDisposition() {
  if (!selectedDisposition.value) return;
  const loading = ElLoading.service({ text: '正在执行处置联动...' });
  try {
    await executeDisposition(selectedDisposition.value.id!);
    ElMessage.success('处置已执行，联动凭据已回写');
    dispositionGridApi.query();
  } finally {
    loading.close();
  }
}

async function handleCreateDisposition(dispositionType: string) {
  if (!selectedTask.value) return;
  await createDisposition({
    taskId: selectedTask.value.id,
    dispositionType,
    qty: selectedTask.value.qtyRejected || 0,
    status: 'DRAFT',
  });
  ElMessage.success('处置单已创建');
  dispositionGridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <ColPage
      auto-content-height
      content-class="overflow-hidden p-0"
      :left-width="45"
      :right-width="55"
      resizable
      split-line
      split-handle
    >
      <template #left>
        <TaskGrid table-title="不合格任务" @cell-click="handleTaskClick" />
      </template>
      <section class="flex h-full min-h-0 w-full flex-col gap-2 p-2">
        <ElDescriptions v-if="selectedTask" border size="small" :column="3">
          <ElDescriptionsItem label="质检任务号">
            {{ selectedTask.taskNo }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="物料">
            {{ selectedTask.matCode }} {{ selectedTask.matName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="不合格数量">
            {{ selectedTask.qtyRejected }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElDescriptions
          v-if="selectedDisposition"
          border
          size="small"
          :column="3"
        >
          <ElDescriptionsItem label="处置单号">
            {{ selectedDisposition.dispositionNo }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="联动业务类型">
            {{ selectedDisposition.targetBizType || '未执行' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="联动业务单号">
            {{ selectedDisposition.targetBizNo || '未执行' }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElAlert
          v-else
          :closable="false"
          show-icon
          title="请选择左侧不合格质检任务"
          type="info"
        />
        <div class="flex shrink-0 justify-end">
          <TableAction
            :actions="[
              {
                label: '返工',
                type: 'primary',
                auth: ['mes:disposition:create'],
                disabled: !selectedTask,
                onClick: () => handleCreateDisposition('REWORK'),
              },
              {
                label: '报废',
                type: 'danger',
                auth: ['mes:disposition:create'],
                disabled: !selectedTask,
                onClick: () => handleCreateDisposition('SCRAP'),
              },
              {
                label: '让步接收',
                type: 'primary',
                auth: ['mes:disposition:create'],
                disabled: !selectedTask,
                onClick: () => handleCreateDisposition('CONCESSION'),
              },
              {
                label: '冻结',
                type: 'warning',
                auth: ['mes:disposition:create'],
                disabled: !selectedTask,
                onClick: () => handleCreateDisposition('FREEZE'),
              },
            ]"
          />
        </div>
        <DispositionGrid
          class="min-h-0 flex-1"
          table-title="处置单"
          @cell-click="handleDispositionClick"
        />
        <div class="flex shrink-0 justify-end">
          <TableAction
            :actions="[
              {
                label: '提交',
                type: 'primary',
                auth: ['mes:disposition:submit'],
                disabled: selectedDisposition?.status !== 'DRAFT',
                onClick: handleSubmitDisposition,
              },
              {
                label: '执行',
                type: 'primary',
                auth: ['mes:disposition:execute'],
                disabled: selectedDisposition?.status !== 'APPROVED',
                onClick: handleExecuteDisposition,
              },
            ]"
          />
        </div>
      </section>
    </ColPage>
  </Page>
</template>
