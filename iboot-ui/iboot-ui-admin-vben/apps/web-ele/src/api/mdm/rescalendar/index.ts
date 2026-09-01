import type { Dayjs } from 'dayjs';

import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmResCalendarApi {
  /** 资源日历信息 */
  export interface ResCalendar {
    id: number; // 主键
    resId?: number; // 资源编号
    resCode?: string; // 资源编码
    resName?: string; // 资源名称
    workDate?: Dayjs | string; // 工作日期
    shiftId?: number; // 班次编号
    shiftCode?: string; // 班次编码
    shiftName?: string; // 班次名称
    availableMinutes?: number; // 可用产能分钟
    remark: string; // 备注
  }

  /** 批量排班请求 */
  export interface BatchSaveReq {
    resIds: number[]; // 资源编号列表
    dateFrom: string; // 工作日期开始
    dateTo: string; // 工作日期结束
    weekdays?: number[]; // 参与排班的星期，1 表示周一、7 表示周日；为空表示每天都排
    shiftId: number; // 班次编号
    availableMinutes: number; // 可用产能分钟
    remark?: string; // 备注
  }

  /** 批量排班结果 */
  export interface BatchSaveResp {
    createCount: number; // 新增条数
    updateCount: number; // 覆盖更新条数
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

/** 查询资源日历列表（按日期区间，供日历视图加载） */
export function getResCalendarList(params: {
  dateFrom: string;
  dateTo: string;
  resIds?: number[];
}) {
  const { dateFrom, dateTo, resIds } = params;
  return requestClient.get<MdmResCalendarApi.ResCalendar[]>(
    '/mdm/res-calendar/list',
    {
      params: {
        dateFrom,
        dateTo,
        ...(resIds && resIds.length > 0 ? { resIds: resIds.join(',') } : {}),
      },
    },
  );
}

/** 新增资源日历 */
export function createResCalendar(data: MdmResCalendarApi.ResCalendar) {
  return requestClient.post('/mdm/res-calendar/create', data);
}

/** 批量排班：按资源与日期区间逐日写入，已存在的同资源同日期记录整体覆盖 */
export function saveResCalendarBatch(data: MdmResCalendarApi.BatchSaveReq) {
  return requestClient.post<MdmResCalendarApi.BatchSaveResp>(
    '/mdm/res-calendar/save-batch',
    data,
  );
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
