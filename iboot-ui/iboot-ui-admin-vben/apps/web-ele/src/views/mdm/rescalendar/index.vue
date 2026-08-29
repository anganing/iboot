<script lang="ts" setup>
import { ref, useTemplateRef } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { ElTabPane, ElTabs } from 'element-plus';

import CalendarView from './modules/calendar.vue';
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

/** 新增：日历传入的日期作为工作日期默认值 */
function handleCreate(workDate?: string) {
  formModalApi.setData(workDate ? { workDate } : null).open();
}

/** 编辑 */
function handleEdit(id: number) {
  formModalApi.setData({ id }).open();
}

/** 保存成功后两个 Tab 都刷新，避免数据陈旧 */
function handleSuccess() {
  calendarRef.value?.refresh();
  listRef.value?.refresh();
}
</script>

<template>
  <Page auto-content-height content-class="flex flex-col overflow-hidden">
    <FormModal @success="handleSuccess" />
    <ElTabs
      v-model="activeTabName"
      class="flex min-h-0 flex-1 flex-col overflow-hidden [&_.el-tab-pane]:h-full [&_.el-tabs__content]:min-h-0 [&_.el-tabs__content]:flex-1 [&_.el-tabs__header]:shrink-0"
    >
      <ElTabPane class="h-full overflow-auto" name="calendar" label="日历">
        <CalendarView
          ref="calendarRef"
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