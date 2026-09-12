package com.iboot.pf.module.wms.api.inventory;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryPageReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.QualityChangeReqDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存 API 实现类。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给别的模块调的，
 * 调用方的登录用户与库存余额的数据范围毫无关系——MES 领料要占的是仓库里客观存在的料，
 * 跨模块调用天生是系统身份而不是用户身份。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class InventoryApiImpl implements InventoryApi {

    @Resource
    private WmsInventoryService inventoryService;

    @Override
    public List<InventoryBalanceRespDTO> getInventoryBalanceList(InventoryQueryReqDTO query) {
        List<WmsInvBalanceDO> list = inventoryService.getBalanceList(toBO(query));
        return BeanUtils.toBean(list, InventoryBalanceRespDTO.class, this::fillAvailable);
    }

    @Override
    public BigDecimal getAvailableQty(InventoryQueryReqDTO query) {
        return inventoryService.getAvailableQty(toBO(query));
    }

    @Override
    public Map<Long, BigDecimal> getAvailableQtyMap(InventoryQueryReqDTO query) {
        return inventoryService.getAvailableQtyMap(toBO(query));
    }

    @Override
    public List<InventoryTxnRespDTO> getInventoryTxnList(InventoryTxnQueryReqDTO query) {
        WmsInventoryTxnQueryBO bo = new WmsInventoryTxnQueryBO();
        bo.setInvOrgId(query.getInvOrgId());
        bo.setWhId(query.getWhId());
        bo.setMatId(query.getMatId());
        bo.setBatchId(query.getBatchId());
        bo.setBatchIds(query.getBatchIds());
        bo.setLotNos(query.getLotNos());
        bo.setTxnType(query.getTxnType());
        bo.setBizType(query.getBizType());
        bo.setBizNo(query.getBizNo());
        bo.setFromTime(query.getFromTime());
        bo.setToTime(query.getToTime());
        List<WmsInvTxnDO> list = inventoryService.getTxnList(bo);
        return BeanUtils.toBean(list, InventoryTxnRespDTO.class);
    }

    @Override
    public PageResult<InventoryBalanceRespDTO> getInventoryBalancePage(InventoryPageReqDTO pageReq) {
        WmsInventoryQueryBO bo = toBOPage(pageReq);
        PageResult<WmsInvBalanceDO> page = inventoryService.getBalancePage(pageReq, bo);
        return BeanUtils.toBean(page, InventoryBalanceRespDTO.class, this::fillAvailable);
    }

    @Override
    public Long changeQualityStatus(QualityChangeReqDTO reqDTO) {
        WmsInvPostBO bo = BeanUtils.toBean(reqDTO, WmsInvPostBO.class);
        bo.setTxnType(WmsTxnTypeEnum.QUALITY_CHANGE.name());
        bo.setBizType("QUALITY_DISPOSITION");
        bo.setBizNo(reqDTO.getSourceBizNo());
        bo.setBizLineNo(reqDTO.getSourceBizLineNo());
        bo.setFromLocId(reqDTO.getLocId());
        bo.setToLocId(reqDTO.getLocId());
        bo.setQualityStatusFrom(reqDTO.getQualityStatusFrom());
        bo.setQualityStatusTo(reqDTO.getQualityStatusTo());
        bo.setProjectIdFrom(reqDTO.getProjectId());
        bo.setProjectTaskIdFrom(reqDTO.getProjectTaskId());
        bo.setProjectIdTo(reqDTO.getProjectId());
        bo.setProjectTaskIdTo(reqDTO.getProjectTaskId());
        bo.setFromStockScope(reqDTO.getStockScope());
        bo.setToStockScope(reqDTO.getStockScope());
        bo.setFromLocCode(reqDTO.getLocCode());
        bo.setToLocCode(reqDTO.getLocCode());
        return inventoryService.post(bo).getId();
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
