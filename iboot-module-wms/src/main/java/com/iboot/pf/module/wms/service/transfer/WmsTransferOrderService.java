package com.iboot.pf.module.wms.service.transfer;

import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.transfer.bo.WmsCreateTransferOrderBO;

import java.util.List;

/** 转移调拨单 Service。库位、仓库、库存组织、项目、质量状态转移共用同一套过账逻辑。 */
public interface WmsTransferOrderService {

    /**
     * 创建转移单，幂等。
     *
     * @param bo 创建参数
     * @return 转移单编号；幂等命中时返回原单据编号
     */
    Long createTransferOrder(WmsCreateTransferOrderBO bo);

    /**
     * 修改转移单，只有新建状态可以改。
     *
     * <p>行按全量替换处理：旧行整批删除后按新参数重建。已提交的单据不许改数量与库位，
     * 要改先取消再重开。
     *
     * @param orderId 转移单编号
     * @param bo      新的单据内容，字段集与创建时相同
     */
    void updateTransferOrder(Long orderId, WmsCreateTransferOrderBO bo);

    /** 删除转移单，只有新建状态可以删，头与行一起逻辑删除。 */
    void deleteTransferOrder(Long orderId);

    /** 提交转移单，新建推进到已提交，幂等。 */
    void submitTransferOrder(WmsSubmitOrderBO bo);

    /** 取消转移单，只有新建状态可以取消。 */
    void cancelTransferOrder(WmsCancelOrderBO bo);

    /** 过账转移单：来源维度扣减、目标维度增加，两条余额变更在同一事务内。 */
    void confirmTransferOrder(WmsConfirmOrderBO bo);

    WmsTransferOrderDO getTransferOrder(Long id);

    List<WmsTransferOrderLineDO> getTransferOrderLineList(Long orderId);

}
