<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmVersion,
  exportVersion,
  getVersionPage,
  publishVersion,
} from '#/api/aps/version';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 确认排程版本 */
async function handleConfirm(row: ApsVersionApi.Version) {
  const loadingInstance = ElLoading.service({ text: '正在确认版本...' });
  try {
    await confirmVersion(row.id!);
    ElMessage.success('排程版本已确认');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 发布排程版本 */
async function handlePublish(row: ApsVersionApi.Version) {
  const loadingInstance = ElLoading.service({ text: '正在发布版本...' });
  try {
    await publishVersion(row.id!);
    ElMessage.success('排程版本已发布');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportVersion(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '排程版本.xls', source: data });
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
          return await getVersionPage({
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
  } as VxeTableGridOptions<ApsVersionApi.Version>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="排程版本列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:version:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '确认',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.AUDIT,
              auth: ['aps:version:update'],
              disabled: !['PENDING_CONFIRM', 'SOLVED', 'SOLVING'].includes(
                row.status ?? '',
              ),
              popConfirm: {
                title: `确认排程版本【${row.scenarioCode} V${row.versionNo}】的求解结果吗？`,
                confirm: handleConfirm.bind(null, row),
              },
            },
            {
              label: '发布',
              type: 'success',
              link: true,
              icon: ACTION_ICON.UPLOAD,
              auth: ['aps:version:update'],
              disabled: row.status !== 'CONFIRMED',
              popConfirm: {
                title: `发布后同场景原已发布版本将被置为已被替换，确认发布【${row.scenarioCode} V${row.versionNo}】吗？`,
                confirm: handlePublish.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
