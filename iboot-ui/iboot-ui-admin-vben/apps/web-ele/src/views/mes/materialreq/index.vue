<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMaterialReq,
  deleteMaterialReqList,
  exportMaterialReq,
  getMaterialReqPage,
} from '#/api/mes/materialreq';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import MaterialReqLineDrawer from './modules/material-req-line-drawer.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
/** 明细走独立抽屉，列表页不内嵌平铺子表 */
const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: MaterialReqLineDrawer,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建领退补料申请 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑领退补料申请 */
function handleEdit(row: MesMaterialReqApi.MaterialReq) {
  formModalApi.setData(row).open();
}

/** 打开申请明细抽屉 */
function handleLines(row: MesMaterialReqApi.MaterialReq) {
  lineDrawerApi.setData(row).open();
}

/** 删除领退补料申请 */
async function handleDelete(row: MesMaterialReqApi.MaterialReq) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.reqNo]),
  });
  try {
    await deleteMaterialReq(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.reqNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除领退补料申请 */
async function handleDeleteBatch() {
  await confirm('确认删除所选领退补料申请？申请明细会一并级联删除。');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteMaterialReqList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesMaterialReqApi.MaterialReq[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportMaterialReq(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '领退补料申请.xls', source: data });
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
          return await getMaterialReqPage({
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReq>,
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
    <Grid table-title="领退补料申请列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['领退补料申请']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:material-req:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:material-req:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:material-req:delete'],
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
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:material-req:query'],
              onClick: handleLines.bind(null, row),
            },
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
                title: $t('ui.actionMessage.deleteConfirm', [row.reqNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
