import type { PageParam, PageResult } from '@vben/request';

/** MES 现场作业页共用的分页读取约定。 */
export const LINE_PAGE_SIZE = 200;
export const LINE_MAX_COUNT = 5000;

export interface LoadAllLinesResult<T> {
  list: T[];
  truncated: boolean;
}

/**
 * 现场页必须一次展示完整明细，不能只处理分页中的前一部分。
 * 超出上限时调用方必须禁止确认，改回单据页处理。
 */
export async function loadAllLines<T>(
  fetchPage: (params: PageParam) => Promise<PageResult<T>>,
  params: PageParam = {},
  maxCount = LINE_MAX_COUNT,
): Promise<LoadAllLinesResult<T>> {
  const list: T[] = [];
  let pageNo = 1;
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
