<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWaveApi } from '#/api/wms/wave';

import { computed, nextTick, ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteWaveLine,
  deleteWaveLineList,
  getWaveLinePage,
} from '#/api/wms/wave';
import { $t } from '#/locales';

import { useWaveLineGridColumns, useWaveLineGridFormSchema } from '../data';
import WaveLineForm from './wave-line-form.vue';

const props = defineProps<{
  waveId?: number; // 出库波次编号（主表的关联字段）
  waveNo?: string; // 出库波次单号，用于表格标题
}>();

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在维护哪一单 */
const tableTitle = computed(() =>
  props.waveNo ? `波次明细列表 - ${props.waveNo}` : '波次明细列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: WaveLineForm,
  destroyOnClose: true,
});

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

/** 创建波次明细 */
function handleCreate() {
  if (!props.waveId) {
    ElMessage.warning('请先选择一个出库波次');
    return;
  }
  formModalApi.setData({ waveId: props.waveId }).open();
}

/** 编辑波次明细 */
function handleEdit(row: WmsWaveApi.WaveLine) {
  formModalApi.setData(row).open();
}

/** 删除波次明细 */
async function handleDelete(row: WmsWaveApi.WaveLine) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.lineNo]),
  });
  try {
    await deleteWaveLine(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.lineNo]));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除波次明细 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteWaveLineList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: WmsWaveApi.WaveLine[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useWaveLineGridFormSchema(),
  },
  gridOptions: {
    columns: useWaveLineGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.waveId) {
            return { list: [], total: 0 };
          }
          return await getWaveLinePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            waveId: props.waveId,
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
      custom: true,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<WmsWaveApi.WaveLine>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 监听主表的关联字段变化，切换父记录时重新加载并清空勾选 */
watch(
  () => props.waveId,
  async (val) => {
    checkedIds.value = [];
    if (!val) {
      return;
    }
    await nextTick();
    await handleRefresh();
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex h-full min-h-0 min-w-0 flex-col overflow-hidden">
    <FormModal @success="handleRefresh" />
    <Grid class="w-full min-w-0 overflow-hidden" :table-title="tableTitle">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['波次明细']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:wave:create'],
              disabled: !waveId,
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:wave:delete'],
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
              auth: ['wms:wave:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:wave:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.lineNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
