import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsProjectionMatApi {
  /** APS-MDM物料投影信息 */
  export interface ProjectionMat {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    sourceMatId?: number; // MDM物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    matType?: string; // 物料类型快照
    baseUomId?: number; // 基准计量单位编号
    baseUomCode?: string; // 基准计量单位编码快照
    batchFlag?: boolean; // 是否批次管控
    bomId?: number; // 当前有效BOM编号，0表示无
    bomVersion: string; // 当前有效BOM版本
    routeId?: number; // 当前有效工艺路线编号，0表示无
    routeVersion: string; // 当前有效工艺路线版本
    sourceUpdateTime?: string | Dayjs; // 来源数据更新时间
    snapshotTime?: string | Dayjs; // 投影快照时间
    remark: string; // 备注
  }
}

/** 查询APS-MDM物料投影分页 */
export function getProjectionMatPage(params: PageParam) {
  return requestClient.get<PageResult<ApsProjectionMatApi.ProjectionMat>>(
    '/aps/projection-mat/page',
    { params },
  );
}

/** 查询APS-MDM物料投影详情 */
export function getProjectionMat(id: number) {
  return requestClient.get<ApsProjectionMatApi.ProjectionMat>(
    `/aps/projection-mat/get?id=${id}`,
  );
}

/** 新增APS-MDM物料投影 */
export function createProjectionMat(data: ApsProjectionMatApi.ProjectionMat) {
  return requestClient.post('/aps/projection-mat/create', data);
}

/** 修改APS-MDM物料投影 */
export function updateProjectionMat(data: ApsProjectionMatApi.ProjectionMat) {
  return requestClient.put('/aps/projection-mat/update', data);
}

/** 删除APS-MDM物料投影 */
export function deleteProjectionMat(id: number) {
  return requestClient.delete(`/aps/projection-mat/delete?id=${id}`);
}

/** 批量删除APS-MDM物料投影 */
export function deleteProjectionMatList(ids: number[]) {
  return requestClient.delete(
    `/aps/projection-mat/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-MDM物料投影 */
export function exportProjectionMat(params: any) {
  return requestClient.download('/aps/projection-mat/export-excel', { params });
}

/** 下载APS-MDM物料投影导入模板 */
export function importProjectionMatTemplate() {
  return requestClient.download('/aps/projection-mat/get-import-template');
}

/** 导入APS-MDM物料投影 */
export function importProjectionMat(file: File) {
  return requestClient.upload('/aps/projection-mat/import', { file });
}

