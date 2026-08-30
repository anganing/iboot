import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesDefectApi {
  /** MES-缺陷记录信息 */
  export interface Defect {
    id: number; // 主键
    taskId?: number; // 质检任务编号
    resultId: number; // 关联检验结果编号
    defectCode?: string; // 缺陷编码
    defectName?: string; // 缺陷名称
    defectLevel: string; // 缺陷等级：CRITICAL、MAJOR、MINOR
    qty?: number; // 缺陷数量
    description: string; // 缺陷描述
    location: string; // 缺陷位置
    attachmentUrls: string; // 缺陷附件地址列表
    remark: string; // 备注
  }
}

/** 查询MES-缺陷记录分页 */
export function getDefectPage(params: PageParam) {
  return requestClient.get<PageResult<MesDefectApi.Defect>>(
    '/mes/defect/page',
    { params },
  );
}

/** 查询MES-缺陷记录详情 */
export function getDefect(id: number) {
  return requestClient.get<MesDefectApi.Defect>(
    `/mes/defect/get?id=${id}`,
  );
}

/** 新增MES-缺陷记录 */
export function createDefect(data: MesDefectApi.Defect) {
  return requestClient.post('/mes/defect/create', data);
}

/** 修改MES-缺陷记录 */
export function updateDefect(data: MesDefectApi.Defect) {
  return requestClient.put('/mes/defect/update', data);
}

/** 删除MES-缺陷记录 */
export function deleteDefect(id: number) {
  return requestClient.delete(`/mes/defect/delete?id=${id}`);
}

/** 批量删除MES-缺陷记录 */
export function deleteDefectList(ids: number[]) {
  return requestClient.delete(
    `/mes/defect/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-缺陷记录 */
export function exportDefect(params: any) {
  return requestClient.download('/mes/defect/export-excel', { params });
}

/** 下载MES-缺陷记录导入模板 */
export function importDefectTemplate() {
  return requestClient.download('/mes/defect/get-import-template');
}

/** 导入MES-缺陷记录 */
export function importDefect(file: File) {
  return requestClient.upload('/mes/defect/import', { file });
}

