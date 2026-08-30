package com.iboot.pf.module.wms.dal.mysql.outorderline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.outorderline.OutOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS-出库单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OutOrderLineMapper extends BaseMapperX<OutOrderLineDO> {

    default PageResult<OutOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OutOrderLineDO>()
            .eq(OutOrderLineDO::getOrderId, orderId)
            .orderByDesc(OutOrderLineDO::getId));
    }

    default int deleteByOrderId(Long orderId) {
        return delete(OutOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(OutOrderLineDO::getOrderId, orderIds);
	}

}