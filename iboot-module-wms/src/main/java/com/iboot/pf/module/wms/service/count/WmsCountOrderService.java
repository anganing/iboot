package com.iboot.pf.module.wms.service.count;

import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;

import java.util.List;

/** 盘点单 Service。 */
public interface WmsCountOrderService {

    /**
     * 创建盘点单并按仓库、库区、物料生成盘点行，幂等。
     *
     * @param bo 创建参数
     * @return 盘点单编号；幂等命中时返回原单据编号
     */
    Long createCountOrder(WmsCreateCountOrderBO bo);

    /**
     * 修改盘点单，只有新建状态可以改。
     *
     * <p>盘点行是按账面快照生成的，不是人录的，所以改盘点范围（仓库、库区、物料）后
     * 旧行整批删除、按新范围重新生成，`snapshot_time` 也一并刷新。
     *
     * @param orderId 盘点单编号
     * @param bo      新的盘点范围，字段集与创建时相同
     */
    void updateCountOrder(Long orderId, WmsCreateCountOrderBO bo);

    /** 删除盘点单，只有新建状态可以删，头与行一起逻辑删除。 */
    void deleteCountOrder(Long orderId);

    /**
     * 冻结盘点范围，新建推进到已冻结，幂等。
     *
     * <p>逐行按账面数量做 {@code FREEZE} 过账，冻结量进 {@code qty_frozen} 从而反映在可用量里。
     * 冻结不是打个标记：盘点期间如果可用量不变，照样能把正在盘的货领走，盘完就对不上了。
     */
    void freezeCountOrder(WmsSubmitOrderBO bo);

    /** 提交实盘结果，计算差异数量 = 实盘数量 - 账面数量。 */
    void submitCountResult(WmsSubmitCountResultBO bo);

    /** 确认盘点差异：生成盘盈入库单或盘亏出库单并过账，禁止直接修改余额。 */
    void confirmCountDiff(WmsConfirmCountDiffBO bo);

    /** 提交盘点差异审批：发起 BPM 流程，审批通过后由回调逐行确认差异。 */
    void submitCountDiffApproval(Long countOrderId);

    /**
     * 回写盘点差异审批终态（三层幂等）。
     *
     * <p>通过：在审批终态事务提交后逐行确认差异（盘盈入库 / 盘亏出库过账）；驳回与撤销：单据退回
     * {@code COUNTING} 要求重盘，审批期间未过账，无需冲销。
     *
     * @param countOrderId      盘点单编号
     * @param processInstanceId 流程实例编号
     * @param status            流程终态，取值见 {@code BpmProcessInstanceStatusEnum}
     * @param reason            审批意见或驳回原因
     */
    void updateCountDiffApprovalStatus(Long countOrderId, String processInstanceId,
                                       Integer status, String reason);

    WmsCountOrderDO getCountOrder(Long id);

    List<WmsCountOrderLineDO> getCountOrderLineList(Long orderId);

}
