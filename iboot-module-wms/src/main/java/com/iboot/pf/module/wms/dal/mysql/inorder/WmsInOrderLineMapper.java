package com.iboot.pf.module.wms.dal.mysql.inorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 入库单明细 Mapper。 */
@Mapper
public interface WmsInOrderLineMapper extends BaseMapperX<WmsInOrderLineDO> {

    default List<WmsInOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsInOrderLineDO>()
                .eq(WmsInOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsInOrderLineDO::getLineNo));
    }

}
