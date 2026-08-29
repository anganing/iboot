import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmCalendarApi {
  /** 日历主档信息 */
  export interface Calendar {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark?: string; // 备注
  }

  /** 日历常规规则信息 */
  export interface CalendarRule {
    id: number; // 主键
    calendarId: number; // 日历编号
    dayOfWeek?: number; // 星期几，1-7
    shiftId?: number; // 班次编号
    shiftCode?: string; // 班次编码
    shiftName?: string; // 班次名称
    availableMinutes?: number; // 可用分钟数
  }

  /** 日历例外信息 */
  export interface CalendarException {
    id: number; // 主键
    calendarId: number; // 日历编号
    exceptionDate?: string; // 例外日期
    exceptionType?: string; // 例外类型
    shiftId?: number; // 班次编号
    shiftCode?: string; // 班次编码
    shiftName?: string; // 班次名称
    availableMinutes?: number; // 可用分钟数
  }
}

// ==================== 日历主档 ====================

/** 查询日历分页 */
export function getCalendarPage(params: PageParam) {
  return requestClient.get<PageResult<MdmCalendarApi.Calendar>>(
    '/mdm/calendar/page',
    { params },
  );
}

/** 查询日历详情 */
export function getCalendar(id: number) {
  return requestClient.get<MdmCalendarApi.Calendar>(
    `/mdm/calendar/get?id=${id}`,
  );
}

/** 新增日历 */
export function createCalendar(data: MdmCalendarApi.Calendar) {
  return requestClient.post('/mdm/calendar/create', data);
}

/** 修改日历 */
export function updateCalendar(data: MdmCalendarApi.Calendar) {
  return requestClient.put('/mdm/calendar/update', data);
}

/** 删除日历 */
export function deleteCalendar(id: number) {
  return requestClient.delete(`/mdm/calendar/delete?id=${id}`);
}

/** 批量删除日历 */
export function deleteCalendarList(ids: number[]) {
  return requestClient.delete(`/mdm/calendar/delete-list?ids=${ids.join(',')}`);
}

/** 导出日历 */
export function exportCalendar(params: any) {
  return requestClient.download('/mdm/calendar/export-excel', { params });
}

// ==================== 常规规则 ====================

/** 查询日历规则分页 */
export function getCalendarRulePage(
  params: PageParam & { calendarId: number },
) {
  return requestClient.get<PageResult<MdmCalendarApi.CalendarRule>>(
    '/mdm/calendar/rule/page',
    { params },
  );
}

/** 新增日历规则 */
export function createCalendarRule(data: MdmCalendarApi.CalendarRule) {
  return requestClient.post('/mdm/calendar/rule/create', data);
}

/** 修改日历规则 */
export function updateCalendarRule(data: MdmCalendarApi.CalendarRule) {
  return requestClient.put('/mdm/calendar/rule/update', data);
}

/** 删除日历规则 */
export function deleteCalendarRule(id: number) {
  return requestClient.delete(`/mdm/calendar/rule/delete?id=${id}`);
}

// ==================== 日期例外 ====================

/** 查询日历例外分页 */
export function getCalendarExceptionPage(
  params: PageParam & { calendarId: number },
) {
  return requestClient.get<PageResult<MdmCalendarApi.CalendarException>>(
    '/mdm/calendar/exception/page',
    { params },
  );
}

/** 新增日历例外 */
export function createCalendarException(
  data: MdmCalendarApi.CalendarException,
) {
  return requestClient.post('/mdm/calendar/exception/create', data);
}

/** 修改日历例外 */
export function updateCalendarException(
  data: MdmCalendarApi.CalendarException,
) {
  return requestClient.put('/mdm/calendar/exception/update', data);
}

/** 删除日历例外 */
export function deleteCalendarException(id: number) {
  return requestClient.delete(`/mdm/calendar/exception/delete?id=${id}`);
}

// ==================== 资源日历生成 ====================

/** 按日历模板生成资源日历 */
export function generateResCalendars(data: {
  calendarId: number;
  dateFrom: string;
  dateTo: string;
  resIds: number[];
}) {
  return requestClient.post<number>('/mdm/calendar/generate', data);
}
