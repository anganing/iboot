import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesKitSnapshotApi {
  /**
   * MES-齐套检查快照。
   *
   * 快照由齐套检查过账写入，属于账表，只提供查询与导出，没有新增/修改/删除接口。
   */
  export interface KitSnapshot {
    id?: number;
    checkNo?: string;
    checkScope?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    lineNo?: number;
    matId?: number;
    matCode?: string;
    matName?: string;
    reqQty?: number;
    availQty?: number;
    reservedQty?: number;
    issuedQty?: number;
    shortQty?: number;
    altMatId?: number;
    altMatName?: string;
    result?: string;
    checkTime?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询齐套检查快照分页 */
export function getKitSnapshotPage(params: PageParam) {
  return requestClient.get<PageResult<MesKitSnapshotApi.KitSnapshot>>(
    '/mes/kit-snapshot/page',
    { params },
  );
}

/** 查询齐套检查快照详情 */
export function getKitSnapshot(id: number) {
  return requestClient.get<MesKitSnapshotApi.KitSnapshot>(
    `/mes/kit-snapshot/get?id=${id}`,
  );
}

/** 导出齐套检查快照 */
export function exportKitSnapshot(params: any) {
  return requestClient.download('/mes/kit-snapshot/export-excel', { params });
}
