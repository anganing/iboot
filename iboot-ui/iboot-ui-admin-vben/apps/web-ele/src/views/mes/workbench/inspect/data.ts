import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { DICT_TYPE } from '@vben/constants';

/**
 * 检验录入作业页专用列定义。
 *
 * 没有复用 `views/mes/qctask/data.ts` 的 `useInspectFormColumns`：那份是单据页共用的只读列，
 * 作业页要在行尾挂「明细」按钮打开检验抽屉，加操作列不能去改共用的 data.ts（会波及单据页）。
 * 函数名带 Inspect 前缀，与 qctask 里的同名函数区分开。
 */
export function useInspectTaskGridColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 150 },
    { field: 'matCode', title: '物料编码', minWidth: 120 },
    { field: 'matName', title: '物料名称', minWidth: 140 },
    { field: 'sampleQty', title: '抽样数量', width: 100 },
    {
      field: 'status',
      title: '状态',
      width: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TASK_STATUS },
      },
    },
    // 入口从"整行点击"改为显式按钮：整行热区会误触，操作员点行往往只是想看清某格内容。
    // 只有「明细」1 个固定动作，按规范 3.4 取 1 位档 100。
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}
