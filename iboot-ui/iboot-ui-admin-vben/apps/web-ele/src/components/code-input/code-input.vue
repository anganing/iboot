<script lang="ts" setup>
import { ElButton, ElInput } from 'element-plus';

import { generateCode } from '#/api/infra/code-rule/generate';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
    ruleCode: string;
    disabled?: boolean;
  }>(),
  {
    modelValue: '',
    placeholder: '请输入编码',
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

async function handleGenerate() {
  emit('update:modelValue', await generateCode(props.ruleCode));
}
</script>

<template>
  <div class="flex w-full gap-2">
    <ElInput
      :disabled="disabled"
      :model-value="modelValue"
      :placeholder="placeholder"
      @update:model-value="emit('update:modelValue', $event)"
    />
    <ElButton :disabled="disabled" title="获取编码" @click="handleGenerate">获取</ElButton>
  </div>
</template>
