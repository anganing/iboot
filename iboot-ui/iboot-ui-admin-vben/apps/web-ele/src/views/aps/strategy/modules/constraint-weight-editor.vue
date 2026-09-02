<script lang="ts" setup>
import type { ApsStrategyApi } from '#/api/aps/strategy';

import { computed, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElInputNumber,
  ElSkeleton,
  ElTooltip,
} from 'element-plus';

import { loadConstraintMetadata } from '../constraint-metadata';

defineOptions({ name: 'ApsConstraintWeightEditor' });

const props = withDefaults(
  defineProps<{
    /** 未开启物料齐套约束时禁掉那一行，避免配了权重却不生效 */
    materialConstrained?: boolean;
    modelValue?: Record<string, ApsStrategyApi.ConstraintWeight>;
  }>(),
  {
    materialConstrained: false,
    modelValue: undefined,
  },
);

const emit = defineEmits<{
  'update:modelValue': [
    value: Record<string, ApsStrategyApi.ConstraintWeight> | undefined,
  ];
}>();

/** 物料齐套约束名，与后端 ApsConstraintNames.MATERIAL_SHORTAGE 对齐 */
const MATERIAL_SHORTAGE = 'materialShortage';

/** 权重上限，与后端 StrategyConfigVO.MAX_CONSTRAINT_WEIGHT 对齐 */
const MAX_WEIGHT = 1_000_000;

const metadata = ref<ApsStrategyApi.ConstraintMetadataResult>();
const loading = ref(true);
const loadError = ref<string>();

const groups = computed(() => [
  {
    title: '硬约束（违反即判计划不可行）',
    items: (metadata.value?.constraints ?? []).filter(
      (constraint) => constraint.category === 'HARD',
    ),
  },
  {
    title: '软约束（尽量满足，违反只扣分）',
    items: (metadata.value?.constraints ?? []).filter(
      (constraint) => constraint.category !== 'HARD',
    ),
  },
]);

function getWeight(constraintName: string) {
  return props.modelValue?.[constraintName];
}

/** 硬软权重都是 0 的约束等于不参与求解，标出来避免"配了没生效"却查不出原因 */
function isInactive(constraintName: string) {
  const weight = getWeight(constraintName);
  return !weight || (weight.hard === 0 && weight.soft === 0);
}

function isLocked(constraintName: string) {
  return constraintName === MATERIAL_SHORTAGE && !props.materialConstrained;
}

/**
 * 改一格权重时整份回传。
 *
 * 权重是全量提交的：没配的约束按元数据默认值补齐，让库里与快照里每条约束的实际权重都是明的，
 * 而不是"没写就是走默认"这种要靠猜的状态。
 */
function updateWeight(
  constraintName: string,
  side: 'hard' | 'soft',
  value: unknown,
) {
  const next: Record<string, ApsStrategyApi.ConstraintWeight> = {};
  for (const constraint of metadata.value?.constraints ?? []) {
    const current = getWeight(constraint.constraintName);
    next[constraint.constraintName] = {
      hard: current?.hard ?? constraint.defaultHard,
      soft: current?.soft ?? constraint.defaultSoft,
    };
  }
  const target = next[constraintName];
  if (!target) {
    return;
  }
  target[side] =
    typeof value === 'number' && Number.isFinite(value) ? value : 0;
  emit('update:modelValue', next);
}

/** 恢复默认权重：默认值来自后端元数据，不在前端写第二份 */
function restoreDefaults() {
  const defaults: Record<string, ApsStrategyApi.ConstraintWeight> = {};
  for (const constraint of metadata.value?.constraints ?? []) {
    defaults[constraint.constraintName] = {
      hard: constraint.defaultHard,
      soft: constraint.defaultSoft,
    };
  }
  emit('update:modelValue', defaults);
}

onMounted(async () => {
  try {
    metadata.value = await loadConstraintMetadata();
  } catch {
    loadError.value = '约束清单加载失败，请关闭弹窗后重试';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="w-full">
    <ElSkeleton v-if="loading" :rows="4" animated />
    <ElAlert
      v-else-if="loadError"
      :closable="false"
      :title="loadError"
      type="error"
    />
    <template v-else>
      <div
        class="border-border bg-accent/40 mb-3 flex flex-wrap items-center justify-between gap-2 rounded border px-3 py-2"
      >
        <span class="text-muted-foreground text-xs leading-5">
          硬约束权重非零即生效，数值大小不影响结果；软约束按权重大小相互权衡，默认量纲为交期延误
          100 &gt; 物料未齐套 20 &gt; 总完工 10 &gt; 优先级倒挂 5 &gt;
          工序间等待与换型 1。硬软权重都为 0 的约束不参与求解。
        </span>
        <ElButton link type="primary" @click="restoreDefaults">
          恢复默认权重
        </ElButton>
      </div>

      <div class="grid grid-cols-1 gap-x-6 gap-y-4 xl:grid-cols-2">
        <section v-for="group in groups" :key="group.title" class="min-w-0">
          <div class="text-foreground mb-2 text-xs font-medium">
            {{ group.title }}
          </div>
          <div
            class="text-muted-foreground grid grid-cols-[minmax(0,1fr)_104px_104px] items-center gap-x-3 pb-1 text-xs"
          >
            <span>约束</span>
            <span class="text-center">硬权重</span>
            <span class="text-center">软权重</span>
          </div>
          <div
            v-for="constraint in group.items"
            :key="constraint.constraintName"
            class="border-border grid grid-cols-[minmax(0,1fr)_104px_104px] items-center gap-x-3 border-t py-2"
          >
            <div class="flex min-w-0 items-center gap-1">
              <span class="truncate text-sm" :title="constraint.displayName">
                {{ constraint.displayName }}
              </span>
              <ElTooltip placement="top" :show-after="200">
                <template #content>
                  <div class="max-w-[360px] leading-5">
                    {{ constraint.description }}
                  </div>
                </template>
                <IconifyIcon
                  class="text-muted-foreground size-3.5 shrink-0 cursor-help"
                  icon="lucide:circle-help"
                />
              </ElTooltip>
              <ElTooltip
                v-if="isLocked(constraint.constraintName)"
                content="需先把“约束物料齐套”设为是，这条约束才会生效"
                placement="top"
              >
                <IconifyIcon
                  class="text-warning size-3.5 shrink-0 cursor-help"
                  icon="lucide:lock"
                />
              </ElTooltip>
              <ElTooltip
                v-else-if="isInactive(constraint.constraintName)"
                content="硬软权重都为 0，该约束不参与求解"
                placement="top"
              >
                <IconifyIcon
                  class="text-warning size-3.5 shrink-0 cursor-help"
                  icon="lucide:triangle-alert"
                />
              </ElTooltip>
            </div>
            <ElInputNumber
              :controls="false"
              :disabled="isLocked(constraint.constraintName)"
              :max="MAX_WEIGHT"
              :min="0"
              :model-value="getWeight(constraint.constraintName)?.hard"
              :precision="0"
              class="!w-full"
              @update:model-value="
                updateWeight(constraint.constraintName, 'hard', $event)
              "
            />
            <ElInputNumber
              :controls="false"
              :disabled="isLocked(constraint.constraintName)"
              :max="MAX_WEIGHT"
              :min="0"
              :model-value="getWeight(constraint.constraintName)?.soft"
              :precision="0"
              class="!w-full"
              @update:model-value="
                updateWeight(constraint.constraintName, 'soft', $event)
              "
            />
          </div>
        </section>
      </div>
    </template>
  </div>
</template>
