<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDefectApi } from '#/api/mes/defect';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLink, ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteDefect,
  deleteDefectList,
  exportDefect,
  getDefect,
  getDefectPage,
} from '#/api/mes/defect';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DefectPreview from './modules/defect-preview.vue';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [PreviewModal, previewModalApi] = useVbenModal({
  connectedComponent: DefectPreview,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建缺陷记录 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑缺陷记录 */
function handleEdit(row: MesDefectApi.Defect) {
  formModalApi.setData(row).open();
}

/** 预览缺陷附件：列表不返回附件明细，先回查详情拿到重签名后的地址 */
async function handlePreview(row: MesDefectApi.Defect) {
  const loadingInstance = ElLoading.service({ text: '附件加载中' });
  try {
    const detail = await getDefect(row.id!);
    previewModalApi
      .setData({
        files: detail?.files ?? [],
        title: `${detail?.defectCode ?? ''} ${detail?.defectName ?? ''}`.trim(),
      })
      .open();
  } finally {
    loadingInstance.close();
  }
}

/** 删除缺陷记录 */
async function handleDelete(row: MesDefectApi.Defect) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.defectCode]),
  });
  try {
    await deleteDefect(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.defectCode]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除缺陷记录 */
async function handleDeleteBatch() {
  await confirm('确认删除所选缺陷记录？附件会一并清理。');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteDefectList(checkedIds.value);
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
  records: MesDefectApi.Defect[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportDefect(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '缺陷记录.xls', source: data });
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
          return await getDefectPage({
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
  } as VxeTableGridOptions<MesDefectApi.Defect>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <PreviewModal />
    <Grid table-title="缺陷记录列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['缺陷记录']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:defect:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:defect:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:defect:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #fileCount="{ row }">
        <ElLink
          v-if="(row.fileCount ?? 0) > 0"
          type="primary"
          @click="handlePreview(row)"
        >
          {{ row.fileCount }} 个附件
        </ElLink>
        <span v-else class="text-gray-400">无附件</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mes:defect:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:defect:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.defectCode]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
