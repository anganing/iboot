import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionMatApi } from '#/api/aps/projectionmat';

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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceMatId',
      label: 'MDM物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入MDM物料编号',
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
      fieldName: 'matSpec',
      label: '物料规格快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料规格快照',
      },
    },
    {
      fieldName: 'matType',
      label: '物料类型快照',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择物料类型快照',
      },
    },
    {
      fieldName: 'baseUomId',
      label: '基准计量单位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入基准计量单位编号',
      },
    },
    {
      fieldName: 'baseUomCode',
      label: '基准计量单位编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入基准计量单位编码快照',
      },
    },
    {
      fieldName: 'batchFlag',
      label: '是否批次管控',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'bomId',
      label: '当前有效BOM编号，0表示无',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前有效BOM编号，0表示无',
      },
    },
    {
      fieldName: 'bomVersion',
      label: '当前有效BOM版本',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前有效BOM版本',
      },
    },
    {
      fieldName: 'routeId',
      label: '当前有效工艺路线编号，0表示无',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前有效工艺路线编号，0表示无',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '当前有效工艺路线版本',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前有效工艺路线版本',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
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
      fieldName: 'snapshotTime',
      label: '投影快照时间',
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
      label: 'APS-MDM物料投影数据',
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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceMatId',
      label: 'MDM物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入MDM物料编号',
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
      fieldName: 'matSpec',
      label: '物料规格快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料规格快照',
      },
    },
    {
      fieldName: 'matType',
      label: '物料类型快照',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择物料类型快照',
      },
    },
    {
      fieldName: 'baseUomId',
      label: '基准计量单位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入基准计量单位编号',
      },
    },
    {
      fieldName: 'baseUomCode',
      label: '基准计量单位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入基准计量单位编码快照',
      },
    },
    {
      fieldName: 'batchFlag',
      label: '是否批次管控',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否批次管控',
      },
    },
    {
      fieldName: 'bomId',
      label: '当前有效BOM编号，0表示无',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前有效BOM编号，0表示无',
      },
    },
    {
      fieldName: 'bomVersion',
      label: '当前有效BOM版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前有效BOM版本',
      },
    },
    {
      fieldName: 'routeId',
      label: '当前有效工艺路线编号，0表示无',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前有效工艺路线编号，0表示无',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '当前有效工艺路线版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前有效工艺路线版本',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
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
export function useGridColumns(): VxeTableGridOptions<ApsProjectionMatApi.ProjectionMat>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'sourceMatId',
      title: 'MDM物料编号',
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
      field: 'matSpec',
      title: '物料规格快照',
      minWidth: 120,
    },
    {
      field: 'matType',
      title: '物料类型快照',
      minWidth: 120,
    },
    {
      field: 'baseUomId',
      title: '基准计量单位编号',
      minWidth: 120,
    },
    {
      field: 'baseUomCode',
      title: '基准计量单位编码快照',
      minWidth: 120,
    },
    {
      field: 'batchFlag',
      title: '是否批次管控',
      minWidth: 120,
    },
    {
      field: 'bomId',
      title: '当前有效BOM编号，0表示无',
      minWidth: 120,
    },
    {
      field: 'bomVersion',
      title: '当前有效BOM版本',
      minWidth: 120,
    },
    {
      field: 'routeId',
      title: '当前有效工艺路线编号，0表示无',
      minWidth: 120,
    },
    {
      field: 'routeVersion',
      title: '当前有效工艺路线版本',
      minWidth: 120,
    },
    {
      field: 'sourceUpdateTime',
      title: '来源数据更新时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '投影快照时间',
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
      field: 'scenarioCode',
      title: '排程场景编码',
      minWidth: 120,
    },
    {
      field: 'scenarioName',
      title: '排程场景名称',
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