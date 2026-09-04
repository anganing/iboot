package com.iboot.pf.module.wms.dal.mysql.inventory;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

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

    default PageResult<InvTxnRespVO> selectPage(InvTxnPageReqVO reqVO) {
        IPage<InvTxnRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvTxnRespVO selectJoinById(@Param("id") Long id);

    IPage<InvTxnRespVO> selectJoinPage(IPage<InvTxnRespVO> page, @Param("req") InvTxnPageReqVO reqVO);

    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsInvTxnDO selectByTxnNo(String txnNo) {
        return selectOne(WmsInvTxnDO::getTxnNo, txnNo);
    }
}
