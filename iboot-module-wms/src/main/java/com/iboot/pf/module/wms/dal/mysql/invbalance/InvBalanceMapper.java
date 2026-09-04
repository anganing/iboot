package com.iboot.pf.module.wms.dal.mysql.invbalance;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;

/**
 * WMS-多维库存余额 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InvBalanceMapper extends BaseMapperX<WmsInvBalanceDO> {

    default PageResult<InvBalanceRespVO> selectPage(InvBalancePageReqVO reqVO) {
        IPage<InvBalanceRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvBalanceRespVO selectJoinById(@Param("id") Long id);

    IPage<InvBalanceRespVO> selectJoinPage(IPage<InvBalanceRespVO> page, @Param("req") InvBalancePageReqVO reqVO);

}