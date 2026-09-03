<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { Page, useVbenModal } from '@vben/common-ui';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getEffectiveVersionPage } from '#/api/aps/version';
import GanttModal from '#/views/aps/gantt/modules/gantt-modal.vue';

import { useGridColumns, useGridFormSchema } from './data';

/**
 * 排程结果页面。
 *
 * <p>面向车间与 PMC：分页列出所有曾经生效过的排程版本，默认只看当前生效的，切状态可回看上一版。
 * 权限只要 `aps:task:query`，不需要版本管理的查询权，因此看不到求解失败、待确认这些中间版本。
 *
 * <p>只读：行操作只有甘特图，弹窗里带 `readonly` 打开，点任务条不会有调整入口。
 * 计划的调整与发布是计划员在排程版本列表里做的事。
 *
 * <p>不做跨场景合并甘特图：场景自带工厂、车间与计划窗口，不同场景的已发布计划本就并存。
 * 合到一张资源维度的图上，同一台设备会出现来自不同场景的重叠任务条，看起来像资源冲突，
 * 而时间窗、负荷合计、超期统计都是按单版本聚合的，一叠就没有口径了。
 */
const [ResultGanttModal, resultGanttModalApi] = useVbenModal({
  connectedComponent: GanttModal,
  destroyOnClose: true,
});

/** 查看某个生效版本的排程甘特图，标题带上场景与版本号 */
function handleViewGantt(row: ApsVersionApi.Version) {
  resultGanttModalApi
    .setData({
      readonly: true,
      title: `${row.scenarioName || row.scenarioCode || ''} V${row.versionNo ?? ''}`,
      versionId: row.id,
    })
    .open();
}

const [Grid] = useVbenVxeGrid({
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
          return await getEffectiveVersionPage({
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
    <ResultGanttModal />
    <Grid table-title="排程结果列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '甘特图',
              type: 'primary',
              link: true,
              icon: 'lucide:chart-gantt',
              auth: ['aps:task:query'],
              onClick: handleViewGantt.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
