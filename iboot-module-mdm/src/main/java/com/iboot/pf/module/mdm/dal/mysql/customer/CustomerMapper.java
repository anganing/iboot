package com.iboot.pf.module.mdm.dal.mysql.customer;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.customer.CustomerDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.customer.vo.*;

/**
 * 客户 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CustomerMapper extends BaseMapperX<CustomerDO> {

    default PageResult<CustomerDO> selectPage(CustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerDO>()
                .eqIfPresent(CustomerDO::getCode, reqVO.getCode())
                .likeIfPresent(CustomerDO::getName, reqVO.getName())
                .eqIfPresent(CustomerDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CustomerDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CustomerDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CustomerDO::getId));
    }

}