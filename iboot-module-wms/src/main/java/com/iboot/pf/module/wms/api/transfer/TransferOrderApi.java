package com.iboot.pf.module.wms.api.transfer;

import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.transfer.dto.CreateTransferOrderReqDTO;

/**
 * 转移调拨单 API 门面。
 *
 * <p>向 MES / Integration / UI 提供转移单创建、取消、过账。
 * 支持库位、仓库、库存组织、项目、质量状态转移，共用同一套过账逻辑。
 * 单据状态：DRAFT(新建)、SUBMITTED(已确认)、DONE(已过账)、CANCELLED(已取消)。
 */
public interface TransferOrderApi {

    /**
     * 创建转移单，幂等。
     *
     * @param reqDTO 创建命令
     * @return 转移单编号；幂等命中时返回原编号
     */
    Long createTransferOrder(CreateTransferOrderReqDTO reqDTO);

    /**
     * 取消转移单，只有新建状态可以取消。
     *
     * @param reqDTO 取消命令
     */
    void cancelTransferOrder(CancelOrderReqDTO reqDTO);

    /**
     * 过账转移单：来源维度扣减、目标维度增加，两条余额变更在同一事务内。
     *
     * @param reqDTO 过账命令
     */
    void confirmTransferOrder(ConfirmOrderReqDTO reqDTO);

}
