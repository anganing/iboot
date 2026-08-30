package com.iboot.pf.module.wms.dal.mysql.transfer;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import org.apache.ibatis.annotations.Mapper;

/** 库存转移调拨单 Mapper。 */
@Mapper
public interface WmsTransferOrderMapper extends BaseMapperX<WmsTransferOrderDO> {

    /** 按幂等键查转移单，创建命令判重使用。 */
    default WmsTransferOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsTransferOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsTransferOrderDO selectByTransferNo(String transferNo) {
        return selectOne(WmsTransferOrderDO::getTransferNo, transferNo);
    }

}
