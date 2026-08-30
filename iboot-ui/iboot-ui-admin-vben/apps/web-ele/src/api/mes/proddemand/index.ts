import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesProdDemandApi {
  /** MES-生产需求信息 */
  export interface ProdDemand {
    id: number; // 主键
    demandNo?: string; // 生产需求编号
    sourceType?: string; // 来源类型：MANUAL、EXCEL、ERP、APS
    sourceNo: string; // 来源单号
    plantId?: number; // 工厂编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    matId?: number; // 需求物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    qty?: number; // 需求数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    dueDate?: string | Dayjs; // 需求交期
    priority?: number; // 优先级，数值越大越优先
    status?: string; // 状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED
    idempotencyKey: string; // 创建幂等键
    remark: string; // 备注
  }
}

/** 查询MES-生产需求分页 */
export function getProdDemandPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdDemandApi.ProdDemand>>(
    '/mes/prod-demand/page',
    { params },
  );
}

/** 查询MES-生产需求详情 */
export function getProdDemand(id: number) {
  return requestClient.get<MesProdDemandApi.ProdDemand>(
    `/mes/prod-demand/get?id=${id}`,
  );
}

/** 新增MES-生产需求 */
export function createProdDemand(data: MesProdDemandApi.ProdDemand) {
  return requestClient.post('/mes/prod-demand/create', data);
}

/** 修改MES-生产需求 */
export function updateProdDemand(data: MesProdDemandApi.ProdDemand) {
  return requestClient.put('/mes/prod-demand/update', data);
}

/** 删除MES-生产需求 */
export function deleteProdDemand(id: number) {
  return requestClient.delete(`/mes/prod-demand/delete?id=${id}`);
}

/** 批量删除MES-生产需求 */
export function deleteProdDemandList(ids: number[]) {
  return requestClient.delete(
    `/mes/prod-demand/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-生产需求 */
export function exportProdDemand(params: any) {
  return requestClient.download('/mes/prod-demand/export-excel', { params });
}

/** 下载MES-生产需求导入模板 */
export function importProdDemandTemplate() {
  return requestClient.download('/mes/prod-demand/get-import-template');
}

/** 导入MES-生产需求 */
export function importProdDemand(file: File) {
  return requestClient.upload('/mes/prod-demand/import', { file });
}

