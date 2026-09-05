<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmArrival,
  createInOrderFromArrival,
  deleteArrival,
  deleteArrivalList,
  exportArrival,
  getArrivalPage,
} from '#/api/wms/arrival';
import { $t } from '#/locales';

import { canCreateInOrderFromArrival, isArrivalDraft } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import ArrivalLineDrawer from './modules/arrival-line-drawer.vue';
import Form from './modules/form.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: ArrivalLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 查看当前单据的到货明细（只读，可按选中明细生成入库单） */
function handleLines(row: WmsArrivalApi.Arrival) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建到货登记 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑到货登记，仅草稿态可改 */
function handleEdit(row: WmsArrivalApi.Arrival) {
  formModalApi.setData(row).open();
}

/** 删除到货登记，仅草稿态可删 */
async function handleDelete(row: WmsArrivalApi.Arrival) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.arrivalNo]),
  });
  try {
    await deleteArrival(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.arrivalNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除到货登记 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteArrivalList(checkedIds.value);
    checkedRows.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 确认到货：逐行注册批次，按是否需要质检把明细分流为待质检或待入库 */
async function handleConfirm(row: WmsArrivalApi.Arrival) {
  try {
    await ElMessageBox.confirm(
      `确认到货登记【${row.arrivalNo}】？确认后会按明细逐行注册批次，明细不能再改，只能生成入库单。库存要等入库单过账才增加。`,
      '确认到货',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定确认到货',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在确认到货...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，连点两次会被判重
    await confirmArrival({ id: row.id! });
    ElMessage.success('已确认到货');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/**
 * 整单生成采购入库单：明细编号留空，后端取全部待入库明细。
 *
 * 待质检的明细不参与——它们要等 IQC 结论。只想生成一部分待入库明细时走"明细"抽屉里勾选生成。
 */
async function handleCreateInOrder(row: WmsArrivalApi.Arrival) {
  try {
    await ElMessageBox.confirm(
      `确认按到货登记【${row.arrivalNo}】的全部待入库明细生成采购入库单？待质检的明细不参与，生成后这些明细置为已完成。`,
      '生成入库单',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定生成',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在生成入库单...' });
  try {
    // 幂等键由领域侧按「到货单号 + 本次选中的行号串」派生，前端不传
    await createInOrderFromArrival({ id: row.id! });
    ElMessage.success('入库单已生成');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedRows = ref<WmsArrivalApi.Arrival[]>([]);
const checkedIds = computed(() => checkedRows.value.map((item) => item.id!));
/** 勾选里有非草稿态的单据时不许批量删除：后端逐张校验，删到一半失败前面的已经删了 */
const canDeleteBatch = computed(
  () =>
    !isEmpty(checkedRows.value) &&
    checkedRows.value.every((row) => isArrivalDraft(row.status)),
);

function handleRowCheckboxChange({
  records,
}: {
  records: WmsArrivalApi.Arrival[];
}) {
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportArrival(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '到货登记.xls', source: data });
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
          return await getArrivalPage({
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
  } as VxeTableGridOptions<WmsArrivalApi.Arrival>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>
<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <LineDrawer @success="handleRefresh" />
    <Grid table-title="到货登记列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['到货登记']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:arrival:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:arrival:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:arrival:delete'],
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
              auth: ['wms:arrival:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '确认到货',
              type: 'primary',
              link: true,
              icon: 'lucide:package-check',
              auth: ['wms:arrival:confirm'],
              disabled: !isArrivalDraft(row.status),
              onClick: handleConfirm.bind(null, row),
            },
            {
              label: '生成入库单',
              type: 'primary',
              link: true,
              icon: 'lucide:file-input',
              auth: ['wms:arrival:create-in-order'],
              disabled: !canCreateInOrderFromArrival(row.status),
              onClick: handleCreateInOrder.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:arrival:update'],
              disabled: !isArrivalDraft(row.status),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:arrival:delete'],
              disabled: !isArrivalDraft(row.status),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.arrivalNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
