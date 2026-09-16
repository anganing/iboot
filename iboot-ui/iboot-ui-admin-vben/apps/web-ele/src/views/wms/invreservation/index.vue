<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvReservationApi } from '#/api/wms/invreservation';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportInvReservation,
  getInvReservationPage,
  releaseReservation,
} from '#/api/wms/invreservation';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { canReleaseInvHold } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/inv-reservation-detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 双击预留编号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: WmsInvReservationApi.InvReservation;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}

/**
 * 判断预留是否还能释放
 *
 * 对齐后端 WmsReservationStatusEnum：只有 ACTIVE（全额生效）与 PARTIAL（部分处理）还有剩余占用量，
 * RELEASED / CONSUMED / CANCELLED 已经没有占用量可还，按钮直接禁用而不是隐藏，便于用户看到"为什么不能点"。
 */
function canRelease(status?: string) {
  return canReleaseInvHold(status);
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 释放库存预留：把剩余占用量还回可用库存 */
async function handleRelease(row: WmsInvReservationApi.InvReservation) {
  const remainQty = Number(row.qty ?? 0) - Number(row.releasedQty ?? 0);
  try {
    await ElMessageBox.confirm(
      `确认释放预留【${row.reservationNo}】剩余 ${remainQty} ？释放后这部分数量回到可用库存，不可撤销。`,
      '释放预留',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定释放',
        type: 'warning',
      },
    );
  } catch {
    // 用户取消
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在释放预留...' });
  try {
    await releaseReservation(row.id!);
    ElMessage.success('释放成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportInvReservation(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '库存预留.xls', source: data });
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
          return await getInvReservationPage({
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
  } as VxeTableGridOptions<WmsInvReservationApi.InvReservation>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="库存预留列表" @cell-dblclick="handleCellDblclick">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:inv-reservation:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '释放',
              type: 'primary',
              link: true,
              icon: 'lucide:unlock',
              auth: ['wms:inv-reservation:release'],
              disabled: !canRelease(row.status),
              tooltip: canRelease(row.status)
                ? undefined
                : '仅「生效中」或「部分处理」的预留有剩余占用可释放；已释放、已核销、已取消无占用量可还',
              onClick: handleRelease.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
