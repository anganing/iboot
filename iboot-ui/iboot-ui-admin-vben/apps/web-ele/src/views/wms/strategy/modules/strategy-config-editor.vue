<script lang="ts" setup>
import type { WmsStrategyApi } from '#/api/wms/strategy';

import { computed, onMounted, ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElInput,
  ElInputNumber,
  ElOption,
  ElSelect,
  ElSkeleton,
} from 'element-plus';

import { RelationSelect } from '#/components/relation-select';

import { WH_CODE_RELATION_PROPS } from '../../relation-config';
import { loadFieldMetadata } from '../field-metadata';

defineOptions({ name: 'WmsStrategyConfigEditor' });

const props = withDefaults(
  defineProps<{
    modelValue?: WmsStrategyApi.StrategyConfig;
    /** 当前策略类型，决定能筛哪些字段、能按哪些字段排序 */
    strategyType?: string;
  }>(),
  {
    modelValue: undefined,
    strategyType: undefined,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: undefined | WmsStrategyApi.StrategyConfig];
}>();

/** 布尔字段的选项。用下拉而不是开关：开关分不出"没选"和"选了否" */
const BOOLEAN_OPTIONS = [
  { label: '是', value: true },
  { label: '否', value: false },
];

/**
 * 策略类型，与后端 WmsStrategyTypeEnum 对齐。
 *
 * 条件与排序完全由元数据驱动，只有类型专属选项例外：它们是具名字段，前端得知道字段名才能绑定，
 * 也就绕不开知道类型名。加类型专属选项时这里和模板要一起改。
 */
const PUTAWAY = 'PUTAWAY';
const REPLENISH = 'REPLENISH';

const metadata = ref<WmsStrategyApi.FieldMetadataResult>();
const loading = ref(true);
const loadError = ref<string>();

const typeMetadata = computed(() =>
  metadata.value?.types.find(
    (item) => item.strategyType === props.strategyType,
  ),
);
const conditionFields = computed(() => typeMetadata.value?.conditionFields ?? []);
const sortFields = computed(() => typeMetadata.value?.sortFields ?? []);
const sortOrders = computed(() => metadata.value?.sortOrders ?? []);
const conditions = computed(() => props.modelValue?.conditions ?? []);
const sorts = computed(() => props.modelValue?.sorts ?? []);
const isPutaway = computed(() => props.strategyType === PUTAWAY);
const isReplenish = computed(() => props.strategyType === REPLENISH);
const dimensionCheckOptions = computed(() =>
  getDictOptions(DICT_TYPE.WMS_DIMENSION_CHECK_MODE, 'string'),
);

function findConditionField(field?: string) {
  return conditionFields.value.find((item) => item.field === field);
}

/** 当前算子是否收多个值，决定值控件是单选还是多选 */
function isMultiValue(field?: string, op?: string) {
  return (
    findConditionField(field)?.operators.find((item) => item.op === op)
      ?.multiValue === true
  );
}

function valueOptions(field?: string) {
  const dictType = findConditionField(field)?.dictType;
  return dictType ? getDictOptions(dictType, 'string') : [];
}

/** 改一处就整份回传，避免直接改 props 里的对象 */
function emitConfig(patch: Partial<WmsStrategyApi.StrategyConfig>) {
  emit('update:modelValue', { ...(props.modelValue ?? {}), ...patch });
}

function patchCondition(
  index: number,
  patch: Partial<WmsStrategyApi.Condition>,
) {
  emitConfig({
    conditions: conditions.value.map((item, current) =>
      current === index ? { ...item, ...patch } : item,
    ),
  });
}

function addCondition() {
  const field = conditionFields.value[0];
  if (!field) {
    return;
  }
  emitConfig({
    conditions: [
      ...conditions.value,
      { field: field.field, op: field.operators[0]?.op, value: undefined },
    ],
  });
}

function removeCondition(index: number) {
  emitConfig({
    conditions: conditions.value.filter((_, current) => current !== index),
  });
}

/** 换字段时算子和值一起重置：新字段未必支持原算子，值类型也可能完全不同 */
function updateConditionField(index: number, field: string) {
  patchCondition(index, {
    field,
    op: findConditionField(field)?.operators[0]?.op,
    value: undefined,
  });
}

/** 单值与多值算子互换时清掉原值，留着它会变成后端拒收的错类型 */
function updateConditionOp(index: number, op: string) {
  const condition = conditions.value[index];
  const keepValue =
    isMultiValue(condition?.field, condition?.op) === isMultiValue(condition?.field, op);
  patchCondition(index, { op, value: keepValue ? condition?.value : undefined });
}

function updateConditionValue(index: number, value: unknown) {
  patchCondition(index, { value });
}

/** RelationSelect 的值类型覆盖了数组与数字，来源仓库只收单个编码，这里收窄一下 */
function toWhCode(value: unknown) {
  return typeof value === 'string' && value !== '' ? value : undefined;
}

/**
 * 把每行的元数据一次算好。
 *
 * 模板里对同一行反复调 findConditionField / valueOptions 会让字典每次重渲染都重算一遍，
 * 也让模板里塞满函数调用不好读。
 */
const conditionRows = computed(() =>
  conditions.value.map((condition) => {
    const definition = findConditionField(condition.field);
    return {
      condition,
      definition,
      multiValue: isMultiValue(condition.field, condition.op),
      options: valueOptions(condition.field),
      valueType: definition?.valueType,
    };
  }),
);

/** 已被别的行占用的排序字段不再出现在下拉里：同字段排两次后端会拒 */
function availableSortFields(index: number) {
  const used = new Set(
    sorts.value
      .filter((_, current) => current !== index)
      .map((item) => item.field),
  );
  return sortFields.value.filter((field) => !used.has(field.field));
}

function patchSort(index: number, patch: Partial<WmsStrategyApi.Sort>) {
  emitConfig({
    sorts: sorts.value.map((item, current) =>
      current === index ? { ...item, ...patch } : item,
    ),
  });
}

function addSort() {
  const field = availableSortFields(-1)[0];
  if (!field) {
    return;
  }
  emitConfig({
    sorts: [
      ...sorts.value,
      { field: field.field, order: sortOrders.value[0]?.value },
    ],
  });
}

function removeSort(index: number) {
  emitConfig({ sorts: sorts.value.filter((_, current) => current !== index) });
}

/** 排序链的顺序就是优先级，所以必须能调顺序，不能只给增删 */
function moveSort(index: number, offset: number) {
  const target = index + offset;
  if (target < 0 || target >= sorts.value.length) {
    return;
  }
  const next = [...sorts.value];
  const moved = next[index];
  const replaced = next[target];
  if (!moved || !replaced) {
    return;
  }
  next[index] = replaced;
  next[target] = moved;
  emitConfig({ sorts: next });
}

/** 排序行也把字段定义一次算好，说明常显在行下方，不用 hover 才看得到 */
const sortRows = computed(() =>
  sorts.value.map((sort) => ({
    sort,
    definition: sortFields.value.find((item) => item.field === sort.field),
  })),
);

/** 回传该类型的默认配置。元数据是会话级共享的，拷一份再回传，免得用户的编辑污染缓存 */
function emitDefaultConfig() {
  const preset = typeMetadata.value?.defaultConfig ?? {};
  emit('update:modelValue', { ...preset, conditions: [], sorts: [] });
}

/**
 * 策略类型变化时重置配置。
 *
 * 旧类型配的字段在新类型下大多不合法（上架的库位承重换到拣选就不存在了），留着的话页面上是一行
 * 选不出东西的空白、保存时被后端拒。主动重置比让人自己发现要删哪几行强。
 *
 * previous 为空有两种情形，要分开处理：一是打开已有策略时类型从空填成实际值，此时配置已经填好，
 * 覆盖它等于把人家配好的规则清了；二是新建时首次选类型，那才该填默认值。用"有没有配置对象"
 * 区分两者——已有策略的 config 由 get 接口给出，一定不是 undefined。
 */
watch(
  () => props.strategyType,
  (next, previous) => {
    if (!next || next === previous) {
      return;
    }
    if (previous === undefined && props.modelValue !== undefined) {
      return;
    }
    emitDefaultConfig();
  },
);

onMounted(async () => {
  try {
    metadata.value = await loadFieldMetadata();
    // 元数据比用户手速慢的情况：类型已经选了但配置还是空的，这时才补默认值
    if (props.modelValue === undefined && props.strategyType) {
      emitDefaultConfig();
    }
  } catch {
    loadError.value = '字段清单加载失败，请关闭弹窗后重试';
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
    <ElAlert
      v-else-if="!typeMetadata"
      :closable="false"
      title="先选择上面的策略类型，这里才知道可以筛哪些字段"
      type="info"
    />
    <template v-else>
      <div
        class="border-border bg-accent/40 text-muted-foreground mb-3 rounded border px-3 py-2 text-xs leading-5"
      >
        筛选条件把不能用的{{ typeMetadata.candidateTarget }}排除掉，排序链决定剩下的先用哪一个。
        两者都不配也是有效配置，等于全部候选按稳定顺序取第一个。
      </div>

      <div
        v-if="isPutaway"
        class="border-border mb-3 grid grid-cols-1 gap-x-6 gap-y-3 rounded border px-3 py-3 sm:grid-cols-2"
      >
        <div class="min-w-0">
          <div class="mb-1 flex items-center gap-2">
            <span class="text-muted-foreground w-24 shrink-0 text-xs">
              尺寸校验
            </span>
            <ElSelect
              :model-value="modelValue?.dimensionCheck"
              class="!w-full"
              clearable
              placeholder="留空按库位自身设置"
              @update:model-value="emitConfig({ dimensionCheck: $event })"
            >
              <ElOption
                v-for="option in dimensionCheckOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </ElSelect>
          </div>
          <div class="text-muted-foreground text-xs leading-5">
            留空按每个库位自己的设置判；填了就对这条策略选出的候选统一用这个模式，
            用来应对“这批货必须全尺寸校验”这类临时要求，不必去改一批库位主数据。
          </div>
        </div>
        <div class="min-w-0">
          <div class="mb-1 flex items-center gap-2">
            <span class="text-muted-foreground w-24 shrink-0 text-xs">
              允许混批上架
            </span>
            <ElSelect
              :model-value="modelValue?.allowMixedLot"
              class="!w-full"
              placeholder="请选择"
              @update:model-value="emitConfig({ allowMixedLot: $event })"
            >
              <ElOption
                v-for="option in BOOLEAN_OPTIONS"
                :key="String(option.value)"
                :label="option.label"
                :value="option.value"
              />
            </ElSelect>
          </div>
          <div class="text-muted-foreground text-xs leading-5">
            是否允许把新批次放到已有其它批次的库位上。开了库容更省，代价是这个库位上的先进先出
            无从谈起。库区自己也有混批开关，两个都放行才会真的混。
          </div>
        </div>
      </div>

      <div
        v-else-if="isReplenish"
        class="border-border mb-3 grid grid-cols-1 gap-x-6 gap-y-3 rounded border px-3 py-3 sm:grid-cols-2"
      >
        <div class="flex min-w-0 items-center gap-2">
          <span class="text-muted-foreground w-24 shrink-0 text-xs">
            来源仓库
          </span>
          <RelationSelect
            v-bind="WH_CODE_RELATION_PROPS"
            :model-value="modelValue?.fromWhCode"
            class="w-full"
            @update:model-value="emitConfig({ fromWhCode: toWhCode($event) })"
          />
        </div>
        <div class="flex min-w-0 items-center gap-2">
          <span class="text-muted-foreground w-24 shrink-0 text-xs">
            单次补货量
          </span>
          <ElInputNumber
            :controls="false"
            :min="0"
            :model-value="modelValue?.replenishQty"
            class="!w-full"
            placeholder="每次补多少"
            @update:model-value="emitConfig({ replenishQty: $event })"
          />
        </div>
        <div class="text-muted-foreground text-xs leading-5 sm:col-span-2">
          来源仓库、单次补货量与至少一条触发条件，在把策略改成“启用”时必填；草稿阶段可以留空。
        </div>
      </div>

      <div class="grid grid-cols-1 gap-x-6 gap-y-5 xl:grid-cols-2">
        <section class="min-w-0">
          <div class="text-foreground mb-2 text-xs font-medium">
            筛选条件（多条之间是“且”）
          </div>
          <div
            v-if="conditionRows.length === 0"
            class="text-muted-foreground border-border rounded border border-dashed px-3 py-2 text-xs"
          >
            没有条件，全部{{ typeMetadata.candidateTarget }}都参与
          </div>
          <div
            v-for="(row, index) in conditionRows"
            :key="index"
            class="border-border border-t py-2 first:border-t-0"
          >
            <div
              class="grid grid-cols-[minmax(0,1fr)_104px_minmax(0,1.1fr)_28px] items-center gap-x-2"
            >
              <ElSelect
                :model-value="row.condition.field"
                class="!w-full"
                placeholder="选择字段"
                @update:model-value="updateConditionField(index, $event)"
              >
                <ElOption
                  v-for="field in conditionFields"
                  :key="field.field"
                  :label="field.displayName"
                  :value="field.field"
                />
              </ElSelect>

              <ElSelect
                :model-value="row.condition.op"
                class="!w-full"
                placeholder="算子"
                @update:model-value="updateConditionOp(index, $event)"
              >
                <ElOption
                  v-for="operator in row.definition?.operators ?? []"
                  :key="operator.op"
                  :label="operator.opName"
                  :value="operator.op"
                />
              </ElSelect>

              <ElSelect
                v-if="row.valueType === 'ENUM'"
                :model-value="row.condition.value"
                :multiple="row.multiValue"
                class="!w-full"
                collapse-tags
                collapse-tags-tooltip
                placeholder="选择值"
                @update:model-value="updateConditionValue(index, $event)"
              >
                <ElOption
                  v-for="option in row.options"
                  :key="String(option.value)"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
              <ElSelect
                v-else-if="row.valueType === 'BOOLEAN'"
                :model-value="row.condition.value"
                class="!w-full"
                placeholder="选择值"
                @update:model-value="updateConditionValue(index, $event)"
              >
                <ElOption
                  v-for="option in BOOLEAN_OPTIONS"
                  :key="String(option.value)"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
              <ElInputNumber
                v-else-if="row.valueType === 'NUMBER'"
                :controls="false"
                :model-value="row.condition.value"
                class="!w-full"
                placeholder="填数值"
                @update:model-value="updateConditionValue(index, $event)"
              />
              <ElInput
                v-else
                :model-value="row.condition.value"
                class="!w-full"
                placeholder="填内容"
                @update:model-value="updateConditionValue(index, $event)"
              />

              <ElButton
                link
                title="删除这条条件"
                type="danger"
                @click="removeCondition(index)"
              >
                <IconifyIcon class="size-4" icon="lucide:trash-2" />
              </ElButton>
            </div>
            <div
              v-if="row.definition?.description"
              class="text-muted-foreground mt-1 pr-8 text-xs leading-5"
            >
              {{ row.definition.description }}
            </div>
          </div>
          <ElButton
            :disabled="conditionFields.length === 0"
            class="mt-2"
            link
            type="primary"
            @click="addCondition"
          >
            添加条件
          </ElButton>
        </section>

        <section v-if="sortFields.length > 0" class="min-w-0">
          <div class="text-foreground mb-2 text-xs font-medium">
            排序链（排在前面的先比，比不出高下才看下一条）
          </div>
          <div
            v-if="sortRows.length === 0"
            class="text-muted-foreground border-border rounded border border-dashed px-3 py-2 text-xs"
          >
            没有排序，候选按稳定顺序取第一个
          </div>
          <div
            v-for="(row, index) in sortRows"
            :key="index"
            class="border-border border-t py-2 first:border-t-0"
          >
            <div
              class="grid grid-cols-[24px_minmax(0,1fr)_96px_84px] items-center gap-x-2"
            >
              <span class="text-muted-foreground text-xs">{{ index + 1 }}</span>

              <ElSelect
                :model-value="row.sort.field"
                class="!w-full"
                placeholder="选择字段"
                @update:model-value="patchSort(index, { field: $event })"
              >
                <ElOption
                  v-for="field in availableSortFields(index)"
                  :key="field.field"
                  :label="field.displayName"
                  :value="field.field"
                />
              </ElSelect>

              <ElSelect
                :model-value="row.sort.order"
                class="!w-full"
                placeholder="方向"
                @update:model-value="patchSort(index, { order: $event })"
              >
                <ElOption
                  v-for="order in sortOrders"
                  :key="order.value"
                  :label="order.label"
                  :value="order.value"
                />
              </ElSelect>

              <div class="flex items-center justify-end gap-1">
                <ElButton
                  :disabled="index === 0"
                  link
                  title="上移一位"
                  type="primary"
                  @click="moveSort(index, -1)"
                >
                  <IconifyIcon class="size-4" icon="lucide:arrow-up" />
                </ElButton>
                <ElButton
                  :disabled="index === sortRows.length - 1"
                  link
                  title="下移一位"
                  type="primary"
                  @click="moveSort(index, 1)"
                >
                  <IconifyIcon class="size-4" icon="lucide:arrow-down" />
                </ElButton>
                <ElButton
                  link
                  title="删除这条排序"
                  type="danger"
                  @click="removeSort(index)"
                >
                  <IconifyIcon class="size-4" icon="lucide:trash-2" />
                </ElButton>
              </div>
            </div>
            <div
              v-if="row.definition?.description"
              class="text-muted-foreground mt-1 pl-6 pr-8 text-xs leading-5"
            >
              {{ row.definition.description }}
            </div>
          </div>
          <ElButton
            :disabled="availableSortFields(-1).length === 0"
            class="mt-2"
            link
            type="primary"
            @click="addSort"
          >
            添加排序
          </ElButton>
        </section>
      </div>
    </template>
  </div>
</template>
