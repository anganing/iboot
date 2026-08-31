package com.iboot.pf.module.wms.dal.mysql.arrival;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;

/**
 * WMS-到货登记 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ArrivalMapper extends BaseMapperX<ArrivalDO> {

    default PageResult<ArrivalRespVO> selectPage(ArrivalPageReqVO reqVO) {
        IPage<ArrivalRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    /** 按到货单号查询，唯一索引 uk_wms_arrival_no_deleted 的应用层前置校验。 */
    default ArrivalDO selectByArrivalNo(String arrivalNo) {
        return selectOne(ArrivalDO::getArrivalNo, arrivalNo);
    }

    ArrivalRespVO selectJoinById(@Param("id") Long id);

    IPage<ArrivalRespVO> selectJoinPage(IPage<ArrivalRespVO> page, @Param("req") ArrivalPageReqVO reqVO);

}
