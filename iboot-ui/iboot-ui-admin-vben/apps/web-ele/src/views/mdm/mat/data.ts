import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmMatApi } from '#/api/mdm/mat';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { getMatCatList } from '#/api/mdm/matcat';
import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { MDM_MAT_TYPE_OPTIONS } from '../constants';
import { MAT_CAT_RELATION_PROPS, UOM_RELATION_PROPS } from '../relation-config';

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
    // ===== 基本标识 =====
    {
      fieldName: 'catId',
      label: '物料分类',
      rules: 'required',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => handleTree(await getMatCatList({})),
        childrenField: 'children',
        filterable: true,
        labelField: 'name',
        placeholder: '请选择物料分类',
        treeDefaultExpandAll: true,
        valueField: 'id',
      },
    },
    {
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'matType',
      label: '物料类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: MDM_MAT_TYPE_OPTIONS,
        placeholder: '请选择物料类型',
      },
    },
    {
      fieldName: 'spec',
      label: '规格型号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入规格型号',
      },
    },
    {
      fieldName: 'drawingNo',
      label: '图号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入图号',
      },
    },
    // ===== 计量与批次管控 =====
    {
      fieldName: 'baseUomId',
      label: '基准计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'batchFlag',
      label: '是否批次管控',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: false,
    },
    {
      fieldName: 'snFlag',
      label: '是否SN管控',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: false,
    },
    {
      fieldName: 'shelfLifeDays',
      label: '保质期（天）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入保质期（天）',
        precision: 0,
      },
    },
    // ===== 重量与尺寸 =====
    {
      fieldName: 'netWeight',
      label: '净重',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'grossWeight',
      label: '毛重',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'weightUomId',
      label: '重量单位',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'length',
      label: '长（毫米）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'width',
      label: '宽（毫米）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'height',
      label: '高（毫米）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'volume',
      label: '体积（立方毫米）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    // ===== 计划与采购 =====
    {
      fieldName: 'planStrategy',
      label: '计划策略',
      component: 'Select',
      componentProps: {
        options: getDictOptions('mdm_plan_strategy', 'string'),
        clearable: true,
        placeholder: '请选择计划策略',
      },
    },
    {
      fieldName: 'leadTimeDays',
      label: '提前期（天）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
      },
    },
    {
      fieldName: 'safetyStock',
      label: '安全库存',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
        placeholder: 'MTS 策略必填',
      },
      dependencies: {
        triggerFields: ['planStrategy'],
        rules: (values) => {
          return values.planStrategy === 'MTS' ? 'required' : null;
        },
      },
    },
    {
      fieldName: 'minOrderQty',
      label: '最小订货量',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'orderMultiple',
      label: '订货倍数',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'packQty',
      label: '标准包装量',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
      },
    },
    {
      fieldName: 'abcClass',
      label: 'ABC分类',
      component: 'Select',
      componentProps: {
        options: getDictOptions('mdm_abc_class', 'string'),
        clearable: true,
        placeholder: '请选择ABC分类',
      },
    },
    {
      fieldName: 'changeoverGroup',
      label: '换型分组',
      component: 'Input',
      componentProps: {
        placeholder: '如颜色/材质/型号族',
        maxLength: 64,
      },
    },
    // ===== 状态与备注 =====
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: 0,
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

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入编码',
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'catId',
      label: '物料分类',
      component: markRaw(RelationSelect),
      componentProps: MAT_CAT_RELATION_PROPS,
    },
    {
      fieldName: 'matType',
      label: '物料类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: MDM_MAT_TYPE_OPTIONS,
        placeholder: '请选择物料类型',
      },
    },
    {
      fieldName: 'spec',
      label: '规格型号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入规格型号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'drawingNo',
      label: '图号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入图号',
      },
    },
    {
      fieldName: 'baseUomId',
      label: '基准计量单位',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'batchFlag',
      label: '是否批次管控',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否批次管控',
      },
    },
    {
      fieldName: 'snFlag',
      label: '是否SN管控',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否SN管控',
      },
    },
    {
      fieldName: 'shelfLifeDays',
      label: '保质期（天）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入保质期（天）',
        precision: 0,
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
export function useGridColumns(): VxeTableGridOptions<MdmMatApi.Mat>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 160,
    },
    {
      field: 'matType',
      title: '物料类型',
      minWidth: 100,
    },
    {
      field: 'spec',
      title: '规格型号',
      minWidth: 120,
    },
    {
      field: 'drawingNo',
      title: '图号',
      minWidth: 120,
    },
    { field: 'catCode', title: '分类编码', minWidth: 120 },
    { field: 'catName', title: '分类名称', minWidth: 140 },
    {
      field: 'baseUomCode',
      title: '基准计量单位',
      minWidth: 140,
      formatter: ({ row }) =>
        [row.baseUomCode, row.baseUomName].filter(Boolean).join(' / '),
    },
    {
      field: 'batchFlag',
      title: '是否批次管控',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'snFlag',
      title: '是否SN管控',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'shelfLifeDays',
      title: '保质期（天）',
      minWidth: 110,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
