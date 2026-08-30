import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsShortageApi {
  /** APS-排程缺料清单信息 */
  export interface Shortage {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    prodOrderId?: number; // 生产工单编号，0表示汇总缺料
    opTaskId?: number; // 工序任务编号，0表示非工序级缺料
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    matId?: number; // 短缺物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    reqQty?: number; // 需求数量
    availQty?: number; // 可用数量
    shortQty?: number; // 短缺数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    needDate?: string | Dayjs; // 需求到位时间
    expectedSupplyDate: string | Dayjs; // 预计供给时间
    status?: string; // 状态：OPEN、PARTIAL、RESOLVED、IGNORED
    remark: string; // 备注
  }
}

/** 查询APS-排程缺料清单分页 */
export function getShortagePage(params: PageParam) {
  return requestClient.get<PageResult<ApsShortageApi.Shortage>>(
    '/aps/shortage/page',
    { params },
  );
}

/** 查询APS-排程缺料清单详情 */
export function getShortage(id: number) {
  return requestClient.get<ApsShortageApi.Shortage>(
    `/aps/shortage/get?id=${id}`,
  );
}

/** 新增APS-排程缺料清单 */
export function createShortage(data: ApsShortageApi.Shortage) {
  return requestClient.post('/aps/shortage/create', data);
}

/** 修改APS-排程缺料清单 */
export function updateShortage(data: ApsShortageApi.Shortage) {
  return requestClient.put('/aps/shortage/update', data);
}

/** 删除APS-排程缺料清单 */
export function deleteShortage(id: number) {
  return requestClient.delete(`/aps/shortage/delete?id=${id}`);
}

/** 批量删除APS-排程缺料清单 */
export function deleteShortageList(ids: number[]) {
  return requestClient.delete(
    `/aps/shortage/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程缺料清单 */
export function exportShortage(params: any) {
  return requestClient.download('/aps/shortage/export-excel', { params });
}

/** 下载APS-排程缺料清单导入模板 */
export function importShortageTemplate() {
  return requestClient.download('/aps/shortage/get-import-template');
}

/** 导入APS-排程缺料清单 */
export function importShortage(file: File) {
  return requestClient.upload('/aps/shortage/import', { file });
}

