import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesTraceApi } from '#/api/mes/trace';

import { codeLinkColumn } from '#/utils';

/** 节点类型中文映射，键与后端 MesTraceNodeTypeEnum 对齐（9 类）。 */
const NODE_TYPE_LABELS: Record<string, string> = {
  BATCH: '批次',
  CUSTOMER: '客户',
  IN_ORDER: '入库单',
  OP_TASK: '工序任务',
  OUT_ORDER: '出库单',
  PROD_ORDER: '生产工单',
  QC_TASK: '质检任务',
  SUPPLIER: '供应商',
  WIP_TXN: '在制流水',
};

/** 起点类型下拉：值走后端枚举，标签用中文，不把 LOT 这类原始值暴露给用户。 */
const START_TYPE_OPTIONS = [
  { label: '批次号', value: 'LOT' },
  { label: '生产工单号', value: 'PROD_ORDER' },
  { label: '销售出库单号', value: 'SALES_OUT' },
];

/** 追溯方向下拉：正向到客户，反向到供应商。 */
const DIRECTION_OPTIONS = [
  { label: '正向（到客户）', value: 'FORWARD' },
  { label: '反向（到供应商）', value: 'BACKWARD' },
];

/**
 * 安全读文本。
 *
 * 快照是后端 Map 原样透出，值是 unknown（可能是字符串、数字、数组、null），
 * 直接 `.xxx` 会因类型不安全被打回，这里统一收敛成可展示字符串。
 * 数组拼成逗号串（工单的 workOrderNos、虚拟根的 lotNos 都是数组）。
 */
export function pickText(
  snapshot: MesTraceApi.TraceSnapshot | undefined,
  key: string,
): string {
  const value = snapshot?.[key];
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  if (Array.isArray(value)) {
    return value.length > 0 ? value.map((item) => String(item)).join('、') : '-';
  }
  if (typeof value === 'object') {
    return '-';
  }
  return String(value);
}

/** 安全读数值：数字原样，数字字符串转数字，取不到返回 undefined。 */
export function pickNumber(
  snapshot: MesTraceApi.TraceSnapshot | undefined,
  key: string,
): number | undefined {
  const value = snapshot?.[key];
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  const num = Number(value);
  return Number.isFinite(num) ? num : undefined;
}

/**
 * 安全读时间：后端 LocalDateTime 序列化成毫秒时间戳，统一格式化成可读字符串。
 *
 * 特意不用 vxe 的 `formatter: 'formatDateTime'`——那个 formatter 作用在行字段上，
 * 而这里的时间埋在 snapshot 里，只能自己格式化。
 */
export function pickTime(
  snapshot: MesTraceApi.TraceSnapshot | undefined,
  key: string,
): string {
  const value = snapshot?.[key];
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  const millis = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(millis)) {
    // LocalDate 是 'yyyy-MM-dd' 字符串，原样返回
    return String(value);
  }
  const date = new Date(millis);
  const pad = (n: number) => String(n).padStart(2, '0');
  const ymd = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  const hms = `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  return `${ymd} ${hms}`;
}

/** 节点类型转中文，未登记的原始值兜底显示自身，避免空白格。 */
export function nodeTypeLabel(nodeType?: string): string {
  if (!nodeType) {
    return '-';
  }
  return NODE_TYPE_LABELS[nodeType] ?? nodeType;
}

/**
 * 关键快照列：按节点类型挑该类型最有判断价值的字段，拼成一行可读文本。
 *
 * 树是异构的，九类节点字段各不相同，用一列承载"该节点看什么"比开九列更实际
 * （任务书要求工单看 BOM/工艺版本、批次看供应商与供应商批次、质检看结论、时间要可读）。
 */
export function snapshotSummary(row: MesTraceApi.TraceRow): string {
  const snap = row.snapshot;
  switch (row.nodeType) {
    case 'BATCH': {
      const parts = [
        snap?.matCode ? `物料 ${pickText(snap, 'matCode')}` : '',
        snap?.supplierName
          ? `供应商 ${pickText(snap, 'supplierName')}`
          : snap?.supplierId
            ? `供应商 ${pickText(snap, 'supplierId')}`
            : '',
        snap?.supplierLotNo ? `供应商批次 ${pickText(snap, 'supplierLotNo')}` : '',
        snap?.furnaceLotNo ? `炉批号 ${pickText(snap, 'furnaceLotNo')}` : '',
        snap?.produceDate ? `生产日期 ${pickText(snap, 'produceDate')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'PROD_ORDER': {
      const parts = [
        snap?.matCode ? `物料 ${pickText(snap, 'matCode')}` : '',
        snap?.bomVersion ? `BOM ${pickText(snap, 'bomVersion')}` : '',
        snap?.routeVersion ? `工艺 ${pickText(snap, 'routeVersion')}` : '',
        pickNumber(snap, 'qty') === undefined
          ? ''
          : `计划 ${pickText(snap, 'qty')}`,
        snap?.workOrderNos ? `加工单 ${pickText(snap, 'workOrderNos')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'OP_TASK': {
      const parts = [
        snap?.opCode ? `工序 ${pickText(snap, 'opCode')}` : '',
        snap?.plannedQty ? `计划 ${pickText(snap, 'plannedQty')}` : '',
        snap?.completedQty ? `完工 ${pickText(snap, 'completedQty')}` : '',
        snap?.actualStartTime
          ? `开工 ${pickTime(snap, 'actualStartTime')}`
          : '',
        snap?.actualEndTime ? `完工 ${pickTime(snap, 'actualEndTime')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'QC_TASK': {
      const parts = [
        snap?.conclusion ? `结论 ${pickText(snap, 'conclusion')}` : '结论 -',
        snap?.qcPlanVersion ? `检验方案 ${pickText(snap, 'qcPlanVersion')}` : '',
        snap?.qtyQualified ? `合格 ${pickText(snap, 'qtyQualified')}` : '',
        snap?.qtyRejected ? `不合格 ${pickText(snap, 'qtyRejected')}` : '',
        snap?.inspectEndTime
          ? `检验完成 ${pickTime(snap, 'inspectEndTime')}`
          : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'IN_ORDER': {
      const parts = [
        pickNumber(snap, 'qty') === undefined
          ? ''
          : `数量 ${pickText(snap, 'qty')}`,
        snap?.sourceBizNo ? `来源 ${pickText(snap, 'sourceBizNo')}` : '',
        snap?.txnTime ? `入库时间 ${pickTime(snap, 'txnTime')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'OUT_ORDER': {
      const parts = [
        pickNumber(snap, 'qty') === undefined
          ? ''
          : `数量 ${pickText(snap, 'qty')}`,
        snap?.materialReqNo ? `领料单 ${pickText(snap, 'materialReqNo')}` : '',
        snap?.txnTime ? `出库时间 ${pickTime(snap, 'txnTime')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'WIP_TXN': {
      const parts = [
        pickNumber(snap, 'qty') === undefined
          ? ''
          : `数量 ${pickText(snap, 'qty')}`,
        snap?.fromStatus ? `由 ${pickText(snap, 'fromStatus')}` : '',
        snap?.toStatus ? `至 ${pickText(snap, 'toStatus')}` : '',
        snap?.txnTime ? `过账 ${pickTime(snap, 'txnTime')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'SUPPLIER': {
      const parts = [
        snap?.supplierLotNo ? `供应商批次 ${pickText(snap, 'supplierLotNo')}` : '',
        snap?.furnaceLotNo ? `炉批号 ${pickText(snap, 'furnaceLotNo')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    case 'CUSTOMER': {
      const parts = [
        snap?.customerCode ? `编码 ${pickText(snap, 'customerCode')}` : '',
        snap?.qty ? `数量 ${pickText(snap, 'qty')}` : '',
      ];
      return parts.filter(Boolean).join('；') || '-';
    }
    default: {
      // 虚拟根等未登记类型：把 lotNos 这种明显有意义的字段透出来
      const parts = [snap?.lotNos ? `批次 ${pickText(snap, 'lotNos')}` : ''];
      return parts.filter(Boolean).join('；') || '-';
    }
  }
}

/** 查询表单：起点类型 + 起点值 + 方向，三项全宽容器用默认响应式栅格。 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'startType',
      label: '起点类型',
      component: 'Select',
      componentProps: {
        options: START_TYPE_OPTIONS,
        placeholder: '请选择起点类型',
      },
      defaultValue: 'LOT',
      rules: 'selectRequired',
    },
    {
      fieldName: 'startValue',
      label: '起点值',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号 / 生产工单号 / 销售出库单号',
      },
      rules: 'required',
    },
    {
      fieldName: 'direction',
      label: '追溯方向',
      component: 'Select',
      componentProps: {
        options: DIRECTION_OPTIONS,
        placeholder: '请选择追溯方向',
      },
      defaultValue: 'FORWARD',
      rules: 'selectRequired',
    },
  ];
}

/** 结果区列定义：树形表格用 `treeNode: true` 指定展开列。 */
export function useGridColumns(): VxeTableGridOptions<MesTraceApi.TraceRow>['columns'] {
  return [
    {
      field: 'bizNo',
      title: '业务单号',
      minWidth: 180,
      treeNode: true,
      ...codeLinkColumn(),
    },
    {
      field: 'nodeType',
      title: '节点类型',
      minWidth: 110,
      formatter: ({ row }) => nodeTypeLabel(row.nodeType),
    },
    { field: 'nodeName', title: '名称', minWidth: 160 },
    { field: 'depth', title: '层级', width: 80 },
    { field: 'parentBizNo', title: '上级单号', minWidth: 170 },
    {
      field: 'snapshot',
      title: '关键快照',
      minWidth: 340,
      formatter: ({ row }) => snapshotSummary(row),
    },
  ];
}
