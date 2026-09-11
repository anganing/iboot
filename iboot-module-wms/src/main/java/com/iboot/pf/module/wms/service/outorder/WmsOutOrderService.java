package com.iboot.pf.module.wms.service.outorder;

import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.api.outorder.dto.ResolveOqcTaskReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.CompleteOqcReqDTO;

import java.util.List;

/** 出库单 Service。 */
public interface WmsOutOrderService {

    /**
     * 创建出库单，幂等。
     *
     * @param bo 创建参数
     * @return 出库单编号；幂等命中时返回原单据编号
     */
    Long createOutOrder(WmsCreateOutOrderBO bo);

    /**
     * 修改出库单，只有新建状态可以改。
     *
     * <p>行按全量替换处理：旧行整批删除后按新参数重建。已提交的单据不许改数量与库位，
     * 要改先取消再重开。
     *
     * @param orderId 出库单编号
     * @param bo      新的单据内容，字段集与创建时相同
     */
    void updateOutOrder(Long orderId, WmsCreateOutOrderBO bo);

    /** 删除出库单，只有新建状态可以删，头与行一起逻辑删除。 */
    void deleteOutOrder(Long orderId);

    /** 提交出库单，新建推进到已提交，幂等。 */
    void submitOutOrder(WmsSubmitOrderBO bo);

    /** 取消出库单，只有新建状态可以取消。 */
    void cancelOutOrder(WmsCancelOrderBO bo);

    /** 过账出库单：校验库位、批次、可用量后写库存流水并扣减库存余额。 */
    void confirmOutOrder(WmsConfirmOrderBO bo);

    /** 拣选完成。销售出库会触发 OQC 规则解析。 */
    void pickOutOrder(WmsConfirmOrderBO bo);

    /** 回写 OQC 规则解析结果，供 MES 跨模块监听器调用。 */
    void resolveOqcTask(ResolveOqcTaskReqDTO reqDTO);

    /** OQC 合格后放行确认发货。 */
    void completeOqc(CompleteOqcReqDTO reqDTO);

    WmsOutOrderDO getOutOrder(Long id);

    List<WmsOutOrderLineDO> getOutOrderLineList(Long orderId);

}
