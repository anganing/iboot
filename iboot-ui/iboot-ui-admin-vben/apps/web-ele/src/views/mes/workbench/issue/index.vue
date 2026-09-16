<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmMaterialReq,
  getMaterialReq,
  getMaterialReqLinePage,
  getMaterialReqPage,
} from '#/api/mes/materialreq';
import { LINE_MAX_COUNT, loadAllLines } from '#/api/mes/workbench';

import {
  useLineColumns,
  useReqGridColumns,
  useReqGridFormSchema,
} from './data';

const request = ref<MesMaterialReqApi.MaterialReq>();
const lines = ref<MesMaterialReqApi.MaterialReqLine[]>([]);
const truncated = ref(false);
const lineLoading = ref(false);
const remainingLineCount = computed(
  () =>
    lines.value.filter(
      (line) => Number(line.qty ?? 0) > Number(line.processedQty ?? 0),
    ).length,
);
const drawerTitle = computed(() =>
  request.value?.reqNo ? `领料确认 - ${request.value.reqNo}` : '领料确认',
);

/**
 * 不能确认出库的原因，空串表示可以确认。
 *
 * 禁用按钮不隐藏，原因放进 tooltip：现场操作员看到灰按钮猜不到是状态没到还是明细没读完。
 */
const confirmBlockReason = computed(() => {
  if (truncated.value) {
    return `明细超过 ${LINE_MAX_COUNT} 行，现场页不允许只处理部分明细，请转单据页处理`;
  }
  if (request.value?.status !== 'SUBMITTED') {
    return '仅已提交的领料申请可以确认出库';
  }
  return '';
});

const [ReqGrid, reqGridApi] = useVbenVxeGrid({
  formOptions: { schema: useReqGridFormSchema() },
  gridOptions: {
    columns: useReqGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getMaterialReqPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    // 不再用 isCurrent：vxe 的当前行高亮由点击行触发，入口改成「明细」按钮后
    // "被高亮的行"和"真正打开抽屉的行"会分叉，反而误导操作员
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReq>,
});
const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useLineColumns(),
    data: [],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: false, search: false, zoom: true },
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReqLine>,
});

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      return;
    }
    // 关闭抽屉即退出选中态，回到纯列表
    request.value = undefined;
    lines.value = [];
    truncated.value = false;
    lineGridApi.setGridOptions({ data: [] });
    void reqGridApi.query();
  },
});

function applyLines() {
  lineGridApi.setGridOptions({ data: lines.value });
}
/**
 * 打开领料确认抽屉：入口是操作列「明细」按钮，不再是整行点击（整行热区会误触）。
 */
async function handleOpenDetail(row: MesMaterialReqApi.MaterialReq) {
  request.value = await getMaterialReq(row.id!);
  // 主记录到位即开抽屉，明细在抽屉内加载，加载态对用户可见
  drawerApi.open();
  lineLoading.value = true;
  try {
    const loaded = await loadAllLines((params) =>
      getMaterialReqLinePage({ ...params, reqId: row.id! }),
    );
    lines.value = loaded.list;
    truncated.value = loaded.truncated;
    applyLines();
  } finally {
    lineLoading.value = false;
  }
}
async function handleConfirm() {
  if (!request.value?.id || truncated.value) return;
  try {
    await ElMessageBox.confirm(
      `确认领料申请【${request.value.reqNo}】出库？本次将处理 ${remainingLineCount.value} 行明细。`,
      '确认出库',
      { type: 'warning' },
    );
  } catch {
    return;
  }
  const loading = ElLoading.service({ text: '正在确认出库...' });
  try {
    await confirmMaterialReq({ id: request.value.id });
    request.value = await getMaterialReq(request.value.id);
    ElMessage.success(`领料已确认，WMS 单号：${request.value.wmsBizNo || '-'}`);
    await reqGridApi.query();
  } finally {
    loading.close();
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
      <div class="flex h-full min-h-0 flex-col p-4">
        <template v-if="request">
          <ElDescriptions :column="3" border size="small" class="mb-3 shrink-0">
            <ElDescriptionsItem label="申请单号">
              {{ request.reqNo }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="申请类型">
              {{
                getDictLabel(DICT_TYPE.MES_MATERIAL_REQ_TYPE, request.reqType) ||
                '-'
              }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="仓库">
              {{ request.whName || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="生产工单">
              {{ request.prodOrderNo || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="工序任务">
              {{ request.opTaskNo || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="WMS 单号">
              {{ request.wmsBizNo || '-' }}
            </ElDescriptionsItem>
          </ElDescriptions>
          <ElAlert
            v-if="truncated"
            class="mb-3 shrink-0"
            :closable="false"
            show-icon
            :title="`明细超过 ${LINE_MAX_COUNT} 行，现场页不允许只处理部分明细，请转单据页处理`"
            type="warning"
          />
          <LineGrid
            v-loading="lineLoading"
            class="min-h-0 w-full min-w-0 flex-1 overflow-hidden"
            :table-title="`领料明细 - ${request.reqNo}`"
          />
          <div class="mt-3 flex shrink-0 justify-end">
            <TableAction
              :actions="[
                {
                  label: '确认出库',
                  type: 'primary',
                  icon: 'lucide:package-check',
                  auth: ['mes:material-req:confirm'],
                  disabled: confirmBlockReason !== '',
                  // 禁用不隐藏，原因写进 tooltip；空串时 TableAction 不渲染提示
                  tooltip: confirmBlockReason,
                  onClick: handleConfirm,
                },
              ]"
            />
          </div>
        </template>
      </div>
    </Drawer>
    <ReqGrid table-title="待确认领料申请">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:material-req:query'],
              onClick: handleOpenDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </ReqGrid>
  </Page>
</template>
