import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsShortageApi } from '#/api/aps/shortage';

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
      fieldName: 'versionId',
      label: '排程版本编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号，0表示汇总缺料',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单编号，0表示汇总缺料',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序级缺料',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号，0表示非工序级缺料',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'matId',
      label: '短缺物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入短缺物料编号',
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
      label: '可用数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入可用数量',
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
      fieldName: 'uomId',
      label: '计量单位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计量单位编号',
      },
    },
    {
      fieldName: 'uomCode',
      label: '计量单位编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计量单位编码快照',
      },
    },
    {
      fieldName: 'needDate',
      label: '需求到位时间',
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
      fieldName: 'expectedSupplyDate',
      label: '预计供给时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'status',
      label: '状态：OPEN、PARTIAL、RESOLVED、IGNORED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
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
      label: 'APS-排程缺料清单数据',
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
      fieldName: 'versionId',
      label: '排程版本编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号，0表示汇总缺料',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单编号，0表示汇总缺料',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序级缺料',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号，0表示非工序级缺料',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'matId',
      label: '短缺物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入短缺物料编号',
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
      label: '可用数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入可用数量',
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
      fieldName: 'uomId',
      label: '计量单位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计量单位编号',
      },
    },
    {
      fieldName: 'uomCode',
      label: '计量单位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计量单位编码快照',
      },
    },
    {
      fieldName: 'needDate',
      label: '需求到位时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'expectedSupplyDate',
      label: '预计供给时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：OPEN、PARTIAL、RESOLVED、IGNORED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：OPEN、PARTIAL、RESOLVED、IGNORED',
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
export function useGridColumns(): VxeTableGridOptions<ApsShortageApi.Shortage>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'prodOrderId',
      title: '生产工单编号，0表示汇总缺料',
      minWidth: 120,
    },
    {
      field: 'opTaskId',
      title: '工序任务编号，0表示非工序级缺料',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目编号，0表示无项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务编号，0表示无项目任务',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '短缺物料编号',
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
      title: '可用数量',
      minWidth: 120,
    },
    {
      field: 'shortQty',
      title: '短缺数量',
      minWidth: 120,
    },
    {
      field: 'uomId',
      title: '计量单位编号',
      minWidth: 120,
    },
    {
      field: 'uomCode',
      title: '计量单位编码快照',
      minWidth: 120,
    },
    {
      field: 'needDate',
      title: '需求到位时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'expectedSupplyDate',
      title: '预计供给时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：OPEN、PARTIAL、RESOLVED、IGNORED',
      minWidth: 120,
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
      field: 'versionVersion_no',
      title: '排程版本version_no',
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