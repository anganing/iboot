package com.iboot.pf.module.wms.service.count;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.enums.WmsCountLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.WmsOutOrderService;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_LINE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_NO_DIFF;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.COUNT_ORDER;

/** 盘点单 Service 实现。 */
@Service
public class WmsCountOrderServiceImpl implements WmsCountOrderService {

    @Resource
    private WmsCountOrderMapper wmsCountOrderMapper;
    @Resource
    private WmsCountOrderLineMapper wmsCountOrderLineMapper;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsInOrderService inOrderService;
    @Resource
    private WmsOutOrderService outOrderService;
    @Resource
    private WmsCodeService codeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCountOrder(WmsCreateCountOrderBO bo) {
        WmsCountOrderDO existed = wmsCountOrderMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        warehouseService.validateWh(bo.getWhId());

        WmsCountOrderDO order = BeanUtils.toBean(bo, WmsCountOrderDO.class);
        order.setId(null);
        order.setCountNo(codeService.generate(COUNT_ORDER, bo.getCountNo()));
        order.setZoneId(bo.getZoneId() == null ? 0L : bo.getZoneId());
        order.setProjectId(bo.getProjectId() == null ? 0L : bo.getProjectId());
        order.setSnapshotTime(LocalDateTime.now());
        order.setStatus(WmsOrderStatusEnum.DRAFT.name());
        wmsCountOrderMapper.insert(order);

        generateLines(order, bo);
        return order.getId();
    }

    /** 按仓库、库区、物料圈选库存余额生成盘点行，账面数量取生成时的余额快照。 */
    private void generateLines(WmsCountOrderDO order, WmsCreateCountOrderBO bo) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(order.getInvOrgId());
        query.setWhId(order.getWhId());
        query.setMatIds(bo.getMatIds());
        query.setQualifiedOnly(false);
        if (order.getZoneId() != null && order.getZoneId() > 0) {
            Set<Long> locIds = convertSet(warehouseService.getLocListByZone(order.getZoneId()),
                    com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO::getId);
            query.setLocIds(locIds);
        }
        if (order.getProjectId() != null && order.getProjectId() > 0) {
            query.setProjectId(order.getProjectId());
        }

        int lineNo = 10;
        for (WmsInvBalanceDO balance : inventoryService.getBalanceList(query)) {
            WmsCountOrderLineDO line = new WmsCountOrderLineDO();
            line.setId(null);
            line.setOrderId(order.getId());
            line.setLineNo(lineNo);
            line.setBalanceId(balance.getId());
            line.setMatId(balance.getMatId());
            line.setMatCode(balance.getMatCode());
            line.setMatName(balance.getMatName());
            line.setBatchId(balance.getBatchId());
            line.setLotNo(balance.getLotNo());
            line.setLocId(balance.getLocId());
            line.setLocCode(balance.getLocCode());
            line.setContainerId(balance.getContainerId());
            line.setQualityStatus(balance.getQualityStatus());
            line.setProjectId(balance.getProjectId());
            line.setProjectTaskId(balance.getProjectTaskId());
            line.setBookQty(balance.getQtyOnhand());
            line.setStatus(WmsCountLineStatusEnum.PENDING.name());
            wmsCountOrderLineMapper.insert(line);
            lineNo += 10;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCountResult(WmsSubmitCountResultBO bo) {
        WmsCountOrderDO order = requireOrder(bo.getOrderId(), bo.getCountNo());
        if (!WmsOrderStatusEnum.DRAFT.name().equals(order.getStatus())
                && !WmsOrderStatusEnum.COUNTING.name().equals(order.getStatus())) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        if (CollUtil.isEmpty(bo.getResults())) {
            return;
        }
        List<WmsCountOrderLineDO> lines = wmsCountOrderLineMapper.selectListByOrderId(order.getId());
        for (WmsSubmitCountResultBO.LineResult result : bo.getResults()) {
            WmsCountOrderLineDO line = findLine(lines, result);
            if (result.getActualQty() == null || result.getActualQty().compareTo(BigDecimal.ZERO) < 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            BigDecimal diff = result.getActualQty().subtract(nvl(line.getBookQty()));
            line.setActualQty(result.getActualQty());
            line.setDiffQty(diff);
            line.setStatus(diff.compareTo(BigDecimal.ZERO) == 0
                    ? WmsCountLineStatusEnum.COUNTED.name() : WmsCountLineStatusEnum.DIFF.name());
            line.setRemark(result.getRemark());
            wmsCountOrderLineMapper.updateById(line);
        }
        order.setStatus(WmsOrderStatusEnum.COUNTING.name());
        wmsCountOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCountDiff(WmsConfirmCountDiffBO bo) {
        WmsCountOrderDO order = requireOrder(bo.getOrderId(), bo.getCountNo());
        if (!WmsOrderStatusEnum.COUNTING.name().equals(order.getStatus())
                && !WmsOrderStatusEnum.DIFF_CONFIRMED.name().equals(order.getStatus())) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        List<WmsCountOrderLineDO> diffLines = new ArrayList<>();
        for (WmsCountOrderLineDO line : wmsCountOrderLineMapper.selectListByOrderId(order.getId())) {
            if (line.getDiffQty() == null || line.getDiffQty().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (WmsCountLineStatusEnum.ADJUSTED.name().equals(line.getStatus())) {
                continue;
            }
            diffLines.add(line);
        }
        if (diffLines.isEmpty()) {
            throw exception(COUNT_ORDER_NO_DIFF);
        }

        for (WmsCountOrderLineDO line : diffLines) {
            String lineKey = bo.getIdempotencyKey() + ":" + line.getLineNo();
            // 差异只能走单据过账，不能直接改余额
            if (line.getDiffQty().compareTo(BigDecimal.ZERO) > 0) {
                line.setAdjustBizNo(createGainInOrder(order, line, lineKey));
            } else {
                line.setAdjustBizNo(createLossOutOrder(order, line, lineKey));
            }
            line.setStatus(WmsCountLineStatusEnum.ADJUSTED.name());
            wmsCountOrderLineMapper.updateById(line);
        }

        order.setStatus(WmsOrderStatusEnum.DONE.name());
        wmsCountOrderMapper.updateById(order);
    }

    private String createGainInOrder(WmsCountOrderDO order, WmsCountOrderLineDO line, String lineKey) {
        WmsCreateInOrderBO createBO = new WmsCreateInOrderBO();
        createBO.setIdempotencyKey(lineKey + ":GAIN");
        createBO.setSourceModule(WmsSourceModuleEnum.WMS.name());
        createBO.setSourceBizNo(order.getCountNo());
        createBO.setBizType(WmsInBizTypeEnum.COUNT_GAIN_IN.name());
        createBO.setCountOrderNo(order.getCountNo());
        createBO.setInvOrgId(order.getInvOrgId());
        createBO.setPlantId(warehouseService.getWh(order.getWhId()).getPlantId());
        createBO.setWhId(order.getWhId());
        createBO.setProjectId(line.getProjectId());
        createBO.setProjectTaskId(line.getProjectTaskId());
        createBO.setQualityStatus(line.getQualityStatus());
        createBO.setRemark("盘点盘盈自动入库");

        WmsCreateInOrderBO.Line lineBO = new WmsCreateInOrderBO.Line();
        lineBO.setLineNo(line.getLineNo());
        lineBO.setMatId(line.getMatId());
        lineBO.setMatCode(line.getMatCode());
        lineBO.setMatName(line.getMatName());
        lineBO.setBatchId(line.getBatchId());
        lineBO.setLotNo(line.getLotNo());
        lineBO.setLocId(line.getLocId());
        lineBO.setLocCode(line.getLocCode());
        lineBO.setContainerId(line.getContainerId());
        lineBO.setQualityStatus(line.getQualityStatus());
        lineBO.setPlanQty(line.getDiffQty().abs());
        createBO.setLines(List.of(lineBO));

        Long orderId = inOrderService.createInOrder(createBO);
        WmsConfirmOrderBO confirmBO = new WmsConfirmOrderBO();
        confirmBO.setIdempotencyKey(lineKey + ":GAIN");
        confirmBO.setSourceModule(WmsSourceModuleEnum.WMS.name());
        confirmBO.setSourceBizNo(order.getCountNo());
        confirmBO.setOrderId(orderId);
        confirmBO.setRemark("盘点盘盈自动过账");
        inOrderService.confirmInOrder(confirmBO);
        return inOrderService.getInOrder(orderId).getInNo();
    }

    private String createLossOutOrder(WmsCountOrderDO order, WmsCountOrderLineDO line, String lineKey) {
        WmsCreateOutOrderBO createBO = new WmsCreateOutOrderBO();
        createBO.setIdempotencyKey(lineKey + ":LOSS");
        createBO.setSourceModule(WmsSourceModuleEnum.WMS.name());
        createBO.setSourceBizNo(order.getCountNo());
        createBO.setBizType(WmsOutBizTypeEnum.COUNT_LOSS_OUT.name());
        createBO.setCountOrderNo(order.getCountNo());
        createBO.setInvOrgId(order.getInvOrgId());
        createBO.setPlantId(warehouseService.getWh(order.getWhId()).getPlantId());
        createBO.setWhId(order.getWhId());
        createBO.setProjectId(line.getProjectId());
        createBO.setProjectTaskId(line.getProjectTaskId());
        createBO.setRemark("盘点盘亏自动出库");

        WmsCreateOutOrderBO.Line lineBO = new WmsCreateOutOrderBO.Line();
        lineBO.setLineNo(line.getLineNo());
        lineBO.setMatId(line.getMatId());
        lineBO.setMatCode(line.getMatCode());
        lineBO.setMatName(line.getMatName());
        lineBO.setBatchId(line.getBatchId());
        lineBO.setLotNo(line.getLotNo());
        lineBO.setFromLocId(line.getLocId());
        lineBO.setFromLocCode(line.getLocCode());
        lineBO.setContainerId(line.getContainerId());
        lineBO.setQualityStatus(line.getQualityStatus());
        lineBO.setPlanQty(line.getDiffQty().abs());
        createBO.setLines(List.of(lineBO));

        Long orderId = outOrderService.createOutOrder(createBO);
        WmsConfirmOrderBO confirmBO = new WmsConfirmOrderBO();
        confirmBO.setIdempotencyKey(lineKey + ":LOSS");
        confirmBO.setSourceModule(WmsSourceModuleEnum.WMS.name());
        confirmBO.setSourceBizNo(order.getCountNo());
        confirmBO.setOrderId(orderId);
        confirmBO.setRemark("盘点盘亏自动过账");
        outOrderService.confirmOutOrder(confirmBO);
        return outOrderService.getOutOrder(orderId).getOutNo();
    }

    private WmsCountOrderLineDO findLine(List<WmsCountOrderLineDO> lines, WmsSubmitCountResultBO.LineResult result) {
        for (WmsCountOrderLineDO line : lines) {
            if (line.getId().equals(result.getLineId())
                    || (result.getLineNo() != null && result.getLineNo().equals(line.getLineNo()))) {
                return line;
            }
        }
        throw exception(COUNT_ORDER_LINE_NOT_EXISTS);
    }

    private WmsCountOrderDO requireOrder(Long orderId, String countNo) {
        WmsCountOrderDO order = orderId != null ? wmsCountOrderMapper.selectById(orderId) : null;
        if (order == null && countNo != null && !countNo.isEmpty()) {
            order = wmsCountOrderMapper.selectByCountNo(countNo);
        }
        if (order == null) {
            throw exception(COUNT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsCountOrderDO getCountOrder(Long id) {
        return wmsCountOrderMapper.selectById(id);
    }

    @Override
    public List<WmsCountOrderLineDO> getCountOrderLineList(Long orderId) {
        return wmsCountOrderLineMapper.selectListByOrderId(orderId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
