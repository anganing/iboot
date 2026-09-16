import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmMatCatApi } from '#/api/mdm/matcat';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { getMatCatList } from '#/api/mdm/matcat';
import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { MAT_CAT_RELATION_PROPS } from '../relation-config';

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
      fieldName: 'parentId',
      label: '上级物料分类',
      component: 'ApiTreeSelect',
      componentProps: {
        clearable: true,
        api: async () => {
          const data = await getMatCatList({});
          data.unshift({
            id: 0,
            name: '顶级物料分类',
          });
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择上级物料分类',
        // Element Plus 的 ElTreeSelect 只认 defaultExpandAll，treeDefaultExpandAll 是 antd 的 prop 名，
        // 透传下去不会被识别，原先写在配置里等于没写
        defaultExpandAll: true,
        // 必须开 checkStrictly：ElTreeSelect 未开启 showCheckbox 时，onNodeClick 只在
        // `checkStrictly || node.isLeaf` 时才提交选中，非叶子节点被点击只展开不选中。
        // “顶级物料分类”和任何已有下级分类都是非叶子节点，缺了这个配置就永远选不中。
        // 上级分类本来就允许挂在任意层级（含 parentId=0 的顶级），所以不能反过来禁用非叶子节点。
        checkStrictly: true,
      },
      rules: 'selectRequired',
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
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
      fieldName: 'parentId',
      label: '上级物料分类',
      component: markRaw(RelationSelect),
      componentProps: MAT_CAT_RELATION_PROPS,
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
export function useGridColumns(): VxeTableGridOptions<MdmMatCatApi.MatCat>['columns'] {
  return [
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
      treeNode: true,
    },
    { field: 'parentCode', title: '父级分类编码', minWidth: 120 },
    { field: 'parentName', title: '父级分类名称', minWidth: 140 },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
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
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
