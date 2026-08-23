import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmUomConvApi {
  /** 单位换算信息 */
  export interface UomConv {
    id: number; // 主键
    matId?: number; // 物料编号
    fromUomId?: number; // 来源计量单位编号
    toUomId?: number; // 目标计量单位编号
    ratio?: number; // 换算比例
    remark: string; // 备注
  }
}

/** 查询单位换算分页 */
export function getUomConvPage(params: PageParam) {
  return requestClient.get<PageResult<MdmUomConvApi.UomConv>>(
    '/mdm/uom-conv/page',
    { params },
  );
}

/** 查询单位换算详情 */
export function getUomConv(id: number) {
  return requestClient.get<MdmUomConvApi.UomConv>(
    `/mdm/uom-conv/get?id=${id}`,
  );
}

/** 新增单位换算 */
export function createUomConv(data: MdmUomConvApi.UomConv) {
  return requestClient.post('/mdm/uom-conv/create', data);
}

/** 修改单位换算 */
export function updateUomConv(data: MdmUomConvApi.UomConv) {
  return requestClient.put('/mdm/uom-conv/update', data);
}

/** 删除单位换算 */
export function deleteUomConv(id: number) {
  return requestClient.delete(`/mdm/uom-conv/delete?id=${id}`);
}

/** 批量删除单位换算 */
export function deleteUomConvList(ids: number[]) {
  return requestClient.delete(
    `/mdm/uom-conv/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出单位换算 */
export function exportUomConv(params: any) {
  return requestClient.download('/mdm/uom-conv/export-excel', { params });
}


