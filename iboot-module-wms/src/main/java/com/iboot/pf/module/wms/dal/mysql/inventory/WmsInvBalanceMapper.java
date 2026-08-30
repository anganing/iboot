package com.iboot.pf.module.wms.dal.mysql.inventory;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 多维库存余额 Mapper。 */
@Mapper
public interface WmsInvBalanceMapper extends BaseMapperX<WmsInvBalanceDO> {

    /** 按库存维度唯一定位一行余额，不存在返回 null。维度字段为 null 表示不限定该维度。 */
    default WmsInvBalanceDO selectByDim(WmsInvBalanceDO dim) {
        return selectOne(new LambdaQueryWrapperX<WmsInvBalanceDO>()
                .eqIfPresent(WmsInvBalanceDO::getInvOrgId, dim.getInvOrgId())
                .eqIfPresent(WmsInvBalanceDO::getPlantId, dim.getPlantId())
                .eqIfPresent(WmsInvBalanceDO::getWhId, dim.getWhId())
                .eqIfPresent(WmsInvBalanceDO::getLocId, dim.getLocId())
                .eqIfPresent(WmsInvBalanceDO::getMatId, dim.getMatId())
                .eqIfPresent(WmsInvBalanceDO::getBatchId, dim.getBatchId())
                .eqIfPresent(WmsInvBalanceDO::getContainerId, dim.getContainerId())
                .eqIfPresent(WmsInvBalanceDO::getProjectId, dim.getProjectId())
                .eqIfPresent(WmsInvBalanceDO::getProjectTaskId, dim.getProjectTaskId())
                .eqIfPresent(WmsInvBalanceDO::getStockScope, dim.getStockScope())
                .eqIfPresent(WmsInvBalanceDO::getQualityStatus, dim.getQualityStatus()));
    }

    default List<WmsInvBalanceDO> selectBalanceList(WmsInventoryQueryBO query) {
        return selectList(buildWrapper(query));
    }

    default PageResult<WmsInvBalanceDO> selectBalancePage(PageParam pageParam, WmsInventoryQueryBO query) {
        return selectPage(pageParam, buildWrapper(query));
    }

    private LambdaQueryWrapperX<WmsInvBalanceDO> buildWrapper(WmsInventoryQueryBO query) {
        return new LambdaQueryWrapperX<WmsInvBalanceDO>()
                .eqIfPresent(WmsInvBalanceDO::getInvOrgId, query.getInvOrgId())
                .eqIfPresent(WmsInvBalanceDO::getPlantId, query.getPlantId())
                .eqIfPresent(WmsInvBalanceDO::getWhId, query.getWhId())
                .eqIfPresent(WmsInvBalanceDO::getLocId, query.getLocId())
                .inIfPresent(WmsInvBalanceDO::getLocId, query.getLocIds())
                .eqIfPresent(WmsInvBalanceDO::getMatId, query.getMatId())
                .inIfPresent(WmsInvBalanceDO::getMatId, query.getMatIds())
                .eqIfPresent(WmsInvBalanceDO::getBatchId, query.getBatchId())
                .eqIfPresent(WmsInvBalanceDO::getContainerId, query.getContainerId())
                .eqIfPresent(WmsInvBalanceDO::getProjectId, query.getProjectId())
                .eqIfPresent(WmsInvBalanceDO::getProjectTaskId, query.getProjectTaskId())
                .eqIfPresent(WmsInvBalanceDO::getStockScope, query.getStockScope())
                .eqIfPresent(WmsInvBalanceDO::getQualityStatus, query.getQualityStatus())
                .eq(Boolean.TRUE.equals(query.getQualifiedOnly()), WmsInvBalanceDO::getQualityStatus, "QUALIFIED")
                .orderByDesc(WmsInvBalanceDO::getId);
    }

}
