import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsArrivalApi {
  /** WMS-到货登记明细信息 */
  export interface ArrivalLine {
            id: number; // 主键
            arrivalId?: number; // 到货登记编号
            lineNo?: number; // 行号
            sourceLineNo: string; // 来源单据行号
            matId?: number; // 物料编号
            matCode?: string; // 物料编码快照
            matName?: string; // 物料名称快照
            matSpec: string; // 物料规格快照
            lotNo: string; // 到货批次号
            supplierLotNo: string; // 供应商批次号
            arrivalQty?: number; // 到货数量
            uomId?: number; // 计量单位编号
            uomCode?: string; // 计量单位编码快照
            needQc?: boolean; // 是否需要质检
            iqcMode: string; // 来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING
            qcTaskId: number; // 质检任务编号
            qualifiedQty?: number; // 合格数量
            rejectedQty?: number; // 不合格数量
            status?: string; // 状态：PENDING、QC_PENDING、READY、DONE、CANCELLED
            remark: string; // 备注
  }

  /** WMS-到货登记信息 */
  export interface Arrival {
    id: number; // 主键
    arrivalNo?: string; // 到货登记单号
    sourceType?: string; // 来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER
    sourceNo?: string; // 来源单号
    supplierId: number; // 供应商编号
    supplierCode: string; // 供应商编码快照
    supplierName: string; // 供应商名称快照
    invOrgId?: number; // 库存组织编号
    plantId?: number; // 工厂编号
    whId?: number; // 目标仓库编号
    expectDate: string | Dayjs; // 预计到货日期
    arrivalTime: string | Dayjs; // 实际到货时间
    status?: string; // 状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED
    remark: string; // 备注
  }
}

/** 查询WMS-到货登记分页 */
export function getArrivalPage(params: PageParam) {
  return requestClient.get<PageResult<WmsArrivalApi.Arrival>>(
    '/wms/arrival/page',
    { params },
  );
}

/** 查询WMS-到货登记详情 */
export function getArrival(id: number) {
  return requestClient.get<WmsArrivalApi.Arrival>(
    `/wms/arrival/get?id=${id}`,
  );
}

/** 新增WMS-到货登记 */
export function createArrival(data: WmsArrivalApi.Arrival) {
  return requestClient.post('/wms/arrival/create', data);
}

/** 修改WMS-到货登记 */
export function updateArrival(data: WmsArrivalApi.Arrival) {
  return requestClient.put('/wms/arrival/update', data);
}

/** 删除WMS-到货登记 */
export function deleteArrival(id: number) {
  return requestClient.delete(`/wms/arrival/delete?id=${id}`);
}

/** 批量删除WMS-到货登记 */
export function deleteArrivalList(ids: number[]) {
  return requestClient.delete(
    `/wms/arrival/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-到货登记 */
export function exportArrival(params: any) {
  return requestClient.download('/wms/arrival/export-excel', { params });
}

/** 下载WMS-到货登记导入模板 */
export function importArrivalTemplate() {
  return requestClient.download('/wms/arrival/get-import-template');
}

/** 导入WMS-到货登记 */
export function importArrival(file: File) {
  return requestClient.upload('/wms/arrival/import', { file });
}


// ==================== 子表（WMS-到货登记明细） ====================

/** 获得WMS-到货登记明细分页 */
export function getArrivalLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsArrivalApi.ArrivalLine>>(
    `/wms/arrival/arrival-line/page`,
    { params },
  );
}
/** 新增WMS-到货登记明细 */
export function createArrivalLine(data: WmsArrivalApi.ArrivalLine) {
  return requestClient.post(`/wms/arrival/arrival-line/create`, data);
}

/** 修改WMS-到货登记明细 */
export function updateArrivalLine(data: WmsArrivalApi.ArrivalLine) {
  return requestClient.put(`/wms/arrival/arrival-line/update`, data);
}

/** 删除WMS-到货登记明细 */
export function deleteArrivalLine(id: number) {
  return requestClient.delete(`/wms/arrival/arrival-line/delete?id=${id}`);
}

/** 批量删除WMS-到货登记明细 */
export function deleteArrivalLineList(ids: number[]) {
  return requestClient.delete(
    `/wms/arrival/arrival-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得WMS-到货登记明细 */
export function getArrivalLine(id: number) {
  return requestClient.get<WmsArrivalApi.ArrivalLine>(
    `/wms/arrival/arrival-line/get?id=${id}`,
  );
}
