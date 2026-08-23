package com.iboot.pf.module.mdm.dal.mysql.matalt;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.matalt.vo.*;

/**
 * 替代料 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatAltMapper extends BaseMapperX<MatAltDO> {

    default PageResult<MatAltDO> selectPage(MatAltPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MatAltDO>()
                .eqIfPresent(MatAltDO::getMatId, reqVO.getMatId())
                .eqIfPresent(MatAltDO::getAltMatId, reqVO.getAltMatId())
                .eqIfPresent(MatAltDO::getPriority, reqVO.getPriority())
                .eqIfPresent(MatAltDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(MatAltDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MatAltDO::getId));
    }

}