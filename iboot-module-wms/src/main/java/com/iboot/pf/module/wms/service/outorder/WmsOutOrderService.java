package com.iboot.pf.module.wms.service.outorder;

import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;

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

    /** 取消出库单，只有新建状态可以取消。 */
    void cancelOutOrder(WmsCancelOrderBO bo);

    /** 过账出库单：校验库位、批次、可用量后写库存流水并扣减库存余额。 */
    void confirmOutOrder(WmsConfirmOrderBO bo);

    WmsOutOrderDO getOutOrder(Long id);

    List<WmsOutOrderLineDO> getOutOrderLineList(Long orderId);

}
