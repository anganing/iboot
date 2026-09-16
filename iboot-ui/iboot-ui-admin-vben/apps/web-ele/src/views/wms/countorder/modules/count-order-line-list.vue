<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, nextTick, watch } from 'vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCountOrderLinePage } from '#/api/wms/countorder';

import {
  useCountOrderLineGridColumns,
  useCountOrderLineGridFormSchema,
} from '../data';

/**
 * 盘点明细列表，只读。
 *
 * 明细的增删改端点已随写入路径收口撤销：盘点行是领域侧按余额快照生成的，`bookQty` 是账实对比的
 * 基准，人工加行、改账面、删掉不想盘的行就是账实一致的后门。实盘数量只能经列表页的
 * "录实盘"按钮走 `/submit-result` 批量提交。
 */
const props = defineProps<{
  // 主键是雪花 ID，超出 JS 安全整数（2^53）时后端序列化成字符串，因此父键两种形态都要接受。
  // 不能在这里 Number()/parseInt 转换：2098432401759043585 会被截成 2098432401759043600，
  // 拿一个不存在的编号去查明细，是查不出数据的静默故障。
  orderId?: number | string; // 盘点单编号（主表的关联字段）
  orderNo?: string; // 盘点单单号，用于表格标题
}>();

/** 表格标题带上当前父记录标识，抽屉里一眼能看出在看哪一单 */
const tableTitle = computed(() =>
  props.orderNo ? `盘点明细列表 - ${props.orderNo}` : '盘点明细列表',
);

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useCountOrderLineGridFormSchema(),
  },
  gridOptions: {
    columns: useCountOrderLineGridColumns(),
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
          return await getCountOrderLinePage({
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
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>,
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
