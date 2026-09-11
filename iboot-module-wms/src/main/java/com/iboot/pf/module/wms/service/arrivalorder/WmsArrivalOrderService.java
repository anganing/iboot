package com.iboot.pf.module.wms.service.arrivalorder;

import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.module.wms.api.arrivalorder.dto.CompleteIqcReqDTO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsConfirmArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsCreateArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsUpdateArrivalBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;

import java.util.List;

/**
 * 到货登记领域 Service，入库链路的起点。
 *
 * <p>到货登记本身不动库存：它记录"哪个供应商的哪批货到了多少"，确认到货时注册批次、按是否需要
 * 质检给明细分流，待入库的明细再生成入库单，库存由入库单过账时增加。
 *
 * <p>与 {@code service.arrival.ArrivalService}（codegen 侧）的分工：那边只做列表展示查询，
 * 写入一律走这里。
 */
public interface WmsArrivalOrderService {

    /**
     * 创建到货登记，落 DRAFT 态。
     *
     * @param bo 创建参数
     * @return 到货登记编号
     */
    Long createArrival(WmsCreateArrivalBO bo);

    /**
     * 修改到货登记，只有新建状态可以改。
     *
     * <p>明细按全量替换处理：旧行整批删除后按新参数重建。已确认到货的单据不许改物料与数量，
     * 要改先取消再重开。
     *
     * @param bo 新的单据内容，字段集与创建时相同
     */
    void updateArrival(WmsUpdateArrivalBO bo);

    /** 删除到货登记，只有新建状态可以删，头与明细一起逻辑删除。 */
    void deleteArrival(Long arrivalId);

    /**
     * 确认到货，幂等。
     *
     * <p>四件事：单头推进到 ARRIVED 并写实际到货时间；逐行注册批次（带供应商与供应商批次号）
     * 并回写批次号；逐行按 {@code need_qc} 分流为待质检或待入库；整个动作走命令幂等。
     *
     * @param bo 确认参数，含幂等键
     */
    void confirmArrival(WmsConfirmArrivalBO bo);

    /** 取消到货登记，已完成或已取消的不能再取消；已生成入库单的明细不回退。 */
    void cancelArrival(WmsCancelOrderBO bo);

    /**
     * 按待入库明细生成采购入库单（{@code biz_type = PO_IN}）。
     *
     * <p>入库单的质量状态按明细的来料检验模式取：先入待检落待检，其余落合格。生成后明细回写
     * DONE，全部明细都 DONE 时到货登记置 DONE。
     *
     * @param arrivalId 到货登记编号
     * @param lineIds   要生成入库单的明细编号；为空表示取全部待入库明细
     * @return 入库单编号
     */
    Long createInOrderFromArrival(Long arrivalId, List<Long> lineIds);

    /** 回写 IQC 规则解析结果；任务为空代表规则未命中，业务直接放行。 */
    void resolveIqcTask(Long arrivalLineId, Long qcTaskId);

    /** 回写 IQC 结论；仅 MES 通过 {@code ArrivalOrderApi} 调用。 */
    void completeIqc(CompleteIqcReqDTO reqDTO);

    ArrivalDO getArrival(Long id);

    List<ArrivalLineDO> getArrivalLineList(Long arrivalId);

}
