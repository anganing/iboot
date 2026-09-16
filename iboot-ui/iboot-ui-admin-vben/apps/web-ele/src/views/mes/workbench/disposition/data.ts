import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { DICT_TYPE } from '@vben/constants';

/**
 * 不合格处置作业页专用列定义。
 *
 * 没有复用 `views/mes/qctask/data.ts`：那份是单据页共用的只读列，作业页要在行尾挂「明细」按钮
 * 打开处置抽屉，加操作列不能去回改共用的 data.ts（会波及质检任务单据页）。
 * 函数名带 Disposition 前缀，与 qctask 里的同名函数区分开。
 */
export function useDispositionTaskGridColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 160 },
    { field: 'matCode', title: '物料编码', minWidth: 130 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    { field: 'qtyRejected', title: '不合格数量', width: 110 },
    {
      field: 'triggerPoint',
      title: '触发点',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TRIGGER_POINT },
      },
    },
    // 入口从"整行点击"改为显式按钮，避免整行误触。只有「明细」1 个固定动作，按规范 3.4 取 1 位档 100。
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}
