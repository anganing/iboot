<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmInvOrgApi } from '#/api/mdm/invorg';

import { Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteInvOrg, getInvOrgPage } from '#/api/mdm/invorg';
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
  formModalApi.setData(undefined).open();
}

function edit(row: MdmInvOrgApi.InvOrg) {
  formModalApi.setData(row).open();
}

async function remove(row: MdmInvOrgApi.InvOrg) {
  const loading = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteInvOrg(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
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
          getInvOrgPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<MdmInvOrgApi.InvOrg>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refresh" />
    <Grid table-title="库存组织列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '新建库存组织',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mdm:inv-org:create'],
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
              auth: ['mdm:inv-org:update'],
              onClick: edit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:inv-org:delete'],
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
