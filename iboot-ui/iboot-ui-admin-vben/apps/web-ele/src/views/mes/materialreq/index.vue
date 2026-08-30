<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElTabs, ElTabPane } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMaterialReq,
  deleteMaterialReqList,
  exportMaterialReq,
  getMaterialReqPage,
} from '#/api/mes/materialreq';
import MaterialReqImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import MaterialReqLineList from './modules/material-req-line-list.vue';

 /** 子表的列表 */
const subTabsName = ref('materialReqLine')
const selectMaterialReq = ref<MesMaterialReqApi.MaterialReq>();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: MaterialReqImport,
  destroyOnClose: true,
});

/** 导入MES-领退补料申请 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建MES-领退补料申请 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑MES-领退补料申请 */
function handleEdit(row: MesMaterialReqApi.MaterialReq) {
  formModalApi.setData(row).open();
}

/** 删除MES-领退补料申请 */
async function handleDelete(row: MesMaterialReqApi.MaterialReq) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteMaterialReq(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除MES-领退补料申请 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
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
  downloadFileFromBlobPart({ fileName: 'MES-领退补料申请.xls', source: data });
}


const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: '600px',
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
      isCurrent: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReq>,
  gridEvents: {
    cellClick: ({ row }: { row: MesMaterialReqApi.MaterialReq}) => {
      selectMaterialReq.value = row;
    },
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <ImportFormModal @success="handleRefresh" />
   <div>
    <Grid table-title="MES-领退补料申请列表">
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
              label: '导入',
              type: 'primary',
              auth: ['mes:material-req:import'],
              onClick: handleImport,
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
     <!-- 子表的表单 -->
    <ElTabs v-model="subTabsName" class="mt-2">
          <ElTabPane name="materialReqLine" label="MES-领退补料申请明细">
            <MaterialReqLineList :req-id="selectMaterialReq?.id" />
          </ElTabPane>
    </ElTabs>
    </div>
  </Page>
</template>