package com.iboot.pf.module.mdm.dal.mysql.supplier;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.supplier.SupplierDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.supplier.vo.*;

/**
 * 供应商 Mapper
 *
 * @author iboot
 */
@Mapper
public interface SupplierMapper extends BaseMapperX<SupplierDO> {

    default PageResult<SupplierDO> selectPage(SupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SupplierDO>()
                .eqIfPresent(SupplierDO::getCode, reqVO.getCode())
                .likeIfPresent(SupplierDO::getName, reqVO.getName())
                .eqIfPresent(SupplierDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SupplierDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(SupplierDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SupplierDO::getId));
    }

}