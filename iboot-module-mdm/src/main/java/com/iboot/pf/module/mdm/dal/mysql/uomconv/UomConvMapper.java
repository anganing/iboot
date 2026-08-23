package com.iboot.pf.module.mdm.dal.mysql.uomconv;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.*;

/**
 * 单位换算 Mapper
 *
 * @author iboot
 */
@Mapper
public interface UomConvMapper extends BaseMapperX<UomConvDO> {

    default PageResult<UomConvDO> selectPage(UomConvPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UomConvDO>()
                .eqIfPresent(UomConvDO::getMatId, reqVO.getMatId())
                .eqIfPresent(UomConvDO::getFromUomId, reqVO.getFromUomId())
                .eqIfPresent(UomConvDO::getToUomId, reqVO.getToUomId())
                .eqIfPresent(UomConvDO::getRatio, reqVO.getRatio())
                .eqIfPresent(UomConvDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(UomConvDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UomConvDO::getId));
    }

}