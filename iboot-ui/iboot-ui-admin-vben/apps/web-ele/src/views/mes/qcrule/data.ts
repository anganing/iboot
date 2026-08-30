import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcRuleApi } from '#/api/mes/qcrule';

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
      fieldName: 'code',
      label: '质检规则编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检规则编码',
      },
    },
    {
      fieldName: 'name',
      label: '质检规则名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检规则名称',
      },
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点：IQC、IPQC、FQC、OQC、FAI',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入触发点：IQC、IPQC、FQC、OQC、FAI',
      },
    },
    {
      fieldName: 'objectType',
      label: '检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER',
      },
    },
    {
      fieldName: 'matId',
      label: '指定物料编号，0表示不限',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指定物料编号，0表示不限',
      },
    },
    {
      fieldName: 'matCatId',
      label: '指定物料分类编号，0表示不限',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指定物料分类编号，0表示不限',
      },
    },
    {
      fieldName: 'opId',
      label: '指定工序编号，0表示不限',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指定工序编号，0表示不限',
      },
    },
    {
      fieldName: 'wcId',
      label: '指定工作中心编号，0表示不限',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指定工作中心编号，0表示不限',
      },
    },
    {
      fieldName: 'supplierId',
      label: '指定供应商编号，0表示不限',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指定供应商编号，0表示不限',
      },
    },
    {
      fieldName: 'qcPlanId',
      label: '检验方案编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验方案编号',
      },
    },
    {
      fieldName: 'qcMode',
      label: '检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级，数值越大越优先',
      },
    },
    {
      fieldName: 'status',
      label: '状态：0-启用，1-停用',
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
      label: 'MES-质检触发规则数据',
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
      fieldName: 'code',
      label: '质检规则编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检规则编码',
      },
    },
    {
      fieldName: 'name',
      label: '质检规则名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检规则名称',
      },
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点：IQC、IPQC、FQC、OQC、FAI',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入触发点：IQC、IPQC、FQC、OQC、FAI',
      },
    },
    {
      fieldName: 'objectType',
      label: '检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER',
      },
    },
    {
      fieldName: 'matId',
      label: '指定物料编号，0表示不限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指定物料编号，0表示不限',
      },
    },
    {
      fieldName: 'matCatId',
      label: '指定物料分类编号，0表示不限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指定物料分类编号，0表示不限',
      },
    },
    {
      fieldName: 'opId',
      label: '指定工序编号，0表示不限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指定工序编号，0表示不限',
      },
    },
    {
      fieldName: 'wcId',
      label: '指定工作中心编号，0表示不限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指定工作中心编号，0表示不限',
      },
    },
    {
      fieldName: 'supplierId',
      label: '指定供应商编号，0表示不限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指定供应商编号，0表示不限',
      },
    },
    {
      fieldName: 'qcPlanId',
      label: '检验方案编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验方案编号',
      },
    },
    {
      fieldName: 'qcMode',
      label: '检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级，数值越大越优先',
      },
    },
    {
      fieldName: 'status',
      label: '状态：0-启用，1-停用',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：0-启用，1-停用',
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
export function useGridColumns(): VxeTableGridOptions<MesQcRuleApi.QcRule>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '质检规则编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '质检规则名称',
      minWidth: 120,
    },
    {
      field: 'triggerPoint',
      title: '触发点：IQC、IPQC、FQC、OQC、FAI',
      minWidth: 120,
    },
    {
      field: 'objectType',
      title: '检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '指定物料编号，0表示不限',
      minWidth: 120,
    },
    {
      field: 'matCatId',
      title: '指定物料分类编号，0表示不限',
      minWidth: 120,
    },
    {
      field: 'opId',
      title: '指定工序编号，0表示不限',
      minWidth: 120,
    },
    {
      field: 'wcId',
      title: '指定工作中心编号，0表示不限',
      minWidth: 120,
    },
    {
      field: 'supplierId',
      title: '指定供应商编号，0表示不限',
      minWidth: 120,
    },
    {
      field: 'qcMode',
      title: '检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      minWidth: 120,
    },
    {
      field: 'priority',
      title: '优先级，数值越大越优先',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：0-启用，1-停用',
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
      field: 'qcPlanCode',
      title: '检验方案编码',
      minWidth: 120,
    },
    {
      field: 'qcPlanName',
      title: '检验方案名称',
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