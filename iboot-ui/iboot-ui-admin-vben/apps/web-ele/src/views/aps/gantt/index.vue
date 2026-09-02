<script lang="ts" setup>
import type { ApsVersionApi } from '#/api/aps/version';

import { computed, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  ElAlert,
  ElMessage,
  ElOption,
  ElSelect,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import {
  getEffectiveVersionList,
  getPublishedVersion,
} from '#/api/aps/version';
import { RelationSelect } from '#/components/relation-select';

import { APS_SCENARIO_RELATION_PROPS } from '../relation-config';
import GanttView from './modules/gantt-view.vue';

/**
 * 生效排程计划页面。
 *
 * <p>面向车间与 PMC：他们只关心"现在生效的计划是什么"，选场景后直接落到该场景已发布的版本，
 * 不必在一堆求解失败、已被替换的版本里自己挑。计划员评审某一轮求解结果走排程版本列表的甘特图入口。
 *
 * <p>渲染逻辑都在 GanttView 里，两个入口共用，避免两处各画一遍。
 */
const scenarioId = ref<number>();
const versionId = ref<number>();
const publishedVersionNo = ref<number>();
const historyMode = ref(false);
const historyVersions = ref<ApsVersionApi.Version[]>([]);

/** 历史版本下拉：带上状态与发布时间，才分得清哪一版是当前生效、哪些是被替换过的 */
const historyOptions = computed(() =>
  historyVersions.value.map((item) => ({
    label: `V${item.versionNo ?? '-'} ${item.status === 'PUBLISHED' ? '生效中' : '已被替换'}${
      item.publishedTime ? ` · ${formatDateTime(item.publishedTime)}` : ''
    }`,
    value: item.id as number,
  })),
);

/** 选场景后带出已发布版本；场景还没发布过计划是正常状态，给提示而不是报错 */
async function handleScenarioChange() {
  historyMode.value = false;
  versionId.value = undefined;
  publishedVersionNo.value = undefined;
  historyVersions.value = [];
  if (!scenarioId.value) {
    return;
  }
  const version = await getPublishedVersion(scenarioId.value);
  if (!version?.id) {
    ElMessage.warning('该排程场景还没有已发布的计划');
    return;
  }
  versionId.value = version.id;
  publishedVersionNo.value = version.versionNo;
}

/**
 * 切换历史模式。
 *
 * <p>打开时只列本场景曾经生效过的版本（已发布与已被替换），关掉就回到当前生效版本，
 * 避免停在某个历史版本上却以为看的是生效计划。
 */
async function handleHistoryModeChange(value: boolean | number | string) {
  if (!value) {
    await handleScenarioChange();
    return;
  }
  if (!scenarioId.value) {
    return;
  }
  historyVersions.value = await getEffectiveVersionList(scenarioId.value);
}
</script>

<template>
  <Page auto-content-height>
    <div class="flex h-full min-h-0 flex-col gap-3">
      <ElAlert
        v-if="historyMode"
        :closable="false"
        title="正在查看历史版本，不是当前生效的计划"
        type="warning"
        class="shrink-0"
      />
      <div class="min-h-0 flex-1">
        <GanttView :version-id="versionId">
          <template #toolbar-prefix>
            <div class="w-[260px]">
              <RelationSelect
                v-model="scenarioId"
                v-bind="APS_SCENARIO_RELATION_PROPS"
                @change="handleScenarioChange"
              />
            </div>
            <ElTooltip
              content="切到历史版本对比，关闭后回到生效计划"
              placement="top"
            >
              <ElSwitch
                v-model="historyMode"
                active-text="历史版本"
                :disabled="!scenarioId"
                size="small"
                @change="handleHistoryModeChange"
              />
            </ElTooltip>
            <!-- el-select 自带 width:100%，只能用行内宽度压住，否则会把工具栏撑成整行 -->
            <ElSelect
              v-if="historyMode"
              v-model="versionId"
              placeholder="请选择历史版本"
              size="small"
              style="width: 260px"
            >
              <ElOption
                v-for="item in historyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
            <span v-else-if="publishedVersionNo" class="text-xs">
              生效版本 V{{ publishedVersionNo }}
            </span>
          </template>
        </GanttView>
      </div>
    </div>
  </Page>
</template>
