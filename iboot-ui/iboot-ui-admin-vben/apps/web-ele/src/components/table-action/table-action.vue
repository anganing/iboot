<!-- add by 星语：参考 vben2 的方式，增加 TableAction 组件 -->
<script setup lang="ts">
// TODO @xingyu：要不要和 apps/web-antd/src/components/table-action/table-action.vue 代码风格，进一步风格对齐？现在每个方法，会有一些差异
import type { PropType } from 'vue';

import type { ActionItem, PopConfirm } from './typing';

import { computed, toRaw } from 'vue';

import { useAccess } from '@vben/access';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { isBoolean, isFunction } from '@vben/utils';

import {
  ElButton,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElPopconfirm,
  ElSpace,
  ElTooltip,
} from 'element-plus';

const props = defineProps({
  actions: {
    type: Array as PropType<ActionItem[]>,
    default() {
      return [];
    },
  },
  dropDownActions: {
    type: Array as PropType<ActionItem[]>,
    default() {
      return [];
    },
  },
  divider: {
    type: Boolean,
    default: true,
  },
});

const { hasAccessByCodes } = useAccess();

/** 检查是否显示 */
function isIfShow(action: ActionItem): boolean {
  const ifShow = action.ifShow;
  let isIfShow = true;
  if (isBoolean(ifShow)) {
    isIfShow = ifShow;
  }
  if (isFunction(ifShow)) {
    isIfShow = ifShow(action);
  }
  if (isIfShow) {
    isIfShow =
      hasAccessByCodes(action.auth || []) || (action.auth || []).length === 0;
  }
  return isIfShow;
}

/** 处理按钮 actions */
const getActions = computed(() => {
  return (toRaw(props.actions) || [])
    .filter((action) => {
      return (
        (hasAccessByCodes(action.auth || []) ||
          (action.auth || []).length === 0) &&
        isIfShow(action)
      );
    })
    .map((action) => {
      const { popConfirm } = action;
      return {
        type: action.type || 'primary',
        ...action,
        ...popConfirm,
        onConfirm: popConfirm?.confirm,
        onCancel: popConfirm?.cancel,
        enable: !!popConfirm,
      };
    });
});

const getDropdownList = computed((): any[] => {
  return (toRaw(props.dropDownActions) || [])
    .filter((action) => {
      return (
        (hasAccessByCodes(action.auth || []) ||
          (action.auth || []).length === 0) &&
        isIfShow(action)
      );
    })
    .map((action, index) => {
      const { label, popConfirm } = action;
      return {
        ...action,
        ...popConfirm,
        onConfirm: popConfirm?.confirm,
        onCancel: popConfirm?.cancel,
        text: label,
        divider:
          index < props.dropDownActions.length - 1 ? props.divider : false,
      };
    });
});

function getPopConfirmProps(attrs: PopConfirm) {
  const originAttrs: any = attrs;
  delete originAttrs.icon;
  if (attrs.confirm && isFunction(attrs.confirm)) {
    originAttrs.onConfirm = attrs.confirm;
    delete originAttrs.confirm;
  }
  if (attrs.cancel && isFunction(attrs.cancel)) {
    originAttrs.onCancel = attrs.cancel;
    delete originAttrs.cancel;
  }
  return originAttrs;
}

function getButtonProps(action: ActionItem) {
  const res = {
    type: action.type || 'primary',
    ...action,
  };
  delete res.icon;
  return res;
}

function handleMenuClick(command: any) {
  const action = getDropdownList.value[command];
  if (action.onClick && isFunction(action.onClick)) {
    action.onClick();
  }
}
</script>

<template>
  <div class="table-actions">
    <ElSpace
      :size="
        getActions?.some((item: ActionItem) => item.type === 'text') ? 0 : 8
      "
    >
      <template v-for="(action, index) in getActions" :key="index">
        <ElPopconfirm
          v-if="action.popConfirm"
          v-bind="getPopConfirmProps(action.popConfirm)"
        >
          <template v-if="action.popConfirm.icon" #icon>
            <IconifyIcon :icon="action.popConfirm.icon" />
          </template>
          <template #reference>
            <ElTooltip
              v-if="
                action.tooltip &&
                ((typeof action.tooltip === 'string' && action.tooltip) ||
                  (typeof action.tooltip === 'object' &&
                    action.tooltip.content))
              "
              v-bind="
                typeof action.tooltip === 'string'
                  ? { content: action.tooltip }
                  : { ...action.tooltip }
              "
            >
              <ElButton v-bind="getButtonProps(action)">
                <template v-if="action.icon">
                  <IconifyIcon :icon="action.icon" class="mr-1" />
                </template>
                {{ action.label }}
              </ElButton>
            </ElTooltip>
            <ElButton v-else v-bind="getButtonProps(action)">
              <template v-if="action.icon">
                <IconifyIcon :icon="action.icon" class="mr-1" />
              </template>
              {{ action.label }}
            </ElButton>
          </template>
        </ElPopconfirm>
        <ElTooltip
          v-else-if="
            action.tooltip &&
            ((typeof action.tooltip === 'string' && action.tooltip) ||
              (typeof action.tooltip === 'object' && action.tooltip.content))
          "
          v-bind="
            typeof action.tooltip === 'string'
              ? { content: action.tooltip }
              : { ...action.tooltip }
          "
        >
          <!-- 禁用按钮不派发鼠标事件，tooltip 会永远不触发，所以套一层 span 承接悬停；
               disabled 仍留在 button 上，保留原生禁用样式与不可点击语义 -->
          <span :class="{ 'tooltip-anchor-disabled': action.disabled }">
            <ElButton v-bind="getButtonProps(action)" @click="action.onClick">
              <template v-if="action.icon">
                <IconifyIcon :icon="action.icon" class="mr-1" />
              </template>
              {{ action.label }}
            </ElButton>
          </span>
        </ElTooltip>
        <ElButton
          v-else
          v-bind="getButtonProps(action)"
          @click="action.onClick"
        >
          <template v-if="action.icon">
            <IconifyIcon :icon="action.icon" class="mr-1" />
          </template>
          {{ action.label }}
        </ElButton>
      </template>
    </ElSpace>

    <ElDropdown v-if="getDropdownList.length > 0" @command="handleMenuClick">
      <slot name="more">
        <!-- 固定 primary：原先取下拉第一项的 type，导致"更多"在不同页面颜色不同 -->
        <ElButton type="primary" link>
          {{ $t('page.action.more') }}
          <IconifyIcon icon="lucide:ellipsis-vertical" class="ml-1" />
        </ElButton>
      </slot>
      <template #dropdown>
        <ElDropdownMenu>
          <ElDropdownItem
            v-for="(action, index) in getDropdownList"
            :key="index"
            :command="index"
            :disabled="action.disabled"
          >
            <template v-if="action.popConfirm">
              <ElPopconfirm v-bind="getPopConfirmProps(action.popConfirm)">
                <template v-if="action.popConfirm.icon" #icon>
                  <IconifyIcon :icon="action.popConfirm.icon" />
                </template>
                <template #reference>
                  <div>
                    <IconifyIcon v-if="action.icon" :icon="action.icon" />
                    <span :class="action.icon ? 'ml-1' : ''">
                      {{ action.text }}
                    </span>
                  </div>
                </template>
              </ElPopconfirm>
            </template>
            <template v-else>
              <!-- 下拉项是 li，禁用态只有 cursor:not-allowed、没有 pointer-events:none，
                   所以悬停能命中，直接套 ElTooltip 即可，不需要 span 承接 -->
              <ElTooltip
                v-if="
                  action.tooltip &&
                  ((typeof action.tooltip === 'string' && action.tooltip) ||
                    (typeof action.tooltip === 'object' &&
                      action.tooltip.content))
                "
                placement="left"
                v-bind="
                  typeof action.tooltip === 'string'
                    ? { content: action.tooltip }
                    : { ...action.tooltip, placement: 'left' }
                "
              >
                <div>
                  <IconifyIcon v-if="action.icon" :icon="action.icon" />
                  <span :class="action.icon ? 'ml-1' : ''">
                    {{ action.label }}
                  </span>
                </div>
              </ElTooltip>
              <div v-else>
                <IconifyIcon v-if="action.icon" :icon="action.icon" />
                {{ action.label }}
              </div>
            </template>
          </ElDropdownItem>
        </ElDropdownMenu>
      </template>
    </ElDropdown>
  </div>
</template>
<style lang="scss">
.table-actions {
  .el-button--text {
    padding: 4px;
    margin-left: 0;
  }

  /**
   * 禁用按钮的 tooltip 承载层。
   *
   * 原生 <button disabled> 不派发鼠标事件（浏览器行为，与 CSS 无关），
   * 外层 ElTooltip 因此永远不触发。让按钮穿透指针事件，悬停落到 span 上，
   * 禁用态的按钮也能给出“为何暂不可做”的提示。
   */
  .tooltip-anchor-disabled {
    display: inline-flex;
    cursor: not-allowed;

    .el-button {
      pointer-events: none;
    }
  }

  .el-button .iconify + span,
  .el-button span + .iconify {
    margin-inline-start: 4px;
  }

  .iconify {
    display: inline-flex;
    align-items: center;
    width: 1em;
    height: 1em;
    font-style: normal;
    line-height: 0;
    vertical-align: -0.125em;
    color: inherit;
    text-align: center;
    text-transform: none;
    text-rendering: optimizelegibility;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
}

.el-popconfirm {
  .el-popconfirm__action {
    .el-button {
      margin-left: 8px !important;
    }
  }
}
</style>
