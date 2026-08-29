import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmMatApi {
  /** 物料主档信息 */
  export interface Mat {
    id: number; // 主键
    catId?: number; // 物料分类编号
    catCode?: string; // 物料分类编码
    catName?: string; // 物料分类名称
    code?: string; // 编码
    name?: string; // 名称
    spec: string; // 规格型号
    drawingNo: string; // 图号
    matType?: string; // 物料类型
    baseUomId?: number; // 基准计量单位编号
    baseUomCode?: string; // 基准计量单位编码
    baseUomName?: string; // 基准计量单位名称
    batchFlag?: boolean; // 是否批次管控
    snFlag?: boolean; // 是否SN管控
    shelfLifeDays: number; // 保质期（天）
    netWeight?: number; // 净重
    grossWeight?: number; // 毛重
    weightUomId?: number; // 重量单位编号
    length?: number; // 长（毫米）
    width?: number; // 宽（毫米）
    height?: number; // 高（毫米）
    volume?: number; // 体积（立方毫米）
    planStrategy?: string; // 计划策略：MTO-按单，MTS-备库
    leadTimeDays?: number; // 生产/采购提前期（天）
    safetyStock?: number; // 安全库存
    minOrderQty?: number; // 最小订货量
    orderMultiple?: number; // 订货倍数
    defaultWhId?: number; // 默认仓库编号
    defaultLocId?: number; // 默认库位编号
    packQty?: number; // 标准包装量
    abcClass?: string; // ABC分类
    changeoverGroup?: string; // 换型分组
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询物料主档分页 */
export function getMatPage(params: PageParam) {
  return requestClient.get<PageResult<MdmMatApi.Mat>>('/mdm/mat/page', {
    params,
  });
}

/** 查询物料主档详情 */
export function getMat(id: number) {
  return requestClient.get<MdmMatApi.Mat>(`/mdm/mat/get?id=${id}`);
}

/** 新增物料主档 */
export function createMat(data: MdmMatApi.Mat) {
  return requestClient.post('/mdm/mat/create', data);
}

/** 修改物料主档 */
export function updateMat(data: MdmMatApi.Mat) {
  return requestClient.put('/mdm/mat/update', data);
}

/** 删除物料主档 */
export function deleteMat(id: number) {
  return requestClient.delete(`/mdm/mat/delete?id=${id}`);
}

/** 批量删除物料主档 */
export function deleteMatList(ids: number[]) {
  return requestClient.delete(`/mdm/mat/delete-list?ids=${ids.join(',')}`);
}

/** 导出物料主档 */
export function exportMat(params: any) {
  return requestClient.download('/mdm/mat/export-excel', { params });
}
