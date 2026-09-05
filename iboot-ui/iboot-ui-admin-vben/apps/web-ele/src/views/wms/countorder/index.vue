<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmCountDiff,
  deleteCountOrder,
  deleteCountOrderList,
  exportCountOrder,
  freezeCountOrder,
  getCountOrderPage,
} from '#/api/wms/countorder';
import { $t } from '#/locales';

import { isOrderDraft, WMS_ORDER_STATUS } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import CountOrderLineDrawer from './modules/count-order-line-drawer.vue';
import CountResultForm from './modules/count-result-form.vue';
import Form from './modules/form.vue';

/**
 * 盘点单状态机是 `DRAFT → FROZEN → COUNTING → DIFF_CONFIRMED → DONE`，没有取消动作。
 * 按钮的启用条件贴着这条链走，非可用状态一律禁用而不是隐藏，用户才知道"为什么不能点"。
 *
 * 三个动作的判定写在本文件而不是取 `order-form.ts` 里的 `canSubmitCountResult` /
 * `canConfirmCountDiff`：那两个函数照抄的是领域 Service 的入口校验（放行 `DRAFT` 与
 * `DIFF_CONFIRMED`），是"接口收什么"；这里要的是"界面给不给点"，比接口更严，理由见下。
 * 盘点动作只有这一个页面用，判定留在页面里不存在"改一处漏四处"。
 */

/** 可冻结：只有新建态。冻结是 FREEZE 过账，把账面数量挂到 qty_frozen 上 */
function canFreeze(status?: string) {
  return isOrderDraft(status);
}

/**
 * 可录实盘：已冻结或盘点中。
 *
 * 领域侧 `submitCountResult` 连 `DRAFT` 也放行，界面刻意收窄成"先冻结再录"：
 * 一旦从 `DRAFT` 直接录实盘，单据就跳到 `COUNTING`，而 `freezeCountOrder` 只认新建态——
 * 这批货再也冻不上，盘点期间照样能被领走。要不冻结直接盘只能走接口，不在界面上开这条路。
 */
function canSubmitResult(status?: string) {
  return (
    status === WMS_ORDER_STATUS.FROZEN || status === WMS_ORDER_STATUS.COUNTING
  );
}

/**
 * 可确认差异：录过实盘之后（盘点中）。
 *
 * 领域侧还放行 `DIFF_CONFIRMED`（为重试留的口子，已 ADJUSTED 的行会跳过），但
 * `confirmCountDiff` 成功后直接把单据置 `DONE`，代码里没有任何地方写入 `DIFF_CONFIRMED`，
 * 界面上不会出现这个状态的单据，因此不为它开按钮。
 */
function canConfirmDiff(status?: string) {
  return status === WMS_ORDER_STATUS.COUNTING;
}

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: CountOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [CountResultModal, countResultModalApi] = useVbenModal({
  connectedComponent: CountResultForm,
  destroyOnClose: true,
});

/** 查看当前单据的盘点明细（只读） */
function handleLines(row: WmsCountOrderApi.CountOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建盘点单，盘点行由服务端按盘点范围生成 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑盘点单，仅新建态可改 */
function handleEdit(row: WmsCountOrderApi.CountOrder) {
  formModalApi.setData(row).open();
}

/** 删除盘点单，仅新建态可删 */
async function handleDelete(row: WmsCountOrderApi.CountOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.countNo]),
  });
  try {
    await deleteCountOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.countNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除盘点单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteCountOrderList(checkedIds.value);
    checkedRows.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 冻结盘点范围：逐行按账面数量做 FREEZE 过账，冻结期间这批货不能被领走 */
async function handleFreeze(row: WmsCountOrderApi.CountOrder) {
  try {
    await ElMessageBox.confirm(
      `确认冻结盘点单【${row.countNo}】的盘点范围？冻结会按账面数量逐行过账，盘点期间这批库存不能被领用，且冻结后不能再改盘点范围。`,
      '冻结盘点范围',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定冻结',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在冻结...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，连点两次会被判重
    await freezeCountOrder({ id: row.id! });
    ElMessage.success('冻结成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 录实盘：打开批量录入弹窗 */
function handleSubmitResult(row: WmsCountOrderApi.CountOrder) {
  countResultModalApi.setData(row).open();
}

/** 确认差异：生成盘盈入库单或盘亏出库单并过账，不可撤销 */
async function handleConfirmDiff(row: WmsCountOrderApi.CountOrder) {
  try {
    await ElMessageBox.confirm(
      `确认盘点单【${row.countNo}】的差异？服务端会按差异逐行生成盘盈入库单或盘亏出库单并立即过账，库存余额随之变动，不可撤销。`,
      '确认盘点差异',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定确认差异',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在确认差异...' });
  try {
    await confirmCountDiff({ id: row.id! });
    ElMessage.success('差异已确认');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedRows = ref<WmsCountOrderApi.CountOrder[]>([]);
const checkedIds = computed(() => checkedRows.value.map((item) => item.id!));
/** 勾选里有非新建态的单据时不许批量删除：后端逐张校验，删到一半失败前面的已经删了 */
const canDeleteBatch = computed(
  () =>
    !isEmpty(checkedRows.value) &&
    checkedRows.value.every((row) => isOrderDraft(row.status)),
);

function handleRowCheckboxChange({
  records,
}: {
  records: WmsCountOrderApi.CountOrder[];
}) {
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportCountOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '盘点单.xls', source: data });
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
          return await getCountOrderPage({
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
      isCurrent: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrder>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <CountResultModal @success="handleRefresh" />
    <LineDrawer />
    <Grid table-title="盘点单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['盘点单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:count-order:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:count-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:count-order:delete'],
              disabled: !canDeleteBatch,
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['wms:count-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '冻结',
              type: 'primary',
              link: true,
              icon: 'lucide:snowflake',
              auth: ['wms:count-order:freeze'],
              disabled: !canFreeze(row.status),
              onClick: handleFreeze.bind(null, row),
            },
            {
              label: '录实盘',
              type: 'primary',
              link: true,
              icon: 'lucide:clipboard-pen-line',
              auth: ['wms:count-order:submit-result'],
              disabled: !canSubmitResult(row.status),
              onClick: handleSubmitResult.bind(null, row),
            },
            {
              label: '确认差异',
              type: 'warning',
              link: true,
              icon: 'lucide:check-check',
              auth: ['wms:count-order:confirm-diff'],
              disabled: !canConfirmDiff(row.status),
              onClick: handleConfirmDiff.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:count-order:update'],
              disabled: !isOrderDraft(row.status),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:count-order:delete'],
              disabled: !isOrderDraft(row.status),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.countNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
