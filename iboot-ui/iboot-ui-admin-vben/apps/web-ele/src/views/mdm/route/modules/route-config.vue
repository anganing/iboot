<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';
import type { MdmRouteOpMatApi } from '#/api/mdm/routeopmat';
import type { MdmToolApi } from '#/api/mdm/tool';

import { computed, nextTick, ref } from 'vue';

import {
  ColPage,
  confirm,
  useVbenDrawer,
  useVbenModal,
} from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { isEmpty } from '@vben/utils';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElLink,
  ElMessage,
  ElTabPane,
  ElTabs,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteRouteOp,
  deleteRouteOpList,
  getRouteDetail,
  updateRouteOpSort,
} from '#/api/mdm/route';
import {
  deleteRouteOpEsop,
  deleteRouteOpEsopList,
  getRouteOpEsop,
  getRouteOpEsopPage,
} from '#/api/mdm/routeopesop';
import { deleteRouteOpMat, deleteRouteOpMatList } from '#/api/mdm/routeopmat';
import {
  deleteRouteOpTool,
  deleteRouteOpToolList,
  getRouteOpToolPage,
} from '#/api/mdm/tool';

import RouteOpEsopForm from './route-op-esop-form.vue';
import RouteOpEsopPreview from './route-op-esop-preview.vue';
import RouteOpForm from './route-op-form.vue';
import RouteOpMatForm from './route-op-mat-form.vue';
import RouteOpToolForm from './route-op-tool-form.vue';

const loading = ref(false);
const routeSummary = ref<MdmRouteApi.Route>();
const detail = ref<MdmRouteApi.RouteDetail>();
const selectedOpId = ref<MdmRouteApi.Id>();
const checkedOperationIds = ref<MdmRouteApi.Id[]>([]);
const checkedMaterialIds = ref<MdmRouteApi.Id[]>([]);
const checkedEsopIds = ref<MdmRouteApi.Id[]>([]);
const checkedToolIds = ref<MdmRouteApi.Id[]>([]);
const esopList = ref<
  import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop[]
>([]);
const detailTab = ref('materials');

const routeId = computed(() =>
  String(detail.value?.id ?? routeSummary.value?.id ?? ''),
);
const drawerTitle = computed(() => {
  const route = detail.value ?? routeSummary.value;
  return (
    [route?.matCode, route?.matName, route?.version]
      .filter(Boolean)
      .join(' · ') || '工艺路线配置'
  );
});
const isEditable = computed(() => detail.value?.status !== 0);
const operations = computed(() => detail.value?.operations ?? []);
const selectedOperation = computed(() =>
  operations.value.find((item) => isSameId(item.id, selectedOpId.value)),
);
const materialTableTitle = computed(() => {
  const operation = selectedOperation.value;
  if (!operation) {
    return '工序用料列表';
  }
  return `工序用料列表 - ${operation.opCode ?? ''} ${operation.opName ?? ''}`;
});

const selectedOperationContext = computed(() => {
  const operation = selectedOperation.value;
  if (!operation) {
    return [];
  }
  return [
    { label: '工序编码', value: operation.opCode || '-' },
    { label: '工序名称', value: operation.opName || '-' },
    {
      label: '工作中心',
      value:
        [operation.wcCode, operation.wcName].filter(Boolean).join(' · ') ||
        '-',
    },
    {
      label: '工序标识',
      value:
        [
          operation.keyFlag ? '关键' : undefined,
          operation.faiFlag ? '首检' : undefined,
        ]
          .filter(Boolean)
          .join(' / ') || '-',
    },
  ];
});

const operationColumns: VxeTableGridOptions<MdmRouteApi.RouteOp>['columns'] = [
  { type: 'checkbox', width: 40 },
  { field: 'seq', title: '序号', width: 70 },
  { field: 'opCode', title: '工序编码', minWidth: 120 },
  { field: 'opName', title: '工序名称', minWidth: 140 },
  { field: 'materialCount', title: '用料数', width: 80 },
  {
    title: '操作',
    width: 180,
    fixed: 'right',
    slots: { default: 'actions' },
  },
];

const materialColumns: VxeTableGridOptions<MdmRouteApi.RouteOpMaterial>['columns'] =
  [
    { type: 'checkbox', width: 40 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格', minWidth: 140 },
    { field: 'qty', title: '数量', minWidth: 100 },
    {
      title: '单位',
      minWidth: 120,
      slots: { default: 'uom' },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];

const [OperationGrid, operationGridApi] = useVbenVxeGrid<MdmRouteApi.RouteOp>({
  gridOptions: {
    columns: operationColumns,
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => ({
          list: operations.value,
          total: operations.value.length,
        }),
      },
    },
    rowConfig: {
      keyField: 'id',
      isCurrent: true,
      isHover: true,
    },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: false,
      zoom: true,
    },
  } as VxeTableGridOptions<MdmRouteApi.RouteOp>,
  gridEvents: {
    cellClick: ({ row }: { row: MdmRouteApi.RouteOp }) => {
      handleSelectOperation(row);
    },
    checkboxAll: handleOperationCheckboxChange,
    checkboxChange: handleOperationCheckboxChange,
  },
});

const [MaterialGrid, materialGridApi] =
  useVbenVxeGrid<MdmRouteApi.RouteOpMaterial>({
    gridOptions: {
      columns: materialColumns,
      height: 'auto',
      keepSource: true,
      pagerConfig: { enabled: false },
      proxyConfig: {
        ajax: {
          query: async () => ({
            list: selectedOperation.value?.materials ?? [],
            total: selectedOperation.value?.materials?.length ?? 0,
          }),
        },
      },
      rowConfig: {
        keyField: 'id',
        isHover: true,
      },
      toolbarConfig: {
        custom: true,
        refresh: true,
        search: false,
        zoom: true,
      },
    } as VxeTableGridOptions<MdmRouteApi.RouteOpMaterial>,
    gridEvents: {
      checkboxAll: handleMaterialCheckboxChange,
      checkboxChange: handleMaterialCheckboxChange,
    },
  });

const [RouteOpFormModal, routeOpFormModalApi] = useVbenModal({
  connectedComponent: RouteOpForm,
  destroyOnClose: true,
});
const [RouteOpMatFormModal, routeOpMatFormModalApi] = useVbenModal({
  connectedComponent: RouteOpMatForm,
  destroyOnClose: true,
});
const [RouteOpEsopFormModal, routeOpEsopFormModalApi] = useVbenModal({
  connectedComponent: RouteOpEsopForm,
  destroyOnClose: true,
});
const [RouteOpEsopPreviewModal, routeOpEsopPreviewModalApi] = useVbenModal({
  connectedComponent: RouteOpEsopPreview,
  destroyOnClose: true,
});
const [RouteOpToolFormModal, routeOpToolFormModalApi] = useVbenModal({
  connectedComponent: RouteOpToolForm,
  destroyOnClose: true,
});

const [RouteOpEsopGrid, routeOpEsopGridApi] = useVbenVxeGrid<
  import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop
>({
  gridOptions: {
    columns: [
      { type: 'checkbox', width: 40 },
      { field: 'seq', title: '顺序', width: 70 },
      { field: 'title', title: '标题', minWidth: 200 },
      {
        field: 'fileCount',
        title: '附件数',
        minWidth: 120,
        slots: { default: 'esopFile' },
      },
      {
        field: 'requiredConfirmFlag',
        title: '要求确认阅读',
        minWidth: 110,
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
        },
      },
      {
        field: 'status',
        title: '状态',
        minWidth: 90,
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.COMMON_STATUS },
        },
      },
      {
        title: '操作',
        width: 150,
        fixed: 'right',
        slots: { default: 'actions' },
      },
    ],
    height: 'auto',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!selectedOperation.value) {
            return { list: [], total: 0 };
          }
          const result = await getRouteOpEsopPage({
            routeOpId: selectedOperation.value.id,
            pageNo: 1,
            pageSize: 100,
          });
          esopList.value = result.list;
          return { list: result.list, total: result.total };
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: false,
      zoom: true,
    },
  },
  gridEvents: {
    checkboxAll: handleEsopCheckboxChange,
    checkboxChange: handleEsopCheckboxChange,
  },
});

const [RouteOpToolGrid, routeOpToolGridApi] =
  useVbenVxeGrid<MdmToolApi.RouteOpTool>({
    gridOptions: {
      columns: [
        { type: 'checkbox', width: 40 },
        { type: 'seq', title: '序号', width: 60 },
        { field: 'toolCode', title: '工装编码', minWidth: 140 },
        { field: 'toolName', title: '工装名称', minWidth: 160 },
        { field: 'qty', title: '需求数量', minWidth: 100 },
        {
          field: 'requiredFlag',
          title: '是否必备',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
          },
        },
        {
          title: '操作',
          width: 150,
          fixed: 'right',
          slots: { default: 'actions' },
        },
      ],
      height: 'auto',
      keepSource: true,
      pagerConfig: { enabled: false },
      proxyConfig: {
        ajax: {
          query: async () => {
            if (!selectedOperation.value) {
              return { list: [], total: 0 };
            }
            const result = await getRouteOpToolPage({
              routeOpId: selectedOperation.value.id as number,
              pageNo: 1,
              pageSize: 100,
            });
            return { list: result.list, total: result.total };
          },
        },
      },
      rowConfig: {
        keyField: 'id',
        isHover: true,
      },
      toolbarConfig: {
        custom: true,
        refresh: true,
        search: false,
        zoom: true,
      },
    } as VxeTableGridOptions<MdmToolApi.RouteOpTool>,
    gridEvents: {
      checkboxAll: handleToolCheckboxChange,
      checkboxChange: handleToolCheckboxChange,
    },
  });

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      routeSummary.value = undefined;
      detail.value = undefined;
      selectedOpId.value = undefined;
      checkedOperationIds.value = [];
      checkedMaterialIds.value = [];
      checkedEsopIds.value = [];
      checkedToolIds.value = [];
      return;
    }
    routeSummary.value = drawerApi.getData<MdmRouteApi.Route>();
    detail.value = undefined;
    selectedOpId.value = undefined;
    await loadDetail();
  },
});

function isSameId(left?: MdmRouteApi.Id, right?: MdmRouteApi.Id) {
  return (
    left !== undefined && right !== undefined && String(left) === String(right)
  );
}

function getOperationIndex(operation: MdmRouteApi.RouteOp) {
  return operations.value.findIndex((item) => isSameId(item.id, operation.id));
}

async function syncGridData() {
  await nextTick();
  checkedOperationIds.value = [];
  checkedMaterialIds.value = [];
  checkedEsopIds.value = [];
  checkedToolIds.value = [];
  await operationGridApi.query();
  await materialGridApi.query();
  await routeOpEsopGridApi.query();
  await routeOpToolGridApi.query();
  if (selectedOperation.value) {
    await operationGridApi.grid.setCurrentRow(selectedOperation.value);
  }
}

async function loadDetail(preferredOpId = selectedOpId.value) {
  if (!routeId.value) {
    return;
  }
  loading.value = true;
  try {
    const data = await getRouteDetail(routeId.value);
    detail.value = data;
    const preferredOperation = data.operations.find((item) =>
      isSameId(item.id, preferredOpId),
    );
    selectedOpId.value = preferredOperation?.id ?? data.operations[0]?.id;
    await syncGridData();
  } finally {
    loading.value = false;
  }
}

async function handleSelectOperation(operation: MdmRouteApi.RouteOp) {
  selectedOpId.value = operation.id;
  checkedMaterialIds.value = [];
  checkedEsopIds.value = [];
  checkedToolIds.value = [];
  await materialGridApi.query();
  await routeOpEsopGridApi.query();
  await routeOpToolGridApi.query();
}

function handleOperationCheckboxChange({
  records,
}: {
  records: MdmRouteApi.RouteOp[];
}) {
  checkedOperationIds.value = records.map((item) => item.id);
}

function handleMaterialCheckboxChange({
  records,
}: {
  records: MdmRouteApi.RouteOpMaterial[];
}) {
  checkedMaterialIds.value = records.map((item) => item.id);
}

function handleEsopCheckboxChange({
  records,
}: {
  records: Array<{ id: MdmRouteApi.Id }>;
}) {
  checkedEsopIds.value = records.map((item) => item.id);
}

function handleToolCheckboxChange({
  records,
}: {
  records: MdmToolApi.RouteOpTool[];
}) {
  checkedToolIds.value = records.map((item) => item.id);
}

function handleCreateOperation() {
  routeOpFormModalApi
    .setData({ routeId: routeId.value } as MdmRouteApi.RouteOp)
    .open();
}

function handleEditOperation(operation: MdmRouteApi.RouteOp) {
  routeOpFormModalApi.setData(operation).open();
}

async function handleDeleteOperation(operation: MdmRouteApi.RouteOp) {
  const currentIndex = getOperationIndex(operation);
  const nextSelectedId = isSameId(operation.id, selectedOpId.value)
    ? (operations.value[currentIndex + 1]?.id ??
      operations.value[currentIndex - 1]?.id)
    : selectedOpId.value;
  await deleteRouteOp(operation.id);
  ElMessage.success('工序已删除');
  await loadDetail(nextSelectedId);
}

async function handleDeleteOperationBatch() {
  const materialCount = operations.value
    .filter((operation) =>
      checkedOperationIds.value.some((id) => isSameId(id, operation.id)),
    )
    .reduce((total, operation) => total + (operation.materialCount ?? 0), 0);
  const materialTip =
    materialCount > 0 ? `，并级联删除 ${materialCount} 条用料` : '';
  await confirm(`确定批量删除选中的工序${materialTip}吗？`);
  await deleteRouteOpList(checkedOperationIds.value);
  ElMessage.success('工序已批量删除');
  await loadDetail();
}

async function handleMoveOperation(
  operation: MdmRouteApi.RouteOp,
  offset: number,
) {
  const currentIndex = getOperationIndex(operation);
  const targetIndex = currentIndex + offset;
  if (
    currentIndex < 0 ||
    targetIndex < 0 ||
    targetIndex >= operations.value.length
  ) {
    return;
  }
  const orderedIds = operations.value.map((item) => item.id);
  const [movedId] = orderedIds.splice(currentIndex, 1);
  if (movedId === undefined) {
    return;
  }
  orderedIds.splice(targetIndex, 0, movedId);
  await updateRouteOpSort(routeId.value, orderedIds);
  ElMessage.success('工序顺序已更新');
  await loadDetail(operation.id);
}

function handleCreateMaterial() {
  if (!selectedOperation.value) {
    ElMessage.warning('请先选择一道工序');
    return;
  }
  routeOpMatFormModalApi
    .setData({
      routeOpId: selectedOperation.value.id,
    } as MdmRouteOpMatApi.RouteOpMat)
    .open();
}

function handleEditMaterial(material: MdmRouteApi.RouteOpMaterial) {
  routeOpMatFormModalApi
    .setData(material as MdmRouteOpMatApi.RouteOpMat)
    .open();
}

async function handleDeleteMaterial(material: MdmRouteApi.RouteOpMaterial) {
  await deleteRouteOpMat(material.id);
  ElMessage.success('用料已删除');
  await loadDetail(selectedOpId.value);
}

async function handleDeleteMaterialBatch() {
  await confirm('确定批量删除选中的工序用料吗？');
  await deleteRouteOpMatList(checkedMaterialIds.value);
  ElMessage.success('用料已批量删除');
  await loadDetail(selectedOpId.value);
}

function handleCreateEsop() {
  if (!selectedOperation.value) {
    ElMessage.warning('请先选择一道工序');
    return;
  }
  routeOpEsopFormModalApi
    .setData({
      routeOpId: selectedOperation.value.id,
    } as import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop)
    .open();
}

function handleEditEsop(
  esop: import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop,
) {
  routeOpEsopFormModalApi.setData(esop).open();
}

async function handlePreviewEsop(
  esop: import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop,
) {
  // 列表接口只返回附件数量，预览前拉一次详情拿到完整附件列表
  const detailData = await getRouteOpEsop(esop.id);
  routeOpEsopPreviewModalApi
    .setData({ title: detailData.title, files: detailData.files ?? [] })
    .open();
}

async function handleDeleteEsop(
  esop: import('#/api/mdm/routeopesop').MdmRouteOpEsopApi.RouteOpEsop,
) {
  await deleteRouteOpEsop(esop.id);
  ElMessage.success('作业步骤已删除');
  await routeOpEsopGridApi.query();
}

async function handleDeleteEsopBatch() {
  await confirm('确定批量删除选中的作业步骤吗？');
  await deleteRouteOpEsopList(checkedEsopIds.value as number[]);
  ElMessage.success('作业步骤已批量删除');
  await routeOpEsopGridApi.query();
}

function handleCreateTool() {
  if (!selectedOperation.value) {
    ElMessage.warning('请先选择一道工序');
    return;
  }
  routeOpToolFormModalApi
    .setData({
      routeOpId: selectedOperation.value.id,
    } as MdmToolApi.RouteOpTool)
    .open();
}

function handleEditTool(tool: MdmToolApi.RouteOpTool) {
  routeOpToolFormModalApi.setData(tool).open();
}

async function handleDeleteTool(tool: MdmToolApi.RouteOpTool) {
  await deleteRouteOpTool(tool.id);
  ElMessage.success('工序工装已删除');
  await routeOpToolGridApi.query();
}

async function handleDeleteToolBatch() {
  await confirm('确定批量删除选中的工序工装吗？');
  await deleteRouteOpToolList(checkedToolIds.value as number[]);
  ElMessage.success('工序工装已批量删除');
  await routeOpToolGridApi.query();
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <RouteOpFormModal @success="loadDetail(selectedOpId)" />
    <RouteOpMatFormModal @success="loadDetail(selectedOpId)" />
    <RouteOpEsopFormModal @success="routeOpEsopGridApi.query()" />
    <RouteOpEsopPreviewModal />
    <RouteOpToolFormModal @success="routeOpToolGridApi.query()" />

    <div v-loading="loading" class="h-full min-h-0">
      <ColPage
        content-class="overflow-hidden p-0"
        :left-width="38"
        :left-min-width="25"
        :left-max-width="60"
        :right-width="62"
        :right-min-width="40"
        :right-max-width="75"
        resizable
        split-line
        split-handle
      >
        <template #left>
          <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
            <OperationGrid
              class="w-full min-w-0 overflow-hidden"
              table-title="工艺路线工序列表"
            >
              <template #toolbar-tools>
                <TableAction
                  :actions="[
                    {
                      label: '新增工序',
                      type: 'primary',
                      icon: ACTION_ICON.ADD,
                      auth: ['mdm:route:create'],
                      disabled: !isEditable,
                      onClick: handleCreateOperation,
                    },
                    {
                      label: '批量删除',
                      type: 'danger',
                      icon: ACTION_ICON.DELETE,
                      auth: ['mdm:route:delete'],
                      disabled: !isEditable || isEmpty(checkedOperationIds),
                      onClick: handleDeleteOperationBatch,
                    },
                  ]"
                />
              </template>
              <template #actions="{ row }">
                <TableAction
                  :actions="[
                    {
                      label: '上移',
                      type: 'primary',
                      link: true,
                      icon: 'lucide:arrow-up',
                      auth: ['mdm:route:update'],
                      disabled: !isEditable || getOperationIndex(row) === 0,
                      onClick: handleMoveOperation.bind(null, row, -1),
                    },
                    {
                      label: '下移',
                      type: 'primary',
                      link: true,
                      icon: 'lucide:arrow-down',
                      auth: ['mdm:route:update'],
                      disabled:
                        !isEditable ||
                        getOperationIndex(row) === operations.length - 1,
                      onClick: handleMoveOperation.bind(null, row, 1),
                    },
                    {
                      label: '修改',
                      type: 'primary',
                      link: true,
                      icon: ACTION_ICON.EDIT,
                      auth: ['mdm:route:update'],
                      disabled: !isEditable,
                      onClick: handleEditOperation.bind(null, row),
                    },
                    {
                      label: '删除',
                      type: 'danger',
                      link: true,
                      icon: ACTION_ICON.DELETE,
                      auth: ['mdm:route:delete'],
                      disabled: !isEditable,
                      popConfirm: {
                        title: `确定删除工序“${row.opCode ?? row.seq}”${(row.materialCount ?? 0) > 0 ? `，并级联删除 ${row.materialCount} 条用料` : ''}吗？`,
                        confirm: handleDeleteOperation.bind(null, row),
                      },
                    },
                  ]"
                />
              </template>
            </OperationGrid>
          </section>
        </template>

        <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
          <ElDescriptions
            v-if="selectedOperation"
            :column="4"
            border
            size="small"
            class="mb-3 shrink-0"
          >
            <ElDescriptionsItem
              v-for="item in selectedOperationContext"
              :key="item.label"
              :label="item.label"
            >
              {{ item.value }}
            </ElDescriptionsItem>
          </ElDescriptions>
          <ElEmpty v-else description="请选择一道工序" class="shrink-0" />

          <ElTabs
            v-model="detailTab"
            class="route-config-tabs flex min-h-0 flex-1 flex-col"
          >
            <ElTabPane name="materials" label="工序用料" class="h-full">
              <MaterialGrid
                class="w-full min-w-0 overflow-hidden"
                :table-title="materialTableTitle"
              >
                <template #toolbar-tools>
                  <TableAction
                    :actions="[
                      {
                        label: '新增用料',
                        type: 'primary',
                        icon: ACTION_ICON.ADD,
                        auth: ['mdm:route-op-mat:create'],
                        disabled: !isEditable || !selectedOperation,
                        onClick: handleCreateMaterial,
                      },
                      {
                        label: '批量删除',
                        type: 'danger',
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:route-op-mat:delete'],
                        disabled: !isEditable || isEmpty(checkedMaterialIds),
                        onClick: handleDeleteMaterialBatch,
                      },
                    ]"
                  />
                </template>
                <template #uom="{ row }">
                  {{ row.uomCode || '-' }}
                  <span v-if="row.uomName"> · {{ row.uomName }}</span>
                </template>
                <template #actions="{ row }">
                  <TableAction
                    :actions="[
                      {
                        label: '修改',
                        type: 'primary',
                        link: true,
                        icon: ACTION_ICON.EDIT,
                        auth: ['mdm:route-op-mat:update'],
                        disabled: !isEditable,
                        onClick: handleEditMaterial.bind(null, row),
                      },
                      {
                        label: '删除',
                        type: 'danger',
                        link: true,
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:route-op-mat:delete'],
                        disabled: !isEditable,
                        popConfirm: {
                          title: `确定删除用料“${row.matCode ?? row.matName ?? row.id}”吗？`,
                          confirm: handleDeleteMaterial.bind(null, row),
                        },
                      },
                    ]"
                  />
                </template>
              </MaterialGrid>
            </ElTabPane>
            <ElTabPane name="esop" label="ESOP 作业指导" class="h-full">
              <RouteOpEsopGrid
                class="w-full min-w-0 overflow-hidden"
                :table-title="
                  selectedOperation
                    ? `ESOP 作业步骤 - ${selectedOperation.opCode ?? ''} ${selectedOperation.opName ?? ''}`
                    : 'ESOP 作业步骤列表'
                "
              >
                <template #toolbar-tools>
                  <TableAction
                    :actions="[
                      {
                        label: '新增作业步骤',
                        type: 'primary',
                        icon: ACTION_ICON.ADD,
                        auth: ['mdm:esop:create'],
                        disabled: !isEditable || !selectedOperation,
                        onClick: handleCreateEsop,
                      },
                      {
                        label: '批量删除',
                        type: 'danger',
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:esop:delete'],
                        disabled: !isEditable || isEmpty(checkedEsopIds),
                        onClick: handleDeleteEsopBatch,
                      },
                    ]"
                  />
                </template>
                <template #esopFile="{ row }">
                  <ElLink
                    v-if="(row.fileCount ?? 0) > 0"
                    type="primary"
                    @click="handlePreviewEsop(row)"
                  >
                    {{ row.fileCount }} 个附件
                  </ElLink>
                  <span v-else>无附件</span>
                </template>
                <template #actions="{ row }">
                  <TableAction
                    :actions="[
                      {
                        label: '修改',
                        type: 'primary',
                        link: true,
                        icon: ACTION_ICON.EDIT,
                        auth: ['mdm:esop:update'],
                        disabled: !isEditable,
                        onClick: handleEditEsop.bind(null, row),
                      },
                      {
                        label: '删除',
                        type: 'danger',
                        link: true,
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:esop:delete'],
                        disabled: !isEditable,
                        popConfirm: {
                          title: `确定删除作业步骤“${row.title ?? row.id}”吗？`,
                          confirm: handleDeleteEsop.bind(null, row),
                        },
                      },
                    ]"
                  />
                </template>
              </RouteOpEsopGrid>
            </ElTabPane>
            <ElTabPane name="tools" label="工装" class="h-full">
              <RouteOpToolGrid
                class="w-full min-w-0 overflow-hidden"
                :table-title="
                  selectedOperation
                    ? `工序工装列表 - ${selectedOperation.opCode ?? ''} ${selectedOperation.opName ?? ''}`
                    : '工序工装列表'
                "
              >
                <template #toolbar-tools>
                  <TableAction
                    :actions="[
                      {
                        label: '新增工装',
                        type: 'primary',
                        icon: ACTION_ICON.ADD,
                        auth: ['mdm:tool:update'],
                        disabled: !isEditable || !selectedOperation,
                        onClick: handleCreateTool,
                      },
                      {
                        label: '批量删除',
                        type: 'danger',
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:tool:update'],
                        disabled: !isEditable || isEmpty(checkedToolIds),
                        onClick: handleDeleteToolBatch,
                      },
                    ]"
                  />
                </template>
                <template #actions="{ row }">
                  <TableAction
                    :actions="[
                      {
                        label: '修改',
                        type: 'primary',
                        link: true,
                        icon: ACTION_ICON.EDIT,
                        auth: ['mdm:tool:update'],
                        disabled: !isEditable,
                        onClick: handleEditTool.bind(null, row),
                      },
                      {
                        label: '删除',
                        type: 'danger',
                        link: true,
                        icon: ACTION_ICON.DELETE,
                        auth: ['mdm:tool:update'],
                        disabled: !isEditable,
                        popConfirm: {
                          title: `确定删除工装“${row.toolCode ?? row.toolName ?? row.id}”吗？`,
                          confirm: handleDeleteTool.bind(null, row),
                        },
                      },
                    ]"
                  />
                </template>
              </RouteOpToolGrid>
            </ElTabPane>
          </ElTabs>
        </section>
      </ColPage>
    </div>
  </Drawer>
</template>
<style scoped>
.route-config-tabs {
  flex-grow: 1;
  height: 0;
  min-height: 0;
}

.route-config-tabs :deep(.el-tabs__content) {
  display: flex;
  flex: 1 1 0%;
  min-height: 0;
}

.route-config-tabs :deep(.el-tab-pane) {
  display: flex;
  flex: 1 1 0%;
  min-height: 0;
  /* flex item 的 automatic minimum size 会按表格列宽总和撑开面板，
     必须显式放行收缩，宽度才能从 Tabs 容器一路约束到 vxe-grid */
  min-width: 0;
}
</style>
