package com.iboot.pf.module.wms.service.inorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.batch.WmsBatchService;
import com.iboot.pf.module.wms.service.batch.bo.RegisterBatchBO;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandService;
import com.iboot.pf.module.wms.service.command.bo.WmsCommandBO;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_SUBMIT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_LOC_REQUIRED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.IN_ORDER;

/** 入库单 Service 实现。 */
@Service
public class WmsInOrderServiceImpl implements WmsInOrderService {

    @Resource
    private WmsInOrderMapper wmsInOrderMapper;
    @Resource
    private WmsInOrderLineMapper wmsInOrderLineMapper;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsBatchService batchService;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsCommandService commandService;
    @Resource
    private MaterialApi materialApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInOrder(WmsCreateInOrderBO bo) {
        WmsInOrderDO existed = wmsInOrderMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(IN_ORDER_LINE_EMPTY);
        }
        List<Long> materialIds = bo.getLines().stream().map(WmsCreateInOrderBO.Line::getMatId).toList();
        materialApi.validateMaterialList(materialIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(materialIds);
        warehouseService.validateWh(bo.getWhId());

        WmsWhDO wh = warehouseService.getWh(bo.getWhId());
        WmsInOrderDO order = BeanUtils.toBean(bo, WmsInOrderDO.class);
        order.setId(null);
        order.setSourceType(bo.getSourceModule());
        order.setSourceBizNo(bo.getSourceBizNo());
        order.setInNo(codeService.generate(IN_ORDER, bo.getInNo()));
        order.setWhCode(wh == null ? null : wh.getCode());
        order.setQualityStatus(bo.getQualityStatus() == null || bo.getQualityStatus().isEmpty()
                ? WmsQualityStatusEnum.QUALIFIED.name() : bo.getQualityStatus());
        order.setStatus(WmsOrderStatusEnum.DRAFT.name());
        wmsInOrderMapper.insert(order);

        insertLines(order, bo, materialMap);
        return order.getId();
    }

    /** 按参数建入库行。创建与修改共用：修改走行的全量替换，建行规则与创建时必须一致。 */
    private void insertLines(WmsInOrderDO order, WmsCreateInOrderBO bo,
                             Map<Long, MaterialRespDTO> materialMap) {
        for (WmsCreateInOrderBO.Line line : bo.getLines()) {
            if (line.getPlanQty() == null || line.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            fillMaterialSnapshot(line, materialMap.get(line.getMatId()));
            WmsInOrderLineDO lineDO = BeanUtils.toBean(line, WmsInOrderLineDO.class);
            lineDO.setId(null);
            lineDO.setOrderId(order.getId());
            lineDO.setQualityStatus(line.getQualityStatus() == null || line.getQualityStatus().isEmpty()
                    ? order.getQualityStatus() : line.getQualityStatus());
            lineDO.setCompletedQty(BigDecimal.ZERO);
            lineDO.setPostedQty(BigDecimal.ZERO);
            lineDO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
            // 建行阶段注册批次并回填 batchId，此时调用方传入的批次扩展信息仍然可用
            if (lineDO.getBatchId() == null) {
                lineDO.setBatchId(registerBatch(order, line, materialMap.get(line.getMatId())));
                lineDO.setLotNo(line.getLotNo() == null || line.getLotNo().isEmpty()
                        ? WmsConstants.DEFAULT_LOT_NO : line.getLotNo());
            }
            wmsInOrderLineMapper.insert(lineDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInOrder(Long orderId, WmsCreateInOrderBO bo) {
        WmsInOrderDO order = requireDraft(orderId);
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(IN_ORDER_LINE_EMPTY);
        }
        List<Long> materialIds = bo.getLines().stream().map(WmsCreateInOrderBO.Line::getMatId).toList();
        materialApi.validateMaterialList(materialIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(materialIds);
        warehouseService.validateWh(bo.getWhId());

        // 单号、状态、创建幂等键保持原值：它们是单据身份，不随内容改。
        // updateById 只写非空字段，与 codegen 侧行为一致，清空某个可选外键要靠调用方传 0 而不是 null。
        WmsWhDO wh = warehouseService.getWh(bo.getWhId());
        WmsInOrderDO update = BeanUtils.toBean(bo, WmsInOrderDO.class);
        update.setId(order.getId());
        update.setInNo(order.getInNo());
        update.setStatus(order.getStatus());
        update.setIdempotencyKey(order.getIdempotencyKey());
        update.setSourceType(bo.getSourceModule());
        update.setSourceBizNo(bo.getSourceBizNo());
        update.setWhCode(wh == null ? null : wh.getCode());
        update.setQualityStatus(bo.getQualityStatus() == null || bo.getQualityStatus().isEmpty()
                ? WmsQualityStatusEnum.QUALIFIED.name() : bo.getQualityStatus());
        wmsInOrderMapper.updateById(update);

        // 行全量替换：逐行比对容易漏掉删行的情况，且行号可能整体重排
        for (WmsInOrderLineDO old : wmsInOrderLineMapper.selectListByOrderId(order.getId())) {
            wmsInOrderLineMapper.deleteById(old.getId());
        }
        insertLines(update, bo, materialMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInOrder(Long orderId) {
        requireDraft(orderId);
        for (WmsInOrderLineDO line : wmsInOrderLineMapper.selectListByOrderId(orderId)) {
            wmsInOrderLineMapper.deleteById(line.getId());
        }
        wmsInOrderMapper.deleteById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitInOrder(WmsSubmitOrderBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_IN_ORDER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsInOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
                        throw exception(IN_ORDER_CANNOT_SUBMIT);
                    }
                    if (CollUtil.isEmpty(wmsInOrderLineMapper.selectListByOrderId(order.getId()))) {
                        throw exception(IN_ORDER_LINE_EMPTY);
                    }
                    order.setStatus(WmsOrderStatusEnum.SUBMITTED.name());
                    if (bo.getRemark() != null) {
                        order.setRemark(bo.getRemark());
                    }
                    wmsInOrderMapper.updateById(order);
                });
    }

    /** 取单据并校验处于新建状态。改单与删单的共同前置：已提交的单据不许改行、改数量、改库位。 */
    private WmsInOrderDO requireDraft(Long orderId) {
        WmsInOrderDO order = requireOrder(orderId, null);
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(IN_ORDER_CANNOT_MODIFY);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInOrder(WmsCancelOrderBO bo) {
        WmsInOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(IN_ORDER_CANNOT_CANCEL);
        }
        order.setStatus(WmsOrderStatusEnum.CANCELLED.name());
        order.setRemark(bo.getReason());
        wmsInOrderMapper.updateById(order);
        for (WmsInOrderLineDO line : wmsInOrderLineMapper.selectListByOrderId(order.getId())) {
            line.setStatus(WmsOrderLineStatusEnum.CANCELLED.name());
            wmsInOrderLineMapper.updateById(line);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmInOrder(WmsConfirmOrderBO bo) {
        WmsInOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.canPost(order.getStatus())) {
            throw exception(IN_ORDER_CANNOT_CONFIRM);
        }
        List<WmsInOrderLineDO> lines = wmsInOrderLineMapper.selectListByOrderId(order.getId());
        if (CollUtil.isEmpty(lines)) {
            throw exception(IN_ORDER_LINE_EMPTY);
        }
        warehouseService.validateWh(order.getWhId());

        for (WmsInOrderLineDO line : lines) {
            if (line.getLocId() == null) {
                throw exception(IN_ORDER_LOC_REQUIRED);
            }
            warehouseService.validateLoc(line.getLocId());
            BigDecimal qty = line.getPlanQty().subtract(nvl(line.getPostedQty()));
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 批次已在创建阶段注册并回填，过账直接复用
            WmsBatchDO batch = line.getBatchId() == null ? null : batchService.getBatch(line.getBatchId());
            line.setLotNo(batch == null ? line.getLotNo() : batch.getLotNo());

            String postKey = bo.getIdempotencyKey() + ":POST:" + line.getLineNo();
            inventoryService.post(buildPostBO(order, line, qty, postKey, bo.getRemark()));

            line.setPostedQty(nvl(line.getPostedQty()).add(qty));
            line.setCompletedQty(nvl(line.getCompletedQty()).add(qty));
            line.setStatus(WmsOrderLineStatusEnum.DONE.name());
            wmsInOrderLineMapper.updateById(line);
        }

        order.setStatus(WmsOrderStatusEnum.DONE.name());
        wmsInOrderMapper.updateById(order);
    }

    private Long registerBatch(WmsInOrderDO order, WmsCreateInOrderBO.Line line, MaterialRespDTO material) {
        RegisterBatchBO registerBO = new RegisterBatchBO();
        registerBO.setMatId(line.getMatId());
        registerBO.setMatCode(line.getMatCode());
        registerBO.setMatName(line.getMatName());
        registerBO.setLotNo(line.getLotNo() == null || line.getLotNo().isEmpty()
                ? WmsConstants.DEFAULT_LOT_NO : line.getLotNo());
        registerBO.setSupplierId(line.getSupplierId());
        registerBO.setSupplierCode(line.getSupplierCode());
        registerBO.setSupplierLotNo(line.getSupplierLotNo());
        registerBO.setFurnaceLotNo(line.getFurnaceLotNo());
        registerBO.setProduceDate(line.getProduceDate());
        registerBO.setExpiryDate(line.getExpiryDate());
        return batchService.registerBatch(registerBO, material).getId();
    }

    private void fillMaterialSnapshot(WmsCreateInOrderBO.Line line, MaterialRespDTO material) {
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
    }

    private WmsInvPostBO buildPostBO(WmsInOrderDO order, WmsInOrderLineDO line, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(WmsTxnTypeEnum.RECEIPT.name());
        postBO.setBizType(order.getBizType());
        postBO.setBizNo(order.getInNo());
        postBO.setBizLineNo(String.valueOf(line.getLineNo()));
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setToLocId(line.getLocId());
        postBO.setToLocCode(line.getLocCode());
        postBO.setQualityStatusTo(line.getQualityStatus());
        postBO.setProjectIdTo(order.getProjectId());
        postBO.setProjectTaskIdTo(order.getProjectTaskId());
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

    private WmsInOrderDO requireOrder(Long orderId, String orderNo) {
        WmsInOrderDO order = orderId != null ? wmsInOrderMapper.selectById(orderId) : null;
        if (order == null && orderNo != null && !orderNo.isEmpty()) {
            order = wmsInOrderMapper.selectByInNo(orderNo);
        }
        if (order == null) {
            throw exception(IN_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsInOrderDO getInOrder(Long id) {
        return wmsInOrderMapper.selectById(id);
    }

    @Override
    public List<WmsInOrderLineDO> getInOrderLineList(Long orderId) {
        return wmsInOrderLineMapper.selectListByOrderId(orderId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
