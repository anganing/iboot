package com.iboot.pf.module.wms.dal.mysql.transferorderline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS-库存转移调拨明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TransferOrderLineMapper extends BaseMapperX<TransferOrderLineDO> {

    default PageResult<TransferOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TransferOrderLineDO>()
            .eq(TransferOrderLineDO::getOrderId, orderId)
            .orderByDesc(TransferOrderLineDO::getId));
    }

    default int deleteByOrderId(Long orderId) {
        return delete(TransferOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(TransferOrderLineDO::getOrderId, orderIds);
	}

}