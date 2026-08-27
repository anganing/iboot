import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmPlantApi {
  /** 工厂信息 */
  export interface Plant {
    id: number; // 主键
    invOrgId?: number; // 库存组织编号
    invOrgCode?: string; // 库存组织编码
    invOrgName?: string; // 库存组织名称
    code?: string; // 编码
    name?: string; // 名称
    address: string; // 地址
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询工厂分页 */
export function getPlantPage(params: PageParam) {
  return requestClient.get<PageResult<MdmPlantApi.Plant>>('/mdm/plant/page', {
    params,
  });
}

/** 查询工厂详情 */
export function getPlant(id: number) {
  return requestClient.get<MdmPlantApi.Plant>(`/mdm/plant/get?id=${id}`);
}

/** 新增工厂 */
export function createPlant(data: MdmPlantApi.Plant) {
  return requestClient.post('/mdm/plant/create', data);
}

/** 修改工厂 */
export function updatePlant(data: MdmPlantApi.Plant) {
  return requestClient.put('/mdm/plant/update', data);
}

/** 删除工厂 */
export function deletePlant(id: number) {
  return requestClient.delete(`/mdm/plant/delete?id=${id}`);
}

/** 批量删除工厂 */
export function deletePlantList(ids: number[]) {
  return requestClient.delete(`/mdm/plant/delete-list?ids=${ids.join(',')}`);
}

/** 导出工厂 */
export function exportPlant(params: any) {
  return requestClient.download('/mdm/plant/export-excel', { params });
}
