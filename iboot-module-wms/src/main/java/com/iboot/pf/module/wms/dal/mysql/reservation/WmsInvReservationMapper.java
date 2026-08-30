package com.iboot.pf.module.wms.dal.mysql.reservation;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import org.apache.ibatis.annotations.Mapper;

/** 库存预留 Mapper。 */
@Mapper
public interface WmsInvReservationMapper extends BaseMapperX<WmsInvReservationDO> {

    /** 按幂等键查预留，预留命令判重使用。 */
    default WmsInvReservationDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsInvReservationDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsInvReservationDO selectByReservationNo(String reservationNo) {
        return selectOne(WmsInvReservationDO::getReservationNo, reservationNo);
    }

}
