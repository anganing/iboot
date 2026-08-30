package com.iboot.pf.module.wms.dal.mysql.countorderline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.countorderline.CountOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS-盘点明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CountOrderLineMapper extends BaseMapperX<CountOrderLineDO> {

    default PageResult<CountOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CountOrderLineDO>()
            .eq(CountOrderLineDO::getOrderId, orderId)
            .orderByDesc(CountOrderLineDO::getId));
    }

    default int deleteByOrderId(Long orderId) {
        return delete(CountOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(CountOrderLineDO::getOrderId, orderIds);
	}

}