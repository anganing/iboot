package com.iboot.pf.module.wms.api.outorder;

import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;

import java.util.Collection;
import java.util.Map;

/**
 * 出库单 API 门面。
 *
 * <p>向 MES / Integration / UI 提供出库单创建、取消、过账。
 * 单据状态：DRAFT(新建)、SUBMITTED(已确认)、DONE(已过账)、CANCELLED(已取消)。
 * 只有新建状态可取消；只有新建或已确认状态可过账。
 */
public interface OutOrderApi {

    /**
     * 创建出库单，幂等。
     *
     * @param reqDTO 创建命令
     * @return 出库单编号；幂等命中时返回原编号
     */
    Long createOutOrder(CreateOutOrderReqDTO reqDTO);

    /**
     * 取消出库单，只有新建状态可以取消。
     *
     * @param reqDTO 取消命令
     */
    void cancelOutOrder(CancelOrderReqDTO reqDTO);

    /**
     * 过账出库单：校验库位、批次、可用量后写库存流水并扣减库存余额。
     *
     * @param reqDTO 过账命令
     */
    void confirmOutOrder(ConfirmOrderReqDTO reqDTO);

    /**
     * 按编号批量反查出库单的可读字段。
     *
     * <p>{@link #createOutOrder} 只返回单据 id，调用方要落真实的出库单号或展示单据状态时，
     * 用这个方法把 id 换成 {@code outNo} 等可读字段，避免在业务表里存裸 id。
     *
     * @param ids 出库单编号集合
     * @return 编号到出库单只读事实的映射；空集合返回空 Map，不抛异常
     */
    Map<Long, OutOrderRespDTO> getOutOrderMap(Collection<Long> ids);

}
