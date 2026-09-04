package com.iboot.pf.module.wms.dal.mysql.reservation;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

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

    default PageResult<InvReservationRespVO> selectPage(InvReservationPageReqVO reqVO) {
        IPage<InvReservationRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvReservationRespVO selectJoinById(@Param("id") Long id);

    IPage<InvReservationRespVO> selectJoinPage(IPage<InvReservationRespVO> page, @Param("req") InvReservationPageReqVO reqVO);
}
