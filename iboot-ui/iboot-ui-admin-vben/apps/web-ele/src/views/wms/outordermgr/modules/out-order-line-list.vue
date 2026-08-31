<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { computed, nextTick, ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOutOrderLine,
  deleteOutOrderLineList,
  getOutOrderLinePage,
} from '#/api/wms/outordermgr';
import { $t } from '#/locales';

import { useOutOrderLineGridColumns, useOutOrderLineGridFormSchema } from '../data';
import OutOrderLineForm from './out-order-line-form.vue';

const props = defineProps<{
  orderId?: number; // 出库单编号（主表的关联字段）
  orderNo?: string; // 出库单单号，用于表格标题
}>();

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在维护哪一单 */
const tableTitle = computed(() =>
  props.orderNo ? `出库明细列表 - ${props.orderNo}` : '出库明细列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: OutOrderLineForm,
  destroyOnClose: true,
});

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

/** 创建出库明细 */
function handleCreate() {
  if (!props.orderId) {
    ElMessage.warning('请先选择一个出库单');
    return;
  }
  formModalApi.setData({ orderId: props.orderId }).open();
}

/** 编辑出库明细 */
function handleEdit(row: WmsOutOrderApi.OutOrderLine) {
  formModalApi.setData(row).open();
}

/** 删除出库明细 */
async function handleDelete(row: WmsOutOrderApi.OutOrderLine) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.lineNo]),
  });
  try {
    await deleteOutOrderLine(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.lineNo]));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除出库明细 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteOutOrderLineList(checkedIds.value);
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
  records: WmsOutOrderApi.OutOrderLine[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useOutOrderLineGridFormSchema(),
  },
  gridOptions: {
    columns: useOutOrderLineGridColumns(),
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
          return await getOutOrderLinePage({
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
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>,
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
              label: $t('ui.actionTitle.create', ['出库明细']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:out-order:create'],
              disabled: !orderId,
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
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
              auth: ['wms:out-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
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
