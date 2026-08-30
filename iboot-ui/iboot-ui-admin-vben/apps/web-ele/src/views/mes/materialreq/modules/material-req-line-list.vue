<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

 import MaterialReqLineForm from './material-req-line-form.vue'
import { useVbenModal } from '@vben/common-ui';
import { ElMessage, ElLoading } from 'element-plus';
import { ref, computed, nextTick,watch } from 'vue';
import { $t } from '#/locales';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

 import { deleteMaterialReqLine, deleteMaterialReqLineList, getMaterialReqLinePage } from '#/api/mes/materialreq';
import { useMaterialReqLineGridFormSchema, useMaterialReqLineGridColumns } from '../data';
import { isEmpty } from '@vben/utils';

const props = defineProps<{
  reqId?: number // 领退补料申请编号（主表的关联字段）
}>()

 const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: MaterialReqLineForm,
  destroyOnClose: true,
});

/** 刷新表格 */
async function handleRefresh() {
   await gridApi.query();
}

/** 创建MES-领退补料申请明细 */
function handleCreate() {
  if (!props.reqId){
    ElMessage.warning("请先选择一个MES-领退补料申请!")
    return
  }
  formModalApi.setData({reqId: props.reqId}).open();
}

/** 编辑MES-领退补料申请明细 */
function handleEdit(row: MesMaterialReqApi.MaterialReqLine) {
  formModalApi.setData(row).open();
}

/** 删除MES-领退补料申请明细 */
async function handleDelete(row: MesMaterialReqApi.MaterialReqLine) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteMaterialReqLine(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除MES-领退补料申请明细 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteMaterialReqLineList(checkedIds.value);
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
  records: MesMaterialReqApi.MaterialReqLine[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useMaterialReqLineGridFormSchema(),
  },
  gridOptions: {
  columns: useMaterialReqLineGridColumns(),
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.reqId){
              return []
          }
          return await getMaterialReqLinePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            reqId: props.reqId,
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
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReqLine>,
  gridEvents:{
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  }
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.reqId,
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
      <Grid table-title="MES-领退补料申请明细列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['MES-领退补料申请']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mes:material-req:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                disabled: isEmpty(checkedIds),
                auth: ['mes:material-req:delete'],
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
                auth: ['mes:material-req:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mes:material-req:delete'],
                popConfirm: {
                  title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                  confirm: handleDelete.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </Grid>
</template>