<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { ColPage } from '@vben/common-ui';
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
  ElSwitch,
} from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmCountDiff,
  freezeCountOrder,
  getCountOrder,
  getCountOrderLinePage,
  getCountOrderPage,
  submitCountResult,
} from '#/api/wms/countorder';
import { LINE_MAX_COUNT, loadAllLines } from '#/api/wms/workbench';

import {
  isCountLineDiff,
  isOrderDraft,
  WMS_ORDER_STATUS,
} from '../../order-form';
import {
  useCountLineColumns,
  useCountOrderGridColumns,
  useCountOrderGridFormSchema,
} from './data';

/**
 * 盘点作业页：一屏走完"选盘点单 → 冻结范围 → 逐行录实盘 → 提交 → 就地看差异 → 确认差异"。
 *
 * 与单据式的 `views/wms/countorder` 的分工：单据页管盘点单的建、改、删与只读明细复核，
 * 作业页只管把一张已经建好的单做完，不提供任何单据的增删改。两者共用同一套领域动作端点。
 *
 * 明细行的形态照 `countorder/modules/count-result-form.vue`：行由服务端按余额快照生成，
 * 界面不能加行也不能删行，用户只是给已有行填数，最后一次性批量提交。做成"一行一个弹窗"
 * 会把一次录入拆成 N 个请求，出现"前几行已提交、单据已推进到盘点中、剩余行还没录"的半截状态。
 */

/** 冻结按钮虽然任务书没要求，但不给它这一屏就走不完：见 {@link handleFreeze} 的注释 */
function canFreeze(status?: string) {
  return isOrderDraft(status);
}

/**
 * 可录实盘：已冻结或盘点中。
 *
 * 领域侧 `submitCountResult` 连 `DRAFT` 也放行，界面刻意收窄成"先冻结再录"，与单据页一致：
 * 一旦从 `DRAFT` 直接录实盘，单据就跳到 `COUNTING`，而 `freezeCountOrder` 只认新建态——
 * 这批货再也冻不上，盘点期间照样能被领走，账实对比的基准当场失效。
 */
function canSubmitResult(status?: string) {
  return (
    status === WMS_ORDER_STATUS.FROZEN || status === WMS_ORDER_STATUS.COUNTING
  );
}

/** 可确认差异：录过实盘之后（盘点中）。`DIFF_CONFIRMED` 代码里无人写入，界面不为它开按钮 */
function canConfirmDiff(status?: string) {
  return status === WMS_ORDER_STATUS.COUNTING;
}

/** 实盘数量为空表示这一行还没盘：请求 VO 上 `actualQty` 必填，不能把空值当 0 提交 */
function hasActualQty(line: WmsCountOrderApi.CountOrderLine) {
  return line.actualQty !== undefined && line.actualQty !== null;
}

const order = ref<WmsCountOrderApi.CountOrder>();
/** 盘点行，本地数组就是数据源：表格渲染的就是这些对象，行内输入框直接写回行上 */
const lines = ref<WmsCountOrderApi.CountOrderLine[]>([]);
/** 行数超过上限被截断，此时禁止提交，避免只提交前一部分让操作员以为整单盘完了 */
const truncated = ref(false);
/** 只看差异行：提交后差异往往只有几行，全量表格里翻找差异行很费时间 */
const diffOnly = ref(false);
const lineLoading = ref(false);

/** 盲盘：账面数量列不渲染，防止盘点员照抄 */
const blindCount = computed(() => order.value?.blindCountFlag === true);
const recordedCount = computed(() => lines.value.filter(hasActualQty).length);
const diffLines = computed(() =>
  lines.value.filter((line) => isCountLineDiff(line.status)),
);
/** 盘盈合计：差异为正的行的差异量之和 */
const gainQty = computed(() =>
  diffLines.value
    .filter((line) => Number(line.diffQty ?? 0) > 0)
    .reduce((sum, line) => sum + Number(line.diffQty ?? 0), 0),
);
/** 盘亏合计：差异为负的行取绝对值求和，界面上写成正数更好读 */
const lossQty = computed(() =>
  diffLines.value
    .filter((line) => Number(line.diffQty ?? 0) < 0)
    .reduce((sum, line) => sum - Number(line.diffQty ?? 0), 0),
);
const lineTableTitle = computed(() => {
  if (!order.value) {
    return '盘点行';
  }
  return `盘点行 - ${order.value.countNo ?? ''}（已录 ${recordedCount.value} / 共 ${lines.value.length} 行，差异 ${diffLines.value.length} 行）`;
});

const [OrderGrid, orderGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useCountOrderGridFormSchema(),
    wrapperClass: 'grid-cols-1',
  },
  gridOptions: {
    columns: useCountOrderGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCountOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isCurrent: true,
      isHover: true,
    },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: true,
      zoom: false,
    },
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrder>,
  gridEvents: {
    cellClick: ({ row }: { row: WmsCountOrderApi.CountOrder }) => {
      handleSelectOrder(row);
    },
  },
});

const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useCountLineColumns(),
    data: [],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    // 差异行整行淡色底：提交后要在几十行里一眼找到差异，只靠差异列的数字不够
    rowClassName: ({ row }: { row: WmsCountOrderApi.CountOrderLine }) =>
      isCountLineDiff(row.status) ? 'wms-count-diff-row' : '',
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: {
      custom: true,
      // 明细区是本地数组驱动的，没有 proxyConfig，自带刷新按钮点了不会重新取数，
      // 换成工具栏里的"重新载入"动作，语义也更准（丢弃未提交的录入重新读）
      refresh: false,
      search: false,
      zoom: true,
    },
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>,
});

/**
 * 明细区重新灌数据。
 *
 * 本地数组是数据源，vxe 不会自己感知数组变化，必须显式 `setGridOptions`
 * （与 `views/wms/arrival/modules/form.vue` 的 `syncLines` 同一套做法）。
 * 列定义一起给：盲盘开关由所选单据决定，换单据时账面列的可见性要跟着换。
 */
function applyLines() {
  lineGridApi.setGridOptions({
    columns: useCountLineColumns(blindCount.value),
    data: diffOnly.value ? diffLines.value : lines.value,
  });
}

/**
 * 行号索引，把 vxe 交给插槽的行对象换成 `lines` 里的那一个。
 *
 * 为什么要这一层：vxe 交给插槽的 `row` 指向同一个原始对象，但不经过 `ref` 的响应式代理，
 * 直接 `v-model="row.actualQty"` 写进去的值读得到、却不会触发 `recordedCount` 这类计算属性，
 * 结果就是"数字填上了、提交按钮还是灰的"（实测如此）。所以行内控件一律经这里读写。
 * 索引只依赖每行的主键，改数量不会让它失效，不存在反复重建。
 */
const lineIndex = computed(
  () => new Map(lines.value.map((line) => [line.id, line])),
);

function lineOf(row: WmsCountOrderApi.CountOrderLine) {
  return lineIndex.value.get(row.id);
}

/** 写实盘数量。清空时落 undefined 而不是 0：没盘与盘出 0 是两件事 */
function setActualQty(
  row: WmsCountOrderApi.CountOrderLine,
  value: null | number | undefined,
) {
  const target = lineOf(row);
  if (target) {
    target.actualQty = value ?? undefined;
  }
}

/** 写行备注 */
function setLineRemark(row: WmsCountOrderApi.CountOrderLine, value: string) {
  const target = lineOf(row);
  if (target) {
    target.remark = value;
  }
}

/** 读全部盘点行：明细分页每页最多 200 行，全盘单据要翻页读完 */
async function loadLines() {
  if (!order.value?.id) {
    lines.value = [];
    truncated.value = false;
    applyLines();
    return;
  }
  lineLoading.value = true;
  try {
    const loaded = await loadAllLines(getCountOrderLinePage, {
      orderId: order.value.id,
    });
    truncated.value = loaded.truncated;
    lines.value = loaded.list;
    applyLines();
  } finally {
    lineLoading.value = false;
  }
}

/** 左侧选中一张盘点单：清掉上一张单的过滤态，重新读明细 */
async function handleSelectOrder(row: WmsCountOrderApi.CountOrder) {
  order.value = row;
  diffOnly.value = false;
  await loadLines();
}

/**
 * 动作成功后重读。
 *
 * 单据状态、差异数量、行状态全部由服务端算，界面不自己推：自己推等于把状态机抄一遍，
 * 抄错了就会出现"界面说能确认差异、后端拒绝"的空转。
 */
async function reload() {
  if (order.value?.id) {
    order.value = await getCountOrder(order.value.id);
  }
  await loadLines();
  await orderGridApi.query();
}

/**
 * 冻结盘点范围。
 *
 * 任务书没要求作业页给冻结入口，但不给就一屏走不完：新建态的盘点单在作业页上无法推进，
 * 操作员得先跳回盘点单页点冻结再跳回来，直接违反"作业页一屏完成、不跳页"这条红线。
 * 端点与权限码沿用单据页的 `wms:count-order:freeze`，没有新增后端。
 */
async function handleFreeze() {
  const current = order.value;
  if (!current) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认冻结盘点单【${current.countNo}】的盘点范围？冻结会按账面数量逐行过账，盘点期间这批库存不能被领用，且冻结后不能再改盘点范围。`,
      '冻结盘点范围',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定冻结',
        type: 'warning',
      },
    );
  } catch {
    return; // 用户取消
  }
  const loadingInstance = ElLoading.service({ text: '正在冻结...' });
  try {
    // 不传幂等键：服务端按单据编号与动作确定性派生，连点两次会被判重
    await freezeCountOrder({ id: current.id });
    ElMessage.success('冻结成功');
    await reload();
  } finally {
    loadingInstance.close();
  }
}

/** 提交实盘：把已录过数的行一次性批量提交，差异由服务端按"实盘 - 账面"算 */
async function handleSubmitResult() {
  const current = order.value;
  if (!current) {
    return;
  }
  if (truncated.value) {
    ElMessage.warning(
      `盘点行超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，请改用抽盘拆分单据后再录`,
    );
    return;
  }
  const results = lines.value.filter(hasActualQty).map((line) => ({
    lineId: line.id,
    actualQty: Number(line.actualQty),
    remark: line.remark,
  }));
  if (results.length === 0) {
    ElMessage.warning('请至少录入一行实盘数量');
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认提交盘点单【${current.countNo}】的 ${results.length} 行实盘结果？服务端会按"实盘 - 账面"逐行算差异，单据推进到盘点中。未录数的 ${lines.value.length - results.length} 行保持待盘点，可以稍后补录。`,
      '提交实盘结果',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定提交',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在提交实盘...' });
  try {
    // 不传幂等键：服务端按 UI:{单据编号}:SUBMIT_COUNT_RESULT:0 确定性派生，
    // 前端填 uuid 或时间戳等于每次都是新键，幂等直接失效
    await submitCountResult({ id: current.id, results });
    ElMessage.success(`已提交 ${results.length} 行实盘结果`);
    await reload();
  } finally {
    loadingInstance.close();
  }
}

/**
 * 确认差异：按差异逐行生成盘盈入库单或盘亏出库单并立即过账。
 *
 * 刻意做成独立按钮而不是挂在提交实盘的成功回调里：阶段 3 接入 BPM 差异审批后，这个入口会
 * 换成"提交差异审批"，届时只改本函数与对应的那一个按钮，提交实盘的链路完全不受影响。
 * 提交实盘与确认差异本来也是两个决定——录完数不等于认账，差异要有人拍板。
 */
async function handleConfirmDiff() {
  const current = order.value;
  if (!current) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认盘点单【${current.countNo}】的 ${diffLines.value.length} 行差异？服务端会逐行生成盘盈入库单或盘亏出库单并立即过账，库存余额随之变动，不可撤销。`,
      '确认盘点差异',
      {
        cancelButtonText: '取消',
        confirmButtonText: '确定确认差异',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在确认差异...' });
  try {
    await confirmCountDiff({ id: current.id });
    ElMessage.success('差异已确认，已生成调整单并过账');
    await reload();
  } finally {
    loadingInstance.close();
  }
}
</script>

<template>
  <ColPage
    auto-content-height
    content-class="overflow-hidden p-0"
    :left-width="34"
    :left-min-width="26"
    :left-max-width="52"
    :right-width="66"
    :right-min-width="48"
    :right-max-width="74"
    resizable
    split-line
    split-handle
  >
    <template #left>
      <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
        <OrderGrid
          class="w-full min-w-0 overflow-hidden"
          table-title="盘点单列表"
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
        <ElDescriptionsItem label="盘点单号">
          {{ order.countNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点类型">
          {{ getDictLabel(DICT_TYPE.WMS_COUNT_TYPE, order.countType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, order.status) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点仓库">
          {{ order.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点库区">
          {{ order.zoneName || '全部库区' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否盲盘">
          {{
            getDictLabel(
              DICT_TYPE.INFRA_BOOLEAN_STRING,
              String(order.blindCountFlag ?? false),
            ) || '-'
          }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <ElAlert
        v-else
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        title="请在左侧选择一张盘点单开始作业"
        type="info"
      />
      <ElAlert
        v-if="blindCount"
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        title="盲盘单据：录入阶段不显示账面数量，差异在提交实盘后才可见"
        type="info"
      />
      <ElAlert
        v-if="truncated"
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        :title="`盘点行超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，不能提交，请改用抽盘拆分单据`"
        type="error"
      />
      <ElAlert
        v-if="diffLines.length > 0"
        class="mb-2 shrink-0"
        :closable="false"
        show-icon
        :title="`差异 ${diffLines.length} 行：盘盈合计 ${gainQty}，盘亏合计 ${lossQty}。确认差异会逐行生成盘盈入库单或盘亏出库单并立即过账。`"
        type="warning"
      />
      <!-- 过滤开关单独一行：塞进表格工具栏会被表格标题与四个按钮挤没 -->
      <div class="mb-2 flex shrink-0 items-center gap-2">
        <ElSwitch
          v-model="diffOnly"
          active-text="只看差异行"
          :disabled="diffLines.length === 0"
          @change="applyLines"
        />
        <span class="text-xs text-gray-500">
          提交实盘后差异往往只有几行，打开它可以直接把差异行筛出来复核
        </span>
      </div>
      <LineGrid
        v-loading="lineLoading"
        class="w-full min-w-0 overflow-hidden"
        :table-title="lineTableTitle"
      >
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '冻结范围',
                type: 'primary',
                icon: 'lucide:snowflake',
                auth: ['wms:count-order:freeze'],
                disabled: !order || !canFreeze(order.status),
                onClick: handleFreeze,
              },
              {
                label: '提交实盘',
                type: 'primary',
                icon: 'lucide:clipboard-pen-line',
                auth: ['wms:count-order:submit-result'],
                disabled:
                  !order ||
                  !canSubmitResult(order.status) ||
                  truncated ||
                  recordedCount === 0,
                onClick: handleSubmitResult,
              },
              {
                label: '确认差异',
                type: 'warning',
                icon: 'lucide:check-check',
                auth: ['wms:count-order:confirm-diff'],
                disabled:
                  !order ||
                  !canConfirmDiff(order.status) ||
                  diffLines.length === 0,
                onClick: handleConfirmDiff,
              },
              {
                label: '重新载入',
                type: 'default',
                icon: 'lucide:rotate-ccw',
                auth: ['wms:count-order:query'],
                disabled: !order,
                onClick: loadLines,
              },
            ]"
          />
        </template>
        <template #actualQty="{ row }">
          <ElInputNumber
            class="!w-full"
            controls-position="right"
            :disabled="!order || !canSubmitResult(order.status)"
            :min="0"
            :model-value="lineOf(row)?.actualQty"
            placeholder="请输入实盘数量"
            :precision="6"
            @update:model-value="(value) => setActualQty(row, value)"
          />
        </template>
        <template #diffQty="{ row }">
          <span v-if="row.diffQty === undefined || row.diffQty === null">
            -
          </span>
          <span v-else-if="Number(row.diffQty) > 0" class="text-green-600">
            +{{ row.diffQty }}
          </span>
          <span v-else-if="Number(row.diffQty) < 0" class="text-red-600">
            {{ row.diffQty }}
          </span>
          <span v-else>0</span>
        </template>
        <template #lineRemark="{ row }">
          <ElInput
            clearable
            :disabled="!order || !canSubmitResult(order.status)"
            :maxlength="500"
            :model-value="lineOf(row)?.remark"
            placeholder="盘点备注"
            @update:model-value="(value) => setLineRemark(row, value)"
          />
        </template>
      </LineGrid>
    </section>
  </ColPage>
</template>

<style scoped>
/* 差异行淡色底。只给单元格上色而不是整行：vxe 的背景色画在 td 上，
   给 tr 上色会被 td 的背景盖掉。悬停时让 vxe 自己的 hover 色接管，不加 !important 去抢。 */
:deep(.wms-count-diff-row td) {
  background-color: var(--el-color-warning-light-9);
}
</style>
