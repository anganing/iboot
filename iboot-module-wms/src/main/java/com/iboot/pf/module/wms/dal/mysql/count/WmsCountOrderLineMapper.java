package com.iboot.pf.module.wms.dal.mysql.count;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 盘点明细 Mapper。 */
@Mapper
public interface WmsCountOrderLineMapper extends BaseMapperX<WmsCountOrderLineDO> {

    default List<WmsCountOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsCountOrderLineDO>()
                .eq(WmsCountOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsCountOrderLineDO::getLineNo));
    }

}
