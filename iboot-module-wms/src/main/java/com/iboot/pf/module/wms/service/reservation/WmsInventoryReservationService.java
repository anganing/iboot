package com.iboot.pf.module.wms.service.reservation;

import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 库存预留 Service。
 *
 * <p>预留只占用可用量，不扣减在库量；在库量的扣减发生在出库过账，届时通过核销消掉预留。
 */
public interface WmsInventoryReservationService {

    /**
     * 预留库存：校验可用量充足后占用，幂等。
     *
     * @param bo 预留参数
     * @return 预留编号；幂等命中时返回原预留编号
     */
    Long reserve(WmsReserveBO bo);

    /**
     * 释放预留：把占用量还回可用量，幂等。
     *
     * @param bo 释放参数
     */
    void release(WmsReservationHandleBO bo);

    /**
     * 核销预留：库存实际出库后消掉占用，幂等。
     *
     * @param bo 核销参数
     */
    void consume(WmsReservationHandleBO bo);

    WmsInvReservationDO getReservation(Long id);

    List<WmsInvReservationDO> getReservationList(Collection<Long> ids);

    Map<Long, WmsInvReservationDO> getReservationMap(Collection<Long> ids);

}
