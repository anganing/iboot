import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MdmBomApi {
  /** 物料清单明细信息 */
  export interface BomLine {
    id: number; // 主键
    bomId?: number; // BOM编号
    lineNo?: number; // 行号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    qty?: number; // 数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码
    uomName?: string; // 计量单位名称
    altGroup: string; // 替代组
    remark: string; // 备注
  }

  /** 物料清单信息 */
  export interface Bom {
    id: number; // 主键
    matId?: number; // 物料编号
    matCode?: string; // 物料编码
    matName?: string; // 物料名称
    version?: string; // 版本号
    status?: string; // 状态
    effectiveDate?: string | Dayjs; // 生效日期
    expiryDate: string | Dayjs; // 失效日期
    remark: string; // 备注
  }
}

/** 查询物料清单分页 */
export function getBomPage(params: PageParam) {
  return requestClient.get<PageResult<MdmBomApi.Bom>>('/mdm/bom/page', {
    params,
  });
}

/** 查询物料清单详情 */
export function getBom(id: number) {
  return requestClient.get<MdmBomApi.Bom>(`/mdm/bom/get?id=${id}`);
}

/** 新增物料清单 */
export function createBom(data: MdmBomApi.Bom) {
  return requestClient.post('/mdm/bom/create', data);
}

/** 修改物料清单 */
export function updateBom(data: MdmBomApi.Bom) {
  return requestClient.put('/mdm/bom/update', data);
}

/** 删除物料清单 */
export function deleteBom(id: number) {
  return requestClient.delete(`/mdm/bom/delete?id=${id}`);
}

/** 批量删除物料清单 */
export function deleteBomList(ids: number[]) {
  return requestClient.delete(`/mdm/bom/delete-list?ids=${ids.join(',')}`);
}

/** 导出物料清单 */
export function exportBom(params: any) {
  return requestClient.download('/mdm/bom/export-excel', { params });
}

// ==================== 子表（物料清单明细） ====================

/** 获得物料清单明细分页 */
export function getBomLinePage(params: PageParam) {
  return requestClient.get<PageResult<MdmBomApi.BomLine>>(
    `/mdm/bom/bom-line/page`,
    { params },
  );
}
/** 新增物料清单明细 */
export function createBomLine(data: MdmBomApi.BomLine) {
  return requestClient.post(`/mdm/bom/bom-line/create`, data);
}

/** 修改物料清单明细 */
export function updateBomLine(data: MdmBomApi.BomLine) {
  return requestClient.put(`/mdm/bom/bom-line/update`, data);
}

/** 删除物料清单明细 */
export function deleteBomLine(id: number) {
  return requestClient.delete(`/mdm/bom/bom-line/delete?id=${id}`);
}

/** 批量删除物料清单明细 */
export function deleteBomLineList(ids: number[]) {
  return requestClient.delete(
    `/mdm/bom/bom-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得物料清单明细 */
export function getBomLine(id: number) {
  return requestClient.get<MdmBomApi.BomLine>(`/mdm/bom/bom-line/get?id=${id}`);
}
