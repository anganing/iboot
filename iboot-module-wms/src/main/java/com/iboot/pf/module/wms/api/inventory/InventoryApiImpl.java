package com.iboot.pf.module.wms.api.inventory;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryPageReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存 API 实现类。
 *
 * <p>查询方法关闭数据权限，避免内部跨模块调用被当前用户数据权限过滤。
 */
@Service
@Validated
public class InventoryApiImpl implements InventoryApi {

    @Resource
    private WmsInventoryService inventoryService;

    @Override
    @DataPermission(enable = false)
    public List<InventoryBalanceRespDTO> getInventoryBalanceList(InventoryQueryReqDTO query) {
        List<WmsInvBalanceDO> list = inventoryService.getBalanceList(toBO(query));
        return BeanUtils.toBean(list, InventoryBalanceRespDTO.class, this::fillAvailable);
    }

    @Override
    @DataPermission(enable = false)
    public BigDecimal getAvailableQty(InventoryQueryReqDTO query) {
        return inventoryService.getAvailableQty(toBO(query));
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, BigDecimal> getAvailableQtyMap(InventoryQueryReqDTO query) {
        return inventoryService.getAvailableQtyMap(toBO(query));
    }

    @Override
    @DataPermission(enable = false)
    public List<InventoryTxnRespDTO> getInventoryTxnList(InventoryTxnQueryReqDTO query) {
        WmsInventoryTxnQueryBO bo = new WmsInventoryTxnQueryBO();
        bo.setInvOrgId(query.getInvOrgId());
        bo.setWhId(query.getWhId());
        bo.setMatId(query.getMatId());
        bo.setBatchId(query.getBatchId());
        bo.setTxnType(query.getTxnType());
        bo.setBizType(query.getBizType());
        bo.setBizNo(query.getBizNo());
        bo.setFromTime(query.getFromTime());
        bo.setToTime(query.getToTime());
        List<WmsInvTxnDO> list = inventoryService.getTxnList(bo);
        return BeanUtils.toBean(list, InventoryTxnRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public PageResult<InventoryBalanceRespDTO> getInventoryBalancePage(InventoryPageReqDTO pageReq) {
        WmsInventoryQueryBO bo = toBOPage(pageReq);
        PageResult<WmsInvBalanceDO> page = inventoryService.getBalancePage(pageReq, bo);
        return BeanUtils.toBean(page, InventoryBalanceRespDTO.class, this::fillAvailable);
    }

    private WmsInventoryQueryBO toBOPage(InventoryPageReqDTO query) {
        WmsInventoryQueryBO bo = new WmsInventoryQueryBO();
        bo.setInvOrgId(query.getInvOrgId());
        bo.setWhId(query.getWhId());
        bo.setLocId(query.getLocId());
        bo.setLocIds(query.getLocIds());
        bo.setMatId(query.getMatId());
        bo.setMatIds(query.getMatIds());
        bo.setBatchId(query.getBatchId());
        bo.setContainerId(query.getContainerId());
        bo.setProjectId(query.getProjectId());
        bo.setStockScope(query.getStockScope());
        bo.setQualityStatus(query.getQualityStatus());
        bo.setQualifiedOnly(query.getQualifiedOnly());
        return bo;
    }

    private WmsInventoryQueryBO toBO(InventoryQueryReqDTO query) {
        WmsInventoryQueryBO bo = new WmsInventoryQueryBO();
        bo.setInvOrgId(query.getInvOrgId());
        bo.setPlantId(query.getPlantId());
        bo.setWhId(query.getWhId());
        bo.setLocId(query.getLocId());
        bo.setLocIds(query.getLocIds());
        bo.setMatId(query.getMatId());
        bo.setMatIds(query.getMatIds());
        bo.setBatchId(query.getBatchId());
        bo.setContainerId(query.getContainerId());
        bo.setProjectId(query.getProjectId());
        bo.setProjectTaskId(query.getProjectTaskId());
        bo.setStockScope(query.getStockScope());
        bo.setQualityStatus(query.getQualityStatus());
        bo.setQualifiedOnly(query.getQualifiedOnly());
        return bo;
    }

    /** 填充可用量字段。 */
    private void fillAvailable(InventoryBalanceRespDTO dto) {
        WmsInvBalanceDO balance = new WmsInvBalanceDO();
        balance.setQtyOnhand(dto.getQtyOnhand());
        balance.setQtyReserved(dto.getQtyReserved());
        balance.setQtyFrozen(dto.getQtyFrozen());
        dto.setQtyAvailable(WmsInventoryService.availableQtyOf(balance));
    }

}
