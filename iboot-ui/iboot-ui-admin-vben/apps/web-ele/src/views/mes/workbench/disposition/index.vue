<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispositionApi } from '#/api/mes/disposition';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { computed, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createDisposition,
  executeDisposition,
  getDispositionPage,
  submitDisposition,
} from '#/api/mes/disposition';
import { getQcTaskPage } from '#/api/mes/qctask';

import { useWorkbenchGridFormSchema } from '../../qctask/data';
import { useDispositionTaskGridColumns } from './data';

const selectedTask = ref<MesQcTaskApi.QcTask>();

const selectedDisposition = ref<MesDispositionApi.Disposition>();

const drawerTitle = computed(() =>
  selectedTask.value?.taskNo
    ? `不合格处置 - ${selectedTask.value.taskNo}`
    : '不合格处置',
);

const [TaskGrid, taskGridApi] = useVbenVxeGrid({
  formOptions: { schema: useWorkbenchGridFormSchema() },
  gridOptions: {
    columns: useDispositionTaskGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getQcTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            // 作业范围固定为检验不合格，放在 formValues 之后防止被筛选表单覆盖
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
      {
        field: 'dispositionType',
        title: '处置类型',
        minWidth: 120,
        // 裸值 REWORK/SCRAP 操作员读不懂，枚举列一律走字典
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.MES_DISPOSITION_TYPE },
        },
      },
      { field: 'qty', title: '处置数量', width: 100 },
      { field: 'targetBizType', title: '联动类型', minWidth: 110 },
      { field: 'targetBizNo', title: '联动单号', minWidth: 150 },
      {
        field: 'status',
        title: '状态',
        width: 100,
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.MES_DISPOSITION_STATUS },
        },
      },
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
    rowConfig: { keyField: 'id', isCurrent: true, isHover: true },
    toolbarConfig: { refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesDispositionApi.Disposition>,
});

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  // 关闭动画结束后清空选中态并刷新主列表，避免关闭过程中抽屉内容闪变
  onClosed() {
    selectedTask.value = undefined;
    selectedDisposition.value = undefined;
    taskGridApi.query();
  },
});

/**
 * 打开处置抽屉：入口是主列表操作列「明细」按钮，不再是整行点击。
 * 整行热区会让人想看某格内容时误开抽屉，原来为"点行不误触"写的列类型判断随入口一起删掉。
 */
function handleOpenDetail(row: MesQcTaskApi.QcTask) {
  selectedTask.value = row;
  selectedDisposition.value = undefined;
  drawerApi.open();
}

/**
 * 选中一张处置单：抽屉内子表只做"选这一行让下方动作生效"，不打开新抽屉，
 * 沿用 route-config 里子表行选中的既有形态（isCurrent 高亮 + cell-click）。
 */
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
    <TaskGrid table-title="不合格任务">
      <!-- 入口权限跟列表数据源（getQcTaskPage）对齐，不挂 mes:disposition:query：
           否则会出现"列表有行却整列没按钮"，而处置动作权限已在抽屉内各按钮上单独控制 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:qc-task:query'],
              onClick: handleOpenDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </TaskGrid>
    <Drawer
      :title="drawerTitle"
      class="w-full lg:w-4/5"
      content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
    >
      <section
        class="flex h-full min-h-0 w-full flex-col gap-2 overflow-hidden p-2"
      >
        <ElDescriptions
          v-if="selectedTask"
          border
          size="small"
          :column="3"
          class="shrink-0"
        >
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
          class="shrink-0"
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
          title="请选择处置单记录"
          type="info"
          class="shrink-0"
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
          class="min-h-0 min-w-0 flex-1"
          table-title="处置单"
          @cell-click="handleDispositionClick"
        />
        <div class="flex shrink-0 justify-end">
          <!-- 状态不满足时保留按钮并置灰，原因进 tooltip（可用时给空串，TableAction 不渲染空提示，
               免得在可点按钮上挂着"仅某状态可用"的误导文案） -->
          <TableAction
            :actions="[
              {
                label: '提交',
                type: 'primary',
                auth: ['mes:disposition:submit'],
                disabled: selectedDisposition?.status !== 'DRAFT',
                tooltip:
                  selectedDisposition?.status === 'DRAFT'
                    ? ''
                    : '请先在上方处置单列表选中一张新建态的处置单',
                onClick: handleSubmitDisposition,
              },
              {
                label: '执行',
                type: 'primary',
                auth: ['mes:disposition:execute'],
                disabled: selectedDisposition?.status !== 'APPROVED',
                tooltip:
                  selectedDisposition?.status === 'APPROVED'
                    ? ''
                    : '仅已批准的处置单可以执行联动并回写凭据单号',
                onClick: handleExecuteDisposition,
              },
            ]"
          />
        </div>
      </section>
    </Drawer>
  </Page>
</template>
