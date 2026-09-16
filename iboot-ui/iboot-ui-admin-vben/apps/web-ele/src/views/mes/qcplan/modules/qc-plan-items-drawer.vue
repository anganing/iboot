<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcItemApi } from '#/api/mes/qcitem';
import type { MesQcPlanApi } from '#/api/mes/qcplan';

import { computed, nextTick, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem, ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteQcItem,
  deleteQcItemList,
  exportQcItem,
  getQcItemPage,
} from '#/api/mes/qcitem';
import { getQcPlan } from '#/api/mes/qcplan';
import { $t } from '#/locales';

import {
  useQcItemGridColumns,
  useQcItemGridFormSchema,
} from '../data';
import QcPlanItemForm from './qc-plan-item-form.vue';

/**
 * 检验项目抽屉。
 *
 * 原独立菜单「检验项目」并入此处：检验项是检验方案的子表，脱离方案看项目没有意义，
 * 且后端写接口强校验父方案必须是草稿，只有站在方案上下文里才能判断该不该放行写操作。
 */
defineOptions({ name: 'MesQcPlanItemsDrawer' });

const plan = ref<MesQcPlanApi.QcPlan>();

/** 父方案标识：为空时子表不发请求，避免列出全库检验项、跨方案误删 */
const planId = computed(() => plan.value?.id);
/** 只有草稿方案允许维护检验项，与后端 validatePlanDraft 对齐 */
const isEditable = computed(() => plan.value?.status === 'DRAFT');
const lockedTip = '方案已不是草稿态，不可变更项目';

const drawerTitle = computed(() =>
  plan.value?.code ? `检验项目 - ${plan.value.code}` : '检验项目',
);
const tableTitle = computed(() =>
  plan.value?.code
    ? `检验项目列表 - ${plan.value.code} ${plan.value.name ?? ''}`
    : '检验项目列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: QcPlanItemForm,
  destroyOnClose: true,
});

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesQcItemApi.QcItem[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 刷新表格并清空勾选，切换方案时不残留上一个方案的选中态 */
async function handleRefresh() {
  checkedIds.value = [];
  await gridApi.query();
}

function handleCreate() {
  if (!planId.value) {
    return;
  }
  // 父键由抽屉注入，不在表单里露出
  formModalApi.setData({ planId: planId.value }).open();
}

function handleEdit(row: MesQcItemApi.QcItem) {
  formModalApi.setData({ id: row.id, planId: planId.value }).open();
}

async function handleDelete(row: MesQcItemApi.QcItem) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.itemCode]),
  });
  try {
    await deleteQcItem(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.itemCode]));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteQcItemList(checkedIds.value);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    await handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/**
 * 导出当前方案的检验项目。
 *
 * 独立菜单下线后 `mes:qc-item:export` 只剩这个入口，不能省；
 * planId 必须在 formValues 之后覆盖，否则导出会把别的方案一起带出去。
 */
async function handleExport() {
  if (!planId.value) {
    return;
  }
  const data = await exportQcItem({
    ...(await gridApi.formApi.getValues()),
    planId: planId.value,
  });
  downloadFileFromBlobPart({
    fileName: `检验项目_${plan.value?.code ?? ''}.xls`,
    source: data,
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    // 抽屉宽 4/5 屏，不属于规范里需显式选列数的受限容器，沿用默认响应式栅格
    schema: useQcItemGridFormSchema(),
  },
  gridOptions: {
    columns: useQcItemGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          // 后端 planId 非 @NotNull，不传会返回全量，这里在空父键时短路
          if (!planId.value) {
            return { list: [], total: 0 };
          }
          return await getQcItemPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            // planId 放在 formValues 之后：父键由抽屉独占，筛选表单里没有它，
            // 万一将来有人加回 planId 条件，也不能让它把父键改掉
            ...formValues,
            planId: planId.value,
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
  } as VxeTableGridOptions<MesQcItemApi.QcItem>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      plan.value = undefined;
      checkedIds.value = [];
      return;
    }
    // 先用列表行数据即时展示 code/name/status，再回查补全版本号与抽样配置
    plan.value = drawerApi.getData<MesQcPlanApi.QcPlan>();
    const id = plan.value?.id;
    if (!id) {
      return;
    }
    try {
      plan.value = await getQcPlan(id);
    } catch {
      // 极端情况下方案已被删除，保留行数据兜底
    }
    // onOpenChange 先于内容组件挂载触发，等一格再查，避免 grid 未 mount 时 query 落空
    await nextTick();
    await handleRefresh();
  },
});
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-4/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <FormModal @success="handleRefresh" />
    <div class="flex h-full min-h-0 flex-col overflow-hidden px-4 pb-4">
      <ElDescriptions
        v-if="plan"
        :column="3"
        border
        size="small"
        class="mb-3 shrink-0"
      >
        <ElDescriptionsItem label="方案编码">{{ plan.code || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="方案名称">{{ plan.name || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="版本号">{{ plan.versionNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ getDictLabel(DICT_TYPE.MES_QC_PLAN_STATUS, plan.status) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="抽样类型">
          {{ getDictLabel(DICT_TYPE.MES_QC_SAMPLE_TYPE, plan.sampleType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="AQL 质量限">{{ plan.aql || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
      <Grid
        class="min-h-0 w-full min-w-0 flex-1 overflow-hidden"
        :table-title="tableTitle"
      >
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['检验项目']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mes:qc-item:create'],
                disabled: !isEditable,
                tooltip: isEditable ? undefined : lockedTip,
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.export'),
                type: 'primary',
                icon: ACTION_ICON.DOWNLOAD,
                auth: ['mes:qc-item:export'],
                onClick: handleExport,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mes:qc-item:delete'],
                disabled: !isEditable || isEmpty(checkedIds),
                tooltip: isEditable ? undefined : lockedTip,
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
                auth: ['mes:qc-item:update'],
                disabled: !isEditable,
                tooltip: isEditable ? undefined : lockedTip,
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mes:qc-item:delete'],
                disabled: !isEditable,
                tooltip: isEditable ? undefined : lockedTip,
                popConfirm: {
                  title: $t('ui.actionMessage.deleteConfirm', [row.itemCode]),
                  confirm: handleDelete.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </Grid>
    </div>
  </Drawer>
</template>
