<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, nextTick, ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteArrivalLine,
  deleteArrivalLineList,
  getArrivalLinePage,
} from '#/api/wms/arrival';
import { $t } from '#/locales';

import { useArrivalLineGridColumns, useArrivalLineGridFormSchema } from '../data';
import ArrivalLineForm from './arrival-line-form.vue';

const props = defineProps<{
  arrivalId?: number; // 到货登记编号（主表的关联字段）
  arrivalNo?: string; // 到货登记单号，用于表格标题
}>();

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在维护哪一单 */
const tableTitle = computed(() =>
  props.arrivalNo ? `到货明细列表 - ${props.arrivalNo}` : '到货明细列表',
);

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
  if (!props.arrivalId) {
    ElMessage.warning('请先选择一个到货登记');
    return;
  }
  formModalApi.setData({ arrivalId: props.arrivalId }).open();
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
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除到货明细 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteArrivalLineList(checkedIds.value);
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
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.arrivalId) {
            return { list: [], total: 0 };
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
  } as VxeTableGridOptions<WmsArrivalApi.ArrivalLine>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 监听主表的关联字段变化，切换父记录时重新加载并清空勾选 */
watch(
  () => props.arrivalId,
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
              label: $t('ui.actionTitle.create', ['到货明细']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:arrival:create'],
              disabled: !arrivalId,
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:arrival:delete'],
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
  </div>
</template>
