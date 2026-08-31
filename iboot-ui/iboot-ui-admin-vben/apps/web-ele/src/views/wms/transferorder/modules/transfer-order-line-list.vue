<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, nextTick, ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteTransferOrderLine,
  deleteTransferOrderLineList,
  getTransferOrderLinePage,
} from '#/api/wms/transferorder';
import { $t } from '#/locales';

import { useTransferOrderLineGridColumns, useTransferOrderLineGridFormSchema } from '../data';
import TransferOrderLineForm from './transfer-order-line-form.vue';

const props = defineProps<{
  orderId?: number; // 移库调拨单编号（主表的关联字段）
  orderNo?: string; // 移库调拨单单号，用于表格标题
}>();

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在维护哪一单 */
const tableTitle = computed(() =>
  props.orderNo ? `调拨明细列表 - ${props.orderNo}` : '调拨明细列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: TransferOrderLineForm,
  destroyOnClose: true,
});

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

/** 创建调拨明细 */
function handleCreate() {
  if (!props.orderId) {
    ElMessage.warning('请先选择一个移库调拨单');
    return;
  }
  formModalApi.setData({ orderId: props.orderId }).open();
}

/** 编辑调拨明细 */
function handleEdit(row: WmsTransferOrderApi.TransferOrderLine) {
  formModalApi.setData(row).open();
}

/** 删除调拨明细 */
async function handleDelete(row: WmsTransferOrderApi.TransferOrderLine) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.lineNo]),
  });
  try {
    await deleteTransferOrderLine(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.lineNo]));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除调拨明细 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteTransferOrderLineList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: WmsTransferOrderApi.TransferOrderLine[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useTransferOrderLineGridFormSchema(),
  },
  gridOptions: {
    columns: useTransferOrderLineGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.orderId) {
            return { list: [], total: 0 };
          }
          return await getTransferOrderLinePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            orderId: props.orderId,
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
      custom: true,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 监听主表的关联字段变化，切换父记录时重新加载并清空勾选 */
watch(
  () => props.orderId,
  async (val) => {
    checkedIds.value = [];
    if (!val) {
      return;
    }
    await nextTick();
    await handleRefresh();
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex h-full min-h-0 min-w-0 flex-col overflow-hidden">
    <FormModal @success="handleRefresh" />
    <Grid class="w-full min-w-0 overflow-hidden" :table-title="tableTitle">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['调拨明细']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:transfer-order:create'],
              disabled: !orderId,
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
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
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:transfer-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.lineNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
