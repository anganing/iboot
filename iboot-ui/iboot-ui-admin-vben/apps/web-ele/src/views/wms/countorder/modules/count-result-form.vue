<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCountOrderLinePage, submitCountResult } from '#/api/wms/countorder';

import { useCountResultEditorColumns } from '../data';

const emit = defineEmits(['success']);
/**
 * 录实盘弹窗：把当前盘点单的全部盘点行读进本地表格，逐行填实盘数量，一次性批量提交。
 *
 * 形态选择的理由：盘点行是服务端按余额快照生成的，界面上不能加行也不能删行，用户要做的只是
 * "对已有行批量填一个数"。做成一行一个弹窗会把一次录入拆成 N 个请求，出现"前几行已提交、
 * 单据已推进到盘点中、剩余行还没录"的半截状态——这正是后端把 `/submit-result` 设计成批量端点
 * 要避免的事。表格仍走 `useVbenVxeGrid`（没有手写 el-table），写入仍在 `useVbenModal` 里一次提交，
 * 没有"行内输入框 + 添加按钮"的即时追加式交互。
 */

/** 一次拉多少行：`PageParam.pageSize` 的上限是 200 */
const LINE_PAGE_SIZE = 200;
/** 最多加载多少行，全仓全盘的单据不至于把浏览器拖死 */
const LINE_MAX_COUNT = 5000;

const order = ref<WmsCountOrderApi.CountOrder>();
/** 盘点行，本地数组就是数据源；表格渲染的就是这些对象，输入框直接写回行上 */
const lines = ref<WmsCountOrderApi.CountOrderLine[]>([]);
/** 盘点行数超过上限被截断，此时不允许提交，避免只提交前 5000 行让用户以为盘完了 */
const truncated = ref(false);

/** 盲盘：账面数量与差异数量都不下发到界面，防止盘点员照抄 */
const blindCount = computed(() => order.value?.blindCountFlag === true);
const recordedCount = computed(
  () =>
    lines.value.filter(
      (line) => line.actualQty !== undefined && line.actualQty !== null,
    ).length,
);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useCountResultEditorColumns(),
    data: [],
    height: 420,
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: {
      custom: false,
      refresh: false,
      search: false,
      zoom: false,
    },
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>,
});

/** 读全部盘点行：`/count-order-line/page` 每页最多 200 行，全盘单据要翻页读完 */
async function loadLines(orderId: number) {
  const loaded: WmsCountOrderApi.CountOrderLine[] = [];
  let pageNo = 1;
  // do-while 保证循环体先执行一次，total 必然在第一轮被赋值，故不给多余初始值
  let total: number;
  do {
    const page = await getCountOrderLinePage({
      orderId,
      pageNo,
      pageSize: LINE_PAGE_SIZE,
    });
    total = Number(page.total ?? 0);
    loaded.push(...(page.list ?? []));
    pageNo += 1;
  } while (loaded.length < total && loaded.length < LINE_MAX_COUNT);
  truncated.value = loaded.length < total;
  lines.value = loaded;
  gridApi.setGridOptions({
    columns: useCountResultEditorColumns(blindCount.value),
    data: loaded,
  });
}

const [Modal, modalApi] = useVbenModal({
  confirmText: '提交实盘',
  async onConfirm() {
    if (truncated.value) {
      ElMessage.warning(
        `盘点行超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，请改用抽盘拆分单据后再录`,
      );
      return;
    }
    // 提交已录过数的行：实盘数量为空表示这一行还没盘，请求 VO 上 actualQty 是必填
    const results = lines.value
      .filter((line) => line.actualQty !== undefined && line.actualQty !== null)
      .map((line) => ({
        lineId: line.id,
        actualQty: Number(line.actualQty),
        remark: line.remark,
      }));
    if (results.length === 0) {
      ElMessage.warning('请至少录入一行实盘数量');
      return;
    }
    modalApi.lock();
    try {
      // 不传幂等键：服务端按 UI:{单据编号}:SUBMIT_COUNT_RESULT:0 确定性派生，
      // 前端填 uuid 或时间戳等于每次都是新键，幂等直接失效
      await submitCountResult({ id: order.value!.id, results });
      await modalApi.close();
      emit('success');
      ElMessage.success(`已提交 ${results.length} 行实盘结果`);
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      order.value = undefined;
      lines.value = [];
      truncated.value = false;
      return;
    }
    const data = modalApi.getData<WmsCountOrderApi.CountOrder>();
    if (!data?.id) {
      return;
    }
    order.value = data;
    modalApi.lock();
    try {
      await loadLines(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-[1200px]" title="录入实盘数量">
    <div class="mx-4 mb-4">
      <ElDescriptions v-if="order" :column="4" border class="mb-3" size="small">
        <ElDescriptionsItem label="盘点单号">
          {{ order.countNo || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点类型">
          {{ getDictLabel(DICT_TYPE.WMS_COUNT_TYPE, order.countType) || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="盘点仓库">
          {{ order.whName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ getDictLabel(DICT_TYPE.WMS_ORDER_STATUS, order.status) || '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <ElAlert
        v-if="blindCount"
        class="mb-3"
        :closable="false"
        show-icon
        title="盲盘单据：界面不显示账面数量与差异数量"
        type="info"
      />
      <ElAlert
        v-if="truncated"
        class="mb-3"
        :closable="false"
        show-icon
        :title="`盘点行超过 ${LINE_MAX_COUNT} 行，界面只读到一部分，不能提交`"
        type="error"
      />
      <Grid
        :table-title="`盘点行（已录 ${recordedCount} / 共 ${lines.length} 行）`"
      >
        <template #actualQty="{ row }">
          <ElInputNumber
            v-model="row.actualQty"
            class="!w-full"
            controls-position="right"
            :min="0"
            placeholder="请输入实盘数量"
            :precision="6"
          />
        </template>
        <template #lineRemark="{ row }">
          <ElInput
            v-model="row.remark"
            clearable
            :maxlength="500"
            placeholder="盘点备注"
          />
        </template>
      </Grid>
    </div>
  </Modal>
</template>
