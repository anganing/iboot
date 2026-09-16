/**
 * 宽表编码列的“双击看详情”列配置。
 *
 * 用法（data.ts）：
 *
 * ```ts
 * { field: 'matCode', title: '物料编码', minWidth: 140, ...codeLinkColumn() }
 * ```
 *
 * 用法（index.vue）：
 *
 * ```ts
 * function handleCellDblclick({ column, row }: VxeTableEvents.CellClickParams<Xxx>) {
 *   if (!isCodeLinkColumn(column)) return;
 *   detailDrawerApi.setData({ id: row.id }).open();
 * }
 * ```
 *
 * 约束：只在列表列数 >= 15 且该列是业务编码（`code` / `xxxCode`）时使用，
 * 详见 dev-docs/后台UI开发规范.md 3.5。
 */

/** 挂在列 params 上的标识，供双击分流使用 */
export const CODE_LINK_FLAG = 'detail';

/** vxe 渲染在 <td> 上的类名，样式在 src/styles/index.scss */
export const CODE_LINK_CLASS = 'cell-code-link';

/**
 * 展开进编码列定义的固定配置。
 *
 * `className` 是 vxe 列属性，会附加到该列每个 <td class="vxe-body--column"> 上，
 * 因此样式必须全局（组件 scoped 命不中）。
 */
export function codeLinkColumn() {
  return {
    className: CODE_LINK_CLASS,
    params: { dblclick: CODE_LINK_FLAG },
  };
}

/** 判断被双击的列是否是编码链接列 */
export function isCodeLinkColumn(column: undefined | { params?: unknown }) {
  return (column?.params as undefined | { dblclick?: string })?.dblclick ===
    CODE_LINK_FLAG;
}
