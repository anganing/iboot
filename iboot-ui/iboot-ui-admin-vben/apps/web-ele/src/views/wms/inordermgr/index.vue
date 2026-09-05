<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { computed, ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelInOrder,
  confirmInOrder,
  deleteInOrder,
  deleteInOrderList,
  exportInOrder,
  getInOrderPage,
  submitInOrder,
} from '#/api/wms/inordermgr';
import { $t } from '#/locales';

import { canPostOrder, isOrderDraft } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import InOrderLineDrawer from './modules/in-order-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: InOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 查看当前单据的入库明细（只读） */
function handleLines(row: WmsInOrderApi.InOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建入库单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑入库单，仅新建态可改 */
function handleEdit(row: WmsInOrderApi.InOrder) {
  formModalApi.setData(row).open();
}

/** 删除入库单，仅新建态可删 */
async function handleDelete(row: WmsInOrderApi.InOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.inNo]),
  });
  try {
    await deleteInOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.inNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除入库单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteInOrderList(checkedIds.value);
    checkedRows.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 提交入库单：新建推进到已确认 */
async function handleSubmit(row: WmsInOrderApi.InOrder) {
  try {
    await ElMessageBox.confirm(
      `确认提交入库单【${row.inNo}】？提交后明细不能再改，只能过账或取消。`,
      '提交入库单',
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
    await submitInOrder({ id: row.id! });
    ElMessage.success('提交成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 过账入库单：写库存流水并增加余额 */
async function handleConfirm(row: WmsInOrderApi.InOrder) {
  try {
    await ElMessageBox.confirm(
      `确认过账入库单【${row.inNo}】？过账会写库存流水并增加库存余额，不可撤销。`,
      '入库过账',
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
    await confirmInOrder({ id: row.id! });
    ElMessage.success('过账成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消入库单：仅新建态可取消，取消原因必填 */
async function handleCancel(row: WmsInOrderApi.InOrder) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消入库单【${row.inNo}】？请填写取消原因。`,
      '取消入库单',
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
    await cancelInOrder({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedRows = ref<WmsInOrderApi.InOrder[]>([]);
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
  records: WmsInOrderApi.InOrder[];
}) {
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportInOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '入库单.xls', source: data });
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
          return await getInOrderPage({
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
  } as VxeTableGridOptions<WmsInOrderApi.InOrder>,
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
    <Grid table-title="入库单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['入库单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:in-order:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:in-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:in-order:delete'],
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
              auth: ['wms:in-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '提交',
              type: 'primary',
              link: true,
              icon: 'lucide:send',
              auth: ['wms:in-order:submit'],
              disabled: !isOrderDraft(row.status),
              onClick: handleSubmit.bind(null, row),
            },
            {
              label: '过账',
              type: 'primary',
              link: true,
              icon: 'lucide:check-check',
              auth: ['wms:in-order:confirm'],
              disabled: !canPostOrder(row.status),
              onClick: handleConfirm.bind(null, row),
            },
            {
              label: '取消',
              type: 'warning',
              link: true,
              icon: 'lucide:ban',
              auth: ['wms:in-order:cancel'],
              disabled: !isOrderDraft(row.status),
              onClick: handleCancel.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:in-order:update'],
              disabled: !isOrderDraft(row.status),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:in-order:delete'],
              disabled: !isOrderDraft(row.status),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.inNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
