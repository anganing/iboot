package com.iboot.pf.module.wms.service.inorder;

import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;

import java.util.List;

/** 入库单 Service。 */
public interface WmsInOrderService {

    /**
     * 创建入库单，幂等。
     *
     * @param bo 创建参数
     * @return 入库单编号；幂等命中时返回原单据编号
     */
    Long createInOrder(WmsCreateInOrderBO bo);

    /** 取消入库单，只有新建状态可以取消。 */
    void cancelInOrder(WmsCancelOrderBO bo);

    /** 过账入库单：写库存流水并增加库存余额。 */
    void confirmInOrder(WmsConfirmOrderBO bo);

    WmsInOrderDO getInOrder(Long id);

    List<WmsInOrderLineDO> getInOrderLineList(Long orderId);

}
