package com.iboot.pf.module.mdm.dal.mysql.wc;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.*;

/**
 * 工作中心 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WcMapper extends BaseMapperX<WcDO> {

    default PageResult<WcDO> selectPage(WcPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WcDO>()
                .eqIfPresent(WcDO::getWorkshopId, reqVO.getWorkshopId())
                .eqIfPresent(WcDO::getCode, reqVO.getCode())
                .likeIfPresent(WcDO::getName, reqVO.getName())
                .eqIfPresent(WcDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WcDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WcDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WcDO::getId));
    }

}