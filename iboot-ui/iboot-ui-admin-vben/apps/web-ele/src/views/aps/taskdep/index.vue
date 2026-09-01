<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskDepApi } from '#/api/aps/taskdep';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteTaskDep,
  deleteTaskDepList,
  exportTaskDep,
  getTaskDepPage,
} from '#/api/aps/taskdep';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建任务依赖 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑任务依赖 */
function handleEdit(row: ApsTaskDepApi.TaskDep) {
  formModalApi.setData(row).open();
}

/** 删除任务依赖 */
async function handleDelete(row: ApsTaskDepApi.TaskDep) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.fromTaskNo]),
  });
  try {
    await deleteTaskDep(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.fromTaskNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除任务依赖 */
async function handleDeleteBatch() {
  await confirm('已发布版本的依赖不允许删除，确认删除选中的依赖吗？');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteTaskDepList(checkedIds.value);
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
  records: ApsTaskDepApi.TaskDep[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportTaskDep(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '任务依赖.xls', source: data });
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
          return await getTaskDepPage({
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
  } as VxeTableGridOptions<ApsTaskDepApi.TaskDep>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="任务依赖列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['任务依赖']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['aps:task-dep:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:task-dep:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['aps:task-dep:delete'],
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
              auth: ['aps:task-dep:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['aps:task-dep:delete'],
              popConfirm: {
                title: `确认删除【${row.fromTaskNo} → ${row.toTaskNo}】的依赖吗？`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
