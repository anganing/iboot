package com.iboot.pf.module.wms.service.outorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LOC_REQUIRED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.OUT_ORDER;

/** 出库单 Service 实现。 */
@Service
public class WmsOutOrderServiceImpl implements WmsOutOrderService {

    @Resource
    private WmsOutOrderMapper wmsOutOrderMapper;
    @Resource
    private WmsOutOrderLineMapper wmsOutOrderLineMapper;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private MaterialApi materialApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutOrder(WmsCreateOutOrderBO bo) {
        WmsOutOrderDO existed = wmsOutOrderMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(OUT_ORDER_LINE_EMPTY);
        }
        List<Long> materialIds = bo.getLines().stream().map(WmsCreateOutOrderBO.Line::getMatId).toList();
        materialApi.validateMaterialList(materialIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(materialIds);
        warehouseService.validateWh(bo.getWhId());

        WmsWhDO wh = warehouseService.getWh(bo.getWhId());
        WmsOutOrderDO order = BeanUtils.toBean(bo, WmsOutOrderDO.class);
        order.setId(null);
        order.setOutNo(codeService.generate(OUT_ORDER, bo.getOutNo()));
        order.setWhCode(wh == null ? null : wh.getCode());
        order.setSourceType(bo.getSourceModule());
        order.setSourceBizNo(bo.getSourceBizNo());
        order.setStatus(WmsOrderStatusEnum.DRAFT.name());
        wmsOutOrderMapper.insert(order);

        for (WmsCreateOutOrderBO.Line line : bo.getLines()) {
            if (line.getPlanQty() == null || line.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            fillMaterialSnapshot(line, materialMap.get(line.getMatId()));
            WmsOutOrderLineDO lineDO = BeanUtils.toBean(line, WmsOutOrderLineDO.class);
            lineDO.setId(null);
            lineDO.setOrderId(order.getId());
            lineDO.setQualityStatus(line.getQualityStatus() == null || line.getQualityStatus().isEmpty()
                    ? WmsQualityStatusEnum.QUALIFIED.name() : line.getQualityStatus());
            lineDO.setAllocatedQty(BigDecimal.ZERO);
            lineDO.setPickedQty(BigDecimal.ZERO);
            lineDO.setPostedQty(BigDecimal.ZERO);
            lineDO.setStatus(WmsOrderStatusEnum.DRAFT.name());
            wmsOutOrderLineMapper.insert(lineDO);
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutOrder(WmsCancelOrderBO bo) {
        WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(OUT_ORDER_CANNOT_CANCEL);
        }
        order.setStatus(WmsOrderStatusEnum.CANCELLED.name());
        order.setRemark(bo.getReason());
        wmsOutOrderMapper.updateById(order);
        for (WmsOutOrderLineDO line : wmsOutOrderLineMapper.selectListByOrderId(order.getId())) {
            line.setStatus(WmsOrderStatusEnum.CANCELLED.name());
            wmsOutOrderLineMapper.updateById(line);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutOrder(WmsConfirmOrderBO bo) {
        WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.canPost(order.getStatus())) {
            throw exception(OUT_ORDER_CANNOT_CONFIRM);
        }
        List<WmsOutOrderLineDO> lines = wmsOutOrderLineMapper.selectListByOrderId(order.getId());
        if (CollUtil.isEmpty(lines)) {
            throw exception(OUT_ORDER_LINE_EMPTY);
        }
        warehouseService.validateWh(order.getWhId());

        for (WmsOutOrderLineDO line : lines) {
            if (line.getFromLocId() == null) {
                throw exception(OUT_ORDER_LOC_REQUIRED);
            }
            warehouseService.validateLoc(line.getFromLocId());
            BigDecimal qty = line.getPlanQty().subtract(nvl(line.getPostedQty()));
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 出库过账前必须校验库位、批次、数量可用
            BigDecimal available = inventoryService.getAvailableQty(buildQuery(order, line));
            if (available.compareTo(qty) < 0) {
                throw exception(INV_AVAILABLE_QTY_NOT_ENOUGH);
            }

            String postKey = bo.getIdempotencyKey() + ":POST:" + line.getLineNo();
            inventoryService.post(buildPostBO(order, line, qty, postKey, bo.getRemark()));

            line.setPickedQty(nvl(line.getPickedQty()).add(qty));
            line.setPostedQty(nvl(line.getPostedQty()).add(qty));
            line.setStatus(WmsOrderStatusEnum.DONE.name());
            wmsOutOrderLineMapper.updateById(line);
        }

        order.setStatus(WmsOrderStatusEnum.DONE.name());
        wmsOutOrderMapper.updateById(order);
    }

    private WmsInventoryQueryBO buildQuery(WmsOutOrderDO order, WmsOutOrderLineDO line) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(order.getInvOrgId());
        query.setWhId(order.getWhId());
        query.setLocId(line.getFromLocId());
        query.setMatId(line.getMatId());
        query.setBatchId(line.getBatchId());
        query.setContainerId(line.getContainerId());
        query.setProjectId(order.getProjectId());
        query.setProjectTaskId(order.getProjectTaskId());
        query.setQualityStatus(line.getQualityStatus());
        return query;
    }

    private void fillMaterialSnapshot(WmsCreateOutOrderBO.Line line, MaterialRespDTO material) {
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
    }

    private WmsInvPostBO buildPostBO(WmsOutOrderDO order, WmsOutOrderLineDO line, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(WmsTxnTypeEnum.ISSUE.name());
        postBO.setBizType(order.getBizType());
        postBO.setBizNo(order.getOutNo());
        postBO.setBizLineNo(String.valueOf(line.getLineNo()));
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromLocId(line.getFromLocId());
        postBO.setFromLocCode(line.getFromLocCode());
        postBO.setQualityStatusFrom(line.getQualityStatus());
        postBO.setQualityStatusTo(line.getQualityStatus());
        postBO.setProjectIdFrom(order.getProjectId());
        postBO.setProjectTaskIdFrom(order.getProjectTaskId());
        postBO.setInvOrgId(order.getInvOrgId());
        postBO.setPlantId(order.getPlantId());
        postBO.setWhId(order.getWhId());
        postBO.setMatId(line.getMatId());
        postBO.setBatchId(line.getBatchId());
        postBO.setContainerId(line.getContainerId());
        postBO.setUomId(line.getUomId());
        postBO.setMatCode(line.getMatCode());
        postBO.setMatName(line.getMatName());
        postBO.setUomCode(line.getUomCode());
        postBO.setLotNo(line.getLotNo());
        postBO.setRemark(remark);
        return postBO;
    }

    private WmsOutOrderDO requireOrder(Long orderId, String orderNo) {
        WmsOutOrderDO order = orderId != null ? wmsOutOrderMapper.selectById(orderId) : null;
        if (order == null && orderNo != null && !orderNo.isEmpty()) {
            order = wmsOutOrderMapper.selectByOutNo(orderNo);
        }
        if (order == null) {
            throw exception(OUT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsOutOrderDO getOutOrder(Long id) {
        return wmsOutOrderMapper.selectById(id);
    }

    @Override
    public List<WmsOutOrderLineDO> getOutOrderLineList(Long orderId) {
        return wmsOutOrderLineMapper.selectListByOrderId(orderId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
