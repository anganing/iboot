<script lang="ts" setup>
import type { MesTraceApi } from '#/api/mes/trace';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { ElDescriptions, ElDescriptionsItem, ElTag } from 'element-plus';

import { nodeTypeLabel } from '../data';

/**
 * 追溯链路单节点只读详情。
 *
 * 树形表格一行只能给一列"关键快照"，而九类节点各有十几个字段（质检的合格/不合格数量、
 * 在制流水的状态迁移、工单的加工单清单都塞不进一列），双击业务单号在这里看全。
 */
defineOptions({ name: 'MesTraceNodeDetail' });

const row = ref<MesTraceApi.TraceRow>();

const title = computed(() =>
  row.value?.bizNo ? `追溯节点 ${row.value.bizNo}` : '追溯节点详情',
);

/** 快照键值对，未知类型只做原样展开，不猜字段含义 */
const snapshotEntries = computed(() => {
  const snap = row.value?.snapshot;
  if (!snap) return [];
  return Object.entries(snap).map(([key, value]) => ({
    key,
    value: formatValue(value),
  }));
});

/**
 * 值格式化：时间戳转可读时间，数组拼接，对象转 JSON。
 *
 * 快照是后端 Map 原样透出，值是 unknown，LocalDateTime 是毫秒时间戳，
 * 不能假设某一列一定是时间，只能按值的形态判断。
 */
function formatValue(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-';
  if (Array.isArray(value)) {
    return value.length > 0 ? value.map((item) => String(item)).join('、') : '-';
  }
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return value;
  }
  return String(value);
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      row.value = undefined;
      return;
    }
    row.value = drawerApi.getData<{ row?: MesTraceApi.TraceRow }>().row;
  },
});

/** 空值统一显示短横线 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}
</script>

<template>
  <Drawer
    :title="title"
    class="w-full lg:w-2/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <template v-if="row">
        <ElDescriptions :column="1" border size="small">
          <ElDescriptionsItem label="节点类型">
            <ElTag size="small" type="info">
              {{ nodeTypeLabel(row.nodeType) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="业务单号">{{ text(row.bizNo) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="名称">{{ text(row.nodeName) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="层级">{{ text(row.depth) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="上级单号">{{ text(row.parentBizNo) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="是否截断">
            {{ row.truncated ? '已截断' : '否' }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <div class="mt-4 mb-2 text-sm font-medium text-gray-700">关键快照</div>
        <ElDescriptions v-if="snapshotEntries.length > 0" :column="1" border size="small">
          <ElDescriptionsItem
            v-for="entry in snapshotEntries"
            :key="entry.key"
            :label="entry.key"
          >
            {{ entry.value }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <div v-else class="py-6 text-center text-gray-400">该节点无快照字段</div>
      </template>
      <div v-else class="py-10 text-center text-gray-400">未找到该节点</div>
    </div>
  </Drawer>
</template>
