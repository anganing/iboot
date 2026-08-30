<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

 import TransferOrderLineForm from './transfer-order-line-form.vue'
import { useVbenModal } from '@vben/common-ui';
import { ElMessage, ElLoading } from 'element-plus';
import { nextTick, ref, watch } from 'vue';
import { $t } from '#/locales';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

 import { deleteTransferOrderLine, deleteTransferOrderLineList, getTransferOrderLinePage } from '#/api/wms/transferorder';
import { useTransferOrderLineGridFormSchema, useTransferOrderLineGridColumns } from '../data';
import { isEmpty } from '@vben/utils';

const props = defineProps<{
  orderId?: number // 转移调拨单编号（主表的关联字段）
}>()

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
  if (!props.orderId){
    ElMessage.warning("请先选择一个移库调拨单!")
    return
  }
  formModalApi.setData({orderId: props.orderId}).open();
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
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除调拨明细 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteTransferOrderLineList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([])
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
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.orderId){
              return []
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
  pagerConfig: {
    enabled: true,
  },
  toolbarConfig: {
    refresh: true,
    search: true,
  },
  height: '600px',
  rowConfig: {
    keyField: 'id',
    isHover: true,
  },
  } as VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>,
  gridEvents:{
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  }
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.orderId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await handleRefresh()
  },
  { immediate: true },
);
</script>

<template>
       <FormModal @success="handleRefresh" />
      <Grid table-title="调拨明细列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['调拨明细']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:transfer-order:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                disabled: isEmpty(checkedIds),
                auth: ['wms:transfer-order:delete'],
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
</template>