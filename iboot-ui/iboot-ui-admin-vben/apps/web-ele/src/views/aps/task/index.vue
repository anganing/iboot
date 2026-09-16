<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskApi } from '#/api/aps/task';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportTask, getTaskPage, lockTask, unlockTask } from '#/api/aps/task';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import AdjustForm from './modules/adjust-form.vue';

const [AdjustFormModal, adjustFormModalApi] = useVbenModal({
  connectedComponent: AdjustForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 调整排程任务 */
function handleAdjust(row: ApsTaskApi.Task) {
  adjustFormModalApi.setData(row).open();
}

/** 锁定排程任务 */
async function handleLock(row: ApsTaskApi.Task) {
  const loadingInstance = ElLoading.service({ text: '正在锁定任务...' });
  try {
    await lockTask(row.id!, '人工锁定');
    ElMessage.success('排程任务已锁定');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 解锁排程任务 */
async function handleUnlock(row: ApsTaskApi.Task) {
  const loadingInstance = ElLoading.service({ text: '正在解锁任务...' });
  try {
    await unlockTask(row.id!, '人工解锁');
    ElMessage.success('排程任务已解锁');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportTask(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '排程任务.xls', source: data });
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
          return await getTaskPage({
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
  } as VxeTableGridOptions<ApsTaskApi.Task>,
});
</script>

<template>
  <Page auto-content-height>
    <AdjustFormModal @success="handleRefresh" />
    <Grid table-title="排程任务列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:task:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '调整',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['aps:task:update'],
              disabled: row.lockedFlag || row.status === 'STARTED',
              tooltip:
                row.lockedFlag || row.status === 'STARTED'
                  ? row.status === 'STARTED'
                    ? '任务已开工，不能再手工调整'
                    : '任务已锁定，需先解锁才能调整'
                  : undefined,
              onClick: handleAdjust.bind(null, row),
            },
            {
              label: '锁定',
              type: 'warning',
              link: true,
              auth: ['aps:task:update'],
              disabled: row.lockedFlag,
              tooltip: row.lockedFlag ? '任务已锁定' : undefined,
              popConfirm: {
                title: `锁定后重排不再移动任务【${row.taskNo}】，确认锁定吗？`,
                confirm: handleLock.bind(null, row),
              },
            },
            {
              label: '解锁',
              type: 'primary',
              link: true,
              auth: ['aps:task:update'],
              disabled: !row.lockedFlag || row.status === 'STARTED',
              tooltip:
                !row.lockedFlag || row.status === 'STARTED'
                  ? row.status === 'STARTED'
                    ? '任务已开工，不能解锁'
                    : '任务未锁定，无需解锁'
                  : undefined,
              popConfirm: {
                title: `解锁后重排可能移动任务【${row.taskNo}】，确认解锁吗？`,
                confirm: handleUnlock.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
