<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmBomApi } from '#/api/mdm/bom';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElTabs, ElTabPane } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteBom, deleteBomList, exportBom, getBomPage } from '#/api/mdm/bom';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import BomLineList from './modules/bom-line-list.vue';

/** 子表的列表 */
const subTabsName = ref('bomLine');
const selectBom = ref<MdmBomApi.Bom>();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建物料清单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑物料清单 */
function handleEdit(row: MdmBomApi.Bom) {
  formModalApi.setData(row).open();
}

/** 删除物料清单 */
async function handleDelete(row: MdmBomApi.Bom) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteBom(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除物料清单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteBomList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({ records }: { records: MdmBomApi.Bom[] }) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportBom(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '物料清单.xls', source: data });
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
          return await getBomPage({
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
  } as VxeTableGridOptions<MdmBomApi.Bom>,
  gridEvents: {
    cellClick: ({ row }: { row: MdmBomApi.Bom }) => {
      selectBom.value = row;
    },
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <div>
      <Grid table-title="物料清单列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['物料清单']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:bom:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.export'),
                type: 'primary',
                icon: ACTION_ICON.DOWNLOAD,
                auth: ['mdm:bom:export'],
                onClick: handleExport,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:bom:delete'],
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
                auth: ['mdm:bom:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:bom:delete'],
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
        <ElTabPane name="bomLine" label="物料清单明细">
          <BomLineList :bom-id="selectBom?.id" />
        </ElTabPane>
      </ElTabs>
    </div>
  </Page>
</template>
