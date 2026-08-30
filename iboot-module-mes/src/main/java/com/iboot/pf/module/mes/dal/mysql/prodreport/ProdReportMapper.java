package com.iboot.pf.module.mes.dal.mysql.prodreport;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.*;

/**
 * MES-生产报工记录 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdReportMapper extends BaseMapperX<ProdReportDO> {

    default PageResult<ProdReportRespVO> selectPage(ProdReportPageReqVO reqVO) {
        IPage<ProdReportRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdReportRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdReportRespVO> selectJoinPage(IPage<ProdReportRespVO> page, @Param("req") ProdReportPageReqVO reqVO);

}