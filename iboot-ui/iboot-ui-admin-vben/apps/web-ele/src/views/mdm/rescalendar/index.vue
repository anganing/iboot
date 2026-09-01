<script lang="ts" setup>
import { ref, useTemplateRef } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { ElTabPane, ElTabs } from 'element-plus';

import BatchForm from './modules/batch-form.vue';
import Form from './modules/form.vue';
import ListView from './modules/list.vue';
import ScheduleMatrix from './modules/schedule-matrix.vue';

defineOptions({ name: 'MdmResCalendar' });

const activeTabName = ref('matrix');
const matrixRef =
  useTemplateRef<InstanceType<typeof ScheduleMatrix>>('matrixRef');
const listRef = useTemplateRef<InstanceType<typeof ListView>>('listRef');

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [BatchFormModal, batchFormModalApi] = useVbenModal({
  connectedComponent: BatchForm,
  destroyOnClose: true,
});

/** 新增：矩阵传入的资源与日期作为默认值 */
function handleCreate(payload?: { resId?: number; workDate?: string }) {
  formModalApi.setData(payload ?? null).open();
}

/** 编辑 */
function handleEdit(id: number) {
  formModalApi.setData({ id }).open();
}

/** 批量排班：默认带上矩阵当前的日期区间与勾选资源 */
function handleBatch(payload: {
  dateFrom: string;
  dateTo: string;
  resIds: number[];
}) {
  batchFormModalApi.setData(payload).open();
}

/** 保存成功后两个 Tab 都刷新，避免数据陈旧 */
function handleSuccess() {
  matrixRef.value?.refresh();
  listRef.value?.refresh();
}
</script>

<template>
  <Page auto-content-height content-class="flex flex-col overflow-hidden">
    <FormModal @success="handleSuccess" />
    <BatchFormModal @success="handleSuccess" />
    <ElTabs
      v-model="activeTabName"
      class="res-calendar-tabs flex min-h-0 flex-1 flex-col overflow-hidden [&_.el-tab-pane]:h-full [&_.el-tabs__content]:min-h-0 [&_.el-tabs__content]:flex-1 [&_.el-tabs__header]:shrink-0"
    >
      <ElTabPane class="h-full overflow-hidden" name="matrix" label="排班矩阵">
        <ScheduleMatrix
          ref="matrixRef"
          @batch="handleBatch"
          @create="handleCreate"
          @edit="handleEdit"
        />
      </ElTabPane>
      <ElTabPane class="h-full" name="list" label="列表" lazy>
        <ListView ref="listRef" @create="handleCreate" @edit="handleEdit" />
      </ElTabPane>
    </ElTabs>
  </Page>
</template>

<style scoped>
.res-calendar-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}
</style>
