package com.iboot.pf.module.wms.dal.mysql.outorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 出库单明细 Mapper。 */
@Mapper
public interface WmsOutOrderLineMapper extends BaseMapperX<WmsOutOrderLineDO> {

    default List<WmsOutOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsOutOrderLineDO>()
                .eq(WmsOutOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsOutOrderLineDO::getLineNo));
    }

}
