import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesQcItemApi {
  /** MES-检验项目信息 */
  export interface QcItem {
    id: number; // 主键
    planId?: number; // 检验方案编号
    itemCode?: string; // 检验项编码
    itemName?: string; // 检验项名称
    itemType?: string; // 检验项类型：NUMERIC、TEXT、BOOLEAN
    unit: string; // 检验值单位
    stdValue: string; // 标准值
    upperLimit: number; // 数值上限
    lowerLimit: number; // 数值下限
    requiredFlag?: boolean; // 是否必检
    sortNo?: number; // 排序号
    status?: number; // 状态：0-启用，1-停用
    remark: string; // 备注
  }
}

/** 查询MES-检验项目分页 */
export function getQcItemPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcItemApi.QcItem>>(
    '/mes/qc-item/page',
    { params },
  );
}

/** 查询MES-检验项目详情 */
export function getQcItem(id: number) {
  return requestClient.get<MesQcItemApi.QcItem>(
    `/mes/qc-item/get?id=${id}`,
  );
}

/** 新增MES-检验项目 */
export function createQcItem(data: MesQcItemApi.QcItem) {
  return requestClient.post('/mes/qc-item/create', data);
}

/** 修改MES-检验项目 */
export function updateQcItem(data: MesQcItemApi.QcItem) {
  return requestClient.put('/mes/qc-item/update', data);
}

/** 删除MES-检验项目 */
export function deleteQcItem(id: number) {
  return requestClient.delete(`/mes/qc-item/delete?id=${id}`);
}

/** 批量删除MES-检验项目 */
export function deleteQcItemList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-item/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-检验项目 */
export function exportQcItem(params: any) {
  return requestClient.download('/mes/qc-item/export-excel', { params });
}

/** 下载MES-检验项目导入模板 */
export function importQcItemTemplate() {
  return requestClient.download('/mes/qc-item/get-import-template');
}

/** 导入MES-检验项目 */
export function importQcItem(file: File) {
  return requestClient.upload('/mes/qc-item/import', { file });
}

