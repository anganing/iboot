package com.iboot.pf.module.mes.dal.mysql.prodorder;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.*;

/**
 * MES-生产工单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdOrderMapper extends BaseMapperX<ProdOrderDO> {

    default PageResult<ProdOrderRespVO> selectPage(ProdOrderPageReqVO reqVO) {
        IPage<ProdOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdOrderRespVO> selectJoinPage(IPage<ProdOrderRespVO> page, @Param("req") ProdOrderPageReqVO reqVO);

}