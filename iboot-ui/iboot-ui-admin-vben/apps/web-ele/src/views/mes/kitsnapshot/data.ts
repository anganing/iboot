import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesKitSnapshotApi } from '#/api/mes/kitsnapshot';

import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'checkNo',
      label: '齐套检查编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入齐套检查编号',
      },
    },
    {
      fieldName: 'checkScope',
      label: '检查范围：PROD_ORDER、WORK_ORDER、OP_TASK',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查范围：PROD_ORDER、WORK_ORDER、OP_TASK',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单编号',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示整单检查',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号，0表示整单检查',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序检查',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号，0表示非工序检查',
      },
    },
    {
      fieldName: 'lineNo',
      label: '检查明细行号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查明细行号',
      },
    },
    {
      fieldName: 'matId',
      label: '需求物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入需求物料编号',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编码快照',
      },
    },
    {
      fieldName: 'matName',
      label: '物料名称快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料名称快照',
      },
    },
    {
      fieldName: 'reqQty',
      label: '需求数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入需求数量',
      },
    },
    {
      fieldName: 'availQty',
      label: '检查时可用数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查时可用数量',
      },
    },
    {
      fieldName: 'reservedQty',
      label: '检查时本需求已预留数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查时本需求已预留数量',
      },
    },
    {
      fieldName: 'issuedQty',
      label: '检查时已领用数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查时已领用数量',
      },
    },
    {
      fieldName: 'shortQty',
      label: '短缺数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入短缺数量',
      },
    },
    {
      fieldName: 'altMatId',
      label: '实际采用替代料编号，0表示未采用',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实际采用替代料编号，0表示未采用',
      },
    },
    {
      fieldName: 'result',
      label: '检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE',
      },
    },
    {
      fieldName: 'checkTime',
      label: '检查时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'MES-齐套检查快照数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'checkNo',
      label: '齐套检查编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入齐套检查编号',
      },
    },
    {
      fieldName: 'checkScope',
      label: '检查范围：PROD_ORDER、WORK_ORDER、OP_TASK',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查范围：PROD_ORDER、WORK_ORDER、OP_TASK',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单编号',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示整单检查',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单编号，0表示整单检查',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序检查',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号，0表示非工序检查',
      },
    },
    {
      fieldName: 'lineNo',
      label: '检查明细行号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查明细行号',
      },
    },
    {
      fieldName: 'matId',
      label: '需求物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入需求物料编号',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编码快照',
      },
    },
    {
      fieldName: 'matName',
      label: '物料名称快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料名称快照',
      },
    },
    {
      fieldName: 'reqQty',
      label: '需求数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入需求数量',
      },
    },
    {
      fieldName: 'availQty',
      label: '检查时可用数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查时可用数量',
      },
    },
    {
      fieldName: 'reservedQty',
      label: '检查时本需求已预留数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查时本需求已预留数量',
      },
    },
    {
      fieldName: 'issuedQty',
      label: '检查时已领用数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查时已领用数量',
      },
    },
    {
      fieldName: 'shortQty',
      label: '短缺数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入短缺数量',
      },
    },
    {
      fieldName: 'altMatId',
      label: '实际采用替代料编号，0表示未采用',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实际采用替代料编号，0表示未采用',
      },
    },
    {
      fieldName: 'result',
      label: '检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE',
      },
    },
    {
      fieldName: 'checkTime',
      label: '检查时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注',
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesKitSnapshotApi.KitSnapshot>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'checkNo',
      title: '齐套检查编号',
      minWidth: 120,
    },
    {
      field: 'checkScope',
      title: '检查范围：PROD_ORDER、WORK_ORDER、OP_TASK',
      minWidth: 120,
    },
    {
      field: 'lineNo',
      title: '检查明细行号',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '需求物料编号',
      minWidth: 120,
    },
    {
      field: 'matCode',
      title: '物料编码快照',
      minWidth: 120,
    },
    {
      field: 'matName',
      title: '物料名称快照',
      minWidth: 120,
    },
    {
      field: 'reqQty',
      title: '需求数量',
      minWidth: 120,
    },
    {
      field: 'availQty',
      title: '检查时可用数量',
      minWidth: 120,
    },
    {
      field: 'reservedQty',
      title: '检查时本需求已预留数量',
      minWidth: 120,
    },
    {
      field: 'issuedQty',
      title: '检查时已领用数量',
      minWidth: 120,
    },
    {
      field: 'shortQty',
      title: '短缺数量',
      minWidth: 120,
    },
    {
      field: 'altMatId',
      title: '实际采用替代料编号，0表示未采用',
      minWidth: 120,
    },
    {
      field: 'result',
      title: '检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE',
      minWidth: 120,
    },
    {
      field: 'checkTime',
      title: '检查时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'prodOrderOrder_no',
      title: '生产工单order_no',
      minWidth: 120,
    },
    {
      field: 'workOrderWork_order_no',
      title: '作业工单编号，0表示整单检查work_order_no',
      minWidth: 120,
    },
    {
      field: 'opTaskTask_no',
      title: '工序任务编号，0表示非工序检查task_no',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}