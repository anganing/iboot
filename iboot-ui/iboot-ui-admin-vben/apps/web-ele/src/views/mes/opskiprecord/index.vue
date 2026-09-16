<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpSkipRecordApi } from '#/api/mes/opskiprecord';

import { useRouter } from 'vue-router';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOpSkipRecord,
  getOpSkipRecordPage,
  revokeOpSkip,
} from '#/api/mes/opskiprecord';
import { $t } from '#/locales';

import { MES_OP_SKIP_STATUS } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import OpSkipRecordDetail from './modules/op-skip-record-detail.vue';

/** 跳序记录没有本行唯一编码，详情只能由操作列按钮打开（不做双击链接列） */
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: OpSkipRecordDetail,
  destroyOnClose: true,
});
const router = useRouter();

function canRevoke(row: MesOpSkipRecordApi.OpSkipRecord) {
  return (
    row.status === MES_OP_SKIP_STATUS.PENDING ||
    row.status === MES_OP_SKIP_STATUS.EFFECTIVE
  ) && row.approvalStatus !== 1;
}

/** 打开只读详情 */
function handleDetail(row: MesOpSkipRecordApi.OpSkipRecord) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function handleProcessDetail(processInstanceId?: string) {
  if (processInstanceId) {
    router.push({ name: 'BpmProcessInstanceDetail', query: { id: processInstanceId } });
  }
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 撤销跳序：已生效的退回待开工，待审批的相当于撤回申请 */
async function handleRevoke(row: MesOpSkipRecordApi.OpSkipRecord) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认撤销工序【${row.opTaskNo}】的跳序记录？请填写撤销原因。`,
      '撤销跳序',
      {
        confirmButtonText: '确认撤销',
        cancelButtonText: '返回',
        inputPlaceholder: '请输入撤销原因',
        inputValidator: (input: string) =>
          input && input.trim().length > 0 ? true : '撤销原因不能为空',
        type: 'warning',
      },
    );
    reason = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在撤销跳序...' });
  try {
    await revokeOpSkip({ skipRecordId: row.id!, reason });
    ElMessage.success('已撤销');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportOpSkipRecord(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '工序跳过记录.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getOpSkipRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesOpSkipRecordApi.OpSkipRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="工序跳过记录列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:op-skip-record:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:op-skip-record:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '撤销',
              type: 'danger',
              link: true,
              icon: 'lucide:undo-2',
              auth: ['mes:op-task:revoke-skip'],
              disabled: !canRevoke(row),
              tooltip: canRevoke(row)
                ? undefined
                : '已撤销的跳序记录不能重复撤销；审批中的跳序须先在流程里驳回',
              onClick: handleRevoke.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: '审批进度',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              ifShow: !!row.processInstanceId,
              onClick: handleProcessDetail.bind(null, row.processInstanceId),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
