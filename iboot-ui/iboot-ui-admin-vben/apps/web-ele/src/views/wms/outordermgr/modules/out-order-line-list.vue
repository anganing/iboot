<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

 import OutOrderLineForm from './out-order-line-form.vue'
import { useVbenModal } from '@vben/common-ui';
import { ElMessage, ElLoading } from 'element-plus';
import { nextTick, ref, watch } from 'vue';
import { $t } from '#/locales';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

 import { deleteOutOrderLine, deleteOutOrderLineList, getOutOrderLinePage } from '#/api/wms/outordermgr';
import { useOutOrderLineGridFormSchema, useOutOrderLineGridColumns } from '../data';
import { isEmpty } from '@vben/utils';

const props = defineProps<{
  orderId?: number // 出库单编号（主表的关联字段）
}>()

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
  if (!props.orderId){
    ElMessage.warning("请先选择一个出库单!")
    return
  }
  formModalApi.setData({orderId: props.orderId}).open();
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
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除出库明细 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteOutOrderLineList(checkedIds.value);
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
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.orderId){
              return []
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
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>,
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
      <Grid table-title="出库明细列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['出库明细']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:out-order:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                disabled: isEmpty(checkedIds),
                auth: ['wms:out-order:delete'],
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
</template>