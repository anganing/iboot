import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsWaveApi {
  /** 波次明细信息 */
  export interface WaveLine {
    id: number; // 主键
    waveId?: number; // 波次编号
    lineNo?: number; // 行号
    outOrderId?: number; // 出库单编号
    outOrderNo?: string; // 出库单号
    outOrderLineId?: number; // 出库单明细编号
    outOrderLineNo?: number; // 出库单明细行号
    matCode?: string; // 出库明细物料编码
    matName?: string; // 出库明细物料名称
    planQty?: number; // 出库明细计划数量
    priority?: number; // 波次内优先级，数值越大越优先
    status?: string; // 明细状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 出库波次信息 */
  export interface Wave {
    id: number; // 主键
    waveNo?: string; // 波次编号
    whId?: number; // 仓库编号
    waveType?: string; // 波次类型
    planStartTime?: string; // 计划开始时间
    planEndTime?: string; // 计划结束时间
    status?: string; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 仓库编码
    whName?: string; // 仓库名称
  }
}

/** 查询出库波次分页 */
export function getWavePage(params: PageParam) {
  return requestClient.get<PageResult<WmsWaveApi.Wave>>(
    '/wms/wave/page',
    { params },
  );
}

/** 查询出库波次详情 */
export function getWave(id: number) {
  return requestClient.get<WmsWaveApi.Wave>(
    `/wms/wave/get?id=${id}`,
  );
}

/** 新增出库波次 */
export function createWave(data: WmsWaveApi.Wave) {
  return requestClient.post('/wms/wave/create', data);
}

/** 修改出库波次 */
export function updateWave(data: WmsWaveApi.Wave) {
  return requestClient.put('/wms/wave/update', data);
}

/** 删除出库波次 */
export function deleteWave(id: number) {
  return requestClient.delete(`/wms/wave/delete?id=${id}`);
}

/** 批量删除出库波次 */
export function deleteWaveList(ids: number[]) {
  return requestClient.delete(
    `/wms/wave/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出出库波次 */
export function exportWave(params: any) {
  return requestClient.download('/wms/wave/export-excel', { params });
}

/** 下载出库波次导入模板 */
export function importWaveTemplate() {
  return requestClient.download('/wms/wave/get-import-template');
}

/** 导入出库波次 */
export function importWave(file: File) {
  return requestClient.upload('/wms/wave/import', { file });
}


// ==================== 子表（波次明细） ====================

/** 获得波次明细分页 */
export function getWaveLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsWaveApi.WaveLine>>(
    `/wms/wave/wave-line/page`,
    { params },
  );
}
/** 新增波次明细 */
export function createWaveLine(data: WmsWaveApi.WaveLine) {
  return requestClient.post(`/wms/wave/wave-line/create`, data);
}

/** 修改波次明细 */
export function updateWaveLine(data: WmsWaveApi.WaveLine) {
  return requestClient.put(`/wms/wave/wave-line/update`, data);
}

/** 删除波次明细 */
export function deleteWaveLine(id: number) {
  return requestClient.delete(`/wms/wave/wave-line/delete?id=${id}`);
}

/** 批量删除波次明细 */
export function deleteWaveLineList(ids: number[]) {
  return requestClient.delete(
    `/wms/wave/wave-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得波次明细 */
export function getWaveLine(id: number) {
  return requestClient.get<WmsWaveApi.WaveLine>(
    `/wms/wave/wave-line/get?id=${id}`,
  );
}

