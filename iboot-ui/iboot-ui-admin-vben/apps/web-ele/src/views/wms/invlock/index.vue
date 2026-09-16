<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvLockApi } from '#/api/wms/invlock';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportInvLock, getInvLockPage, unlockInvLock } from '#/api/wms/invlock';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { canReleaseInvHold } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/inv-lock-detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 双击冻结编号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: WmsInvLockApi.InvLock;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}

/**
 * 判断冻结记录是否还能解冻
 *
 * 对齐后端 WmsLockStatusEnum：只有 ACTIVE（全额冻结）与 PARTIAL（部分解冻）还有剩余冻结量，
 * RELEASED / CANCELLED 已经没有冻结量可还，按钮直接禁用而不是隐藏，便于用户看到"为什么不能点"。
 */
function canUnlock(status?: string) {
  return canReleaseInvHold(status);
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 解冻库存冻结记录：把剩余冻结量还回可用库存 */
async function handleUnlock(row: WmsInvLockApi.InvLock) {
  const remainQty = Number(row.qty ?? 0) - Number(row.releasedQty ?? 0);
  try {
    await ElMessageBox.confirm(
      `确认解冻【${row.lockNo}】剩余 ${remainQty} ？解冻后这部分数量回到可用库存，不可撤销。`,
      '解冻库存',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定解冻',
        type: 'warning',
      },
    );
  } catch {
    // 用户取消
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在解冻...' });
  try {
    await unlockInvLock(row.id!);
    ElMessage.success('解冻成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportInvLock(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '库存冻结.xls', source: data });
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
          return await getInvLockPage({
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
  } as VxeTableGridOptions<WmsInvLockApi.InvLock>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="库存冻结列表" @cell-dblclick="handleCellDblclick">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:inv-lock:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '解冻',
              type: 'primary',
              link: true,
              icon: 'lucide:unlock',
              auth: ['wms:inv-lock:unlock'],
              disabled: !canUnlock(row.status),
              tooltip: canUnlock(row.status)
                ? undefined
                : '仅「生效中」或「部分解冻」的冻结有剩余冻结量可解冻；已解冻、已取消无冻结量可还',
              onClick: handleUnlock.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
