<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsScenarioApi } from '#/api/aps/scenario';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeScenario,
  deleteScenario,
  deleteScenarioList,
  exportScenario,
  getScenarioPage,
  refreshScenarioProjection,
} from '#/api/aps/scenario';
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

/** 创建排程场景 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑排程场景 */
function handleEdit(row: ApsScenarioApi.Scenario) {
  formModalApi.setData(row).open();
}

/** 刷新排程输入投影：物料、库存、资源、工单四张一起重建 */
async function handleRefreshProjection(row: ApsScenarioApi.Scenario) {
  const loadingInstance = ElLoading.service({ text: '正在刷新排程输入...' });
  try {
    await refreshScenarioProjection(row.id!);
    ElMessage.success('排程输入已刷新');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 关闭排程场景 */
async function handleClose(row: ApsScenarioApi.Scenario) {
  const loadingInstance = ElLoading.service({ text: '正在关闭场景...' });
  try {
    await closeScenario(row.id!);
    ElMessage.success('排程场景已关闭');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 删除排程场景 */
async function handleDelete(row: ApsScenarioApi.Scenario) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.code]),
  });
  try {
    await deleteScenario(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.code]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除排程场景 */
async function handleDeleteBatch() {
  await confirm('已生成排程版本的场景不允许删除，确认删除选中的场景吗？');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteScenarioList(checkedIds.value);
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
  records: ApsScenarioApi.Scenario[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportScenario(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '排程场景.xls', source: data });
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
          return await getScenarioPage({
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
  } as VxeTableGridOptions<ApsScenarioApi.Scenario>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="排程场景列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['排程场景']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['aps:scenario:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:scenario:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['aps:scenario:delete'],
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
              label: '刷新输入',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.REFRESH,
              auth: ['aps:scenario:update'],
              disabled: row.status === 'CLOSED',
              popConfirm: {
                title: `确认刷新场景【${row.code}】的物料、库存、资源与工单投影吗？`,
                confirm: handleRefreshProjection.bind(null, row),
              },
            },
            {
              label: '关闭',
              type: 'warning',
              link: true,
              icon: ACTION_ICON.CLOSE,
              auth: ['aps:scenario:update'],
              disabled: row.status === 'CLOSED',
              popConfirm: {
                title: `关闭后场景【${row.code}】不再允许刷新输入与生成版本，确认关闭吗？`,
                confirm: handleClose.bind(null, row),
              },
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['aps:scenario:update'],
              disabled: row.status === 'CLOSED',
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['aps:scenario:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.code]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
