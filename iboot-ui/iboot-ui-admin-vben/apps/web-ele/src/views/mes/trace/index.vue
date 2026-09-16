<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesTraceApi } from '#/api/mes/trace';

import { ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportTrace, getTraceTree, toGridRows } from '#/api/mes/trace';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { useGridColumns, useGridFormSchema } from './data';
import NodeDetailDrawer from './modules/node-detail.vue';

const [NodeDetail, nodeDetailDrawerApi] = useVbenDrawer({
  connectedComponent: NodeDetailDrawer,
  destroyOnClose: true,
});

/** 树形展开/收缩切换，树深了逐层点太慢 */
const isExpanded = ref(true);
function handleToggleExpand() {
  isExpanded.value = !isExpanded.value;
  if (isExpanded.value) {
    gridApi.grid.setAllTreeExpand(true);
  } else {
    gridApi.grid.setAllTreeExpand(false);
  }
}

/**
 * 双击业务单号（链接列）打开节点详情。
 *
 * 单号这里确实能唯一标识本行记录，满足宽表链接列规则；
 * 单击行不开抽屉——整行是最大热区，误触率最高。
 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesTraceApi.TraceRow;
}) {
  if (!isCodeLinkColumn(column)) return;
  nodeDetailDrawerApi.setData({ row }).open();
}

/** 导出当前链路 Excel（树已拍平带层级列，由后端生成） */
async function handleExport() {
  const values = await gridApi.formApi.getValues<MesTraceApi.TraceExportQuery>();
  if (!values?.startValue) {
    ElMessage.warning('请先填写起点值再导出');
    return;
  }
  const data = await exportTrace({
    direction: values.direction ?? 'FORWARD',
    startType: values.startType ?? 'LOT',
    startValue: values.startValue,
  });
  downloadFileFromBlobPart({ fileName: '批次追溯链路.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    // 追溯是一次性拉全树，不该有分页语义
    showCollapseButton: false,
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    pagerConfig: {
      enabled: false,
    },
    // 结果由我们自己算好塞进 rows，不走 ajax 代理
    proxyConfig: {
      ajax: {
        query: async (_params, formValues: MesTraceApi.TraceExportQuery) => {
          if (!formValues?.startValue) {
            return [];
          }
          const root = await getTraceTree({
            direction: formValues.direction ?? 'FORWARD',
            startType: formValues.startType ?? 'LOT',
            startValue: formValues.startValue,
          });
          return toGridRows(root);
        },
      },
    },
    rowConfig: {
      keyField: 'rowKey',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
    treeConfig: {
      childrenField: 'children',
      expandAll: true,
      reserve: true,
    },
  } as VxeTableGridOptions<MesTraceApi.TraceRow>,
});
</script>

<template>
  <Page auto-content-height>
    <NodeDetail />
    <Grid table-title="批次追溯链路" @cell-dblclick="handleCellDblclick">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: isExpanded ? '收缩' : '展开',
              type: 'primary',
              onClick: handleToggleExpand,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:trace:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
