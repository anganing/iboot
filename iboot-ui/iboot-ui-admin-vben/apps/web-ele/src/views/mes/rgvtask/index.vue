<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesRgvTaskApi } from '#/api/mes/rgvtask';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteRgvTask,
  deleteRgvTaskList,
  exportRgvTask,
  getRgvTaskPage,
} from '#/api/mes/rgvtask';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import RgvTaskDetail from './modules/rgv-task-detail.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
/** 只读详情走双击本地任务号，操作列仍只有修改/删除，不再追加详情按钮挤占宽度 */
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: RgvTaskDetail,
  destroyOnClose: true,
});

/** 双击本地任务号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesRgvTaskApi.RgvTask;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建RGV 流转任务 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑RGV 流转任务 */
function handleEdit(row: MesRgvTaskApi.RgvTask) {
  formModalApi.setData(row).open();
}

/** 删除RGV 流转任务 */
async function handleDelete(row: MesRgvTaskApi.RgvTask) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.taskNo]),
  });
  try {
    await deleteRgvTask(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.taskNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除RGV 流转任务 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteRgvTaskList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesRgvTaskApi.RgvTask[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportRgvTask(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'RGV 流转任务.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getRgvTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesRgvTaskApi.RgvTask>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <DetailDrawer />
    <Grid
      table-title="RGV 流转任务列表"
      @cell-dblclick="handleCellDblclick"
    >
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['RGV 流转任务']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:rgv-task:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:rgv-task:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:rgv-task:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mes:rgv-task:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:rgv-task:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.taskNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
