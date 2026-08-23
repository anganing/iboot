package com.iboot.pf.module.mdm.dal.mysql.shift;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.shift.vo.*;

/**
 * 班次 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ShiftMapper extends BaseMapperX<ShiftDO> {

    default PageResult<ShiftDO> selectPage(ShiftPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ShiftDO>()
                .eqIfPresent(ShiftDO::getCode, reqVO.getCode())
                .likeIfPresent(ShiftDO::getName, reqVO.getName())
                .betweenIfPresent(ShiftDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(ShiftDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(ShiftDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ShiftDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ShiftDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ShiftDO::getId));
    }

}