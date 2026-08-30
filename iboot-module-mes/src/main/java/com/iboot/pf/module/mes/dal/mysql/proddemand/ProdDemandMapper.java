package com.iboot.pf.module.mes.dal.mysql.proddemand;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.*;

/**
 * MES-生产需求 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdDemandMapper extends BaseMapperX<ProdDemandDO> {

    default PageResult<ProdDemandRespVO> selectPage(ProdDemandPageReqVO reqVO) {
        IPage<ProdDemandRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdDemandRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdDemandRespVO> selectJoinPage(IPage<ProdDemandRespVO> page, @Param("req") ProdDemandPageReqVO reqVO);

}