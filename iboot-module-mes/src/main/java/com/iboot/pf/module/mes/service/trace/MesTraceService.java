package com.iboot.pf.module.mes.service.trace;

import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExcelRowRespVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExportReqVO;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;

import java.util.List;

/**
 * 批次追溯查询接口。
 *
 * <p>一次调用返回完整链路树，前端不许逐层调接口（08 文档"追溯查询的实现要求"）。
 */
public interface MesTraceService {

    /**
     * 正向追溯：从起点拉出到出货客户的完整链路。
     *
     * <p>起点是原料批次时输出"这批料用到哪些产品、发给了哪些客户"；起点是工单或
     * 销售出库单时向上补全上游（原料批次、领料出库），向下补全下游（完工入库、销售出货），
     * 得到同一棵完整树。
     *
     * @param query 查询参数（起点类型 + 起点值）
     * @return 链路树，根节点为起点批次或起点上游补全后的原料批次
     */
    MesTraceNodeBO traceForward(MesTraceQueryBO query);

    /**
     * 反向追溯：从成品批次反查到原料供应商的完整链路。
     *
     * <p>输出"这批产品用了哪些原料批次、来自哪些供应商"，并挂上该工单各检验点的质检结论
     * （含锁定的检验方案版本）与在制流水；起点是工单号或销售出库单号时先定位成品批次，
     * 之后与批次起点同路。
     *
     * @param query 查询参数（起点类型 + 起点值）
     * @return 链路树，根节点为成品批次，叶子为原料批次下的供应商
     */
    MesTraceNodeBO traceBackward(MesTraceQueryBO query);

    /**
     * 追溯链路导出：按方向取回完整链路树，再前序拍平成 Excel 行（带层级与路径列）。
     *
     * <p>拍平与枚举中文化都在本方法内完成（08 文档：补全逻辑写在 Service 而非 Controller），
     * Controller 只管把行写进响应流，否则导出会丢字段。
     *
     * @param reqVO 导出参数（方向 + 起点类型 + 起点值）
     * @return 拍平后的行，顺序即树的前序遍历顺序
     */
    List<TraceExcelRowRespVO> exportTraceExcel(TraceExportReqVO reqVO);

}
