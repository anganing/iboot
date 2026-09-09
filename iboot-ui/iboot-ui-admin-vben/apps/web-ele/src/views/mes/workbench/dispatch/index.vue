<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmWcApi } from '#/api/mdm/wc';
import type { MdmWorkshopApi } from '#/api/mdm/workshop';
import type { MesDispatchTaskApi } from '#/api/mes/dispatchtask';
import type { MesOpTaskApi } from '#/api/mes/optask';
import type { MesWorkOrderApi } from '#/api/mes/workorder';

import { computed, onMounted, ref } from 'vue';

import { ColPage } from '@vben/common-ui';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
  ElTree,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getWcPage } from '#/api/mdm/wc';
import { getWorkshopPage } from '#/api/mdm/workshop';
import { createDispatchTask } from '#/api/mes/dispatchtask';
import { getOpTaskPage } from '#/api/mes/optask';
import { getWorkOrder } from '#/api/mes/workorder';

import {
  type OpTaskRow,
  useDispatchFormSchema,
  useTaskGridColumns,
  useTaskGridFormSchema,
} from './data';

/** 树节点：全部入口 + 车间层 + 工作中心层 */
interface ScopeNode {
  children?: ScopeNode[];
  id: string;
  kind: 'all' | 'wc' | 'workshop';
  label: string;
  scopeWcIds: number[];
}

/** 树数据（构造树时已把车间节点覆盖的工作中心 id 集算好挂到节点上） */
const treeData = ref<ScopeNode[]>([]);
const treeLoading = ref(false);
const treeRef = ref<InstanceType<typeof ElTree>>();
const selectedNode = ref<ScopeNode>();

/**
 * 后端 op-task 分页不支持车间/工作中心参数，按任务书兜底约定在已加载行上过滤：
 * enrichRows 已把每行作业工单（含 wcId）补进缓存，选中节点后按“该节点覆盖的
 * 工作中心集合”行内过滤，total 保留后端值，翻页仍可达其他页的范围内任务。
 */
const scopeWcIds = ref<Set<number>>();
const checkedTasks = ref<MesOpTaskApi.OpTask[]>([]);
const selectedCount = computed(() => checkedTasks.value.length);

const [DispatchForm, dispatchFormApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelClass: 'justify-start',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useDispatchFormSchema(),
  showDefaultActions: false,
});

const [TaskGrid, taskGridApi] = useVbenVxeGrid({
  formOptions: { schema: useTaskGridFormSchema(), wrapperClass: 'grid-cols-3' },
  gridOptions: {
    columns: useTaskGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const pageResult = await getOpTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
          const list = (pageResult.list ?? []) as OpTaskRow[];
          await enrichRows(list);
          if (scopeWcIds.value) {
            const allowedWcIds = scopeWcIds.value;
            pageResult.list = list.filter((row) => {
              const wcId = workOrderCache.get(
                row.workOrderId ?? Number.NaN,
              )?.wcId;
              return wcId != null && allowedWcIds.has(wcId);
            });
          }
          return pageResult;
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<OpTaskRow>,
  gridEvents: {
    checkboxAll: ({ records }: { records: MesOpTaskApi.OpTask[] }) => {
      checkedTasks.value = records;
    },
    checkboxChange: ({ records }: { records: MesOpTaskApi.OpTask[] }) => {
      checkedTasks.value = records;
    },
  },
});

/**
 * 物料与齐套状态在作业工单上、工序任务 RespVO 不带，按当前页出现的
 * workOrderId 批量拉工单补全到行；同工单命中缓存不重复请求。
 */
const workOrderCache = new Map<number, MesWorkOrderApi.WorkOrder>();

async function enrichRows(rows: OpTaskRow[]) {
  const needIds = [
    ...new Set(
      rows
        .map((row) => row.workOrderId)
        .filter(
          (id): id is number => id != null && !workOrderCache.has(id),
        ),
    ),
  ];
  if (needIds.length > 0) {
    const details = await Promise.all(
      needIds.map((id) => getWorkOrder(id).catch(() => undefined)),
    );
    for (const [index, id] of needIds.entries()) {
      const workOrder = details[index];
      if (workOrder) {
        workOrderCache.set(id, workOrder);
      }
    }
  }
  for (const row of rows) {
    const workOrder =
      row.workOrderId != null ? workOrderCache.get(row.workOrderId) : undefined;
    row.matCode = workOrder?.matCode;
    row.matName = workOrder?.matName;
    row.kitStatus = workOrder?.kitStatus;
  }
}

/** 树数据：全部 → 车间（带编码） → 车间下工作中心 */
function buildTree(
  workshops: MdmWorkshopApi.Workshop[],
  wcs: MdmWcApi.Wc[],
): ScopeNode[] {
  const wcIdsByWorkshop = new Map<number, number[]>();
  const nodesByWorkshop = new Map<number, ScopeNode[]>();
  for (const wc of wcs) {
    if (wc.workshopId == null) continue;
    const ids = wcIdsByWorkshop.get(wc.workshopId) ?? [];
    ids.push(wc.id);
    wcIdsByWorkshop.set(wc.workshopId, ids);
    const nodes = nodesByWorkshop.get(wc.workshopId) ?? [];
    nodes.push({
      id: `wc-${wc.id}`,
      kind: 'wc',
      label: [wc.code, wc.name].filter(Boolean).join(' '),
      scopeWcIds: [wc.id],
    });
    nodesByWorkshop.set(wc.workshopId, nodes);
  }
  return [
    {
      id: 'all',
      kind: 'all',
      label: '全部',
      scopeWcIds: [],
    },
    ...workshops.map((workshop) => ({
      children: nodesByWorkshop.get(workshop.id),
      id: `workshop-${workshop.id}`,
      kind: 'workshop' as const,
      label: [workshop.code, workshop.name].filter(Boolean).join(' '),
      scopeWcIds: wcIdsByWorkshop.get(workshop.id) ?? [],
    })),
  ];
}

async function loadTree() {
  treeLoading.value = true;
  try {
    const [workshops, wcs] = await Promise.all([
      getWorkshopPage({ pageNo: 1, pageSize: 200 }),
      getWcPage({ pageNo: 1, pageSize: 200 }),
    ]);
    treeData.value = buildTree(workshops.list ?? [], wcs.list ?? []);
  } finally {
    treeLoading.value = false;
  }
}

/**
 * 点击已选节点再次取消选中，回到全部；清空后同步清掉 ElTree
 * highlight-current 的选中高亮，并清掉本页因过滤被隐藏的勾选行。
 */
async function handleNodeClick(node: ScopeNode) {
  const isDeselect = selectedNode.value?.id === node.id;
  selectedNode.value = isDeselect ? undefined : node;
  if (isDeselect) {
    treeRef.value?.setCurrentKey(undefined);
  }
  scopeWcIds.value = isDeselect
    ? undefined
    : new Set(selectedNode.value?.scopeWcIds ?? []);
  checkedTasks.value = [];
  await taskGridApi.query();
}

async function handleDispatch() {
  const { valid } = await dispatchFormApi.validate();
  if (!valid || selectedCount.value === 0) return;
  const values = await dispatchFormApi.getValues();
  const loading = ElLoading.service({
    text: `正在派发 ${selectedCount.value} 个工序任务...`,
  });
  try {
    await Promise.all(
      checkedTasks.value.map((task) =>
        createDispatchTask({
          opTaskId: task.id,
          resId: values.resId,
          operatorId: values.operatorId || 0,
          teamId: values.teamId || 0,
          shiftId: values.shiftId || 0,
          plannedStartTime: task.plannedStartTime,
          plannedEndTime: task.plannedEndTime,
          status: 'PENDING',
          remark: values.remark,
        } as MesDispatchTaskApi.DispatchTask),
      ),
    );
    ElMessage.success(`已派工 ${selectedCount.value} 个工序任务`);
    checkedTasks.value = [];
    await taskGridApi.query();
  } finally {
    loading.close();
  }
}

onMounted(() => {
  loadTree();
});
</script>

<template>
  <ColPage
    auto-content-height
    content-class="overflow-hidden p-0"
    :left-width="20"
    :left-min-width="14"
    :left-max-width="34"
    :right-width="80"
    :right-min-width="56"
    resizable
    split-line
    split-handle
  >
    <template #left>
      <section class="flex h-full min-h-0 w-full flex-col overflow-hidden">
        <div
          class="border-border bg-card shrink-0 border-b px-3 py-2 text-sm font-semibold"
        >
          车间 / 工作中心
        </div>
        <div
          v-loading="treeLoading"
          class="min-h-0 flex-1 overflow-auto px-2 py-2"
        >
          <ElTree
            v-if="treeData.length > 0"
            ref="treeRef"
            :data="treeData"
            default-expand-all
            highlight-current
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            @node-click="handleNodeClick"
          />
          <div
            v-else-if="!treeLoading"
            class="py-4 text-center text-gray-500"
          >
            暂无车间数据
          </div>
        </div>
      </section>
    </template>
    <div class="flex h-full min-h-0 w-full gap-2">
      <!-- 中区：待派工工序任务列表 -->
      <section
        class="flex h-full min-h-0 min-w-0 flex-1 flex-col overflow-hidden"
      >
        <TaskGrid
          class="w-full min-w-0 overflow-hidden"
          table-title="待派工工序任务"
        />
      </section>
      <!-- 右区：派工表单 -->
      <section
        class="border-border bg-card flex h-full min-h-0 w-[300px] shrink-0 flex-col overflow-hidden rounded-md border p-3"
      >
        <ElAlert
          class="shrink-0"
          :closable="false"
          show-icon
          type="info"
          :title="
            selectedCount
              ? `已选择 ${selectedCount} 个工序任务`
              : '请在列表勾选待派工工序任务'
          "
        />
        <ElDescriptions
          v-if="selectedCount"
          class="my-3 shrink-0"
          :column="1"
          border
          size="small"
        >
          <ElDescriptionsItem label="首个任务">
            {{ checkedTasks[0]?.taskNo }} - {{ checkedTasks[0]?.opName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="批量数量">
            {{ selectedCount }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <DispatchForm class="mt-1 min-h-0 flex-1 overflow-auto" />
        <div class="mt-3 shrink-0">
          <TableAction
            :actions="[
              {
                label: '确认派工',
                type: 'primary',
                icon: 'lucide:user-round-check',
                auth: ['mes:dispatch-task:create'],
                disabled: selectedCount === 0,
                onClick: handleDispatch,
              },
            ]"
          />
        </div>
      </section>
    </div>
  </ColPage>
</template>
