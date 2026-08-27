<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';
import type { MdmRouteOpMatApi } from '#/api/mdm/routeopmat';

import { computed, nextTick, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage, ElTabPane, ElTabs, ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteRouteOp,
  deleteRouteOpList,
  getRouteDetail,
  updateRouteOpSort,
} from '#/api/mdm/route';
import { deleteRouteOpMat, deleteRouteOpMatList } from '#/api/mdm/routeopmat';

import RouteOpForm from './route-op-form.vue';
import RouteOpMatForm from './route-op-mat-form.vue';

const loading = ref(false);
const routeSummary = ref<MdmRouteApi.Route>();
const detail = ref<MdmRouteApi.RouteDetail>();
const selectedOpId = ref<MdmRouteApi.Id>();
const checkedOperationIds = ref<MdmRouteApi.Id[]>([]);
const checkedMaterialIds = ref<MdmRouteApi.Id[]>([]);
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
const isDraft = computed(() => detail.value?.status === 'DRAFT');
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

const operationColumns: VxeTableGridOptions<MdmRouteApi.RouteOp>['columns'] = [
  { type: 'checkbox', width: 40 },
  { field: 'seq', title: '序号', width: 70 },
  { field: 'opCode', title: '工序编码', minWidth: 120 },
  { field: 'opName', title: '工序名称', minWidth: 140 },
  {
    title: '工作中心',
    minWidth: 180,
    slots: { default: 'workCenter' },
  },
  {
    title: '工序标识',
    minWidth: 150,
    slots: { default: 'flags' },
  },
  { field: 'parallelGroup', title: '并行组', minWidth: 100 },
  { field: 'materialCount', title: '用料数', width: 90 },
  { field: 'remark', title: '备注', minWidth: 140 },
  {
    title: '操作',
    width: 280,
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
    height: '280px',
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
      height: '280px',
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
  await operationGridApi.query();
  await materialGridApi.query();
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
  await materialGridApi.query();
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
</script>

<template>
  <Drawer :title="drawerTitle" class="w-full lg:w-4/5">
    <RouteOpFormModal @success="loadDetail(selectedOpId)" />
    <RouteOpMatFormModal @success="loadDetail(selectedOpId)" />

    <div v-loading="loading" class="px-4 pb-4">
      <OperationGrid table-title="工艺路线工序列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增工序',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:route:create'],
                disabled: !isDraft,
                onClick: handleCreateOperation,
              },
              {
                label: '批量删除',
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:route:delete'],
                disabled: !isDraft || isEmpty(checkedOperationIds),
                onClick: handleDeleteOperationBatch,
              },
            ]"
          />
        </template>
        <template #workCenter="{ row }">
          {{ row.wcCode || '-' }}
          <span v-if="row.wcName"> · {{ row.wcName }}</span>
        </template>
        <template #flags="{ row }">
          <div class="flex items-center justify-center gap-1">
            <ElTag v-if="row.keyFlag" size="small" type="danger"> 关键 </ElTag>
            <ElTag v-if="row.faiFlag" size="small" type="warning"> 首检 </ElTag>
            <span v-if="!row.keyFlag && !row.faiFlag">-</span>
          </div>
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
                disabled: !isDraft || getOperationIndex(row) === 0,
                onClick: handleMoveOperation.bind(null, row, -1),
              },
              {
                label: '下移',
                type: 'primary',
                link: true,
                icon: 'lucide:arrow-down',
                auth: ['mdm:route:update'],
                disabled:
                  !isDraft || getOperationIndex(row) === operations.length - 1,
                onClick: handleMoveOperation.bind(null, row, 1),
              },
              {
                label: '修改',
                type: 'primary',
                link: true,
                icon: ACTION_ICON.EDIT,
                auth: ['mdm:route:update'],
                disabled: !isDraft,
                onClick: handleEditOperation.bind(null, row),
              },
              {
                label: '删除',
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:route:delete'],
                disabled: !isDraft,
                popConfirm: {
                  title: `确定删除工序“${row.opCode ?? row.seq}”${(row.materialCount ?? 0) > 0 ? `，并级联删除 ${row.materialCount} 条用料` : ''}吗？`,
                  confirm: handleDeleteOperation.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </OperationGrid>

      <ElTabs v-model="detailTab" class="mt-2">
        <ElTabPane name="materials" label="工序用料">
          <MaterialGrid :table-title="materialTableTitle">
            <template #toolbar-tools>
              <TableAction
                :actions="[
                  {
                    label: '新增用料',
                    type: 'primary',
                    icon: ACTION_ICON.ADD,
                    auth: ['mdm:route-op-mat:create'],
                    disabled: !isDraft || !selectedOperation,
                    onClick: handleCreateMaterial,
                  },
                  {
                    label: '批量删除',
                    type: 'danger',
                    icon: ACTION_ICON.DELETE,
                    auth: ['mdm:route-op-mat:delete'],
                    disabled: !isDraft || isEmpty(checkedMaterialIds),
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
                    disabled: !isDraft,
                    onClick: handleEditMaterial.bind(null, row),
                  },
                  {
                    label: '删除',
                    type: 'danger',
                    link: true,
                    icon: ACTION_ICON.DELETE,
                    auth: ['mdm:route-op-mat:delete'],
                    disabled: !isDraft,
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
      </ElTabs>
    </div>
  </Drawer>
</template>
