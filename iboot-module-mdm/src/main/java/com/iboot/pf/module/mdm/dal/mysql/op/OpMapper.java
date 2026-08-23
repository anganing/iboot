package com.iboot.pf.module.mdm.dal.mysql.op;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.op.vo.*;

/**
 * 标准工序 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OpMapper extends BaseMapperX<OpDO> {

    default PageResult<OpDO> selectPage(OpPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OpDO>()
                .eqIfPresent(OpDO::getCode, reqVO.getCode())
                .likeIfPresent(OpDO::getName, reqVO.getName())
                .eqIfPresent(OpDO::getStdDuration, reqVO.getStdDuration())
                .eqIfPresent(OpDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OpDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(OpDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OpDO::getId));
    }

}