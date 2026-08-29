<script lang="ts" setup>
import type {
  CalendarOptions,
  DatesSetArg,
  EventClickArg,
  EventInput,
} from '@fullcalendar/core';
import type { DateClickArg } from '@fullcalendar/interaction';

import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { computed, onBeforeUnmount, ref, useTemplateRef } from 'vue';

import { useAccess } from '@vben/access';
import { IconifyIcon } from '@vben/icons';

import zhCnLocale from '@fullcalendar/core/locales/zh-cn';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import FullCalendar from '@fullcalendar/vue3';
import { ElButton, ElButtonGroup, ElCard, ElTooltip } from 'element-plus';

import { getResCalendarList } from '#/api/mdm/rescalendar';
import { RelationSelect } from '#/components/relation-select';

import { RES_RELATION_PROPS } from '../../relation-config';
import { RES_CALENDAR_EVENT_COLORS } from '../data';

defineOptions({ name: 'MdmResCalendarCalendar' });

const emit = defineEmits<{
  create: [workDate: string];
  edit: [id: number];
  view: [workDate: string];
}>();

const { hasAccessByCodes } = useAccess();

const loading = ref(false);
const events = ref<EventInput[]>([]);
const filterResIds = ref<number[]>([]);
const calendarTitle = ref('');
const calendarRef =
  useTemplateRef<InstanceType<typeof FullCalendar>>('calendarRef');
let dateClickTimer: ReturnType<typeof setTimeout> | undefined;
let eventClickTimer: ReturnType<typeof setTimeout> | undefined;
/** 当前视图的日期区间，由 datesSet 维护 */
const viewRange = ref<{ dateFrom: string; dateTo: string }>();

const canCreate = computed(() =>
  hasAccessByCodes(['mdm:res-calendar:create']),
);

const resSelectProps = {
  ...RES_RELATION_PROPS,
  multiple: true,
  placeholder: '按生产资源筛选',
};

/** 按资源在结果集中的出现顺序循环取色 */
function buildResColorMap(list: MdmResCalendarApi.ResCalendar[]) {
  const colorMap = new Map<number, string>();
  list.forEach((item) => {
    const resId = item.resId;
    if (resId === undefined || colorMap.has(resId)) {
      return;
    }
    colorMap.set(
      resId,
      RES_CALENDAR_EVENT_COLORS[
        colorMap.size % RES_CALENDAR_EVENT_COLORS.length
      ]!,
    );
  });
  return colorMap;
}

/** 加载当前视图区间的资源日历 */
async function loadEvents() {
  if (!viewRange.value) {
    return;
  }
  loading.value = true;
  try {
    const list = await getResCalendarList({
      dateFrom: viewRange.value.dateFrom,
      dateTo: viewRange.value.dateTo,
      resIds: filterResIds.value,
    });
    const colorMap = buildResColorMap(list);
    events.value = list.map((item) => {
      const color = colorMap.get(item.resId!);
      return {
        id: String(item.id),
        start: item.workDate as string,
        allDay: true,
        title: `${item.resCode ?? ''} · ${item.shiftName ?? ''}`,
        backgroundColor: color,
        borderColor: color,
        extendedProps: {
          resCode: item.resCode,
          resName: item.resName,
          shiftName: item.shiftName,
          availableMinutes: item.availableMinutes,
        },
      } as EventInput;
    });
  } finally {
    loading.value = false;
  }
}

/** 视图区间变化（初始化、切月、今天） */
function handleDatesSet(arg: DatesSetArg) {
  viewRange.value = {
    dateFrom: arg.startStr.slice(0, 10),
    dateTo: arg.endStr.slice(0, 10),
  };
  calendarTitle.value = arg.view.title;
  loadEvents();
}

/** 切换日历月份或回到今天 */
function handleNavigate(action: 'next' | 'prev' | 'today') {
  const calendarApi = calendarRef.value?.getApi();
  if (!calendarApi) {
    return;
  }
  if (action === 'prev') {
    calendarApi.prev();
  } else if (action === 'next') {
    calendarApi.next();
  } else {
    calendarApi.today();
  }
}

/** 单击日期查看当天安排，双击空白日期新增并预填工作日期 */
function handleDateClick(arg: DateClickArg) {
  if (arg.jsEvent.detail === 2) {
    if (dateClickTimer) {
      clearTimeout(dateClickTimer);
      dateClickTimer = undefined;
    }
    if (canCreate.value) {
      emit('create', arg.dateStr);
    }
    return;
  }
  dateClickTimer = setTimeout(() => {
    emit('view', arg.dateStr);
    dateClickTimer = undefined;
  }, 220);
}

/** 单击排班查看当天安排，双击排班直接编辑 */
function handleEventClick(arg: EventClickArg) {
  if (arg.jsEvent.detail === 2) {
    if (eventClickTimer) {
      clearTimeout(eventClickTimer);
      eventClickTimer = undefined;
    }
    emit('edit', Number(arg.event.id));
    return;
  }
  eventClickTimer = setTimeout(() => {
    emit('view', arg.event.startStr);
    eventClickTimer = undefined;
  }, 220);
}

onBeforeUnmount(() => {
  if (dateClickTimer) {
    clearTimeout(dateClickTimer);
  }
  if (eventClickTimer) {
    clearTimeout(eventClickTimer);
  }
});

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: zhCnLocale,
  headerToolbar: false,
  height: '100%',
  dayMaxEvents: 3,
  selectable: false,
  editable: false,
  events: events.value,
  datesSet: handleDatesSet,
  dateClick: handleDateClick,
  eventClick: handleEventClick,
}));

/** 供父级刷新 */
function refresh() {
  loadEvents();
}

/** 日历标签重新显示后重算网格尺寸 */
function updateSize() {
  calendarRef.value?.getApi().updateSize();
}

defineExpose({ refresh, updateSize });
</script>

<template>
  <ElCard
    v-loading="loading"
    class="res-calendar-card flex h-full flex-col"
    shadow="never"
  >
    <template #header>
      <div class="grid items-center gap-3 lg:grid-cols-[minmax(280px,1fr)_auto_minmax(280px,1fr)]">
        <div class="w-[280px]">
          <RelationSelect
            v-model="filterResIds"
            v-bind="resSelectProps"
            @change="loadEvents"
          />
        </div>
        <div class="flex items-center justify-center gap-3">
          <ElButtonGroup>
            <ElTooltip content="上个月">
              <ElButton circle @click="handleNavigate('prev')">
                <IconifyIcon icon="lucide:chevron-left" />
              </ElButton>
            </ElTooltip>
            <ElTooltip content="回到今天">
              <ElButton @click="handleNavigate('today')">今天</ElButton>
            </ElTooltip>
            <ElTooltip content="下个月">
              <ElButton circle @click="handleNavigate('next')">
                <IconifyIcon icon="lucide:chevron-right" />
              </ElButton>
            </ElTooltip>
          </ElButtonGroup>
          <span class="text-lg font-medium">{{ calendarTitle }}</span>
        </div>
        <div class="flex items-center justify-start gap-2 lg:justify-end">
          <span class="text-foreground/60 text-xs">
            单击日期查看安排，双击空白日期新建，双击排班编辑
          </span>
          <ElTooltip content="刷新日历">
            <ElButton circle :loading="loading" @click="loadEvents">
              <IconifyIcon icon="lucide:refresh-cw" />
            </ElButton>
          </ElTooltip>
        </div>
      </div>
    </template>
    <FullCalendar ref="calendarRef" :options="calendarOptions">
      <template #eventContent="{ event }">
        <div class="truncate px-1 text-xs leading-5">
          {{ event.extendedProps.resCode }} ·
          {{ event.extendedProps.shiftName }} ·
          {{ event.extendedProps.availableMinutes }} 分钟
        </div>
      </template>
    </FullCalendar>
  </ElCard>
</template>

<style scoped>
.res-calendar-card :deep(.fc) {
  --fc-border-color: var(--el-border-color-lighter);
  --fc-page-bg-color: var(--el-bg-color);
  --fc-neutral-bg-color: var(--el-fill-color-light);
  --fc-today-bg-color: var(--el-color-primary-light-9);
}

.res-calendar-card :deep(.el-card__body) {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}

.res-calendar-card :deep(.fc) {
  min-height: 0;
  flex: 1;
}

.res-calendar-card :deep(.fc-col-header-cell-cushion) {
  padding: 8px 4px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.res-calendar-card :deep(.fc-daygrid-day-number) {
  padding: 6px 8px;
  color: var(--el-text-color-regular);
}

.res-calendar-card :deep(.fc-daygrid-event) {
  border-radius: 3px;
}
</style>
