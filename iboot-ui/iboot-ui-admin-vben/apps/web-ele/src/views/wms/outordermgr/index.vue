<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { computed, ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelOutOrder,
  confirmOutOrder,
  deleteOutOrder,
  deleteOutOrderList,
  exportOutOrder,
  getOutOrderPage,
  submitOutOrder,
} from '#/api/wms/outordermgr';
import { $t } from '#/locales';

import { canPostOrder, isOrderDraft } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import OutOrderLineDrawer from './modules/out-order-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: OutOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 查看当前单据的出库明细（只读） */
function handleLines(row: WmsOutOrderApi.OutOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建出库单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑出库单，仅新建态可改 */
function handleEdit(row: WmsOutOrderApi.OutOrder) {
  formModalApi.setData(row).open();
}

/** 删除出库单，仅新建态可删 */
async function handleDelete(row: WmsOutOrderApi.OutOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.outNo]),
  });
  try {
    await deleteOutOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.outNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除出库单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteOutOrderList(checkedIds.value);
    checkedRows.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 提交出库单：新建推进到已确认 */
async function handleSubmit(row: WmsOutOrderApi.OutOrder) {
  try {
    await ElMessageBox.confirm(
      `确认提交出库单【${row.outNo}】？提交后明细不能再改，只能过账或取消。`,
      '提交出库单',
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
    await submitOutOrder({ id: row.id! });
    ElMessage.success('提交成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 过账出库单：写库存流水、扣减余额并核销行上预留 */
async function handleConfirm(row: WmsOutOrderApi.OutOrder) {
  try {
    await ElMessageBox.confirm(
      `确认过账出库单【${row.outNo}】？过账会写库存流水、扣减库存余额并核销行上预留，不可撤销。`,
      '出库过账',
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
    await confirmOutOrder({ id: row.id! });
    ElMessage.success('过账成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消出库单：仅新建态可取消，取消原因必填 */
async function handleCancel(row: WmsOutOrderApi.OutOrder) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消出库单【${row.outNo}】？请填写取消原因。`,
      '取消出库单',
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
    await cancelOutOrder({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedRows = ref<WmsOutOrderApi.OutOrder[]>([]);
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
  records: WmsOutOrderApi.OutOrder[];
}) {
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportOutOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '出库单.xls', source: data });
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
          return await getOutOrderPage({
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
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrder>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <LineDrawer />
    <Grid table-title="出库单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['出库单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:out-order:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:out-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
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
              auth: ['wms:out-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '提交',
              type: 'primary',
              link: true,
              icon: 'lucide:send',
              auth: ['wms:out-order:submit'],
              disabled: !isOrderDraft(row.status),
              onClick: handleSubmit.bind(null, row),
            },
            {
              label: '过账',
              type: 'primary',
              link: true,
              icon: 'lucide:check-check',
              auth: ['wms:out-order:confirm'],
              disabled: !canPostOrder(row.status),
              onClick: handleConfirm.bind(null, row),
            },
            {
              label: '取消',
              type: 'warning',
              link: true,
              icon: 'lucide:ban',
              auth: ['wms:out-order:cancel'],
              disabled: !isOrderDraft(row.status),
              onClick: handleCancel.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:out-order:update'],
              disabled: !isOrderDraft(row.status),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
              disabled: !isOrderDraft(row.status),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.outNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
