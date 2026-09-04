package com.iboot.pf.module.wms.service.inorder;

import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;

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

    /**
     * 修改入库单，只有新建状态可以改。
     *
     * <p>行按全量替换处理：旧行整批删除后按新参数重建。改行等于改执行依据，
     * 已提交的单据不许改数量与库位，要改先取消再重开。
     *
     * @param orderId 入库单编号
     * @param bo      新的单据内容，字段集与创建时相同
     */
    void updateInOrder(Long orderId, WmsCreateInOrderBO bo);

    /** 删除入库单，只有新建状态可以删，头与行一起逻辑删除。 */
    void deleteInOrder(Long orderId);

    /** 提交入库单，新建推进到已提交，幂等。 */
    void submitInOrder(WmsSubmitOrderBO bo);

    /** 取消入库单，只有新建状态可以取消。 */
    void cancelInOrder(WmsCancelOrderBO bo);

    /** 过账入库单：写库存流水并增加库存余额。 */
    void confirmInOrder(WmsConfirmOrderBO bo);

    WmsInOrderDO getInOrder(Long id);

    List<WmsInOrderLineDO> getInOrderLineList(Long orderId);

}
