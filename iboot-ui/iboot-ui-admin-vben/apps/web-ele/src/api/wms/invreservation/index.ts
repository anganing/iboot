import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsInvReservationApi {
  /** 库存预留信息 */
  export interface InvReservation {
    id: number; // 主键
    reservationNo?: string; // 预留编号
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    bizLineNo?: string; // 来源业务行号
    invOrgId?: number; // 库存组织编号
    whId?: number; // 仓库编号
    locId?: number; // 库位编号
    matId?: number; // 物料编号
    batchId?: number; // 批次编号
    containerId?: number; // 容器编号，0表示散件
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    qualityStatus?: string; // 质量状态
    qty?: number; // 预留数量，基准单位
    releasedQty?: number; // 已释放或已核销数量
    status?: string; // 状态
    expireTime?: string; // 预留失效时间
    idempotencyKey?: string; // 预留幂等键
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 仓库编码
    whName?: string; // 仓库名称
    locCode?: string; // 库位编码
    lotNo?: string; // 批次号
    matCode?: string; // 物料编码
    matName?: string; // 物料名称
  }
}

/** 查询库存预留分页 */
export function getInvReservationPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvReservationApi.InvReservation>>(
    '/wms/inv-reservation/page',
    { params },
  );
}

/** 查询库存预留详情 */
export function getInvReservation(id: number) {
  return requestClient.get<WmsInvReservationApi.InvReservation>(
    `/wms/inv-reservation/get?id=${id}`,
  );
}

/** 导出库存预留 */
export function exportInvReservation(params: any) {
  return requestClient.download('/wms/inv-reservation/export-excel', { params });
}

/**
 * 释放库存预留
 *
 * 预留由业务动作创建（MES 领料提交、销售订单确认），界面上唯一正当的写入动作就是释放：
 * 后端转调领域 Service 做 RELEASE 过账，把占用量还回可用量。
 * 新增/修改/删除/导入端点已在后端撤除，前端不再保留对应请求函数。
 */
export function releaseReservation(id: number) {
  return requestClient.post(`/wms/inv-reservation/release?id=${id}`);
}

