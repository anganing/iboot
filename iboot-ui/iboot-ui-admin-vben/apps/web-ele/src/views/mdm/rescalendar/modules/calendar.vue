<script lang="ts" setup>
import type {
  CalendarOptions,
  DatesSetArg,
  EventClickArg,
  EventInput,
} from '@fullcalendar/core';
import type { DateClickArg } from '@fullcalendar/interaction';

import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { computed, ref } from 'vue';

import { useAccess } from '@vben/access';

import zhCnLocale from '@fullcalendar/core/locales/zh-cn';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import FullCalendar from '@fullcalendar/vue3';
import { ElButton, ElCard } from 'element-plus';

import { getResCalendarList } from '#/api/mdm/rescalendar';
import { RelationSelect } from '#/components/relation-select';

import { RES_RELATION_PROPS } from '../../relation-config';
import { RES_CALENDAR_EVENT_COLORS } from '../data';

defineOptions({ name: 'MdmResCalendarCalendar' });

const emit = defineEmits<{
  create: [workDate: string];
  edit: [id: number];
}>();

const { hasAccessByCodes } = useAccess();

const loading = ref(false);
const events = ref<EventInput[]>([]);
const filterResIds = ref<number[]>([]);
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
  loadEvents();
}

/** 点击空白日期：新增并预填工作日期 */
function handleDateClick(arg: DateClickArg) {
  if (!canCreate.value) {
    return;
  }
  emit('create', arg.dateStr);
}

/** 点击事件：编辑 */
function handleEventClick(arg: EventClickArg) {
  emit('edit', Number(arg.event.id));
}

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: zhCnLocale,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: '',
  },
  height: 'auto',
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

defineExpose({ refresh });
</script>

<template>
  <ElCard v-loading="loading" shadow="never">
    <template #header>
      <div class="flex flex-wrap items-center gap-2">
        <div class="w-[280px]">
          <RelationSelect
            v-model="filterResIds"
            v-bind="resSelectProps"
            @change="loadEvents"
          />
        </div>
        <ElButton
          v-if="canCreate"
          type="primary"
          @click="emit('create', '')"
        >
          新增资源日历
        </ElButton>
        <ElButton @click="loadEvents">刷新</ElButton>
        <span class="text-foreground/60 text-xs">
          点击空白日期可快速新增，点击排班可编辑
        </span>
      </div>
    </template>
    <FullCalendar :options="calendarOptions">
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
