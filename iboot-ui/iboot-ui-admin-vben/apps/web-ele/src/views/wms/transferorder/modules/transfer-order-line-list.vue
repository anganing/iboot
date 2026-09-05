<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, nextTick, watch } from 'vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTransferOrderLinePage } from '#/api/wms/transferorder';

import {
  useTransferOrderLineGridColumns,
  useTransferOrderLineGridFormSchema,
} from '../data';

/**
 * 调拨明细列表，只读。
 *
 * 明细的增删改端点已随写入路径收口撤销：已提交、已过账的单据还能改行数量与库位，就是账实一致的后门。
 * 要改明细回到新建态的单据表单里整单改。
 */
const props = defineProps<{
  orderId?: number; // 移库调拨单编号（主表的关联字段）
  orderNo?: string; // 移库调拨单单号，用于表格标题
}>();

const tableTitle = computed(() =>
  props.orderNo ? `调拨明细列表 - ${props.orderNo}` : '调拨明细列表',
);

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useTransferOrderLineGridFormSchema(),
  },
  gridOptions: {
    columns: useTransferOrderLineGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.orderId) {
            return { list: [], total: 0 };
          }
          return await getTransferOrderLinePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            orderId: props.orderId,
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
  } as VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>,
});

/** 监听主表的关联字段变化，切换父记录时重新加载 */
watch(
  () => props.orderId,
  async (val) => {
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
    <Grid class="w-full min-w-0 overflow-hidden" :table-title="tableTitle" />
  </div>
</template>
