package com.iboot.pf.module.wms.dal.mysql.inorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import org.apache.ibatis.annotations.Mapper;

/** 入库单 Mapper。 */
@Mapper
public interface WmsInOrderMapper extends BaseMapperX<WmsInOrderDO> {

    /** 按幂等键查入库单，创建命令判重使用。 */
    default WmsInOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsInOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsInOrderDO selectByInNo(String inNo) {
        return selectOne(WmsInOrderDO::getInNo, inNo);
    }

}
