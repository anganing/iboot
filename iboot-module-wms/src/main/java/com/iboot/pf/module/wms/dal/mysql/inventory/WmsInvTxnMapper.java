package com.iboot.pf.module.wms.dal.mysql.inventory;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 库存流水 Mapper。 */
@Mapper
public interface WmsInvTxnMapper extends BaseMapperX<WmsInvTxnDO> {

    /** 按幂等键查流水，过账判重使用。 */
    default WmsInvTxnDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsInvTxnDO::getIdempotencyKey, idempotencyKey);
    }

    default List<WmsInvTxnDO> selectTxnList(WmsInventoryTxnQueryBO query) {
        return selectList(new LambdaQueryWrapperX<WmsInvTxnDO>()
                .eqIfPresent(WmsInvTxnDO::getInvOrgId, query.getInvOrgId())
                .eqIfPresent(WmsInvTxnDO::getWhId, query.getWhId())
                .eqIfPresent(WmsInvTxnDO::getMatId, query.getMatId())
                .eqIfPresent(WmsInvTxnDO::getBatchId, query.getBatchId())
                .eqIfPresent(WmsInvTxnDO::getTxnType, query.getTxnType())
                .eqIfPresent(WmsInvTxnDO::getBizType, query.getBizType())
                .eqIfPresent(WmsInvTxnDO::getBizNo, query.getBizNo())
                .geIfPresent(WmsInvTxnDO::getCreateTime, query.getFromTime())
                .leIfPresent(WmsInvTxnDO::getCreateTime, query.getToTime())
                .orderByDesc(WmsInvTxnDO::getId));
    }

}
