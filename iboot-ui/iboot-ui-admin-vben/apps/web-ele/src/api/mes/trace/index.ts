import { requestClient } from '#/api/request';

export namespace MesTraceApi {
  /** 追溯查询起点类型，与后端 MesTraceStartTypeEnum 一致。 */
  export type StartType = 'LOT' | 'PROD_ORDER' | 'SALES_OUT';

  /** 追溯方向，与后端 MesTraceDirectionEnum 一致（正向到客户，反向到供应商）。 */
  export type Direction = 'BACKWARD' | 'FORWARD';

  /** 链路树节点类型，与后端 MesTraceNodeTypeEnum 一致（9 类）。 */
  export type NodeType =
    | 'BATCH'
    | 'CUSTOMER'
    | 'IN_ORDER'
    | 'OP_TASK'
    | 'OUT_ORDER'
    | 'PROD_ORDER'
    | 'QC_TASK'
    | 'SUPPLIER'
    | 'WIP_TXN';

  /**
   * 节点关键字段快照：九类节点各有自己的字段，后端用 Map 透出，键随节点类型不同。
   *
   * 值是后端 Jackson 出口的原样形态——LocalDateTime 序列化成毫秒时间戳、LocalDate 是
   * 'yyyy-MM-dd' 字符串、其余是标量或数组，因此这里只能定成 unknown，
   * 取值一律走 data.ts 的 pickText / pickNumber / pickTime 三个安全读取函数。
   */
  export type TraceSnapshot = Record<string, unknown>;

  /** 追溯链路树节点（后端 TraceNodeRespVO）。 */
  export interface TraceNode {
    /** 节点类型 */
    nodeType: NodeType | string;
    /** 业务单号或批次号，节点自然键 */
    bizNo: string;
    /** 节点名称（物料名/客户名/工序名等） */
    nodeName?: string;
    /** 关键字段快照 */
    snapshot?: TraceSnapshot;
    /** 层级深度，起点为 1 */
    depth?: number;
    /** 是否因超过深度上限被截断 */
    truncated?: boolean;
    /** 子节点 */
    children?: TraceNode[];
  }

  /**
   * 树形表格的行：由 {@link toGridRows} 把 TraceNode 树转换而来。
   *
   * vxe 树形表格要求行主键唯一，而后端的 bizNo 在同一父下才唯一（同一张工单可能从两个批次汇入），
   * 故在此拼一行一条的 rowKey，并把节点展平出的常用字段直接放进行里，避免每列都从 snapshot 取。
   *
   * 注意这里用 Omit 排除 children 后重新声明，不能写成 `extends TraceNode`：继承来的
   * `children?: TraceNode[]` 与收窄后的 `children?: TraceRow[]` 不兼容（数组类型对协变不成立），
   * 会让 toGridRows 里展开 node 的那一步直接编译不过。
   */
  export interface TraceRow extends Omit<TraceNode, 'children'> {
    /** 行主键：父路径 + 同层序号，全树唯一 */
    rowKey: string;
    /** 上级节点业务单号（详情列用） */
    parentBizNo?: string;
    /** 子行（vxe childrenField 读取） */
    children?: TraceRow[];
  }

  /** 追溯查询参数 */
  export interface TraceQuery {
    /** 起点类型 */
    startType: StartType | string;
    /** 起点值：批次号 / 生产工单号 / 销售出库单号 */
    startValue: string;
  }

  /** 追溯导出参数：查询参数 + 方向（导出只有一个端点，方向退化成入参） */
  export interface TraceExportQuery extends TraceQuery {
    /** 追溯方向 */
    direction: Direction | string;
  }
}

/** 批次正向追溯：一次返回完整链路树（后端 /mes/trace/forward） */
export function getTraceForward(params: MesTraceApi.TraceQuery) {
  return requestClient.get<MesTraceApi.TraceNode>('/mes/trace/forward', {
    params,
  });
}

/** 批次反向追溯：一次反查到原料批次与供应商的完整链路树（后端 /mes/trace/backward） */
export function getTraceBackward(params: MesTraceApi.TraceQuery) {
  return requestClient.get<MesTraceApi.TraceNode>('/mes/trace/backward', {
    params,
  });
}

/** 按方向取对应端点，页面只用一个入口调它 */
export function getTraceTree(params: MesTraceApi.TraceQuery & { direction: MesTraceApi.Direction | string }) {
  return params.direction === 'BACKWARD'
    ? getTraceBackward(params)
    : getTraceForward(params);
}

/** 导出批次追溯链路 Excel（树拍平成行，含层级列） */
export function exportTrace(params: MesTraceApi.TraceExportQuery) {
  return requestClient.download('/mes/trace/export-excel', { params });
}

/**
 * 把后端链路树转成 vxe 树形表格的行：补 rowKey / depth / parentBizNo，递归保留 children。
 *
 * @param root 后端返回的根节点（可能是多批次时的虚拟根）
 * @param direction 查询方向，只读语义标记，不参与渲染
 */
export function toGridRows(
  root?: MesTraceApi.TraceNode | null,
): MesTraceApi.TraceRow[] {
  if (!root) {
    return [];
  }
  const attach = (
    node: MesTraceApi.TraceNode,
    parentKey: string,
    parentBizNo: string | undefined,
    index: number,
  ): MesTraceApi.TraceRow => {
    const rowKey = `${parentKey}.${index}`;
    const children = (node.children ?? []).map((child, i) =>
      attach(child, rowKey, node.bizNo, i),
    );
    // 显式取字段而非 `...node` 展开：展开会把 node.children（TraceNode[]）一并带进来，
    // 与下面的 children（TraceRow[]）冲突。逐字段搬可保证类型精确、也不怕将来 TraceNode 加字段时静默漏转。
    const row: MesTraceApi.TraceRow = {
      nodeType: node.nodeType,
      bizNo: node.bizNo,
      nodeName: node.nodeName,
      snapshot: node.snapshot,
      truncated: node.truncated,
      rowKey,
      parentBizNo,
      depth: node.depth ?? rowKey.split('.').length - 1,
    };
    if (children.length > 0) {
      row.children = children;
    }
    return row;
  };
  return [attach(root, '0', undefined, 0)];
}
