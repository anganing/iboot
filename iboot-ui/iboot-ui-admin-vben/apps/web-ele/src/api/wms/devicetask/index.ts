import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsDeviceTaskApi {
  /** WMS-设备搬运任务信息 */
  export interface DeviceTask {
    id: number; // 主键
    taskNo?: string; // 本地设备任务号
    externalTaskNo: string; // 外部设备任务号
    taskType?: string; // 任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN
    deviceType?: string; // 设备类型：WCS、AGV、RGV
    sourceBizType?: string; // 来源业务类型
    sourceBizNo?: string; // 来源业务单号
    sourceLineId: number; // 来源业务明细编号
    containerId?: number; // 容器编号，0表示无容器
    fromLocId?: number; // 来源库位编号
    toLocId?: number; // 目标库位编号
    actualLocId?: number; // 设备回传实际库位编号
    externalSystemCode?: string; // 外部设备系统编码
    dispatchStatus?: string; // 下发状态：PENDING、SENT、FAILED
    status?: string; // 执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION
    postStatus?: string; // 库存过账状态：PENDING、POSTED、FAILED
    requestSnapshotJson: string; // 下发业务快照
    callbackSnapshotJson: string; // 完成回调快照
    idempotencyKey?: string; // 业务幂等键
    completedTime: string | Dayjs; // 设备完成时间
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询WMS-设备搬运任务分页 */
export function getDeviceTaskPage(params: PageParam) {
  return requestClient.get<PageResult<WmsDeviceTaskApi.DeviceTask>>(
    '/wms/device-task/page',
    { params },
  );
}

/** 查询WMS-设备搬运任务详情 */
export function getDeviceTask(id: number) {
  return requestClient.get<WmsDeviceTaskApi.DeviceTask>(
    `/wms/device-task/get?id=${id}`,
  );
}

/** 新增WMS-设备搬运任务 */
export function createDeviceTask(data: WmsDeviceTaskApi.DeviceTask) {
  return requestClient.post('/wms/device-task/create', data);
}

/** 修改WMS-设备搬运任务 */
export function updateDeviceTask(data: WmsDeviceTaskApi.DeviceTask) {
  return requestClient.put('/wms/device-task/update', data);
}

/** 删除WMS-设备搬运任务 */
export function deleteDeviceTask(id: number) {
  return requestClient.delete(`/wms/device-task/delete?id=${id}`);
}

/** 批量删除WMS-设备搬运任务 */
export function deleteDeviceTaskList(ids: number[]) {
  return requestClient.delete(
    `/wms/device-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-设备搬运任务 */
export function exportDeviceTask(params: any) {
  return requestClient.download('/wms/device-task/export-excel', { params });
}

/** 下载WMS-设备搬运任务导入模板 */
export function importDeviceTaskTemplate() {
  return requestClient.download('/wms/device-task/get-import-template');
}

/** 导入WMS-设备搬运任务 */
export function importDeviceTask(file: File) {
  return requestClient.upload('/wms/device-task/import', { file });
}

