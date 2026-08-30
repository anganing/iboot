package com.iboot.pf.module.wms.dal.mysql.lock;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockQueryBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 库存冻结记录 Mapper。 */
@Mapper
public interface WmsInvLockMapper extends BaseMapperX<WmsInvLockDO> {

    /** 按幂等键查冻结记录，冻结命令判重使用。 */
    default WmsInvLockDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsInvLockDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsInvLockDO selectByLockNo(String lockNo) {
        return selectOne(WmsInvLockDO::getLockNo, lockNo);
    }

    default List<WmsInvLockDO> selectLockList(WmsLockQueryBO query) {
        return selectList(new LambdaQueryWrapperX<WmsInvLockDO>()
                .eqIfPresent(WmsInvLockDO::getInvOrgId, query.getInvOrgId())
                .eqIfPresent(WmsInvLockDO::getWhId, query.getWhId())
                .eqIfPresent(WmsInvLockDO::getLocId, query.getLocId())
                .eqIfPresent(WmsInvLockDO::getMatId, query.getMatId())
                .eqIfPresent(WmsInvLockDO::getBatchId, query.getBatchId())
                .eqIfPresent(WmsInvLockDO::getContainerId, query.getContainerId())
                .eqIfPresent(WmsInvLockDO::getStatus, query.getStatus())
                .orderByDesc(WmsInvLockDO::getId));
    }

}
