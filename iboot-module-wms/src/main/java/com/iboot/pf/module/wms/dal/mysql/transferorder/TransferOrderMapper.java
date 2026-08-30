package com.iboot.pf.module.wms.dal.mysql.transferorder;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.transferorder.TransferOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;

/**
 * WMS-库存转移调拨单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TransferOrderMapper extends BaseMapperX<TransferOrderDO> {

    default PageResult<TransferOrderRespVO> selectPage(TransferOrderPageReqVO reqVO) {
        IPage<TransferOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TransferOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<TransferOrderRespVO> selectJoinPage(IPage<TransferOrderRespVO> page, @Param("req") TransferOrderPageReqVO reqVO);

}