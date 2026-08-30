import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvBalanceApi } from '#/api/wms/invbalance';

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
      fieldName: 'invOrgId',
      label: '库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '库位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库位编号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编号',
      },
    },
    {
      fieldName: 'batchId',
      label: '批次编号；无批次物料挂默认批次',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次编号；无批次物料挂默认批次',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示散件',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器编号，0表示散件',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目归属',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目编号，0表示无项目归属',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务归属',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务编号，0表示无项目任务归属',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围：COMMON、PROJECT_DEDICATED',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存范围：COMMON、PROJECT_DEDICATED',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'qtyOnhand',
      label: '现存数量，基准单位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入现存数量，基准单位',
      },
    },
    {
      fieldName: 'qtyReserved',
      label: '预留数量，基准单位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入预留数量，基准单位',
      },
    },
    {
      fieldName: 'qtyFrozen',
      label: '冻结数量，基准单位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结数量，基准单位',
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
      fieldName: 'uomCode',
      label: '基准单位编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入基准单位编码快照',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次号快照',
      },
    },
    {
      fieldName: 'whCode',
      label: '仓库编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编码快照',
      },
    },
    {
      fieldName: 'locCode',
      label: '库位编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库位编码快照',
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入乐观锁版本',
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
      label: 'WMS-多维库存余额数据',
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
      fieldName: 'invOrgId',
      label: '库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '库位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位编号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编号',
      },
    },
    {
      fieldName: 'batchId',
      label: '批次编号；无批次物料挂默认批次',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次编号；无批次物料挂默认批次',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示散件',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器编号，0表示散件',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目归属',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目编号，0表示无项目归属',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务归属',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务编号，0表示无项目任务归属',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围：COMMON、PROJECT_DEDICATED',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存范围：COMMON、PROJECT_DEDICATED',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN',
      },
    },
    {
      fieldName: 'qtyOnhand',
      label: '现存数量，基准单位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入现存数量，基准单位',
      },
    },
    {
      fieldName: 'qtyReserved',
      label: '预留数量，基准单位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入预留数量，基准单位',
      },
    },
    {
      fieldName: 'qtyFrozen',
      label: '冻结数量，基准单位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结数量，基准单位',
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
      fieldName: 'uomCode',
      label: '基准单位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入基准单位编码快照',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号快照',
      },
    },
    {
      fieldName: 'whCode',
      label: '仓库编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编码快照',
      },
    },
    {
      fieldName: 'locCode',
      label: '库位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位编码快照',
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入乐观锁版本',
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
export function useGridColumns(): VxeTableGridOptions<WmsInvBalanceApi.InvBalance>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'locId',
      title: '库位编号',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '物料编号',
      minWidth: 120,
    },
    {
      field: 'batchId',
      title: '批次编号；无批次物料挂默认批次',
      minWidth: 120,
    },
    {
      field: 'containerId',
      title: '容器编号，0表示散件',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目编号，0表示无项目归属',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务编号，0表示无项目任务归属',
      minWidth: 120,
    },
    {
      field: 'stockScope',
      title: '库存范围：COMMON、PROJECT_DEDICATED',
      minWidth: 120,
    },
    {
      field: 'qualityStatus',
      title: '质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN',
      minWidth: 120,
    },
    {
      field: 'qtyOnhand',
      title: '现存数量，基准单位',
      minWidth: 120,
    },
    {
      field: 'qtyReserved',
      title: '预留数量，基准单位',
      minWidth: 120,
    },
    {
      field: 'qtyFrozen',
      title: '冻结数量，基准单位',
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
      field: 'uomCode',
      title: '基准单位编码快照',
      minWidth: 120,
    },
    {
      field: 'lotNo',
      title: '批次号快照',
      minWidth: 120,
    },
    {
      field: 'whCode',
      title: '仓库编码快照',
      minWidth: 120,
    },
    {
      field: 'locCode',
      title: '库位编码快照',
      minWidth: 120,
    },
    {
      field: 'version',
      title: '乐观锁版本',
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
      field: 'whName',
      title: '仓库名称',
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