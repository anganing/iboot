package com.iboot.pf.module.mdm.dal.mysql.plant;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.*;

/**
 * 工厂 Mapper
 *
 * @author iboot
 */
@Mapper
public interface PlantMapper extends BaseMapperX<PlantDO> {

    default PageResult<PlantDO> selectPage(PlantPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PlantDO>()
                .eqIfPresent(PlantDO::getInvOrgId, reqVO.getInvOrgId())
                .eqIfPresent(PlantDO::getCode, reqVO.getCode())
                .likeIfPresent(PlantDO::getName, reqVO.getName())
                .eqIfPresent(PlantDO::getAddress, reqVO.getAddress())
                .eqIfPresent(PlantDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PlantDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(PlantDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PlantDO::getId));
    }

}