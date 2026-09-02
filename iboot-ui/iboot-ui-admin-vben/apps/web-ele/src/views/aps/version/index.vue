<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  confirmVersion,
  exportVersion,
  getVersionPage,
  publishVersion,
} from '#/api/aps/version';
import { $t } from '#/locales';
import GanttModal from '#/views/aps/gantt/modules/gantt-modal.vue';

import { useGridColumns, useGridFormSchema } from './data';
import GenerateForm from './modules/generate-form.vue';
import SolveProgressDrawer from './modules/solve-progress-drawer.vue';

const [GenerateModal, generateModalApi] = useVbenModal({
  connectedComponent: GenerateForm,
  destroyOnClose: true,
});

const [VersionGanttModal, versionGanttModalApi] = useVbenModal({
  connectedComponent: GanttModal,
  destroyOnClose: true,
});

const [ProgressDrawer, progressDrawerApi] = useVbenDrawer({
  connectedComponent: SolveProgressDrawer,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 打开生成排程弹窗，把搜索栏里已选的场景带进去 */
async function handleGenerate() {
  const formValues = (await gridApi.formApi.getValues()) as {
    scenarioId?: number;
  };
  generateModalApi.setData({ scenarioId: formValues?.scenarioId }).open();
}

/** 提交成功后立刻打开进度抽屉：求解是异步的，不看进度就只能盯着列表刷 */
function handleGenerated(versionId: number) {
  handleRefresh();
  progressDrawerApi.setData({ versionId }).open();
}

/** 查看某个版本的求解进度，支持刷新页面后回来接着看 */
function handleViewProgress(row: ApsVersionApi.Version) {
  progressDrawerApi.setData({ versionId: row.id }).open();
}

/** 查看某个版本的排程甘特图，标题带上场景与版本号，全屏弹窗里不再重复选版本 */
function handleViewGantt(row: ApsVersionApi.Version) {
  versionGanttModalApi
    .setData({
      versionId: row.id,
      title: `${row.scenarioName || row.scenarioCode || ''} V${row.versionNo ?? ''}`,
    })
    .open();
}

/** 确认排程版本 */
async function handleConfirm(row: ApsVersionApi.Version) {
  const loadingInstance = ElLoading.service({ text: '正在确认版本...' });
  try {
    await confirmVersion(row.id!);
    ElMessage.success('排程版本已确认');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 发布排程版本 */
async function handlePublish(row: ApsVersionApi.Version) {
  const loadingInstance = ElLoading.service({ text: '正在发布版本...' });
  try {
    await publishVersion(row.id!);
    ElMessage.success('排程版本已发布');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportVersion(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '排程版本.xls', source: data });
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
          return await getVersionPage({
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
  } as VxeTableGridOptions<ApsVersionApi.Version>,
});
</script>

<template>
  <Page auto-content-height>
    <GenerateModal @success="handleGenerated" />
    <ProgressDrawer @finished="handleRefresh" />
    <VersionGanttModal />
    <Grid table-title="排程版本列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '生成排程',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['aps:version:update'],
              onClick: handleGenerate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:version:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '进度',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['aps:version:query'],
              onClick: handleViewProgress.bind(null, row),
            },
            {
              label: '甘特图',
              type: 'primary',
              link: true,
              icon: 'lucide:chart-gantt',
              auth: ['aps:task:query'],
              onClick: handleViewGantt.bind(null, row),
            },
            {
              label: '确认',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.AUDIT,
              auth: ['aps:version:update'],
              disabled: !['PENDING_CONFIRM', 'SOLVED', 'SOLVING'].includes(
                row.status ?? '',
              ),
              popConfirm: {
                title: `确认排程版本【${row.scenarioCode} V${row.versionNo}】的求解结果吗？`,
                confirm: handleConfirm.bind(null, row),
              },
            },
            {
              label: '发布',
              type: 'success',
              link: true,
              icon: ACTION_ICON.UPLOAD,
              auth: ['aps:version:update'],
              disabled: row.status !== 'CONFIRMED',
              popConfirm: {
                title: `发布后同场景原已发布版本将被置为已被替换，确认发布【${row.scenarioCode} V${row.versionNo}】吗？`,
                confirm: handlePublish.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
