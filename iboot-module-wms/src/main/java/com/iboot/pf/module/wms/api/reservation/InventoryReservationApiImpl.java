package com.iboot.pf.module.wms.api.reservation;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.reservation.dto.CreateReservationReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationRespDTO;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationService;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 库存预留 API 实现类。
 *
 * <p>命令方法由 {@link WmsInventoryReservationService} 承载事务与幂等，本类只做参数转换。
 */
@Service
@Validated
public class InventoryReservationApiImpl implements InventoryReservationApi {

    @Resource
    private WmsInventoryReservationService reservationService;

    @Override
    public Long reserveInventory(CreateReservationReqDTO reqDTO) {
        WmsReserveBO bo = BeanUtils.toBean(reqDTO, WmsReserveBO.class);
        bo.setBizNo(reqDTO.getSourceBizNo());
        bo.setBizLineNo(reqDTO.getSourceBizLineNo());
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        return reservationService.reserve(bo);
    }

    @Override
    public void releaseInventory(ReservationHandleReqDTO reqDTO) {
        WmsReservationHandleBO bo = toHandleBO(reqDTO);
        reservationService.release(bo);
    }

    @Override
    public void consumeReservation(ReservationHandleReqDTO reqDTO) {
        WmsReservationHandleBO bo = toHandleBO(reqDTO);
        reservationService.consume(bo);
    }

    private WmsReservationHandleBO toHandleBO(ReservationHandleReqDTO reqDTO) {
        WmsReservationHandleBO bo = new WmsReservationHandleBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setReservationId(reqDTO.getReservationId());
        bo.setReservationNo(reqDTO.getReservationNo());
        bo.setQty(reqDTO.getQty());
        bo.setBizType(reqDTO.getBizType());
        bo.setBizNo(reqDTO.getSourceBizNo());
        bo.setBizLineNo(reqDTO.getSourceBizLineNo());
        bo.setRemark(reqDTO.getRemark());
        return bo;
    }

    @Override
    @DataPermission(enable = false)
    public ReservationRespDTO getReservation(Long id) {
        return BeanUtils.toBean(reservationService.getReservation(id), ReservationRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ReservationRespDTO> getReservationList(Collection<Long> ids) {
        return BeanUtils.toBean(reservationService.getReservationList(ids), ReservationRespDTO.class);
    }

    @Override
    public Map<Long, ReservationRespDTO> getReservationMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(reservationService.getReservationList(ids), ReservationRespDTO.class),
                ReservationRespDTO::getId);
    }

}
