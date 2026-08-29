import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmDefectCodeApi {
  /** 缺陷代码信息 */
  export interface DefectCode {
    id: number; // 主键
    code?: string; // 缺陷编码
    name?: string; // 缺陷名称
    level?: string; // 缺陷等级
    opIds?: number[]; // 适用工序编号列表，为空表示不限
    opNames?: string; // 适用工序编码拼接串，仅列表展示
    matCatIds?: number[]; // 适用物料分类编号列表，为空表示不限
    matCatNames?: string; // 适用物料分类名称拼接串，仅列表展示
    parentId?: number; // 父级编号
    parentName?: string; // 父级名称
    status?: number; // 状态
    remark?: string; // 备注
  }
}

/** 查询缺陷代码分页 */
export function getDefectCodePage(params: PageParam) {
  return requestClient.get<PageResult<MdmDefectCodeApi.DefectCode>>(
    '/mdm/defect-code/page',
    { params },
  );
}

/** 查询缺陷代码详情 */
export function getDefectCode(id: number) {
  return requestClient.get<MdmDefectCodeApi.DefectCode>(
    `/mdm/defect-code/get?id=${id}`,
  );
}

/** 新增缺陷代码 */
export function createDefectCode(data: MdmDefectCodeApi.DefectCode) {
  return requestClient.post('/mdm/defect-code/create', data);
}

/** 修改缺陷代码 */
export function updateDefectCode(data: MdmDefectCodeApi.DefectCode) {
  return requestClient.put('/mdm/defect-code/update', data);
}

/** 删除缺陷代码 */
export function deleteDefectCode(id: number) {
  return requestClient.delete(`/mdm/defect-code/delete?id=${id}`);
}

/** 批量删除缺陷代码 */
export function deleteDefectCodeList(ids: number[]) {
  return requestClient.delete(
    `/mdm/defect-code/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出缺陷代码 */
export function exportDefectCode(params: any) {
  return requestClient.download('/mdm/defect-code/export-excel', { params });
}
