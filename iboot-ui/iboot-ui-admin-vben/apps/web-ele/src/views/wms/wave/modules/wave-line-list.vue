<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWaveApi } from '#/api/wms/wave';

 import WaveLineForm from './wave-line-form.vue'
import { useVbenModal } from '@vben/common-ui';
import { ElMessage, ElLoading } from 'element-plus';
import { nextTick, ref, watch } from 'vue';
import { $t } from '#/locales';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

 import { deleteWaveLine, deleteWaveLineList, getWaveLinePage } from '#/api/wms/wave';
import { useWaveLineGridFormSchema, useWaveLineGridColumns } from '../data';
import { isEmpty } from '@vben/utils';

const props = defineProps<{
  waveId?: number // 波次编号（主表的关联字段）
}>()

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
  if (!props.waveId){
    ElMessage.warning("请先选择一个出库波次!")
    return
  }
  formModalApi.setData({waveId: props.waveId}).open();
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
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除波次明细 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteWaveLineList(checkedIds.value);
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
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.waveId){
              return []
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
  } as VxeTableGridOptions<WmsWaveApi.WaveLine>,
  gridEvents:{
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  }
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.waveId,
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
      <Grid table-title="波次明细列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['波次明细']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:wave:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                disabled: isEmpty(checkedIds),
                auth: ['wms:wave:delete'],
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
</template>