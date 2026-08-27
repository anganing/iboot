import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MdmResCalendarApi {
  /** 资源日历信息 */
  export interface ResCalendar {
    id: number; // 主键
    resId?: number; // 资源编号
    resCode?: string; // 资源编码
    resName?: string; // 资源名称
    workDate?: string | Dayjs; // 工作日期
    shiftId?: number; // 班次编号
    shiftCode?: string; // 班次编码
    shiftName?: string; // 班次名称
    availableMinutes?: number; // 可用产能分钟
    remark: string; // 备注
  }
}

/** 查询资源日历分页 */
export function getResCalendarPage(params: PageParam) {
  return requestClient.get<PageResult<MdmResCalendarApi.ResCalendar>>(
    '/mdm/res-calendar/page',
    { params },
  );
}

/** 查询资源日历详情 */
export function getResCalendar(id: number) {
  return requestClient.get<MdmResCalendarApi.ResCalendar>(
    `/mdm/res-calendar/get?id=${id}`,
  );
}

/** 新增资源日历 */
export function createResCalendar(data: MdmResCalendarApi.ResCalendar) {
  return requestClient.post('/mdm/res-calendar/create', data);
}

/** 修改资源日历 */
export function updateResCalendar(data: MdmResCalendarApi.ResCalendar) {
  return requestClient.put('/mdm/res-calendar/update', data);
}

/** 删除资源日历 */
export function deleteResCalendar(id: number) {
  return requestClient.delete(`/mdm/res-calendar/delete?id=${id}`);
}

/** 批量删除资源日历 */
export function deleteResCalendarList(ids: number[]) {
  return requestClient.delete(
    `/mdm/res-calendar/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出资源日历 */
export function exportResCalendar(params: any) {
  return requestClient.download('/mdm/res-calendar/export-excel', { params });
}
