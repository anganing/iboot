package com.iboot.pf.module.mdm.dal.mysql.workshop;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.workshop.vo.*;

/**
 * 车间 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WorkshopMapper extends BaseMapperX<WorkshopDO> {

    default PageResult<WorkshopDO> selectPage(WorkshopPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WorkshopDO>()
                .eqIfPresent(WorkshopDO::getPlantId, reqVO.getPlantId())
                .eqIfPresent(WorkshopDO::getCode, reqVO.getCode())
                .likeIfPresent(WorkshopDO::getName, reqVO.getName())
                .eqIfPresent(WorkshopDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WorkshopDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WorkshopDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WorkshopDO::getId));
    }

}