<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

 import ArrivalLineForm from './arrival-line-form.vue'
import { useVbenModal } from '@vben/common-ui';
import { ElMessage, ElLoading } from 'element-plus';
import { nextTick, ref, watch } from 'vue';
import { $t } from '#/locales';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

 import { deleteArrivalLine, deleteArrivalLineList, getArrivalLinePage } from '#/api/wms/arrival';
import { useArrivalLineGridFormSchema, useArrivalLineGridColumns } from '../data';
import { isEmpty } from '@vben/utils';

const props = defineProps<{
  arrivalId?: number // 到货登记编号（主表的关联字段）
}>()

 const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ArrivalLineForm,
  destroyOnClose: true,
});

/** 刷新表格 */
async function handleRefresh() {
   await gridApi.query();
}

/** 创建到货明细 */
function handleCreate() {
  if (!props.arrivalId){
    ElMessage.warning("请先选择一个到货登记!")
    return
  }
  formModalApi.setData({arrivalId: props.arrivalId}).open();
}

/** 编辑到货明细 */
function handleEdit(row: WmsArrivalApi.ArrivalLine) {
  formModalApi.setData(row).open();
}

/** 删除到货明细 */
async function handleDelete(row: WmsArrivalApi.ArrivalLine) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.lineNo]),
  });
  try {
    await deleteArrivalLine(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.lineNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除到货明细 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteArrivalLineList(checkedIds.value);
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
  records: WmsArrivalApi.ArrivalLine[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useArrivalLineGridFormSchema(),
  },
  gridOptions: {
  columns: useArrivalLineGridColumns(),
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.arrivalId){
              return []
          }
          return await getArrivalLinePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            arrivalId: props.arrivalId,
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
  } as VxeTableGridOptions<WmsArrivalApi.ArrivalLine>,
  gridEvents:{
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  }
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.arrivalId,
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
      <Grid table-title="到货明细列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['到货明细']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:arrival:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                disabled: isEmpty(checkedIds),
                auth: ['wms:arrival:delete'],
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
                auth: ['wms:arrival:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['wms:arrival:delete'],
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