<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElInput,
  ElInputNumber,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmArrival,
  createInOrderFromArrival,
  getArrival,
  getArrivalLinePage,
  getArrivalPage,
  updateArrival,
} from '#/api/wms/arrival';
import {
  confirmInOrder,
  getInOrder,
  updateInOrder,
} from '#/api/wms/inordermgr';
import {
  getAvailableQtyMap,
  LINE_MAX_COUNT,
  loadAllLines,
} from '#/api/wms/workbench';
import { RelationSelect } from '#/components/relation-select';

import {
  canCreateInOrderFromArrival,
  isArrivalDraft,
  isArrivalLineReady,
  WMS_ARRIVAL_LINE_STATUS,
  WMS_ORDER_STATUS,
} from '../../order-form';
import { LOC_RELATION_PROPS } from '../../relation-config';
import {
  useArrivalGridColumns,
  useArrivalGridFormSchema,
  useReceiveLineColumns,
} from './data';

/**
 * 收货上架作业页：一屏走完"选到货单 → 逐行核实数量与批次 → 选上架库位 → 确认到货并上架"。
 *
 * 后端把这件事拆成了四个端点，作业页把它们串成一个动作（{@link handleConfirmAndPutaway}）：
 *
 * 1. `POST /wms/arrival/confirm` 确认到货：单据置已到货，逐行注册批次并回写批次号，
 *    按 `needQc` 把明细分流为待质检或待入库。**这一步不动库存。**
 * 2. `POST /wms/arrival/create-in-order` 按待入库明细生成采购入库单，返回入库单主键。
 *    生成的入库单是新建态，**明细的目标库位是空的**——后端既没走上架策略也没取默认库位。
 * 3. `PUT /wms/in-order/update` 把本次选定的库位写到入库明细上。少了这一步，
 *    第 4 步必定抛"入库明细必须指定目标库位"。
 * 4. `POST /wms/in-order/confirm` 过账：写库存流水、目标库位余额加数量，单据置已过账。
 *
 * 两个不能踩的坑：
 * - **顺序不能反**。没确认到货就没有待入库明细，生成入库单会拿到空明细。
 * - **绝不能调 `/wms/in-order/submit`**。提交会把入库单推到已确认态，而 `update` 只放行新建态，
 *   库位就再也补不上去，这张单永远无法过账。过账本身放行新建与已确认两态，提交纯属多余。
 */

/** 一次上架的结果，用来在界面上回显入库单号与过账结论 */
interface PutawayResult {
  /** 入库单主键 */
  inOrderId: number;
  /** 入库单号 */
  inNo?: string;
  /** 行数 */
  lineCount: number;
  /** 上架库位编码 */
  locCode?: string;
  /** 是否已过账。过账失败时留 false，界面给"重试过账"入口，不让入库单烂在新建态 */
  posted: boolean;
  /** 入库单状态 */
  status?: string;
  /** 合计计划入库数量 */
  totalQty: number;
}

/** 选中的到货单，走 `get` 取详情：列表 RespVO 是裁剪过的，上架要用到 `whId` 等单头字段 */
const arrival = ref<WmsArrivalApi.Arrival>();
/** 到货明细，本地数组就是数据源，行内输入框直接写回行上 */
const lines = ref<WmsArrivalApi.ArrivalLine[]>([]);
const truncated = ref(false);
const lineLoading = ref(false);
/** 物料编号到当前可用量的映射，按到货单的目标仓库现查 */
const availableQtyMap = ref(new Map<number, number>());
/** 本次上架的目标库位 */
const targetLocId = ref<number>();
const targetLocCode = ref<string>();
const lastResult = ref<PutawayResult>();

/** 草稿态才能改明细：`updateArrival` 只放行草稿，确认到货后数量与批次都是既成事实 */
const lineEditable = computed(() => isArrivalDraft(arrival.value?.status));
/** 待入库的明细，只有它们能生成入库单（后端 `selectReadyLines` 对其他状态直接抛错） */
const readyLines = computed(() =>
  lines.value.filter((line) => isArrivalLineReady(line.status)),
);
/** 待质检的明细：先检后入的料要等 IQC 结论，这一轮生成不了入库单 */
const qcPendingLines = computed(() =>
  lines.value.filter(
    (line) => line.status === WMS_ARRIVAL_LINE_STATUS.QC_PENDING,
  ),
);
const dirtyQty = computed(() =>
  lines.value.some(
    (line) => line.arrivalQty === undefined || Number(line.arrivalQty) <= 0,
  ),
);
const lineTableTitle = computed(() => {
  if (!arrival.value) {
    return '到货明细';
  }
  return `到货明细 - ${arrival.value.arrivalNo ?? ''}（共 ${lines.value.length} 行，待入库 ${readyLines.value.length} 行）`;
});

/** 抽屉标题跟随当前到货单，数据未就位时先给业务名兜底 */
const drawerTitle = computed(() =>
  arrival.value?.arrivalNo ? `收货上架 - ${arrival.value.arrivalNo}` : '收货上架',
);

/** 上架库位限定在到货单的目标仓库下：选到别的仓库的库位，余额会落到对不上的仓库维度上 */
const targetLocProps = computed(() => ({
  ...LOC_RELATION_PROPS,
  disabled: !arrival.value,
  extraParams: { whId: arrival.value?.whId },
  placeholder: arrival.value ? '请选择上架库位' : '请先选择到货单',
}));

/** 上架结果的一句话结论。过账失败时把"别重复点收货上架"写进去，否则会多生成一张入库单 */
const resultTitle = computed(() => {
  const result = lastResult.value;
  if (!result) {
    return '';
  }
  const label =
    getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, result.status) ||
    result.status ||
    '-';
  const inNo = result.inNo ?? `#${result.inOrderId}`;
  return result.posted
    ? `入库单 ${inNo} 已过账：${result.lineCount} 行、合计 ${result.totalQty}，上架库位 ${result.locCode ?? '-'}，单据状态 ${label}`
    : `入库单 ${inNo} 已生成但未过账（当前状态 ${label}），目标库位 ${result.locCode ?? '-'}。请点"重试过账"，不要重复点收货上架，否则会多生成一张入库单。`;
});

/**
 * 收货作业抽屉：选中到货单后承载"摘要 + 业务提示 + 选库位 + 明细作业"的整个右半场。
 *
 * destroyOnClose 保证每次打开都是干净的挂载；关闭时的清场与刷新见 {@link handleDrawerClosed}。
 */
const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onClosed: handleDrawerClosed,
});

const [ArrivalGrid, arrivalGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useArrivalGridFormSchema(),
  },
  gridOptions: {
    columns: useArrivalGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getArrivalPage({
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
  } as VxeTableGridOptions<WmsArrivalApi.Arrival>,
  gridEvents: {
    cellClick: ({
      column,
      row,
    }: {
      column: {
        slots?: { default?: unknown };
        type?: null | string;
      };
      row: WmsArrivalApi.Arrival;
    }) => {
      // checkbox 列、序号列与操作列的点击不算选中，避免误开抽屉
      if (column.type === 'checkbox' || column.type === 'seq') {
        return;
      }
      if (column.slots?.default === 'actions') {
        return;
      }
      handleSelectArrival(row);
    },
  },
});

const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useReceiveLineColumns(),
    data: [],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: {
      custom: true,
      // 本地数组驱动，没有 proxyConfig，自带刷新点了不取数，换成抽屉底部的"重新载入"
      refresh: false,
      search: false,
      zoom: true,
    },
  } as VxeTableGridOptions<WmsArrivalApi.ArrivalLine>,
});

/** 本地数组是数据源，vxe 不会自己感知数组变化，必须显式灌数据 */
function applyLines() {
  lineGridApi.setGridOptions({ data: lines.value });
}

/**
 * 行号索引，把 vxe 交给插槽的行对象换成 `lines` 里的那一个。
 *
 * 为什么要这一层：vxe 交给插槽的 `row` 指向同一个原始对象，但不经过 `ref` 的响应式代理，
 * 直接 `v-model="row.arrivalQty"` 写进去的值读得到、却不会触发依赖它的计算属性
 * （`dirtyQty` 这种"有行数量非法就禁用按钮"的判断会失灵）。所以行内控件一律经这里读写。
 * 索引只依赖每行的主键，改数量不会让它失效。
 */
const lineIndex = computed(
  () => new Map(lines.value.map((line) => [line.id, line])),
);

function lineOf(row: WmsArrivalApi.ArrivalLine) {
  return lineIndex.value.get(row.id);
}

/** 写到货数量。清空时落 undefined，由保存前的校验拦住 */
function setArrivalQty(
  row: WmsArrivalApi.ArrivalLine,
  value: null | number | undefined,
) {
  const target = lineOf(row);
  if (target) {
    target.arrivalQty = value ?? undefined;
  }
}

/** 写到货批次号。留空时后端会落默认批次 */
function setLotNo(row: WmsArrivalApi.ArrivalLine, value: string) {
  const target = lineOf(row);
  if (target) {
    target.lotNo = value;
  }
}

/** 写供应商批次号 */
function setSupplierLotNo(row: WmsArrivalApi.ArrivalLine, value: string) {
  const target = lineOf(row);
  if (target) {
    target.supplierLotNo = value;
  }
}

/** 取当前可用量的展示文本。查不到不写 0：没查到与真的是 0 是两件事 */
function availableQtyText(matId?: number) {
  if (!matId) {
    return '-';
  }
  const qty = availableQtyMap.value.get(Number(matId));
  return qty === undefined ? '-' : String(qty);
}

/** 按目标仓库与明细物料现查可用量，给操作员一个账实对照的锚点 */
async function refreshAvailableQty() {
  const matIds = lines.value
    .map((line) => Number(line.matId))
    .filter((id) => id > 0);
  if (matIds.length === 0 || !arrival.value?.whId) {
    availableQtyMap.value = new Map();
    return;
  }
  availableQtyMap.value = await getAvailableQtyMap({
    matIds,
    whId: arrival.value.whId,
  });
}

/** 读全部到货明细：明细分页每页最多 200 行 */
async function loadLines() {
  if (!arrival.value?.id) {
    lines.value = [];
    truncated.value = false;
    applyLines();
    return;
  }
  lineLoading.value = true;
  try {
    const loaded = await loadAllLines(getArrivalLinePage, {
      arrivalId: arrival.value.id,
    });
    truncated.value = loaded.truncated;
    lines.value = loaded.list;
    applyLines();
    await refreshAvailableQty();
  } finally {
    lineLoading.value = false;
  }
}

/** 选中一张到货单并打开作业抽屉：换单据要清掉上一单的库位与上架结果，库位跨仓库无效 */
async function handleSelectArrival(row: WmsArrivalApi.Arrival) {
  targetLocId.value = undefined;
  targetLocCode.value = undefined;
  lastResult.value = undefined;
  arrival.value = await getArrival(row.id);
  drawerApi.open();
  await loadLines();
}

/**
 * 关闭作业抽屉：清空选中状态回到纯列表，并刷新主列表。
 *
 * 动作成功路径里已有 `arrivalGridApi.query()`，这里的刷新兜住"只看未动"与
 * 关闭前结果尚未回显完的场景；本地明细数组与表格数据源要一起清掉，
 * 否则 destroyOnClose 重建 LineGrid 时会闪现上一单的行。
 */
function handleDrawerClosed() {
  arrival.value = undefined;
  lines.value = [];
  truncated.value = false;
  availableQtyMap.value = new Map();
  targetLocId.value = undefined;
  targetLocCode.value = undefined;
  lastResult.value = undefined;
  applyLines();
  arrivalGridApi.grid.clearCurrentRow();
  arrivalGridApi.query();
}

/** 重读当前到货单的单头与明细：状态与行状态都由服务端算，界面不自己推 */
async function reloadArrival() {
  if (arrival.value?.id) {
    arrival.value = await getArrival(arrival.value.id);
  }
  await loadLines();
}

/** 库位选择器回填编码快照，界面上要显示库位编码而不是主键 */
function handleLocChange(item?: Record<string, any> | Record<string, any>[]) {
  const row = Array.isArray(item) ? item[0] : item;
  targetLocCode.value = row?.code;
}

/**
 * 保存明细调整。
 *
 * 后端没有"按行改到货明细"的端点，明细只能经 `PUT /wms/arrival/update` 随单头整批替换
 * （旧行全删后重建），所以这里必须把全部行都带上，漏传的行等于删除。
 * 只映射请求 VO 认的字段：响应 VO 上还有 `qualifiedQty`、`status` 这些由动作推进的字段，
 * 原样回传会被后端忽略，但传过去只会让人误以为界面能改它们。
 */
async function handleSaveLines() {
  const current = arrival.value;
  if (!current) {
    return;
  }
  if (lines.value.length === 0) {
    ElMessage.warning('到货明细为空，不能保存');
    return;
  }
  if (dirtyQty.value) {
    ElMessage.warning('到货数量必须大于 0，请检查明细');
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在保存明细...' });
  try {
    await updateArrival({
      ...current,
      lines: lines.value.map((line) => ({
        lineNo: line.lineNo,
        sourceLineNo: line.sourceLineNo,
        matId: line.matId,
        lotNo: line.lotNo,
        supplierLotNo: line.supplierLotNo,
        arrivalQty: line.arrivalQty,
        uomId: line.uomId,
        needQc: line.needQc,
        iqcMode: line.iqcMode,
        remark: line.remark,
      })),
    });
    ElMessage.success('明细已保存');
    // 明细是整批重建的，行主键全变了，必须重读
    await reloadArrival();
  } finally {
    loadingInstance.close();
  }
}

/**
 * 给入库单填库位并过账。
 *
 * 单头整体回传（响应 VO 的字段名与请求 VO 一一对应，多出来的只读字段后端忽略），
 * 明细逐行覆盖 `locId`——到货生成的入库单明细库位是空的，不填这一步过账必抛
 * "入库明细必须指定目标库位"。
 */
async function postInOrder(inOrderId: number) {
  const inOrder = await getInOrder(inOrderId);
  const inLines = inOrder.lines ?? [];
  lastResult.value = {
    inNo: inOrder.inNo,
    inOrderId,
    lineCount: inLines.length,
    locCode: targetLocCode.value,
    posted: false,
    status: inOrder.status,
    totalQty: inLines.reduce((sum, line) => sum + Number(line.planQty ?? 0), 0),
  };
  if (inLines.length === 0) {
    ElMessage.error(`入库单 ${inOrder.inNo} 没有明细，无法过账`);
    return;
  }
  await updateInOrder({
    ...inOrder,
    lines: inLines.map((line) => ({ ...line, locId: targetLocId.value })),
  });
  // 不传幂等键：服务端按单据编号与动作确定性派生，连点两次会被判重
  await confirmInOrder({ id: inOrderId });
  const posted = await getInOrder(inOrderId);
  lastResult.value = {
    ...lastResult.value,
    posted: posted.status === WMS_ORDER_STATUS.DONE,
    status: posted.status,
  };
  ElMessage.success(
    `入库单 ${posted.inNo} 已过账，${inLines.length} 行上架到库位 ${targetLocCode.value}`,
  );
}

/** 生成入库单并上架：到货已确认的单据走这一段 */
async function createAndPutaway() {
  const current = arrival.value;
  if (!current) {
    return;
  }
  // 不传 lineIds：留空表示按全部待入库明细生成，待质检的行会被后端跳过
  const inOrderId = await createInOrderFromArrival({ id: current.id });
  await postInOrder(inOrderId);
  await reloadArrival();
  await arrivalGridApi.query();
}

/** 一键"确认到货并上架"：四步链的完整走法，草稿态的到货单用它 */
async function handleConfirmAndPutaway() {
  const current = arrival.value;
  if (!current) {
    return;
  }
  if (truncated.value) {
    ElMessage.warning(
      `到货明细超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，请改用入库单页面分批处理`,
    );
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认到货单【${current.arrivalNo}】的 ${lines.value.length} 行并上架到库位【${targetLocCode.value}】？服务端会依次：注册批次并确认到货、按待入库明细生成入库单、写入目标库位、过账增加库存。过账后不可撤销，冲销要另开反向单据。`,
      '确认到货并上架',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定收货上架',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在确认到货...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，前端造键等于每次都是新键，幂等失效
    await confirmArrival({ id: current.id });
    ElMessage.success('已确认到货，批次已按行注册');
    await reloadArrival();
    if (readyLines.value.length === 0) {
      ElMessage.warning(
        `全部 ${qcPendingLines.value.length} 行明细都要先检后入，等 IQC 出结论后再回来生成入库单`,
      );
      await arrivalGridApi.query();
      return;
    }
    await createAndPutaway();
  } finally {
    loadingInstance.close();
  }
}

/** 已确认到货的单据补做后半段：生成入库单、填库位、过账 */
async function handleCreateInOrderAndPutaway() {
  const current = arrival.value;
  if (!current) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认按到货单【${current.arrivalNo}】的 ${readyLines.value.length} 行待入库明细生成入库单，并上架到库位【${targetLocCode.value}】？过账后库存立即增加，不可撤销。`,
      '生成入库单并上架',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定生成并上架',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({
    text: '正在生成入库单并上架...',
  });
  try {
    await createAndPutaway();
  } finally {
    loadingInstance.close();
  }
}

/**
 * 重试过账。
 *
 * 上一次过账失败时入库单已经建出来、库位也写进去了，单据停在新建态。给一个重试入口，
 * 否则操作员只能跑到入库单页面找那张单，或者更糟——再点一次收货上架，多生成一张入库单。
 */
async function handleRetryPost() {
  const result = lastResult.value;
  if (!result) {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在重试过账...' });
  try {
    await postInOrder(result.inOrderId);
    await reloadArrival();
    await arrivalGridApi.query();
  } finally {
    loadingInstance.close();
  }
}
</script>

<template>
  <Page auto-content-height>
    <Drawer
      :title="drawerTitle"
      class="w-full lg:w-4/5"
      content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
    >
      <div class="flex h-full min-h-0 flex-col overflow-hidden px-4 pb-4">
        <ElDescriptions
          v-if="arrival"
          :column="3"
          border
          class="mb-2 shrink-0"
          size="small"
        >
          <ElDescriptionsItem label="到货单号">
            {{ arrival.arrivalNo || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="供应商">
            {{ arrival.supplierName || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="到货状态">
            {{
              getDictLabel(DICT_TYPE.WMS_ARRIVAL_STATUS, arrival.status) || '-'
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="目标仓库">
            {{ arrival.whName || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="来源单号">
            {{ arrival.sourceNo || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="预计到货">
            {{ arrival.expectDate || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElAlert
          v-if="truncated"
          class="mb-2 shrink-0"
          :closable="false"
          show-icon
          :title="`到货明细超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，不能在作业页处理，请改用入库单页面分批处理`"
          type="error"
        />
        <ElAlert
          v-if="qcPendingLines.length > 0"
          class="mb-2 shrink-0"
          :closable="false"
          show-icon
          :title="`有 ${qcPendingLines.length} 行明细是先检后入，本次不会生成入库单，等 IQC 出结论后再回来上架`"
          type="info"
        />
        <ElAlert
          v-if="lastResult"
          class="mb-2 shrink-0"
          :closable="false"
          show-icon
          :title="resultTitle"
          :type="lastResult.posted ? 'success' : 'warning'"
        />
        <!-- 上架库位单独一行：库位是本次上架的关键决策，保持独立行展示，不与表格工具栏混排 -->
        <div class="mb-2 flex shrink-0 items-center gap-2">
          <span class="whitespace-nowrap text-sm">上架库位</span>
          <RelationSelect
            v-model="targetLocId"
            v-bind="targetLocProps"
            class="w-72"
            @change="handleLocChange"
          />
          <span class="text-xs text-gray-500">
            本次上架的全部行进同一个库位；需要分行上架请在入库单页面按行改库位后过账
          </span>
        </div>
        <div class="min-h-0 flex-1">
          <LineGrid
            v-loading="lineLoading"
            class="h-full w-full min-w-0 overflow-hidden"
            :table-title="lineTableTitle"
          >
            <template #arrivalQty="{ row }">
              <ElInputNumber
                v-if="lineEditable"
                class="!w-full"
                controls-position="right"
                :min="0"
                :model-value="lineOf(row)?.arrivalQty"
                placeholder="按实收改"
                :precision="6"
                @update:model-value="(value) => setArrivalQty(row, value)"
              />
              <span v-else>{{ row.arrivalQty ?? '-' }}</span>
            </template>
            <template #availableQty="{ row }">
              {{ availableQtyText(row.matId) }}
            </template>
            <template #lotNo="{ row }">
              <ElInput
                v-if="lineEditable"
                clearable
                :maxlength="64"
                :model-value="lineOf(row)?.lotNo"
                placeholder="留空按默认批次"
                @update:model-value="(value) => setLotNo(row, value)"
              />
              <span v-else>{{ row.lotNo || '-' }}</span>
            </template>
            <template #supplierLotNo="{ row }">
              <ElInput
                v-if="lineEditable"
                clearable
                :maxlength="64"
                :model-value="lineOf(row)?.supplierLotNo"
                placeholder="供应商批次"
                @update:model-value="(value) => setSupplierLotNo(row, value)"
              />
              <span v-else>{{ row.supplierLotNo || '-' }}</span>
            </template>
          </LineGrid>
        </div>
        <div class="flex shrink-0 justify-end pt-2">
          <TableAction
            :actions="[
              {
                label: '保存明细调整',
                type: 'default',
                icon: 'lucide:save',
                auth: ['wms:arrival:update'],
                disabled: !lineEditable || lines.length === 0,
                onClick: handleSaveLines,
              },
              {
                label: '确认到货并上架',
                type: 'primary',
                icon: 'lucide:package-check',
                auth: ['wms:arrival:confirm'],
                disabled:
                  !lineEditable ||
                  lines.length === 0 ||
                  truncated ||
                  !targetLocId,
                onClick: handleConfirmAndPutaway,
              },
              {
                label: '生成入库单并上架',
                type: 'primary',
                icon: 'lucide:file-input',
                auth: ['wms:arrival:create-in-order'],
                disabled:
                  !arrival ||
                  !canCreateInOrderFromArrival(arrival.status) ||
                  readyLines.length === 0 ||
                  !targetLocId,
                onClick: handleCreateInOrderAndPutaway,
              },
              {
                label: '重试过账',
                type: 'warning',
                icon: 'lucide:refresh-cw',
                auth: ['wms:in-order:confirm'],
                disabled: !lastResult || lastResult.posted || !targetLocId,
                onClick: handleRetryPost,
              },
              {
                label: '重新载入',
                type: 'default',
                icon: 'lucide:rotate-ccw',
                auth: ['wms:arrival:query'],
                disabled: !arrival,
                onClick: reloadArrival,
              },
            ]"
          />
        </div>
      </div>
    </Drawer>
    <ArrivalGrid table-title="到货单列表" />
  </Page>
</template>
