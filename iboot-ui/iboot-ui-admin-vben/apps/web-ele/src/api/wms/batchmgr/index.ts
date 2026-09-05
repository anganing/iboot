import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsBatchApi {
  /** 批次信息 */
  export interface Batch {
    id: number; // 主键
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    lotNo?: string; // 批次号；无批次物料使用默认批次
    snNo?: string; // 序列号；SN物料以数量为1的批次承载
    supplierId?: number; // 供应商编号
    supplierCode?: string; // 供应商编码快照
    supplierLotNo?: string; // 供应商批次号
    furnaceLotNo?: string; // 炉批号
    produceDate?: string; // 生产日期
    expiryDate?: string; // 失效日期
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /**
   * 批属性维护入参
   *
   * 与后端 BatchAttrUpdateReqVO 一一对应，只有六个字段。物料、批次号、序列号、供应商是批次的身份，
   * 库存余额与流水都按 batchId 记账，改了身份就与账对不上，所以不放进来。
   */
  export interface BatchAttrUpdate {
    id: number; // 主键
    furnaceLotNo?: string; // 炉批号
    produceDate?: string; // 生产日期
    expiryDate?: string; // 失效日期
    status?: number; // 状态
    remark?: string; // 备注
  }
}

/** 查询批次分页 */
export function getBatchPage(params: PageParam) {
  return requestClient.get<PageResult<WmsBatchApi.Batch>>(
    '/wms/batch/page',
    { params },
  );
}

/** 查询批次详情 */
export function getBatch(id: number) {
  return requestClient.get<WmsBatchApi.Batch>(
    `/wms/batch/get?id=${id}`,
  );
}

/** 导出批次 */
export function exportBatch(params: any) {
  return requestClient.download('/wms/batch/export-excel', { params });
}

/**
 * 维护批次批属性
 *
 * 批次只能由入库动作（到货确认、完工入库）经 WmsBatchService#registerBatch 创建，
 * 人工建批次会造出没有库存的孤儿批次、人工删批次会让引用它的余额与流水失去主档，
 * 所以后端已撤除新增/删除/导入端点，原 PUT /update 也降级成只放开六个批属性的 /update-attr。
 */
export function updateBatchAttr(data: WmsBatchApi.BatchAttrUpdate) {
  return requestClient.put('/wms/batch/update-attr', data);
}

