package com.iboot.pf.module.mes.dal.mysql.qcplan;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.*;

/**
 * MES-检验方案 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcPlanMapper extends BaseMapperX<QcPlanDO> {

    default PageResult<QcPlanRespVO> selectPage(QcPlanPageReqVO reqVO) {
        IPage<QcPlanRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcPlanRespVO selectJoinById(@Param("id") Long id);

    IPage<QcPlanRespVO> selectJoinPage(IPage<QcPlanRespVO> page, @Param("req") QcPlanPageReqVO reqVO);

}