package com.iboot.pf.module.wms.dal.mysql.inorderline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS-入库单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InOrderLineMapper extends BaseMapperX<InOrderLineDO> {

    default PageResult<InOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InOrderLineDO>()
            .eq(InOrderLineDO::getOrderId, orderId)
            .orderByDesc(InOrderLineDO::getId));
    }

    default int deleteByOrderId(Long orderId) {
        return delete(InOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(InOrderLineDO::getOrderId, orderIds);
	}

}