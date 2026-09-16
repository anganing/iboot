<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteRoute,
  deleteRouteList,
  exportRoute,
  getRoutePage,
} from '#/api/mdm/route';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import RouteConfig from './modules/route-config.vue';
import RouteCopyForm from './modules/route-copy-form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [RouteCopyModal, routeCopyModalApi] = useVbenModal({
  connectedComponent: RouteCopyForm,
  destroyOnClose: true,
});
const [RouteConfigDrawer, routeConfigDrawerApi] = useVbenDrawer({
  connectedComponent: RouteConfig,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建工艺路线 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑工艺路线 */
function handleEdit(row: MdmRouteApi.Route) {
  formModalApi.setData(row).open();
}

/** 配置工艺路线 */
function handleConfig(row: MdmRouteApi.Route) {
  routeConfigDrawerApi.setData(row).open();
}

/** 复制工艺路线为新版本 */
function handleCopy(row: MdmRouteApi.Route) {
  routeCopyModalApi.setData(row).open();
}

/** 删除工艺路线 */
async function handleDelete(row: MdmRouteApi.Route) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteRoute(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除工艺路线（启用中的路线后端会拒绝删除，提前拦截给出明确提示） */
async function handleDeleteBatch() {
  const lockedRows = checkedRows.value.filter((row) => row.status === 0);
  if (lockedRows.length > 0) {
    ElMessage.warning(
      `勾选中包含 ${lockedRows.length} 条已启用的工艺路线，启用中的版本不可删除，请先取消勾选`,
    );
    return;
  }
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteRouteList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<MdmRouteApi.Id[]>([]);
const checkedRows = ref<MdmRouteApi.Route[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MdmRouteApi.Route[];
}) {
  checkedIds.value = records.map((item) => item.id!);
  checkedRows.value = records;
}

/** 导出表格 */
async function handleExport() {
  const data = await exportRoute(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '工艺路线.xls', source: data });
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
          return await getRoutePage({
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
  } as VxeTableGridOptions<MdmRouteApi.Route>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <RouteCopyModal @success="handleRefresh" />
    <RouteConfigDrawer @success="handleRefresh" />
    <Grid table-title="工艺路线列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['工艺路线']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mdm:route:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mdm:route:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:route:delete'],
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
              label: '配置',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mdm:route:query'],
              onClick: handleConfig.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mdm:route:update'],
              disabled: row.status === 0,
              tooltip: row.status === 0 ? '该路线已启用锁定，如需调整请复制新版本' : undefined,
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '复制新版本',
              type: 'primary',
              link: true,
              auth: ['mdm:route:create'],
              onClick: handleCopy.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:route:delete'],
              disabled: row.status === 0,
              tooltip: row.status === 0 ? '已启用锁定的路线不可删除' : undefined,
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
