<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, nextTick, ref, watch } from 'vue';

import { isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { createInOrderFromArrival, getArrivalLinePage } from '#/api/wms/arrival';

import {
  canCreateInOrderFromArrival,
  isArrivalLineReady,
} from '../../order-form';
import {
  useArrivalLineGridColumns,
  useArrivalLineGridFormSchema,
} from '../data';

/**
 * 到货明细列表，只读。
 *
 * 明细的增删改端点已随写入路径收口撤销：确认到货是**按明细逐行注册批次**的，事后加行改数量会让
 * 批次与明细对不上。要改明细回到草稿态的单据表单里整单改。
 *
 * 唯一的写动作是"按选中明细生成入库单"——它是领域动作而不是明细编辑：只挑待入库的行生成采购
 * 入库单，混着待质检明细的到货单靠它分批入库。整单生成在主列表的行操作里。
 */
const props = defineProps<{
  arrivalId?: number; // 到货登记编号（主表的关联字段）
  arrivalNo?: string; // 到货登记单号，用于表格标题
  arrivalStatus?: string; // 到货状态，决定能不能生成入库单
}>();
const emit = defineEmits(['success']);

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在看哪一单 */
const tableTitle = computed(() =>
  props.arrivalNo ? `到货明细列表 - ${props.arrivalNo}` : '到货明细列表',
);

const checkedRows = ref<WmsArrivalApi.ArrivalLine[]>([]);
/**
 * 能不能按选中明细生成入库单。
 *
 * 三个条件缺一不可：单据已确认到货、有勾选、且勾选的每一行都是待入库。
 * 后端 `selectReadyLines` 对非待入库的明细直接抛错，按钮先禁用比让用户撞错误提示强。
 */
const canCreateInOrder = computed(
  () =>
    canCreateInOrderFromArrival(props.arrivalStatus) &&
    !isEmpty(checkedRows.value) &&
    checkedRows.value.every((row) => isArrivalLineReady(row.status)),
);

function handleRowCheckboxChange({
  records,
}: {
  records: WmsArrivalApi.ArrivalLine[];
}) {
  checkedRows.value = records;
}

/** 刷新表格 */
async function handleRefresh() {
  checkedRows.value = [];
  await gridApi.query();
}

/** 按选中明细生成采购入库单 */
async function handleCreateInOrder() {
  const lineIds = checkedRows.value.map((row) => row.id!);
  try {
    await ElMessageBox.confirm(
      `确认按选中的 ${lineIds.length} 行明细生成采购入库单？生成后这些明细置为已完成，入库数量以到货数量为准。`,
      '生成入库单',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定生成',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在生成入库单...' });
  try {
    // 不传幂等键：领域侧按「到货单号 + 本次选中的行号串」派生，比前端能拼的更精确
    await createInOrderFromArrival({ id: props.arrivalId!, lineIds });
    ElMessage.success('入库单已生成');
    await handleRefresh();
    // 明细全部生成后到货单会置为已完成，通知主列表刷新状态
    emit('success');
  } finally {
    loadingInstance.close();
  }
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
    checkedRows.value = [];
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
    <Grid class="w-full min-w-0 overflow-hidden" :table-title="tableTitle">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '按选中明细生成入库单',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:arrival:create-in-order'],
              disabled: !canCreateInOrder,
              onClick: handleCreateInOrder,
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
