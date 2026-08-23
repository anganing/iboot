package com.iboot.pf.module.mdm.dal.mysql.res;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.res.vo.*;

/**
 * 生产资源 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResMapper extends BaseMapperX<ResDO> {

    default PageResult<ResDO> selectPage(ResPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ResDO>()
                .eqIfPresent(ResDO::getWcId, reqVO.getWcId())
                .eqIfPresent(ResDO::getResType, reqVO.getResType())
                .eqIfPresent(ResDO::getCode, reqVO.getCode())
                .likeIfPresent(ResDO::getName, reqVO.getName())
                .eqIfPresent(ResDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ResDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ResDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ResDO::getId));
    }

}