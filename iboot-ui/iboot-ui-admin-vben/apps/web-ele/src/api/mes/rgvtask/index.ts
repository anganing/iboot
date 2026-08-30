import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesRgvTaskApi {
  /** MES-RGV工位流转任务信息 */
  export interface RgvTask {
    id: number; // 主键
    taskNo?: string; // RGV本地任务号
    externalTaskNo: string; // RGV外部任务号
    taskType?: string; // 任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN
    workOrderId?: number; // 作业工单编号
    opTaskId?: number; // 来源工序任务编号
    targetOpTaskId: number; // 目标工序任务编号
    fromStationId?: number; // 来源工位或资源编号
    toStationId?: number; // 目标工位或资源编号
    carrierNo: string; // 载具编号
    externalSystemCode?: string; // 外部调度系统编码
    dispatchStatus?: string; // 下发状态：PENDING、SENT、FAILED
    status?: string; // 执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION
    idempotencyKey?: string; // 业务幂等键
    completedTime: string | Dayjs; // 完成时间
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询MES-RGV工位流转任务分页 */
export function getRgvTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesRgvTaskApi.RgvTask>>(
    '/mes/rgv-task/page',
    { params },
  );
}

/** 查询MES-RGV工位流转任务详情 */
export function getRgvTask(id: number) {
  return requestClient.get<MesRgvTaskApi.RgvTask>(
    `/mes/rgv-task/get?id=${id}`,
  );
}

/** 新增MES-RGV工位流转任务 */
export function createRgvTask(data: MesRgvTaskApi.RgvTask) {
  return requestClient.post('/mes/rgv-task/create', data);
}

/** 修改MES-RGV工位流转任务 */
export function updateRgvTask(data: MesRgvTaskApi.RgvTask) {
  return requestClient.put('/mes/rgv-task/update', data);
}

/** 删除MES-RGV工位流转任务 */
export function deleteRgvTask(id: number) {
  return requestClient.delete(`/mes/rgv-task/delete?id=${id}`);
}

/** 批量删除MES-RGV工位流转任务 */
export function deleteRgvTaskList(ids: number[]) {
  return requestClient.delete(
    `/mes/rgv-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-RGV工位流转任务 */
export function exportRgvTask(params: any) {
  return requestClient.download('/mes/rgv-task/export-excel', { params });
}

/** 下载MES-RGV工位流转任务导入模板 */
export function importRgvTaskTemplate() {
  return requestClient.download('/mes/rgv-task/get-import-template');
}

/** 导入MES-RGV工位流转任务 */
export function importRgvTask(file: File) {
  return requestClient.upload('/mes/rgv-task/import', { file });
}

