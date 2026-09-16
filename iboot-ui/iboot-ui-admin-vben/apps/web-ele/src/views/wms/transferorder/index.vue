<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelTransferOrder,
  confirmTransferOrder,
  deleteTransferOrder,
  deleteTransferOrderList,
  exportTransferOrder,
  getTransferOrderPage,
  submitTransferOrder,
} from '#/api/wms/transferorder';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { canPostOrder, isOrderDraft } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import TransferOrderDetailDrawer from './modules/transfer-order-detail.vue';
import TransferOrderLineDrawer from './modules/transfer-order-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: TransferOrderLineDrawer,
  destroyOnClose: true,
});

const [Detail, detailDrawerApi] = useVbenDrawer({
  connectedComponent: TransferOrderDetailDrawer,
  destroyOnClose: true,
});

/** 双击单据编号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: WmsTransferOrderApi.TransferOrder;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 查看当前单据的调拨明细（只读） */
function handleLines(row: WmsTransferOrderApi.TransferOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建移库调拨单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑移库调拨单，仅新建态可改 */
function handleEdit(row: WmsTransferOrderApi.TransferOrder) {
  formModalApi.setData(row).open();
}

/** 删除移库调拨单，仅新建态可删 */
async function handleDelete(row: WmsTransferOrderApi.TransferOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.transferNo]),
  });
  try {
    await deleteTransferOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.transferNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除移库调拨单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteTransferOrderList(checkedIds.value);
    checkedRows.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 提交移库调拨单：新建推进到已确认 */
async function handleSubmit(row: WmsTransferOrderApi.TransferOrder) {
  try {
    await ElMessageBox.confirm(
      `确认提交移库调拨单【${row.transferNo}】？提交后明细不能再改，只能过账或取消。`,
      '提交移库调拨单',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定提交',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在提交...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，连点两次会被判重
    await submitTransferOrder({ id: row.id! });
    ElMessage.success('提交成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 过账移库调拨单：来源维度扣减、目标维度增加 */
async function handleConfirm(row: WmsTransferOrderApi.TransferOrder) {
  try {
    await ElMessageBox.confirm(
      `确认过账移库调拨单【${row.transferNo}】？过账会把库存从来源库位搬到目标库位并写库存流水，不可撤销。`,
      '移库调拨过账',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定过账',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在过账...' });
  try {
    await confirmTransferOrder({ id: row.id! });
    ElMessage.success('过账成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消移库调拨单：仅新建态可取消，取消原因必填 */
async function handleCancel(row: WmsTransferOrderApi.TransferOrder) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消移库调拨单【${row.transferNo}】？请填写取消原因。`,
      '取消移库调拨单',
      {
        cancelButtonText: '返回',
        confirmButtonText: '确定取消单据',
        inputPlaceholder: '请输入取消原因',
        inputValidator: (input: string) =>
          input && input.trim().length > 0 ? true : '取消原因不能为空',
        type: 'warning',
      },
    );
    reason = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在取消...' });
  try {
    await cancelTransferOrder({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedRows = ref<WmsTransferOrderApi.TransferOrder[]>([]);
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
  records: WmsTransferOrderApi.TransferOrder[];
}) {
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportTransferOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '移库调拨单.xls', source: data });
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
          return await getTransferOrderPage({
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
  } as VxeTableGridOptions<WmsTransferOrderApi.TransferOrder>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>
<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Detail />
    <LineDrawer />
    <Grid table-title="移库调拨单列表" @cell-dblclick="handleCellDblclick">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['移库调拨单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:transfer-order:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:transfer-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
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
              auth: ['wms:transfer-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '提交',
              type: 'primary',
              link: true,
              icon: 'lucide:send',
              auth: ['wms:transfer-order:submit'],
              disabled: !isOrderDraft(row.status),
              tooltip: isOrderDraft(row.status) ? undefined : '仅草稿态的单据可提交',
              onClick: handleSubmit.bind(null, row),
            },
            {
              label: '过账',
              type: 'primary',
              link: true,
              icon: 'lucide:check-check',
              auth: ['wms:transfer-order:confirm'],
              disabled: !canPostOrder(row.status),
              tooltip: canPostOrder(row.status) ? undefined : '仅已提交的单据可过账',
              onClick: handleConfirm.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: '取消',
              type: 'warning',
              link: true,
              icon: 'lucide:ban',
              auth: ['wms:transfer-order:cancel'],
              disabled: !isOrderDraft(row.status),
              tooltip: isOrderDraft(row.status) ? undefined : '仅草稿态的单据可取消',
              onClick: handleCancel.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:transfer-order:update'],
              disabled: !isOrderDraft(row.status),
              tooltip: isOrderDraft(row.status) ? undefined : '仅草稿态的单据可修改',
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
              disabled: !isOrderDraft(row.status),
              tooltip: isOrderDraft(row.status) ? undefined : '仅草稿态的单据可删除',
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.transferNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
