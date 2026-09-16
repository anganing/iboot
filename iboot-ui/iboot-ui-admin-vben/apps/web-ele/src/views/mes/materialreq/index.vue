<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelMaterialReq,
  confirmMaterialReq,
  deleteMaterialReq,
  deleteMaterialReqList,
  exportMaterialReq,
  getMaterialReqPage,
  submitMaterialReq,
} from '#/api/mes/materialreq';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { MES_MATERIAL_REQ_STATUS } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import MaterialReqDetail from './modules/material-req-detail.vue';
import MaterialReqLineDrawer from './modules/material-req-line-drawer.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
/** 明细走独立抽屉，列表页不内嵌平铺子表 */
const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: MaterialReqLineDrawer,
  destroyOnClose: true,
});
/** 主表只读详情：子表抽屉顶部的摘要只有 6 个字段，不能充当主表详情 */
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: MaterialReqDetail,
  destroyOnClose: true,
});
const router = useRouter();

function isApprovalRunning(row: MesMaterialReqApi.MaterialReq) {
  return row.approvalStatus === 1;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建领退补料申请 */
function handleCreate() {
  formModalApi.setData(null).open();
}

function isDraft(row: MesMaterialReqApi.MaterialReq) {
  return row.status === MES_MATERIAL_REQ_STATUS.DRAFT;
}

function canSubmit(row: MesMaterialReqApi.MaterialReq) {
  return row.status === MES_MATERIAL_REQ_STATUS.DRAFT;
}

function canConfirm(row: MesMaterialReqApi.MaterialReq) {
  return row.status === MES_MATERIAL_REQ_STATUS.SUBMITTED && !isApprovalRunning(row);
}

function canCancel(row: MesMaterialReqApi.MaterialReq) {
  return (
    row.status === MES_MATERIAL_REQ_STATUS.DRAFT ||
    row.status === MES_MATERIAL_REQ_STATUS.SUBMITTED
  ) && !isApprovalRunning(row);
}

function handleProcessDetail(processInstanceId?: string) {
  if (processInstanceId) {
    router.push({ name: 'BpmProcessInstanceDetail', query: { id: processInstanceId } });
  }
}

/** 打开主表只读详情 */
function handleDetail(row: MesMaterialReqApi.MaterialReq) {
  detailDrawerApi.setData({ id: row.id }).open();
}

/** 双击申请单号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesMaterialReqApi.MaterialReq;
}) {
  if (!isCodeLinkColumn(column)) return;
  handleDetail(row);
}

/** 提交申请：领料与补料逐行占库存预留 */
async function handleSubmit(row: MesMaterialReqApi.MaterialReq) {
  const approvalHint = row.reqType === 'SUPPLEMENT'
    ? '补料属于超发领料，提交后将进入审批，审批通过才可确认出库。'
    : '系统会按 BOM 计划量与历史领用量判断是否需要审批；进入审批后须审批通过才可确认出库。';
  try {
    await ElMessageBox.confirm(
      `确认提交领退补料申请【${row.reqNo}】？领料与补料会逐行占库存预留。${approvalHint}`,
      '提交申请',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '返回',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在提交...' });
  try {
    await submitMaterialReq({ id: row.id! });
    ElMessage.success('提交成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 确认申请：一次性过账，剩余量另开补料单 */
async function handleConfirm(row: MesMaterialReqApi.MaterialReq) {
  try {
    await ElMessageBox.confirm(
      `确认领退补料申请【${row.reqNo}】？将全部行按剩余量发料/退料并过账。`,
      '确认发料',
      {
        confirmButtonText: '确认过账',
        cancelButtonText: '返回',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在确认过账...' });
  try {
    await confirmMaterialReq({ id: row.id! });
    ElMessage.success('确认成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消申请：草稿直接取消，已提交先释放预留 */
async function handleCancel(row: MesMaterialReqApi.MaterialReq) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消领退补料申请【${row.reqNo}】？请填写取消原因。`,
      '取消申请',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '请输入取消原因',
        inputValidator: (input: string) =>
          input && input.trim().length > 0 ? true : '取消原因不能为空',
        type: 'warning',
      },
    );
    reason = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在取消...' });
  try {
    await cancelMaterialReq({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 编辑领退补料申请 */
function handleEdit(row: MesMaterialReqApi.MaterialReq) {
  formModalApi.setData(row).open();
}

/** 打开申请明细抽屉 */
function handleLines(row: MesMaterialReqApi.MaterialReq) {
  lineDrawerApi.setData(row).open();
}

/** 删除领退补料申请 */
async function handleDelete(row: MesMaterialReqApi.MaterialReq) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.reqNo]),
  });
  try {
    await deleteMaterialReq(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.reqNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除领退补料申请 */
async function handleDeleteBatch() {
  await confirm('确认删除所选领退补料申请？申请明细会一并级联删除。');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteMaterialReqList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesMaterialReqApi.MaterialReq[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportMaterialReq(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '领退补料申请.xls', source: data });
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
          return await getMaterialReqPage({
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
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReq>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <LineDrawer />
    <DetailDrawer />
    <Grid
      table-title="领退补料申请列表"
      @cell-dblclick="handleCellDblclick"
    >
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['领退补料申请']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:material-req:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:material-req:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:material-req:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:material-req:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: '提交',
              type: 'primary',
              link: true,
              auth: ['mes:material-req:submit'],
              disabled: !canSubmit(row),
              tooltip: canSubmit(row)
                ? undefined
                : '仅草稿状态可申请提交占预留',
              onClick: handleSubmit.bind(null, row),
            },
            {
              label: '确认',
              type: 'primary',
              link: true,
              auth: ['mes:material-req:confirm'],
              disabled: !canConfirm(row),
              tooltip: canConfirm(row)
                ? undefined
                : '需已提交且流程审批通过后才能确认过账',
              onClick: handleConfirm.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: '详情',
              type: 'primary',
              link: true,
              auth: ['mes:material-req:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '审批进度',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              ifShow: !!row.processInstanceId,
              onClick: handleProcessDetail.bind(null, row.processInstanceId),
            },
            {
              label: '取消',
              type: 'danger',
              link: true,
              auth: ['mes:material-req:cancel'],
              disabled: !canCancel(row),
              onClick: handleCancel.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mes:material-req:update'],
              disabled: !isDraft(row) || isApprovalRunning(row),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:material-req:delete'],
              disabled: !isDraft(row) || isApprovalRunning(row),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.reqNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
