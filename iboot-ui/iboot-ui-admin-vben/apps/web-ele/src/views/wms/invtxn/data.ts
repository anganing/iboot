import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvTxnApi } from '#/api/wms/invtxn';

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
      fieldName: 'txnNo',
      label: '库存事务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存事务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'bizLineNo',
      label: '来源业务行号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务行号',
      },
    },
    {
      fieldName: 'ioType',
      label: '收发方向：IN、OUT、MOVE、STATUS',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择收发方向：IN、OUT、MOVE、STATUS',
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
      fieldName: 'fromLocId',
      label: '来源库位编号，0表示无来源库位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源库位编号，0表示无来源库位',
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位编号，0表示无目标库位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标库位编号，0表示无目标库位',
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
      label: '批次编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次编号',
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
      fieldName: 'uomId',
      label: '基准计量单位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入基准计量单位编号',
      },
    },
    {
      fieldName: 'qty',
      label: '事务数量，使用正数',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入事务数量，使用正数',
      },
    },
    {
      fieldName: 'beforeQty',
      label: '过账前现存数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入过账前现存数量',
      },
    },
    {
      fieldName: 'afterQty',
      label: '过账后现存数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入过账后现存数量',
      },
    },
    {
      fieldName: 'qualityStatusFrom',
      label: '变更前质量状态',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更前质量状态',
      },
    },
    {
      fieldName: 'qualityStatusTo',
      label: '变更后质量状态',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更后质量状态',
      },
    },
    {
      fieldName: 'projectIdFrom',
      label: '变更前项目编号，0表示无项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更前项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskIdFrom',
      label: '变更前项目任务编号，0表示无项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更前项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'projectIdTo',
      label: '变更后项目编号，0表示无项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更后项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskIdTo',
      label: '变更后项目任务编号，0表示无项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入变更后项目任务编号，0表示无项目任务',
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
      fieldName: 'uomCode',
      label: '单位编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入单位编码快照',
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
      fieldName: 'fromLocCode',
      label: '来源库位编码快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源库位编码快照',
      },
    },
    {
      fieldName: 'toLocCode',
      label: '目标库位编码快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标库位编码快照',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '过账幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入过账幂等键',
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
      label: 'WMS-库存流水数据',
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
      fieldName: 'txnNo',
      label: '库存事务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存事务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'bizLineNo',
      label: '来源业务行号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务行号',
      },
    },
    {
      fieldName: 'ioType',
      label: '收发方向：IN、OUT、MOVE、STATUS',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择收发方向：IN、OUT、MOVE、STATUS',
      },
    },
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
      fieldName: 'fromLocId',
      label: '来源库位编号，0表示无来源库位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源库位编号，0表示无来源库位',
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位编号，0表示无目标库位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标库位编号，0表示无目标库位',
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
      label: '批次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次编号',
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
      fieldName: 'uomId',
      label: '基准计量单位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入基准计量单位编号',
      },
    },
    {
      fieldName: 'qty',
      label: '事务数量，使用正数',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入事务数量，使用正数',
      },
    },
    {
      fieldName: 'beforeQty',
      label: '过账前现存数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入过账前现存数量',
      },
    },
    {
      fieldName: 'afterQty',
      label: '过账后现存数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入过账后现存数量',
      },
    },
    {
      fieldName: 'qualityStatusFrom',
      label: '变更前质量状态',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更前质量状态',
      },
    },
    {
      fieldName: 'qualityStatusTo',
      label: '变更后质量状态',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更后质量状态',
      },
    },
    {
      fieldName: 'projectIdFrom',
      label: '变更前项目编号，0表示无项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更前项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskIdFrom',
      label: '变更前项目任务编号，0表示无项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更前项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'projectIdTo',
      label: '变更后项目编号，0表示无项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更后项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskIdTo',
      label: '变更后项目任务编号，0表示无项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入变更后项目任务编号，0表示无项目任务',
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
      fieldName: 'uomCode',
      label: '单位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入单位编码快照',
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
      fieldName: 'fromLocCode',
      label: '来源库位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源库位编码快照',
      },
    },
    {
      fieldName: 'toLocCode',
      label: '目标库位编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标库位编码快照',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '过账幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入过账幂等键',
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
export function useGridColumns(): VxeTableGridOptions<WmsInvTxnApi.InvTxn>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'txnNo',
      title: '库存事务编号',
      minWidth: 120,
    },
    {
      field: 'txnType',
      title: '事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '来源业务类型',
      minWidth: 120,
    },
    {
      field: 'bizNo',
      title: '来源业务单号',
      minWidth: 120,
    },
    {
      field: 'bizLineNo',
      title: '来源业务行号',
      minWidth: 120,
    },
    {
      field: 'ioType',
      title: '收发方向：IN、OUT、MOVE、STATUS',
      minWidth: 120,
    },
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
      field: 'fromLocId',
      title: '来源库位编号，0表示无来源库位',
      minWidth: 120,
    },
    {
      field: 'toLocId',
      title: '目标库位编号，0表示无目标库位',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '物料编号',
      minWidth: 120,
    },
    {
      field: 'batchId',
      title: '批次编号',
      minWidth: 120,
    },
    {
      field: 'containerId',
      title: '容器编号，0表示散件',
      minWidth: 120,
    },
    {
      field: 'uomId',
      title: '基准计量单位编号',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '事务数量，使用正数',
      minWidth: 120,
    },
    {
      field: 'beforeQty',
      title: '过账前现存数量',
      minWidth: 120,
    },
    {
      field: 'afterQty',
      title: '过账后现存数量',
      minWidth: 120,
    },
    {
      field: 'qualityStatusFrom',
      title: '变更前质量状态',
      minWidth: 120,
    },
    {
      field: 'qualityStatusTo',
      title: '变更后质量状态',
      minWidth: 120,
    },
    {
      field: 'projectIdFrom',
      title: '变更前项目编号，0表示无项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskIdFrom',
      title: '变更前项目任务编号，0表示无项目任务',
      minWidth: 120,
    },
    {
      field: 'projectIdTo',
      title: '变更后项目编号，0表示无项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskIdTo',
      title: '变更后项目任务编号，0表示无项目任务',
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
      field: 'uomCode',
      title: '单位编码快照',
      minWidth: 120,
    },
    {
      field: 'lotNo',
      title: '批次号快照',
      minWidth: 120,
    },
    {
      field: 'fromLocCode',
      title: '来源库位编码快照',
      minWidth: 120,
    },
    {
      field: 'toLocCode',
      title: '目标库位编码快照',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '过账幂等键',
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
      field: 'whCode',
      title: '仓库编码',
      minWidth: 120,
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