package com.iboot.pf.module.wms.api.reservation;

import com.iboot.pf.module.wms.api.reservation.dto.CreateReservationReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 库存预留 API 门面。
 *
 * <p>向 MES / APS / UI 提供预留、释放、核销能力。
 * 预留只占用可用量，不扣减在库量；库存实际出库后通过核销消掉占用。
 */
public interface InventoryReservationApi {

    /**
     * 预留库存，幂等。
     *
     * @param reqDTO 预留命令
     * @return 预留记录编号；幂等命中时返回原编号
     */
    Long reserveInventory(CreateReservationReqDTO reqDTO);

    /**
     * 释放预留，幂等。
     *
     * @param reqDTO 释放命令
     */
    void releaseInventory(ReservationHandleReqDTO reqDTO);

    /**
     * 核销预留，幂等。库存实际出库后调用。
     *
     * @param reqDTO 核销命令
     */
    void consumeReservation(ReservationHandleReqDTO reqDTO);

    ReservationRespDTO getReservation(Long id);

    List<ReservationRespDTO> getReservationList(Collection<Long> ids);

    Map<Long, ReservationRespDTO> getReservationMap(Collection<Long> ids);

}
