package com.iboot.pf.module.mdm.dal.mysql.bom;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.*;

/**
 * 物料清单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface BomMapper extends BaseMapperX<BomDO> {

    default PageResult<BomDO> selectPage(BomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BomDO>()
                .eqIfPresent(BomDO::getMatId, reqVO.getMatId())
                .eqIfPresent(BomDO::getVersion, reqVO.getVersion())
                .eqIfPresent(BomDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BomDO::getEffectiveDate, reqVO.getEffectiveDate())
                .betweenIfPresent(BomDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(BomDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(BomDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BomDO::getId));
    }

}