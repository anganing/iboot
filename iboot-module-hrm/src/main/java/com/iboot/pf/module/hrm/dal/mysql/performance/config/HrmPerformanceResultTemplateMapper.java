package com.iboot.pf.module.hrm.dal.mysql.performance.config;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplatePageReqVO;
import com.iboot.pf.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmPerformanceResultTemplateMapper extends BaseMapperX<HrmPerformanceResultTemplateDO> {

    default PageResult<HrmPerformanceResultTemplateDO> selectPage(HrmPerformanceResultTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmPerformanceResultTemplateDO>()
                .likeIfPresent(HrmPerformanceResultTemplateDO::getName, reqVO.getName())
                .eq(HrmPerformanceResultTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(HrmPerformanceResultTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceResultTemplateDO::getId));
    }

    default HrmPerformanceResultTemplateDO selectByName(String name) {
        return selectFirstOne(HrmPerformanceResultTemplateDO::getName, name,
                HrmPerformanceResultTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

    default List<HrmPerformanceResultTemplateDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceResultTemplateDO>()
                .eqIfPresent(HrmPerformanceResultTemplateDO::getStatus, status)
                .orderByDesc(HrmPerformanceResultTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceResultTemplateDO::getId));
    }

}
