import type { PageParam, PageResult } from '@vben/request';

import { getInvBalancePage } from '#/api/wms/invbalance';

/**
 * WMS 作业页（收货上架、拣选发货、盘点作业）的组合查询。
 *
 * 这里只放"单据 api 拿不到、必须组合多次请求才能得到"的查询。动作端点一律直接从
 * `api/wms/{arrival,inordermgr,outordermgr,countorder}` 引入，不在这里再包一层——
 * 同一个端点有两个入口，将来改路径漏掉一个就是线上 404。
 */

/** 一次拉多少行明细：后端 `PageParam.pageSize` 的上限是 200 */
export const LINE_PAGE_SIZE = 200;

/**
 * 一张单最多读多少行明细。
 *
 * 作业页要把明细全部读进本地数组才能逐行录数，行数不设上限会把浏览器拖死。超限时只读到这个数
 * 并置 `truncated`，由页面禁止提交——只提交前一部分，操作员会以为整单已经做完。
 */
export const LINE_MAX_COUNT = 5000;

/** 翻页读取的结果 */
export interface LoadAllLinesResult<T> {
  list: T[];
  /** 行数超过上限被截断，此时不允许提交 */
  truncated: boolean;
}

/**
 * 把某张单据的全部明细行翻页读完。
 *
 * 三个作业页共用：到货明细、出库明细、盘点明细的分页端点签名一致，都是每页最多 200 行，
 * 全仓单据必须翻页读完，否则界面上少的那些行操作员根本看不见。
 *
 * @param fetchPage 明细分页函数，如 `getArrivalLinePage`
 * @param params 父键等固定查询条件，`pageNo` / `pageSize` 由本函数接管
 * @param maxCount 读取行数上限，默认 {@link LINE_MAX_COUNT}
 */
export async function loadAllLines<T>(
  fetchPage: (params: PageParam) => Promise<PageResult<T>>,
  params: PageParam = {},
  maxCount = LINE_MAX_COUNT,
): Promise<LoadAllLinesResult<T>> {
  const list: T[] = [];
  let pageNo = 1;
  // do-while 保证循环体先执行一次，total 必然在第一轮被赋值，故不给多余初始值
  let total: number;
  do {
    const page = await fetchPage({
      ...params,
      pageNo,
      pageSize: LINE_PAGE_SIZE,
    });
    total = Number(page.total ?? 0);
    list.push(...(page.list ?? []));
    pageNo += 1;
  } while (list.length < total && list.length < maxCount);
  return { list, truncated: list.length < total };
}

/**
 * 可用量口径，与后端 `WmsInventoryService.availableQtyOf` 一致：现存 - 预留 - 冻结。
 *
 * 为负时原样返回，不夹到 0：负可用量说明预留或冻结失控，藏起来只会让问题更晚暴露
 * （`03-WMS仓储库存流程.md` 可用量公式）。
 */
export function availableQtyOf(balance: {
  qtyFrozen?: number;
  qtyOnhand?: number;
  qtyReserved?: number;
}) {
  return (
    Number(balance.qtyOnhand ?? 0) -
    Number(balance.qtyReserved ?? 0) -
    Number(balance.qtyFrozen ?? 0)
  );
}

/** 一次并发查多少个物料，避免几十行明细一口气打出几十个请求把浏览器连接数占满 */
const AVAILABLE_QTY_CONCURRENCY = 6;

/**
 * 最多查多少个物料的可用量。
 *
 * 一张到货单或出库单的物料数通常十几个，六十个已经远超现场单据的规模；超出部分不查，
 * 页面上显示"未查询"而不是硬撑着发几百个请求。
 */
const AVAILABLE_QTY_MAT_LIMIT = 60;

/** 可用量查询条件 */
export interface AvailableQtyQuery {
  /** 物料编号集合，重复值与非法值由本函数过滤 */
  matIds: number[];
  /** 质量状态；发货口径传 `QUALIFIED`，待检品不许被拣走 */
  qualityStatus?: string;
  /** 仓库编号，作业页固定取单据头上的仓库 */
  whId?: number;
}

/**
 * 按物料批量取可用量，键是物料编号。
 *
 * 为什么是"多次请求 + 前端汇总"：后端算可用量的批量入口是跨模块门面
 * `InventoryApi.getAvailableQtyMap`，门面不走 HTTP；对外只有 `/wms/inv-balance/page`，
 * 而它的 `matId` 是单值。所以只能按物料并发查余额再按可用量公式汇总。
 * 这是作业页"当场发现账实不符"必需的信息，缺了操作员只能拣到一半才发现拣不出来。
 */
export async function getAvailableQtyMap(query: AvailableQtyQuery) {
  const result = new Map<number, number>();
  const matIds = [
    ...new Set(query.matIds.map((id) => Number(id)).filter((id) => id > 0)),
  ].slice(0, AVAILABLE_QTY_MAT_LIMIT);
  for (let index = 0; index < matIds.length; index += AVAILABLE_QTY_CONCURRENCY) {
    const batch = matIds.slice(index, index + AVAILABLE_QTY_CONCURRENCY);
    const loaded = await Promise.all(
      batch.map((matId) =>
        loadAllLines(getInvBalancePage, {
          matId,
          qualityStatus: query.qualityStatus,
          whId: query.whId,
        }),
      ),
    );
    loaded.forEach((page, offset) => {
      const matId = batch[offset]!;
      result.set(
        matId,
        page.list.reduce((sum, row) => sum + availableQtyOf(row), 0),
      );
    });
  }
  return result;
}
