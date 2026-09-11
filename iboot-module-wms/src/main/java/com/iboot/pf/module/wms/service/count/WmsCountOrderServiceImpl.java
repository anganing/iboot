package com.iboot.pf.module.wms.service.count;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsCountLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandService;
import com.iboot.pf.module.wms.service.command.bo.WmsCommandBO;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.COUNT_ORDER;

/** 盘点单 Service 实现。 */
@Service
public class WmsCountOrderServiceImpl implements WmsCountOrderService {

    public static final String PROCESS_KEY_COUNT_DIFF = "wms-count-diff";

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
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsCommandService commandService;

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
    public void updateCountOrder(Long orderId, WmsCreateCountOrderBO bo) {
        WmsCountOrderDO order = requireDraft(orderId);
        warehouseService.validateWh(bo.getWhId());

        // 单号、状态、创建幂等键保持原值：它们是单据身份，不随盘点范围改
        WmsCountOrderDO update = BeanUtils.toBean(bo, WmsCountOrderDO.class);
        update.setId(order.getId());
        update.setCountNo(order.getCountNo());
        update.setStatus(order.getStatus());
        update.setIdempotencyKey(order.getIdempotencyKey());
        update.setZoneId(bo.getZoneId() == null ? 0L : bo.getZoneId());
        update.setProjectId(bo.getProjectId() == null ? 0L : bo.getProjectId());
        // 盘点范围变了，账面快照必须重取，否则 book_qty 还是旧范围的数
        update.setSnapshotTime(LocalDateTime.now());
        wmsCountOrderMapper.updateById(update);

        // 盘点行是按账面快照生成的、不是人录的，所以整批重建而不是逐行改
        for (WmsCountOrderLineDO old : wmsCountOrderLineMapper.selectListByOrderId(order.getId())) {
            wmsCountOrderLineMapper.deleteById(old.getId());
        }
        generateLines(update, bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCountOrder(Long orderId) {
        requireDraft(orderId);
        for (WmsCountOrderLineDO line : wmsCountOrderLineMapper.selectListByOrderId(orderId)) {
            wmsCountOrderLineMapper.deleteById(line.getId());
        }
        wmsCountOrderMapper.deleteById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeCountOrder(WmsSubmitOrderBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.FREEZE_COUNT,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsCountOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
                        throw exception(COUNT_ORDER_CANNOT_FREEZE);
                    }
                    List<WmsCountOrderLineDO> lines = wmsCountOrderLineMapper.selectListByOrderId(order.getId());
                    if (CollUtil.isEmpty(lines)) {
                        throw exception(COUNT_ORDER_LINE_EMPTY);
                    }
                    Long plantId = warehouseService.getWh(order.getWhId()).getPlantId();
                    for (WmsCountOrderLineDO line : lines) {
                        // 账面为 0 的行没有实物可冻，跳过；否则 post 会因数量非正抛错
                        if (nvl(line.getBookQty()).compareTo(BigDecimal.ZERO) <= 0) {
                            continue;
                        }
                        String postKey = String.format("%s:%s:FREEZE:%d", WmsSourceModuleEnum.UI.name(),
                                order.getCountNo(), line.getLineNo());
                        inventoryService.post(buildFreezePostBO(order, line, plantId, postKey,
                                WmsTxnTypeEnum.FREEZE, "盘点冻结"));
                    }
                    order.setStatus(WmsOrderStatusEnum.FROZEN.name());
                    if (bo.getRemark() != null) {
                        order.setRemark(bo.getRemark());
                    }
                    wmsCountOrderMapper.updateById(order);
                });
    }

    /** 取单据并校验处于新建状态。改单与删单的共同前置：已冻结的盘点单不许改范围。 */
    private WmsCountOrderDO requireDraft(Long orderId) {
        WmsCountOrderDO order = requireOrder(orderId, null);
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(COUNT_ORDER_CANNOT_MODIFY);
        }
        return order;
    }

    /** 盘点冻结/解冻共用的过账参数构造：维度一致，仅事务类型与备注不同。 */
    private WmsInvPostBO buildFreezePostBO(WmsCountOrderDO order, WmsCountOrderLineDO line,
                                           Long plantId, String postKey,
                                           WmsTxnTypeEnum txnType, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(txnType.name());
        postBO.setBizType(WmsSourceBizTypeEnum.COUNT_FREEZE.name());
        postBO.setBizNo(order.getCountNo());
        postBO.setBizLineNo(String.valueOf(line.getLineNo()));
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(line.getBookQty());
        postBO.setInvOrgId(order.getInvOrgId());
        postBO.setPlantId(plantId);
        postBO.setWhId(order.getWhId());
        postBO.setFromLocId(line.getLocId());
        postBO.setFromLocCode(line.getLocCode());
        postBO.setQualityStatusFrom(line.getQualityStatus());
        postBO.setQualityStatusTo(line.getQualityStatus());
        postBO.setProjectIdFrom(line.getProjectId());
        postBO.setProjectTaskIdFrom(line.getProjectTaskId());
        postBO.setMatId(line.getMatId());
        postBO.setMatCode(line.getMatCode());
        postBO.setMatName(line.getMatName());
        postBO.setBatchId(line.getBatchId());
        postBO.setLotNo(line.getLotNo());
        postBO.setContainerId(line.getContainerId());
        postBO.setRemark(remark);
        return postBO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCountResult(WmsSubmitCountResultBO bo) {
        // 命令幂等：实盘是覆盖式录入，重复提交不得二次推进。状态校验放幂等壳内，
        // 同键重试原样返回而非报非法状态。
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_COUNT_RESULT,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsCountOrderDO order = requireOrder(bo.getOrderId(), bo.getCountNo());
                    // 允许 FROZEN 与 COUNTING：冻结后录实盘是常规路径，不冻结直接盘（DRAFT）也放行
                    if (!WmsOrderStatusEnum.DRAFT.name().equals(order.getStatus())
                            && !WmsOrderStatusEnum.FROZEN.name().equals(order.getStatus())
                            && !WmsOrderStatusEnum.COUNTING.name().equals(order.getStatus())) {
                        throw exception(COUNT_ORDER_STATUS_INVALID);
                    }
                    if (BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(order.getApprovalStatus())) {
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
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCountDiff(WmsConfirmCountDiffBO bo) {
        // 单据级命令幂等：同键重复确认原样返回。差异单据（盘盈入库/盘亏出库）自身带
        // idempotency_key 列判重，行级流水键由过账层兜底，这里只挡整单动作重放。
        commandService.executeVoid(WmsCommandTypeEnum.CONFIRM_COUNT_DIFF,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> doConfirmCountDiff(bo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCountDiffApproval(Long countOrderId) {
        WmsCountOrderDO order = wmsCountOrderMapper.selectById(countOrderId);
        if (order == null || !WmsOrderStatusEnum.COUNTING.name().equals(order.getStatus())) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        boolean hasDiffLine = wmsCountOrderLineMapper.selectListByOrderId(countOrderId).stream()
                .anyMatch(line -> WmsCountLineStatusEnum.DIFF.name().equals(line.getStatus()));
        if (!hasDiffLine) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        if (BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(order.getApprovalStatus())) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        BpmProcessInstanceCreateReqDTO createReq = new BpmProcessInstanceCreateReqDTO();
        createReq.setProcessDefinitionKey(PROCESS_KEY_COUNT_DIFF);
        createReq.setBusinessKey(String.valueOf(countOrderId));
        String instanceId = processInstanceApi.createProcessInstance(
                SecurityFrameworkUtils.getLoginUserId(), createReq);
        order.setProcessInstanceId(instanceId);
        order.setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        wmsCountOrderMapper.updateById(order);
    }

    private void doConfirmCountDiff(WmsConfirmCountDiffBO bo) {
        WmsCountOrderDO order = requireOrder(bo.getOrderId(), bo.getCountNo());
        if (!WmsOrderStatusEnum.COUNTING.name().equals(order.getStatus())
                && !WmsOrderStatusEnum.DIFF_CONFIRMED.name().equals(order.getStatus())) {
            throw exception(COUNT_ORDER_STATUS_INVALID);
        }
        // 有审批流程实例的单据必须审批通过后才能确认差异
        if (order.getProcessInstanceId() != null
                && !BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(order.getApprovalStatus())) {
            throw exception(COUNT_DIFF_NOT_APPROVED);
        }
        List<WmsCountOrderLineDO> allLines = wmsCountOrderLineMapper.selectListByOrderId(order.getId());
        List<WmsCountOrderLineDO> diffLines = new ArrayList<>();
        for (WmsCountOrderLineDO line : allLines) {
            if (line.getDiffQty() == null || line.getDiffQty().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (WmsCountLineStatusEnum.ADJUSTED.name().equals(line.getStatus())) {
                continue;
            }
            diffLines.add(line);
        }

        // 收单即解冻：整单走到 DONE，盘点范围内所有冻结都必须释放，不只差异行——
        // 无差异行同样被冻结占着可用量。解冻放在盘亏过账之前：冻结量占用可用量，
        // 不先释放的话盘亏出库会被可用量校验拦住。未冻结过的单（新建直接盘）按
        // 冻结流水判据自然跳过，见 unfreezeLines。
        unfreezeLines(order, allLines);

        // 零差异直接收单：全盘平的盘点也是合法结论，必须能走到 DONE，否则整单永远停在 COUNTING
        if (diffLines.isEmpty()) {
            order.setStatus(WmsOrderStatusEnum.DONE.name());
            wmsCountOrderMapper.updateById(order);
            return;
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

    /**
     * 按冻结时的同样维度做 UNFREEZE 过账，释放盘点冻结占用的可用量。
     *
     * <p>{@code freezeCountOrder} 逐行按账面量挂了 {@code qty_frozen}，盘点收尾不解冻的话
     * 这批货可用量永久偏低。以"该行是否落过冻结流水"为准：从没冻结过的单（新建直接盘）
     * 没有 FREEZE 流水，整行跳过，不会因"解冻超过冻结余量"抛错。
     */
    private void unfreezeLines(WmsCountOrderDO order, List<WmsCountOrderLineDO> lines) {
        Long plantId = warehouseService.getWh(order.getWhId()).getPlantId();
        for (WmsCountOrderLineDO line : lines) {
            if (nvl(line.getBookQty()).compareTo(BigDecimal.ZERO) <= 0) {
                // 账面为 0 的行冻结时被跳过，收尾同样没有冻结量可放
                continue;
            }
            String freezeKey = String.format("%s:%s:FREEZE:%d", WmsSourceModuleEnum.UI.name(),
                    order.getCountNo(), line.getLineNo());
            if (!inventoryService.posted(freezeKey)) {
                // 新建直接盘的单没冻结过，无需解冻
                continue;
            }
            // 行级解冻键与冻结键同构：UI:{盘点单号}:FREEZE:{行号} 对 UI:{盘点单号}:UNFREEZE:{行号}，
            // 与单据级确认幂等键解耦，行级解冻按流水幂等只落一次
            String postKey = String.format("%s:%s:UNFREEZE:%d", WmsSourceModuleEnum.UI.name(),
                    order.getCountNo(), line.getLineNo());
            inventoryService.post(buildFreezePostBO(order, line, plantId, postKey,
                    WmsTxnTypeEnum.UNFREEZE, "盘点收尾解冻"));
        }
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
