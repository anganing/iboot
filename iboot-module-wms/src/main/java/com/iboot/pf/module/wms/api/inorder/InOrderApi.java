package com.iboot.pf.module.wms.api.inorder;

import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;

/**
 * 入库单 API 门面。
 *
 * <p>向 MES / Integration / UI 提供入库单创建、取消、过账。
 * 单据状态：DRAFT(新建)、SUBMITTED(已确认)、DONE(已过账)、CANCELLED(已取消)。
 * 只有新建状态可取消；只有新建或已确认状态可过账。
 */
public interface InOrderApi {

    /**
     * 创建入库单，幂等。
     *
     * @param reqDTO 创建命令
     * @return 入库单编号；幂等命中时返回原编号
     */
    Long createInOrder(CreateInOrderReqDTO reqDTO);

    /**
     * 取消入库单，只有新建状态可以取消。
     *
     * @param reqDTO 取消命令
     */
    void cancelInOrder(CancelOrderReqDTO reqDTO);

    /**
     * 过账入库单：写库存流水并增加库存余额，只有新建或已确认状态可以过账。
     *
     * @param reqDTO 过账命令
     */
    void confirmInOrder(ConfirmOrderReqDTO reqDTO);

}
