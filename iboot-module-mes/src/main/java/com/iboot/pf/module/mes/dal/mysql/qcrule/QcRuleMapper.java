package com.iboot.pf.module.mes.dal.mysql.qcrule;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.*;

/**
 * MES-质检触发规则 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcRuleMapper extends BaseMapperX<QcRuleDO> {

    default PageResult<QcRuleRespVO> selectPage(QcRulePageReqVO reqVO) {
        IPage<QcRuleRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcRuleRespVO selectJoinById(@Param("id") Long id);

    IPage<QcRuleRespVO> selectJoinPage(IPage<QcRuleRespVO> page, @Param("req") QcRulePageReqVO reqVO);

}