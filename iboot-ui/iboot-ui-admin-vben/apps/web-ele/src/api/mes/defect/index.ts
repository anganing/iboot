import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesDefectApi {
  /** MES-缺陷记录附件。提交时只回传这三个字段，seq 与 fileType 由后端判定。 */
  export interface DefectFile {
    id?: number;
    defectId?: number;
    seq?: number;
    fileType?: string;
    fileName?: string;
    fileUrl?: string;
    fileId?: number;
  }

  /**
   * MES-缺陷记录。
   *
   * 缺陷编码、名称、等级是 mdm 缺陷代码库的快照，由后端按 defectCodeId 补全；
   * 附件是独立子表，走全量替换。
   */
  export interface Defect {
    id?: number;
    taskId?: number;
    taskNo?: string;
    resultId?: number;
    resultItemName?: string;
    defectCodeId?: number;
    defectCode?: string;
    defectName?: string;
    defectLevel?: string;
    qty?: number;
    description?: string;
    location?: string;
    fileCount?: number;
    files?: DefectFile[];
    remark?: string;
    createTime?: string;
  }
}

/** 查询缺陷记录分页 */
export function getDefectPage(params: PageParam) {
  return requestClient.get<PageResult<MesDefectApi.Defect>>(
    '/mes/defect/page',
    { params },
  );
}

/** 查询缺陷记录详情，附件地址已重新签发 */
export function getDefect(id: number) {
  return requestClient.get<MesDefectApi.Defect>(`/mes/defect/get?id=${id}`);
}

/** 新增缺陷记录 */
export function createDefect(data: MesDefectApi.Defect) {
  return requestClient.post('/mes/defect/create', data);
}

/** 修改缺陷记录 */
export function updateDefect(data: MesDefectApi.Defect) {
  return requestClient.put('/mes/defect/update', data);
}

/** 删除缺陷记录 */
export function deleteDefect(id: number) {
  return requestClient.delete(`/mes/defect/delete?id=${id}`);
}

/** 批量删除缺陷记录 */
export function deleteDefectList(ids: number[]) {
  return requestClient.delete(`/mes/defect/delete-list?ids=${ids.join(',')}`);
}

/** 导出缺陷记录 */
export function exportDefect(params: any) {
  return requestClient.download('/mes/defect/export-excel', { params });
}
