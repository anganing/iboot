package com.iboot.pf.module.wms.dal.mysql.outorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import org.apache.ibatis.annotations.Mapper;

/** 出库单 Mapper。 */
@Mapper
public interface WmsOutOrderMapper extends BaseMapperX<WmsOutOrderDO> {

    /** 按幂等键查出库单，创建命令判重使用。 */
    default WmsOutOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsOutOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsOutOrderDO selectByOutNo(String outNo) {
        return selectOne(WmsOutOrderDO::getOutNo, outNo);
    }

}
