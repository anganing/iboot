
import { requestClient } from '#/api/request';

export namespace MdmMatCatApi {
  /** 物料分类信息 */
  export interface MatCat {
    id: number; // 主键
    parentId?: number; // 父级编号
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark?: string; // 备注
  children?: MatCat[];
  }
}

/** 查询物料分类列表 */
export function getMatCatList(params: any) {
  return requestClient.get<MdmMatCatApi.MatCat[]>(
    '/mdm/mat-cat/list',
    { params },
  );
}

/** 查询物料分类详情 */
export function getMatCat(id: number) {
  return requestClient.get<MdmMatCatApi.MatCat>(
    `/mdm/mat-cat/get?id=${id}`,
  );
}

/** 新增物料分类 */
export function createMatCat(data: MdmMatCatApi.MatCat) {
  return requestClient.post('/mdm/mat-cat/create', data);
}

/** 修改物料分类 */
export function updateMatCat(data: MdmMatCatApi.MatCat) {
  return requestClient.put('/mdm/mat-cat/update', data);
}

/** 删除物料分类 */
export function deleteMatCat(id: number) {
  return requestClient.delete(`/mdm/mat-cat/delete?id=${id}`);
}

/** 导出物料分类 */
export function exportMatCat(params: any) {
  return requestClient.download('/mdm/mat-cat/export-excel', { params });
}


