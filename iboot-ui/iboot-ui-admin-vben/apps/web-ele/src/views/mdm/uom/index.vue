<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmUomApi } from '#/api/mdm/uom';

import { Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteUom, getUomPage } from '#/api/mdm/uom';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
function refresh() {
  gridApi.query();
}
function create() {
  formModalApi.setData(null).open();
}
function edit(row: MdmUomApi.Uom) {
  formModalApi.setData(row).open();
}
async function remove(row: MdmUomApi.Uom) {
  const loading = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteUom(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    refresh();
  } finally {
    loading.close();
  }
}
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          await getUomPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<MdmUomApi.Uom>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refresh" />
    <Grid table-title="计量单位列表">
      <template #toolbar-tools>
<TableAction
          :actions="[
            {
              label: '新建计量单位',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mdm:uom:create'],
              onClick: create,
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
              auth: ['mdm:uom:update'],
              onClick: edit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:uom:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: remove.bind(null, row),
              },
            },
          ]"
      />
</template>
    </Grid>
  </Page>
</template>
