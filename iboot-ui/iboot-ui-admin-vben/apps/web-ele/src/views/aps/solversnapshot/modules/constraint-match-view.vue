<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

import { computed, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElTag,
  ElTooltip,
} from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { formatNumber, useConstraintMatchColumns } from '../data';

/**
 * 约束命中对照表（只读）。
 *
 * <p>这是 Timefold `SolutionManager.analyze()` 结果在页面上的落点：版本总分只说明"好不好"，
 * 这张表说明"代价花在哪条约束上"。权重列与排程策略的权重编辑器同量纲，看完这里就能回去调策略。
 *
 * <p>做成纯展示组件而不是弹窗：快照详情按快照编号进来，排程版本按版本编号进来，
 * 两个入口的取数方式不同，但呈现完全一致，没必要写两份。
 */
defineOptions({ name: 'ApsConstraintMatchView' });

const props = withDefaults(
  defineProps<{
    data?: ApsSolverSnapshotApi.ConstraintMatch | null;
    /** 是否显示场景与版本号。快照详情弹窗的表头已经有了，那里传 false 避免重复 */
    showContext?: boolean;
  }>(),
  { data: undefined, showContext: true },
);

const constraints = computed(() => props.data?.constraints ?? []);

/**
 * 快照存在但解析不出约束。
 *
 * 早期版本写的快照是另一套结构（`matches/constraint/count`），后端解析不出来时给空清单。
 * 这里必须显式说明，否则一张零行表格会让人以为"这次求解没有任何约束参与"。
 */
const unparsable = computed(() => !!props.data && constraints.value.length === 0);

/** 硬分为负说明存在无法回避的冲突，版本会被判为求解失败 */
const infeasible = computed(() => (props.data?.hardScore ?? 0) < 0);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useConstraintMatchColumns(),
    // 弹窗里给固定高度：约束十来条，420px 刚好一屏放下，不必跟着弹窗高度伸缩
    height: '420px',
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'constraintName', isHover: true },
    // 约束最多十来条，一次给全，排序在本地做
    sortConfig: { multiple: false },
    toolbarConfig: { enabled: false },
  } as VxeTableGridOptions<ApsSolverSnapshotApi.ConstraintMatchItem>,
});

watch(
  constraints,
  (value) => gridApi.setGridOptions({ data: [...value] }),
  { immediate: true },
);
</script>

<template>
  <ElEmpty
    v-if="unparsable"
    description="这条快照的结构较早，无法解析成对照表，请在求解快照页查看原始内容"
  />
  <div v-else-if="data" class="flex flex-col gap-3">
    <ElDescriptions :column="3" border size="small">
      <ElDescriptionsItem v-if="showContext" label="排程场景">
        {{
          [data.scenarioCode, data.scenarioName].filter(Boolean).join(' · ') ||
          '-'
        }}
      </ElDescriptionsItem>
      <ElDescriptionsItem v-if="showContext" label="版本号">
        {{ data.versionNo == null ? '-' : `V${data.versionNo}` }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="分析时间">
        {{ data.snapshotTime ? formatDateTime(data.snapshotTime) : '-' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="硬约束总分">
        <ElTag :type="infeasible ? 'danger' : 'success'" size="small">
          {{ formatNumber(data.hardScore) }}
        </ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem label="软约束总分">
        {{ formatNumber(data.softScore) }}
      </ElDescriptionsItem>
    </ElDescriptions>

    <div
      class="border-border bg-accent/40 text-muted-foreground rounded border px-3 py-2 text-xs leading-5"
    >
      {{
        infeasible
          ? '硬约束总分为负，本次求解没有可行解：先看硬分扣分不为 0 的那几条，它们是排不出计划的直接原因。'
          : '硬约束均已满足。软分扣分绝对值最大的那条就是当前计划付出的主要代价，想改变取舍就回排程策略里调它的权重。'
      }}
    </div>

    <Grid>
      <template #constraint="{ row }">
        <div class="flex min-w-0 items-center gap-1">
          <span class="truncate" :title="row.displayName">
            {{ row.displayName }}
          </span>
          <ElTooltip v-if="row.description" placement="top" :show-after="200">
            <template #content>
              <div class="max-w-[360px] leading-5">{{ row.description }}</div>
            </template>
            <IconifyIcon
              class="text-muted-foreground size-3.5 shrink-0 cursor-help"
              icon="lucide:circle-help"
            />
          </ElTooltip>
        </div>
      </template>
      <template #hardScore="{ row }">
        <span :class="{ 'text-red-500': (row.hardScore ?? 0) < 0 }">
          {{ formatNumber(row.hardScore) }}
        </span>
      </template>
      <template #softScore="{ row }">
        <span :class="{ 'text-red-500': (row.softScore ?? 0) < 0 }">
          {{ formatNumber(row.softScore) }}
        </span>
      </template>
    </Grid>
  </div>
  <ElEmpty v-else description="该版本还没有约束命中分析，求解完成后才会生成" />
</template>
