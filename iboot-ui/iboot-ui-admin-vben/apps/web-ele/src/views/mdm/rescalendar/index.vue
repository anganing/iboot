<script lang="ts" setup>
import { nextTick, ref, useTemplateRef } from 'vue';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';

import { ElTabPane, ElTabs } from 'element-plus';

import CalendarView from './modules/calendar.vue';
import DateScheduleList from './modules/date-schedule-list.vue';
import Form from './modules/form.vue';
import ListView from './modules/list.vue';

defineOptions({ name: 'MdmResCalendar' });

const activeTabName = ref('calendar');
const calendarRef =
  useTemplateRef<InstanceType<typeof CalendarView>>('calendarRef');
const listRef = useTemplateRef<InstanceType<typeof ListView>>('listRef');

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [DateScheduleDrawer, dateScheduleDrawerApi] = useVbenDrawer({
  connectedComponent: DateScheduleList,
  destroyOnClose: true,
});

/** 新增：日历传入的日期作为工作日期默认值 */
function handleCreate(workDate?: string) {
  dateScheduleDrawerApi.close();
  formModalApi.setData(workDate ? { workDate } : null).open();
}

/** 编辑 */
function handleEdit(id: number) {
  dateScheduleDrawerApi.close();
  formModalApi.setData({ id }).open();
}

/** 查看当天的全部资源安排，由用户选择需要编辑的记录 */
function handleView(workDate: string) {
  dateScheduleDrawerApi.setData({ workDate }).open();
}

/** 保存成功后两个 Tab 都刷新，避免数据陈旧 */
function handleSuccess() {
  calendarRef.value?.refresh();
  listRef.value?.refresh();
}

/** 日历从隐藏的标签页恢复时，主动重算尺寸避免网格错位 */
async function handleTabChange(tabName: string | number) {
  if (tabName !== 'calendar') {
    return;
  }
  await nextTick();
  calendarRef.value?.updateSize();
}
</script>

<template>
  <Page auto-content-height content-class="flex flex-col overflow-hidden">
    <FormModal @success="handleSuccess" />
    <DateScheduleDrawer
      @create="handleCreate"
      @edit="handleEdit"
      @success="handleSuccess"
    />
    <ElTabs
      v-model="activeTabName"
      class="res-calendar-tabs flex min-h-0 flex-1 flex-col overflow-hidden [&_.el-tab-pane]:h-full [&_.el-tabs__content]:min-h-0 [&_.el-tabs__content]:flex-1 [&_.el-tabs__header]:shrink-0"
      @tab-change="handleTabChange"
    >
      <ElTabPane class="h-full overflow-hidden" name="calendar" label="日历">
        <CalendarView
          ref="calendarRef"
          @create="handleCreate"
          @edit="handleEdit"
          @view="handleView"
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
