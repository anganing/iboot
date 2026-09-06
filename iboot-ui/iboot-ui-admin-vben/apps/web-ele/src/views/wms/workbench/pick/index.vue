<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { computed, ref } from 'vue';

import { ColPage } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElInputNumber,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmOutOrder,
  getOutOrder,
  getOutOrderLinePage,
  getOutOrderPage,
  updateOutOrder,
} from '#/api/wms/outordermgr';
import {
  getAvailableQtyMap,
  LINE_MAX_COUNT,
  loadAllLines,
} from '#/api/wms/workbench';

import { canPostOrder, isOrderDraft, WMS_ORDER_STATUS } from '../../order-form';
import {
  useOutOrderGridColumns,
  useOutOrderGridFormSchema,
  useOutOrderLineColumns,
} from './data';

/**
 * 拣选发货作业页：一屏走完"选出库单 → 核对批次与库位 → 逐行确认拣选数量 → 确认发货"。
 *
 * 与任务书措辞的两处差别，都是后端事实决定的，不是把交互降级：
 *
 * 1. 任务书写"展示系统建议的批次与库位"。后端**没有拣选分配动作**：`submitOutOrder` 只把
 *    新建推到已确认，既不按 PICK 策略选批次库位，也不写 `allocatedQty`；`wms_strategy` 里的
 *    `PICK` 策略至今没有任何执行代码消费它。所以行上的批次与库位就是建单时指定的值，
 *    界面如实叫"指定批次""来源库位"，不假装是系统建议。缺任一个都过不了账，界面提前拦住。
 * 2. 任务书写"逐行确认拣选数量"。`POST /wms/out-order/confirm` 的请求 VO 只有
 *    `{id, idempotencyKey, remark}`，**没有按行提交拣选数量的入参**，过账量固定是
 *    `planQty - postedQty`。所以"确认拣选数量"的唯一落点是改计划数量后整单保存
 *    （`PUT /wms/out-order/update`，只放行新建态）。非新建态的单据只能核对，不能改。
 *
 * 作业页刻意不做"提交"与"取消"：`confirmOutOrder` 本身放行新建与已确认两态，提交纯属多余的
 * 一步，而提交之后 `update` 就失效——批次或库位没填全的单据会被锁死，再也补不上。
 * 单据的提交与取消留在 `views/wms/outordermgr`，作业页只负责把货发出去。
 */

/** 一次发货的结果，用来在界面上回显过账结论 */
interface DeliverResult {
  /** 过账行数 */
  lineCount: number;
  /** 出库单号 */
  outNo?: string;
  /** 过账后的单据状态 */
  status?: string;
  /** 本次过账合计数量 */
  totalQty: number;
}

/** 选中的出库单，走 `get` 取详情：列表 RespVO 裁剪过，保存明细要用到全部单头字段 */
const order = ref<WmsOutOrderApi.OutOrder>();
/** 出库明细，本地数组就是数据源，行内输入框直接写回行上 */
const lines = ref<WmsOutOrderApi.OutOrderLine[]>([]);
const truncated = ref(false);
const lineLoading = ref(false);
/** 键是 `${物料编号}:${质量状态}`，值是该维度在来源仓库的可用量合计 */
const availableQtyMap = ref(new Map<string, number>());
const lastResult = ref<DeliverResult>();

/** 本行本次要出的量，与后端 `confirmOutOrder` 的过账量口径一致 */
function remainingQty(line: WmsOutOrderApi.OutOrderLine) {
  return Number(line.planQty ?? 0) - Number(line.postedQty ?? 0);
}

/** 可用量的分组键：同一物料的合格品与待检品不能混算，混算会把待检品也当成能拣 */
function qtyKey(line: WmsOutOrderApi.OutOrderLine) {
  return `${Number(line.matId)}:${line.qualityStatus ?? ''}`;
}

/** 新建态才能改拣选数量：`updateOutOrder` 只放行新建态 */
const lineEditable = computed(() => isOrderDraft(order.value?.status));
/** 缺来源库位的行。后端在过账循环的第一步就查它，缺一行整单过不了账 */
const missingLoc = computed(() =>
  lines.value.filter((line) => !line.fromLocId),
);
/** 缺批次的行。只有还要出的行需要：可用量查询带批次维度，批次为空匹配不到余额行 */
const missingBatch = computed(() =>
  lines.value.filter((line) => remainingQty(line) > 0 && !line.batchId),
);

/** 按（物料, 质量状态）汇总本次要出的量：同一物料多行时逐行比会漏掉合计超量 */
const demandByKey = computed(() => {
  const map = new Map<string, number>();
  for (const line of lines.value) {
    const key = qtyKey(line);
    map.set(key, (map.get(key) ?? 0) + remainingQty(line));
  }
  return map;
});

/**
 * 可用量不足的分组键。
 *
 * 界面算的是"仓库 + 物料 + 质量状态"的汇总可用量，后端校验的是"含批次与库位"的精确维度，
 * 汇总量是精确量的上界——汇总都不够，后端必然抛"可用量不足"，所以这里拦住是安全的；
 * 反之汇总够也不保证精确维度够，那种情况只能让后端拦。
 */
const shortageKeys = computed(() => {
  const keys: string[] = [];
  for (const [key, demand] of demandByKey.value) {
    const available = availableQtyMap.value.get(key);
    if (available !== undefined && available < demand) {
      keys.push(key);
    }
  }
  return keys;
});

/**
 * 不能发货的原因，空字符串表示可以发。
 *
 * 做成"一句话原因"而不是单纯禁用按钮：作业页上禁用一个按钮却不说为什么，操作员只会反复点。
 * 这几条判断与后端 `confirmOutOrder` 的校验顺序一一对应，界面拦住的都是必然失败的情况。
 */
const blockReason = computed(() => {
  const current = order.value;
  if (!current) {
    return '请先在左侧选择一张出库单';
  }
  if (!canPostOrder(current.status)) {
    return `当前单据状态是「${getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, current.status) || current.status}」，只有新建与已确认的单据能过账`;
  }
  if (lines.value.length === 0) {
    return '出库明细为空，不能过账';
  }
  if (truncated.value) {
    return `出库明细超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，请改用出库单页面处理`;
  }
  if (missingLoc.value.length > 0) {
    return `有 ${missingLoc.value.length} 行没有来源库位，过账会被后端拒绝。后端没有拣选分配动作，请回出库单页面（仅新建态可改）逐行补库位`;
  }
  if (missingBatch.value.length > 0) {
    return `有 ${missingBatch.value.length} 行没有指定批次，可用量校验会匹配不到余额行。请回出库单页面（仅新建态可改）逐行补批次`;
  }
  if (shortageKeys.value.length > 0) {
    return `有 ${shortageKeys.value.length} 个物料的可用量低于本次要出的量，账实不符，先查清再发货`;
  }
  return '';
});

const lineTableTitle = computed(() => {
  if (!order.value) {
    return '出库明细';
  }
  const total = lines.value.reduce((sum, line) => sum + remainingQty(line), 0);
  return `出库明细 - ${order.value.outNo ?? ''}（共 ${lines.value.length} 行，本次要出 ${total}）`;
});

/** 发货结果的一句话结论 */
const resultTitle = computed(() => {
  const result = lastResult.value;
  if (!result) {
    return '';
  }
  const label =
    getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, result.status) ||
    result.status ||
    '-';
  return `出库单 ${result.outNo ?? '-'} 已确认发货：${result.lineCount} 行、合计 ${result.totalQty}，单据状态 ${label}。库存已按来源库位扣减，行上预留已核销。`;
});

const [OrderGrid, orderGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useOutOrderGridFormSchema(),
    wrapperClass: 'grid-cols-1',
  },
  gridOptions: {
    columns: useOutOrderGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getOutOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isCurrent: true, isHover: true },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: true,
      zoom: false,
    },
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrder>,
  gridEvents: {
    cellClick: ({ row }: { row: WmsOutOrderApi.OutOrder }) => {
      handleSelectOrder(row);
    },
  },
});

const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useOutOrderLineColumns(),
    data: [],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    // 缺批次或库位的行整行淡红：这些行会让整单过不了账，得一眼看见
    rowClassName: ({ row }: { row: WmsOutOrderApi.OutOrderLine }) =>
      row.fromLocId && (remainingQty(row) <= 0 || row.batchId)
        ? ''
        : 'wms-pick-blocked-row',
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: {
      custom: true,
      refresh: false,
      search: false,
      zoom: true,
    },
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>,
});

/** 本地数组是数据源，vxe 不会自己感知数组变化，必须显式灌数据 */
function applyLines() {
  lineGridApi.setGridOptions({ data: lines.value });
}

/**
 * 行号索引，把 vxe 交给插槽的行对象换成 `lines` 里的那一个。
 *
 * 为什么要这一层：vxe 交给插槽的 `row` 指向同一个原始对象，但不经过 `ref` 的响应式代理，
 * 直接 `v-model="row.planQty"` 写进去的值读得到、却不会触发 `shortageKeys`、`blockReason`
 * 这些计算属性——改完数量可用量判断还是旧的，"确认发货"按钮的启停跟着失准。
 * 索引只依赖每行的主键，改数量不会让它失效。
 */
const lineIndex = computed(
  () => new Map(lines.value.map((line) => [line.id, line])),
);

function lineOf(row: WmsOutOrderApi.OutOrderLine) {
  return lineIndex.value.get(row.id);
}

/** 写拣选数量（落在计划出库数量上，后端过账量就是它减已过账量） */
function setPlanQty(
  row: WmsOutOrderApi.OutOrderLine,
  value: null | number | undefined,
) {
  const target = lineOf(row);
  if (target) {
    target.planQty = value ?? undefined;
  }
}

/** 取可用量的展示文本。查不到不写 0：没查到与真的是 0 是两件事 */
function availableQtyText(line: WmsOutOrderApi.OutOrderLine) {
  const qty = availableQtyMap.value.get(qtyKey(line));
  return qty === undefined ? '-' : String(qty);
}

/** 本行所属的（物料, 质量状态）维度可用量是否不足 */
function isShortage(line: WmsOutOrderApi.OutOrderLine) {
  return shortageKeys.value.includes(qtyKey(line));
}

/** 按来源仓库、物料与质量状态分组现查可用量 */
async function refreshAvailableQty() {
  const whId = order.value?.whId;
  const map = new Map<string, number>();
  if (!whId || lines.value.length === 0) {
    availableQtyMap.value = map;
    return;
  }
  const groups = new Map<string, number[]>();
  for (const line of lines.value) {
    const matId = Number(line.matId);
    if (matId > 0) {
      const quality = line.qualityStatus ?? '';
      groups.set(quality, [...(groups.get(quality) ?? []), matId]);
    }
  }
  for (const [quality, matIds] of groups) {
    const loaded = await getAvailableQtyMap({
      matIds,
      qualityStatus: quality || undefined,
      whId,
    });
    loaded.forEach((qty, matId) => map.set(`${matId}:${quality}`, qty));
  }
  availableQtyMap.value = map;
}

/** 读全部出库明细：明细分页每页最多 200 行 */
async function loadLines() {
  if (!order.value?.id) {
    lines.value = [];
    truncated.value = false;
    applyLines();
    return;
  }
  lineLoading.value = true;
  try {
    const loaded = await loadAllLines(getOutOrderLinePage, {
      orderId: order.value.id,
    });
    truncated.value = loaded.truncated;
    lines.value = loaded.list;
    applyLines();
    await refreshAvailableQty();
  } finally {
    lineLoading.value = false;
  }
}

/** 左侧选中一张出库单：换单据要清掉上一单的发货结果 */
async function handleSelectOrder(row: WmsOutOrderApi.OutOrder) {
  lastResult.value = undefined;
  order.value = await getOutOrder(row.id);
  await loadLines();
}

/** 重读当前出库单的单头与明细：状态与进度数量都由服务端算，界面不自己推 */
async function reloadOrder() {
  if (order.value?.id) {
    order.value = await getOutOrder(order.value.id);
  }
  await loadLines();
}

/**
 * 保存拣选数量。
 *
 * 后端没有"按行改出库明细"的端点，明细只能经 `PUT /wms/out-order/update` 随单头整批替换
 * （旧行全删后重建），所以必须把全部行都带上，漏传的行等于删除。行对象整体回传而不是逐字段挑：
 * 请求 VO 的字段是响应 VO 的子集，多出来的只读字段（`matCode`、`postedQty` 这些）后端忽略，
 * 逐字段挑反而容易漏掉 `reservationId` 这种漏了就核销不了预留的关键字段。
 */
async function handleSavePickQty() {
  const current = order.value;
  if (!current) {
    return;
  }
  if (lines.value.length === 0) {
    ElMessage.warning('出库明细为空，不能保存');
    return;
  }
  if (
    lines.value.some(
      (line) => line.planQty === undefined || Number(line.planQty) <= 0,
    )
  ) {
    ElMessage.warning('拣选数量必须大于 0，请检查明细');
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在保存拣选数量...' });
  try {
    await updateOutOrder({
      ...current,
      lines: lines.value.map((line) => ({ ...line })),
    });
    ElMessage.success('拣选数量已保存');
    // 明细是整批重建的，行主键全变了，必须重读
    await reloadOrder();
  } finally {
    loadingInstance.close();
  }
}

/** 确认发货：逐行按"计划量 - 已过账量"过账，扣减来源库位余额并核销行上预留 */
async function handleConfirmDeliver() {
  const current = order.value;
  if (!current) {
    return;
  }
  if (blockReason.value) {
    ElMessage.warning(blockReason.value);
    return;
  }
  const totalQty = lines.value.reduce(
    (sum, line) => sum + remainingQty(line),
    0,
  );
  try {
    await ElMessageBox.confirm(
      `确认发货出库单【${current.outNo}】？服务端会逐行校验可用量后过账，共 ${lines.value.length} 行、合计 ${totalQty}，来源库位余额随之减少，行上预留一并核销。过账后不可撤销，冲销要另开反向单据。`,
      '确认发货',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定确认发货',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在过账发货...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，前端造键等于每次都是新键，幂等直接失效
    await confirmOutOrder({ id: current.id });
    const posted = await getOutOrder(current.id);
    lastResult.value = {
      lineCount: lines.value.length,
      outNo: posted.outNo,
      status: posted.status,
      totalQty,
    };
    ElMessage.success(
      posted.status === WMS_ORDER_STATUS.DONE
        ? `出库单 ${posted.outNo} 已过账发货`
        : `出库单 ${posted.outNo} 已提交过账，当前状态 ${posted.status}`,
    );
    await reloadOrder();
    await orderGridApi.query();
  } finally {
    loadingInstance.close();
  }
}
</script>

<template>
  <ColPage
    auto-content-height
    content-class="overflow-hidden p-0"
    :left-width="32"
    :left-min-width="24"
    :left-max-width="50"
    :right-width="68"
    :right-min-width="50"
    :right-max-width="76"
    resizable
    split-line
    split-handle
  >
    <template #left>
      <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
        <OrderGrid
          class="w-full min-w-0 overflow-hidden"
          table-title="出库单列表"
        />
      </section>
    </template>

    <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
      <ElDescriptions
        v-if="order"
        :column="3"
        border
        class="mb-2 shrink-0"
        size="small"
      >
        <ElDescriptionsItem label="出库单号">
          {{ order.outNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="出库类型">
          {{ getDictLabel(DICT_TYPE.WMS_OUT_BIZ_TYPE, order.bizType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, order.status) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源仓库">
          {{ order.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源单号">
          {{
            order.materialReqNo || order.sourceBizNo || order.originNo || '-'
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单">
          {{ order.prodOrderNo || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <ElAlert
        v-else
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        title="请在左侧选择一张出库单开始拣选"
        type="info"
      />
      <ElAlert
        v-if="order && blockReason"
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        :title="blockReason"
        type="warning"
      />
      <ElAlert
        v-if="lastResult"
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        :title="resultTitle"
        type="success"
      />
      <LineGrid
        v-loading="lineLoading"
        class="w-full min-w-0 overflow-hidden"
        :table-title="lineTableTitle"
      >
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '保存拣选数量',
                type: 'default',
                icon: 'lucide:save',
                auth: ['wms:out-order:update'],
                disabled: !lineEditable || lines.length === 0,
                onClick: handleSavePickQty,
              },
              {
                label: '确认发货',
                type: 'primary',
                icon: 'lucide:truck',
                auth: ['wms:out-order:confirm'],
                disabled: blockReason !== '',
                onClick: handleConfirmDeliver,
              },
              {
                label: '重新载入',
                type: 'default',
                icon: 'lucide:rotate-ccw',
                auth: ['wms:out-order:query'],
                disabled: !order,
                onClick: reloadOrder,
              },
            ]"
          />
        </template>
        <template #lotNo="{ row }">
          <span v-if="row.lotNo">{{ row.lotNo }}</span>
          <span v-else class="text-red-600">未指定</span>
        </template>
        <template #fromLocCode="{ row }">
          <span v-if="row.fromLocCode">{{ row.fromLocCode }}</span>
          <span v-else class="text-red-600">未指定</span>
        </template>
        <template #planQty="{ row }">
          <ElInputNumber
            v-if="lineEditable"
            class="!w-full"
            controls-position="right"
            :min="0"
            :model-value="lineOf(row)?.planQty"
            placeholder="按实拣改"
            :precision="6"
            @update:model-value="(value) => setPlanQty(row, value)"
          />
          <span v-else>{{ row.planQty ?? '-' }}</span>
        </template>
        <template #availableQty="{ row }">
          <span :class="isShortage(row) ? 'text-red-600 font-semibold' : ''">
            {{ availableQtyText(row) }}
          </span>
        </template>
      </LineGrid>
    </section>
  </ColPage>
</template>

<style scoped>
/* 缺批次或库位的行淡红底：这些行会让整单过不了账。只给单元格上色，
   vxe 的背景色画在 td 上，给 tr 上色会被 td 的背景盖掉。 */
:deep(.wms-pick-blocked-row td) {
  background-color: var(--el-color-danger-light-9);
}
</style>
