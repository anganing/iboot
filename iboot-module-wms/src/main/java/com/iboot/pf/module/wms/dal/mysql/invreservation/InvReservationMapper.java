package com.iboot.pf.module.wms.dal.mysql.invreservation;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.invreservation.InvReservationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;

/**
 * WMS-库存预留 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InvReservationMapper extends BaseMapperX<InvReservationDO> {

    default PageResult<InvReservationRespVO> selectPage(InvReservationPageReqVO reqVO) {
        IPage<InvReservationRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvReservationRespVO selectJoinById(@Param("id") Long id);

    IPage<InvReservationRespVO> selectJoinPage(IPage<InvReservationRespVO> page, @Param("req") InvReservationPageReqVO reqVO);

}