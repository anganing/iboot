package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchReqDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchRespDTO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_INBOUND_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_INBOUND_STOCK_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.UOM_NOT_EXISTS;

/**
 * 完工入库实现。
 *
 * <p>幂等有两道：命令记录按 {@code MES:{工单号}:FINISH_INBOUND:{第几次}} 拦住重复下发（{@code result_id}
 * 落 WMS 入库单编号），累计入库上限拦住"键不同但量已入满"的重复请求——第二次算出的剩余量是 0，
 * 什么都不做直接返回。两道都需要：只有键的话调用方换个键就能重复入库，只有上限的话
 * 同一个键的网络重试会去 WMS 建第二张单。
 */
@Service
public class MesFinishInboundServiceImpl implements MesFinishInboundService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 本动作由 MES 自己发起（工单完工带动），来源模块是 MES 而不是 UI。 */
    private static final String SOURCE_MODULE_MES = "MES";
    /** 幂等键的动作段。 */
    private static final String ACTION_FINISH_INBOUND = "FINISH_INBOUND";
    /** 给 WMS 的派生键段。 */
    private static final String ACTION_CREATE_IN_ORDER = "CREATE_IN_ORDER";
    private static final String ACTION_CONFIRM_IN_ORDER = "CONFIRM_IN_ORDER";
    /** 缺省批次号的日期格式，{@code {工单号}-{yyyyMMdd}}。 */
    private static final DateTimeFormatter LOT_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private BatchApi batchApi;
    @Resource
    private InOrderApi inOrderApi;
    @Resource
    private WarehouseApi warehouseApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinishInbound(MesFinishInboundBO bo) {
        return inbound(bo, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoInboundOnOrderDone(Long prodOrderId, Long workOrderId) {
        MesFinishInboundBO bo = new MesFinishInboundBO();
        bo.setProdOrderId(prodOrderId);
        bo.setWorkOrderId(workOrderId);
        bo.setSourceModule(SOURCE_MODULE_MES);
        // 数量、批次、仓库、库位全部留空：数量取"已完工减已入库"的全部剩余，其余回落物料默认值。
        // 幂等键也留空，按 MES:{工单号}:FINISH_INBOUND:{第几次} 派生——不沿用完工那个键，
        // 因为完工键上已经挂着工序流水与两级上卷流水，混用会被 Recorder 判成重复留痕
        bo.setRemark("末道工序完工自动入库");
        return inbound(bo, true);
    }

    @Override
    public BigDecimal inboundedQty(Long prodOrderId) {
        return inboundTxns(prodOrderId).stream().map(WipTxnDO::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 入库主流程。
     *
     * @param bo       入库参数
     * @param silent   配置缺失与无量可入时是否静默返回（自动入库为真）
     * @return WMS 入库单编号，跳过时为空
     */
    private Long inbound(MesFinishInboundBO bo, boolean silent) {
        ProdOrderDO order = requireProdOrder(bo.getProdOrderId());
        // 在产与已完工都允许：分批入库常在末道工序还在跑的时候就开始，等整单完工再入会压库压很久。
        // CLOSED 之后不许再入：关闭已经做过账目收尾，再入库等于在已封版的账上加一笔
        if (!MesProdOrderStatusEnum.RUNNING.name().equals(order.getStatus())
                && !MesProdOrderStatusEnum.DONE.name().equals(order.getStatus())) {
            throw exception(PROD_ORDER_STATUS_INVALID);
        }
        List<WipTxnDO> inboundTxns = inboundTxns(order.getId());
        BigDecimal inbounded = inboundTxns.stream().map(WipTxnDO::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = nvl(order.getCompletedQty()).subtract(inbounded);
        // 入参没给数量时"没有可入的量"是正常情形（已经入满了），直接返回；给了数量的校验放到幂等里面，
        // 见下面那段注释
        if (bo.getQty() == null && remaining.signum() <= 0) {
            return null;
        }
        BigDecimal qty = bo.getQty() == null ? remaining : bo.getQty();

        MaterialRespDTO material = materialApi.getMaterial(order.getMatId());
        if (material == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        Long whId = firstPresent(bo.getWhId(), material.getDefaultWhId());
        Long locId = firstPresent(bo.getLocId(), material.getDefaultLocId());
        if (isNone(whId) || isNone(locId)) {
            if (silent) {
                return null;
            }
            throw exception(PROD_ORDER_INBOUND_STOCK_REQUIRED, material.getCode());
        }

        // 第几次入库进幂等键，与已有流水条数对齐，因此分批入库的每一次都是一个新键
        String idempotencyKey = StrUtil.isBlank(bo.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, SOURCE_MODULE_MES, order.getOrderNo(),
                ACTION_FINISH_INBOUND, String.valueOf(inboundTxns.size() + 1))
                : bo.getIdempotencyKey();
        InboundCommand command = new InboundCommand();
        command.setSourceModule(StrUtil.isBlank(bo.getSourceModule()) ? SOURCE_MODULE_MES : bo.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(bo.getSourceBizNo()) ? order.getOrderNo() : bo.getSourceBizNo());
        command.setIdempotencyKey(idempotencyKey);
        // 数量校验必须放在幂等里面：同键重试时剩余量已经是 0，放在外面会让重试收到
        // "累计入库超过已完工数量"这个错，而它本该原样拿回第一次那张单的编号
        return commandService.execute(MesCommandTypeEnum.FINISH_INBOUND, command, () -> {
            if (qty.signum() <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            if (qty.compareTo(remaining) > 0) {
                throw exception(PROD_ORDER_INBOUND_QTY_EXCEEDED, order.getOrderNo(), plain(qty),
                        plain(inbounded.add(qty)), plain(nvl(order.getCompletedQty())));
            }
            return doInbound(order, bo, material, qty, whId, locId, idempotencyKey);
        });
    }

    /**
     * 注册批次、建入库单、过账、记账。
     *
     * <p>批次先注册再建单：入库行带 {@code batchId} 时 WMS 不会重复注册，把"这批成品是哪一批"
     * 定在 MES 侧，而不是让 WMS 按批次号反查。批次注册本身在 WMS 侧幂等（同物料 + 批次号 + 序列号命中原批次），
     * 所以分批入库用同一个批次号会落到同一个批次上，这正是"一张工单的成品属于同一批"的语义。
     */
    private Long doInbound(ProdOrderDO order, MesFinishInboundBO bo, MaterialRespDTO material,
                           BigDecimal qty, Long whId, Long locId, String idempotencyKey) {
        String lotNo = StrUtil.isBlank(bo.getLotNo()) ? defaultLotNo(order) : bo.getLotNo();
        RegisterBatchReqDTO batchDTO = new RegisterBatchReqDTO();
        batchDTO.setMatId(order.getMatId());
        batchDTO.setMatCode(material.getCode());
        batchDTO.setMatName(material.getName());
        batchDTO.setLotNo(lotNo);
        batchDTO.setProduceDate(LocalDate.now());
        RegisterBatchRespDTO batch = batchApi.registerBatch(batchDTO);

        Long inOrderId = postFinishIn(order, bo, material, qty, whId, locId, lotNo,
                batch == null ? null : batch.getId(), idempotencyKey);
        // 账本：本次入库量落进在制流水，累计入库量就是这些流水之和。biz_no 记 WMS 入库单编号
        // （两个门面都没有按编号查单号的只读方法，MES 侧拿不到单号本身，与 T1-11 的 wms_biz_no 同口径）
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(order.getId())
                .setWorkOrderId(nvl(bo.getWorkOrderId()))
                .setTxnType(MesWipTxnTypeEnum.FINISH_INBOUND)
                .setQty(qty)
                .setToStatus(WmsOrderStatusEnum.DONE.name())
                .setBizType(MesBizTypeEnum.IN_ORDER)
                .setBizNo(String.valueOf(inOrderId))
                .setIdempotencyKey(idempotencyKey)
                .setRemark("完工入库 " + plain(qty) + " " + lotNo));
        return inOrderId;
    }

    /** 建 {@code PROD_FINISH_IN} 入库单并过账，返回 WMS 单据编号。 */
    private Long postFinishIn(ProdOrderDO order, MesFinishInboundBO bo, MaterialRespDTO material,
                              BigDecimal qty, Long whId, Long locId, String lotNo, Long batchId,
                              String idempotencyKey) {
        WhRespDTO wh = warehouseApi.getWh(whId);
        UomRespDTO uom = uom(order.getUomId(), material);
        CreateInOrderReqDTO createDTO = new CreateInOrderReqDTO();
        createDTO.setIdempotencyKey(idempotencyKey + KEY_SEPARATOR + ACTION_CREATE_IN_ORDER);
        createDTO.setSourceModule(SOURCE_MODULE_MES);
        createDTO.setSourceBizNo(order.getOrderNo());
        createDTO.setBizType(WmsInBizTypeEnum.PROD_FINISH_IN.name());
        createDTO.setProdOrderNo(order.getOrderNo());
        createDTO.setWorkOrderNo(workOrderNoOf(bo.getWorkOrderId()));
        // 库存组织从仓库反查：mes_prod_order 上没有这一列，而它在入库单头上是 NOT NULL
        createDTO.setInvOrgId(wh == null ? null : wh.getInvOrgId());
        createDTO.setPlantId(order.getPlantId());
        createDTO.setWhId(whId);
        createDTO.setProjectId(nvl(order.getProjectId()));
        createDTO.setProjectTaskId(nvl(order.getProjectTaskId()));
        // 完工入库不再走入库质检：FQC 在 MES 侧检（阶段 2 的 T2-09），检完才进这一步
        createDTO.setNeedQc(Boolean.FALSE);
        createDTO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        createDTO.setRemark(bo.getRemark());
        CreateInOrderReqDTO.Line line = new CreateInOrderReqDTO.Line();
        line.setLineNo(MesConstants.FIRST_LINE_NO);
        line.setMatId(order.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
        line.setBatchId(isNone(batchId) ? null : batchId);
        line.setLotNo(lotNo);
        line.setUomId(uom.getId());
        line.setUomCode(uom.getCode());
        line.setLocId(locId);
        line.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        line.setPlanQty(qty);
        line.setProduceDate(LocalDate.now());
        createDTO.setLines(List.of(line));
        Long inOrderId = inOrderApi.createInOrder(createDTO);

        ConfirmOrderReqDTO confirmDTO = new ConfirmOrderReqDTO();
        confirmDTO.setIdempotencyKey(idempotencyKey + KEY_SEPARATOR + ACTION_CONFIRM_IN_ORDER);
        confirmDTO.setSourceModule(SOURCE_MODULE_MES);
        confirmDTO.setSourceBizNo(order.getOrderNo());
        confirmDTO.setOrderId(inOrderId);
        confirmDTO.setRemark(bo.getRemark());
        inOrderApi.confirmInOrder(confirmDTO);
        return inOrderId;
    }

    /**
     * 本工单的入库流水，按生产工单 + 来源业务类型一次查回。
     *
     * <p>{@code biz_type = IN_ORDER} 是完工入库流水的唯一标识：同一张工单上还有开工、报工、完工的流水，
     * 它们的来源业务分别是工序任务、报工单、工单本身。
     */
    private List<WipTxnDO> inboundTxns(Long prodOrderId) {
        return wipTxnMapper.selectList(WipTxnDO::getProdOrderId, prodOrderId,
                WipTxnDO::getBizType, MesBizTypeEnum.IN_ORDER.name());
    }

    /**
     * 缺省批次号 {@code {工单号}-{yyyyMMdd}}。
     *
     * <p>不走 {@code MesCodeService}：{@code MesCodeRuleConstants} 里没有成品批号规则，
     * 编造一个规则码会在生产上直接抛"编码规则不存在"；而这个格式依赖工单号这个业务上下文，
     * infra 的编码规则给不出来。工单号本身已经保证唯一，加日期只是为了让批次号一眼看出生产日。
     */
    private String defaultLotNo(ProdOrderDO order) {
        return order.getOrderNo() + "-" + LocalDate.now().format(LOT_NO_DATE);
    }

    /** 单位：工单上的优先，缺失时取物料基本单位。入库行的 {@code uom_id} 在 WMS 侧是 NOT NULL。 */
    private UomRespDTO uom(Long orderUomId, MaterialRespDTO material) {
        Long uomId = isNone(orderUomId) ? material.getBaseUomId() : orderUomId;
        UomRespDTO uom = uomId == null ? null : uomApi.getUomMap(List.of(uomId)).get(uomId);
        if (uom == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        return uom;
    }

    /** 作业工单号快照，只作追溯用，取不到就留空。 */
    private String workOrderNoOf(Long workOrderId) {
        if (isNone(workOrderId)) {
            return null;
        }
        WorkOrderDO workOrder = workOrderMapper.selectById(workOrderId);
        return workOrder == null ? null : workOrder.getWorkOrderNo();
    }

    /** 数量转可读文本：库表是 decimal(20,6)，直接拼会给车间看到"100.000000"这种噪声。 */
    private String plain(BigDecimal qty) {
        return qty.stripTrailingZeros().toPlainString();
    }

    private Long firstPresent(Long preferred, Long fallback) {
        return isNone(preferred) ? fallback : preferred;
    }

    private BigDecimal nvl(BigDecimal qty) {
        return qty == null ? BigDecimal.ZERO : qty;
    }

    /** 可选维度落 0 不落 null，与库表 NOT NULL DEFAULT 0 的口径一致。 */
    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private boolean isNone(Long id) {
        return id == null || ObjUtil.equal(MesConstants.NONE_ID, id);
    }

    private ProdOrderDO requireProdOrder(Long id) {
        Assert.notNull(id, "生产工单编号不能为空");
        ProdOrderDO order = prodOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /** 幂等命令载体，{@link MesCommandReqDTO} 是抽象类，这里给它一个最小实现。 */
    private static class InboundCommand extends MesCommandReqDTO {
    }

}
