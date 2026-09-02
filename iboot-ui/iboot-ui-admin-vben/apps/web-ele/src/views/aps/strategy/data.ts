import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsStrategyApi } from '#/api/aps/strategy';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

import ConstraintWeightEditor from './modules/constraint-weight-editor.vue';

/**
 * 新增/修改的表单。
 *
 * <p>约束权重不是一个 JSON 文本域，而是由后端约束元数据驱动的结构化编辑器：
 * 手写 JSON 既不知道有哪些约束可调，也要等到点"生成排程"才知道配错了。
 */
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
      label: '策略编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则分配',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '策略版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '例如 V1.0',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.APS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
    },
    {
      fieldName: 'objective',
      label: '优化目标',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        maxLength: 255,
        placeholder: '例如：最小化拖期，其次最小化换型时间',
        rows: 2,
      },
      // 写清它不参与求解：见过有人以为改了这段文字排程结果就会变
      help: '给人看的排程意图说明，不参与求解；真正影响排程结果的是下面的约束权重',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'config.spentLimitSeconds',
      label: '求解耗时上限',
      rules: 'selectRequired',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        max: 3600,
        min: 60,
        precision: 0,
        placeholder: '60 ~ 3600 秒',
      },
      help: '单次求解最多跑多久，60 ~ 3600 秒。上限卡在 1 小时是因为场景求解锁只保 2 小时，再长会让同场景并发求解',
    },
    {
      fieldName: 'config.unimprovedSpentLimitSeconds',
      label: '无改进终止',
      rules: 'selectRequired',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        max: 3600,
        min: 10,
        precision: 0,
        placeholder: '10 ~ 3600 秒',
      },
      help: '这么久没找到更优解就收工，避免白烧 CPU。要小于求解耗时上限，否则永远不会触发',
    },
    {
      fieldName: 'config.materialConstrained',
      label: '约束物料齐套',
      rules: 'selectRequired',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      help: '开启后缺料工序给有料工序让路。齐套结论在刷新工单投影时就已冻结，求解期间不再回查库存；BOM 与库存还没对齐时建议关闭',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'config.weights',
      label: '约束权重',
      component: markRaw(ConstraintWeightEditor),
      // 齐套开关关掉时禁掉齐套那一行：配了权重也不生效，不如直接说明白
      dependencies: {
        triggerFields: ['config.materialConstrained'],
        // 表单值是嵌套对象，取值要走 values.config 而不是 values['config.xxx']
        componentProps: (values) => ({
          materialConstrained: values.config?.materialConstrained === true,
        }),
      },
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入备注',
        rows: 2,
      },
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '策略编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '策略版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<ApsStrategyApi.Strategy>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '策略编码', minWidth: 150 },
    { field: 'name', title: '策略名称', minWidth: 160 },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    { field: 'objective', title: '优化目标', minWidth: 220 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_STRATEGY_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
