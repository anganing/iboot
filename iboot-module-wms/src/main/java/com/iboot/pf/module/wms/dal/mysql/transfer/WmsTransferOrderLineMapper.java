package com.iboot.pf.module.wms.dal.mysql.transfer;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 库存转移调拨明细 Mapper。 */
@Mapper
public interface WmsTransferOrderLineMapper extends BaseMapperX<WmsTransferOrderLineDO> {

    default List<WmsTransferOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsTransferOrderLineDO>()
                .eq(WmsTransferOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsTransferOrderLineDO::getLineNo));
    }

}
