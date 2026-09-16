<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { computed, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import dayjs from 'dayjs';
import {
  ElButton,
  ElButtonGroup,
  ElDatePicker,
  ElSegmented,
  ElTooltip,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getResSimpleList } from '#/api/mdm/res';
import { getResCalendarList } from '#/api/mdm/rescalendar';
import { RelationSelect } from '#/components/relation-select';

import { RES_RELATION_PROPS } from '../../relation-config';
import { SHIFT_CELL_COLORS, SPAN_OPTIONS } from '../data';

defineOptions({ name: 'MdmResCalendarScheduleMatrix' });

const emit = defineEmits<{
  batch: [payload: { dateFrom: string; dateTo: string; resIds: number[] }];
  create: [payload: { resId: number; workDate: string }];
  edit: [id: number];
}>();

/** 排班矩阵的一行：一个生产资源，日期列以 dXXXXXXXX 为字段名挂在行上，值是当天的排班数组 */
interface MatrixRow {
  [dateField: string]: any;
  resCode?: string;
  resId: number;
  resName?: string;
  scheduledDays: number;
  wcName?: string;
}

/** 日期单元格里的排班明细 */
interface MatrixCell {
  availableMinutes?: number;
  color: string;
  id: number;
  shiftName?: string;
}

type SpanMode = 'custom' | 'month' | 'twoWeeks' | 'week';

const WEEKDAY_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

const spanMode = ref<SpanMode>('month');
/** 视图锚点，配合 spanMode 推导出日期区间 */
const anchorDate = ref(dayjs().startOf('month'));
/** 自定义区间，仅 spanMode 为 custom 时生效 */
const customRange = ref<[string, string]>([
  dayjs().format('YYYY-MM-DD'),
  dayjs().add(13, 'day').format('YYYY-MM-DD'),
]);
const filterResIds = ref<number[]>([]);
const today = dayjs().format('YYYY-MM-DD');

const resSelectProps = {
  ...RES_RELATION_PROPS,
  multiple: true,
  placeholder: '按生产资源筛选',
};

/** 当前视图的日期区间 */
const dateRange = computed<{ dateFrom: string; dateTo: string }>(() => {
  if (spanMode.value === 'custom') {
    return { dateFrom: customRange.value[0], dateTo: customRange.value[1] };
  }
  const start = alignAnchor(anchorDate.value, spanMode.value);
  const end =
    spanMode.value === 'month'
      ? start.endOf('month')
      : start.add(spanMode.value === 'week' ? 6 : 13, 'day');
  return {
    dateFrom: start.format('YYYY-MM-DD'),
    dateTo: end.format('YYYY-MM-DD'),
  };
});

/** 区间内的逐日日期，既是列定义也是数据填充的依据 */
const dateList = computed(() => {
  const { dateFrom, dateTo } = dateRange.value;
  const dates: string[] = [];
  let cursor = dayjs(dateFrom);
  const end = dayjs(dateTo);
  while (!cursor.isAfter(end, 'day')) {
    dates.push(cursor.format('YYYY-MM-DD'));
    cursor = cursor.add(1, 'day');
  }
  return dates;
});

const rangeTitle = computed(() => {
  const { dateFrom, dateTo } = dateRange.value;
  return `${dateFrom} ~ ${dateTo}（${dateList.value.length} 天）`;
});

/** 周一为一周之首，避免 dayjs 默认按周日起算导致列头错位 */
function alignAnchor(date: dayjs.Dayjs, mode: SpanMode) {
  if (mode === 'month') {
    return date.startOf('month');
  }
  return date.startOf('day').subtract((date.day() + 6) % 7, 'day');
}

function toDateField(date: string) {
  return `d${date.replaceAll('-', '')}`;
}

function buildColumns(): VxeTableGridOptions<MatrixRow>['columns'] {
  const dateColumns: VxeTableGridOptions<MatrixRow>['columns'] =
    dateList.value.map((date) => {
      const current = dayjs(date);
      const isWeekend = current.day() === 0 || current.day() === 6;
      const isToday = date === today;
      const stateClass = isToday
        ? 'res-matrix-today'
        : isWeekend
          ? 'res-matrix-weekend'
          : '';
      return {
        field: toDateField(date),
        title: `${current.format('MM-DD')} 周${WEEKDAY_LABELS[current.day()]}`,
        width: 88,
        align: 'center',
        className: stateClass,
        headerClassName: stateClass,
        params: { date },
        slots: { default: 'dateCell' },
      };
    });
  return [
    { type: 'checkbox', width: 44, fixed: 'left' },
    { field: 'resCode', title: '资源编码', width: 110, fixed: 'left' },
    { field: 'resName', title: '资源名称', width: 190, fixed: 'left' },
    {
      field: 'scheduledDays',
      title: '已排天数',
      width: 80,
      fixed: 'left',
      align: 'center',
    },
    ...(dateColumns ?? []),
  ];
}

/** 班次配色按班次编号稳定分配，保证同一班次跨行同色 */
function buildShiftColorMap(list: MdmResCalendarApi.ResCalendar[]) {
  const colorMap = new Map<number, string>();
  list.forEach((item) => {
    const shiftId = item.shiftId;
    if (shiftId === undefined || colorMap.has(shiftId)) {
      return;
    }
    colorMap.set(
      shiftId,
      SHIFT_CELL_COLORS[colorMap.size % SHIFT_CELL_COLORS.length]!,
    );
  });
  return colorMap;
}

/** 资源行 + 排班明细一次拉取后在前端 pivot 成矩阵 */
async function loadMatrix(): Promise<MatrixRow[]> {
  const { dateFrom, dateTo } = dateRange.value;
  const [resList, calendarList] = await Promise.all([
    getResSimpleList(),
    getResCalendarList({ dateFrom, dateTo, resIds: filterResIds.value }),
  ]);

  const selectedResIds = new Set(filterResIds.value);
  const rowMap = new Map<number, MatrixRow>();
    resList
    .filter((res) => selectedResIds.size === 0 || selectedResIds.has(res.id))
    .forEach((res) => {
      rowMap.set(res.id, {
        resId: res.id,
        resCode: res.code,
        resName: res.name,
        wcName: res.wcName,
        scheduledDays: 0,
      });
    });

  const colorMap = buildShiftColorMap(calendarList);
  calendarList.forEach((item) => {
    const row = rowMap.get(item.resId!);
    if (!row || !item.workDate) {
      return;
    }
    const field = toDateField(String(item.workDate).slice(0, 10));
    const cells = (row[field] ?? []) as MatrixCell[];
    cells.push({
      id: item.id,
      shiftName: item.shiftName,
      availableMinutes: item.availableMinutes,
      color: colorMap.get(item.shiftId!) ?? SHIFT_CELL_COLORS[0]!,
    });
    // 同一天可能排多个班次，按天计一次已排天数
    if (cells.length === 1) {
      row.scheduledDays += 1;
    }
    row[field] = cells;
  });
  return [...rowMap.values()];
}

const checkedResIds = ref<number[]>([]);
function handleRowCheckboxChange({ records }: { records: MatrixRow[] }) {
  checkedResIds.value = records.map((item) => item.resId);
}

/**
 * 单元格悬停提示内容：一个格子里可能堆多个班次，逐行列出后给出操作提示。
 *
 * 不要用逐条 ElTooltip：30 天 × 37 资源就是上千个组件实例，又因为左侧固定列 vxe 会把整行
 * DOM 复制一份用于裁切，实例数直接翻倍。实测本月视图造出 1642 个 ElTooltip、1.6MB 的 DOM，
 * 首屏主线程连续阻塞 9.8s，是页面卡在加载态的根因。
 * 不用 vxe 自带 tooltip：它要么 showAll=true 时给所有列都强开提示，要么 showAll=false 时
 * 忽略自定义内容，两种都会改变既有列的悬停行为，而浏览器原生 title 零实例且支持换行。
 */
function buildCellTitle(workDate: string, cell: MatrixCell) {
  return `${workDate} ${cell.shiftName} · ${cell.availableMinutes} 分钟（单击编辑）`;
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: buildColumns(),
    height: 'auto',
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          const list = await loadMatrix();
          return { list, total: list.length };
        },
      },
    },
    rowConfig: { keyField: 'resId', isHover: true },
    // 单元格内可能纵向堆叠多个班次条，关闭溢出隐藏让内容完整显示
    showOverflow: false,
    // 本月视图是 30 个日期列 × 全部资源，整表 2500+ 单元格一次性渲染要 5s 以上主线程；
    // vxe 默认列数大于 60 才自动开启横向虚拟滚动，这里强制开启，只渲染可视区的日期列
    virtualXConfig: { enabled: true, gt: 0 },
    toolbarConfig: { custom: false, refresh: true, search: false, zoom: true },
  } as VxeTableGridOptions<MatrixRow>,
  gridEvents: {
    cellClick: handleCellClick,
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 点击空白日期格：带资源与日期进新增；已有排班的格子由班次条自行处理点击 */
function handleCellClick({ row, column }: { column: any; row: MatrixRow }) {
  const workDate = column?.params?.date as string | undefined;
  if (!workDate) {
    return;
  }
  if (((row[column.field] ?? []) as MatrixCell[]).length === 0) {
    emit('create', { resId: row.resId, workDate });
  }
}

/** 区间变化后同步列定义并重新取数 */
async function reload() {
  checkedResIds.value = [];
  gridApi.setGridOptions({ columns: buildColumns() });
  await gridApi.query();
}

function handleSpanChange(mode: any) {
  spanMode.value = mode as SpanMode;
  if (mode !== 'custom') {
    anchorDate.value = alignAnchor(dayjs(), mode as SpanMode);
  }
  reload();
}

/** 按当前跨度整段前后翻页 */
function handleNavigate(step: -1 | 0 | 1) {
  if (spanMode.value === 'custom') {
    const days = dateList.value.length;
    const [start, end] = customRange.value;
    customRange.value =
      step === 0
        ? [today, dayjs(today).add(days - 1, 'day').format('YYYY-MM-DD')]
        : [
            dayjs(start).add(step * days, 'day').format('YYYY-MM-DD'),
            dayjs(end).add(step * days, 'day').format('YYYY-MM-DD'),
          ];
    reload();
    return;
  }
  if (step === 0) {
    anchorDate.value = alignAnchor(dayjs(), spanMode.value);
  } else if (spanMode.value === 'month') {
    anchorDate.value = anchorDate.value.add(step, 'month');
  } else {
    anchorDate.value = anchorDate.value.add(
      step * (spanMode.value === 'week' ? 7 : 14),
      'day',
    );
  }
  reload();
}

/** 批量排班默认带上勾选的资源，未勾选则沿用筛选条件 */
function handleBatch() {
  emit('batch', {
    ...dateRange.value,
    resIds:
      checkedResIds.value.length > 0
        ? checkedResIds.value
        : [...filterResIds.value],
  });
}

defineExpose({ refresh: reload });
</script>

<template>
  <div class="res-matrix box-border h-full pt-2">
    <Grid class="h-full" table-title="资源排班矩阵">
      <template #toolbar-actions>
        <div class="flex flex-wrap items-center gap-2">
          <div class="w-[240px]">
            <RelationSelect
              v-model="filterResIds"
              v-bind="resSelectProps"
              @change="reload"
            />
          </div>
          <ElSegmented
            :model-value="spanMode"
            :options="SPAN_OPTIONS"
            size="small"
            @change="handleSpanChange"
          />
          <ElDatePicker
            v-if="spanMode === 'custom'"
            v-model="customRange"
            class="!w-[260px]"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            size="small"
            start-placeholder="开始日期"
            type="daterange"
            value-format="YYYY-MM-DD"
            @change="reload"
          />
          <ElButtonGroup>
            <ElTooltip content="上一段">
              <ElButton size="small" @click="handleNavigate(-1)">
                <IconifyIcon icon="lucide:chevron-left" />
              </ElButton>
            </ElTooltip>
            <ElButton size="small" @click="handleNavigate(0)">今天</ElButton>
            <ElTooltip content="下一段">
              <ElButton size="small" @click="handleNavigate(1)">
                <IconifyIcon icon="lucide:chevron-right" />
              </ElButton>
            </ElTooltip>
          </ElButtonGroup>
          <span class="text-foreground/60 text-xs">{{ rangeTitle }}</span>
        </div>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '批量排班',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mdm:res-calendar:create'],
              onClick: handleBatch,
            },
          ]"
        />
      </template>
      <template #dateCell="{ row, column }">
        <div
          v-if="row[column.field]?.length"
          class="flex flex-col items-stretch gap-[2px]"
        >
          <div
            v-for="cell in row[column.field]"
            :key="cell.id"
            class="res-matrix-cell"
            :title="buildCellTitle(column.params?.date, cell)"
            :style="{ backgroundColor: cell.color }"
            @click.stop="emit('edit', cell.id)"
          >
            <span class="truncate">{{ cell.shiftName }}</span>
            <span class="shrink-0 opacity-80">
              {{ cell.availableMinutes }}′
            </span>
          </div>
        </div>
        <span v-else class="text-foreground/25">+</span>
      </template>
    </Grid>
  </div>
</template>

<style scoped>
.res-matrix-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 2px 6px;
  color: #fff;
  font-size: 12px;
  line-height: 20px;
  border-radius: 3px;
}

.res-matrix :deep(.res-matrix-weekend) {
  background-color: var(--el-fill-color-light);
}

.res-matrix :deep(.res-matrix-today) {
  background-color: var(--el-color-primary-light-9);
}

.res-matrix :deep(.vxe-body--column) {
  cursor: pointer;
}
</style>
