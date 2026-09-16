package com.iboot.pf.module.wms.service.reservation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.reservation.WmsInvReservationMapper;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_LOC_NOT_FOUND;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_ACTIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.RESERVATION;

/** 库存预留 Service 实现。 */
@Service
public class WmsInventoryReservationServiceImpl implements WmsInventoryReservationService {

    /** 过账幂等键后缀，预留命令与它触发的流水各用一个键。 */
    private static final String POST_KEY_SUFFIX = ":POST";

    @Resource
    private WmsInvReservationMapper wmsInvReservationMapper;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private UomApi uomApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reserve(WmsReserveBO bo) {
        WmsInvReservationDO existed = wmsInvReservationMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (bo.getQty() == null || bo.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        String qualityStatus = bo.getQualityStatus() == null || bo.getQualityStatus().isEmpty()
                ? WmsQualityStatusEnum.QUALIFIED.name() : bo.getQualityStatus();
        // 调用方不指定库位、批次时，由 WMS 按账上可用量解析它们：
        // 预留占的是哪条余额记录，后续出库就必须从同一条记录出，库位与批次是这条记录的维度载体，
        // 也是 wms_inv_reservation.loc_id / batch_id 的 NOT NULL 约束要求。挑库位与批次是 WMS
        // 拣选策略的职责，调用方（MES 领料申请只有物料+数量语义）不自己实现，也不该硬填。
        WmsInvBalanceDO picked = resolveDim(bo, qualityStatus);
        Long locId = bo.getLocId();
        Long batchId = bo.getBatchId();

        // 预留必须校验可用量充足
        BigDecimal available = inventoryService.getAvailableQty(buildQuery(bo, locId, batchId, qualityStatus));
        if (available.compareTo(bo.getQty()) < 0) {
            throw exception(INV_AVAILABLE_QTY_NOT_ENOUGH);
        }

        WmsInvReservationDO reservation = new WmsInvReservationDO();
        reservation.setId(null);
        reservation.setReservationNo(codeService.generate(RESERVATION, null));
        reservation.setBizType(bo.getBizType());
        reservation.setBizNo(bo.getBizNo());
        reservation.setBizLineNo(bo.getBizLineNo());
        reservation.setInvOrgId(bo.getInvOrgId());
        reservation.setWhId(bo.getWhId());
        reservation.setLocId(locId);
        reservation.setMatId(bo.getMatId());
        reservation.setBatchId(batchId);
        reservation.setContainerId(bo.getContainerId());
        reservation.setProjectId(bo.getProjectId());
        reservation.setProjectTaskId(bo.getProjectTaskId());
        reservation.setQualityStatus(qualityStatus);
        reservation.setQty(bo.getQty());
        reservation.setReleasedQty(BigDecimal.ZERO);
        reservation.setStatus(WmsReservationStatusEnum.ACTIVE.name());
        reservation.setExpireTime(bo.getExpireTime());
        reservation.setIdempotencyKey(bo.getIdempotencyKey());
        reservation.setRemark(bo.getRemark());
        wmsInvReservationMapper.insert(reservation);

        inventoryService.post(buildPostBO(reservation, picked, WmsTxnTypeEnum.RESERVE, bo.getQty(),
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
        return reservation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(WmsReservationHandleBO bo) {
        handle(bo, WmsTxnTypeEnum.RELEASE, WmsReservationStatusEnum.RELEASED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(WmsReservationHandleBO bo) {
        handle(bo, WmsTxnTypeEnum.RELEASE, WmsReservationStatusEnum.CONSUMED);
    }

    private void handle(WmsReservationHandleBO bo, WmsTxnTypeEnum txnType, WmsReservationStatusEnum targetStatus) {
        WmsInvReservationDO reservation = requireReservation(bo);
        // 幂等：该命令生成的流水已存在，说明已处理过
        if (inventoryService.posted(bo.getIdempotencyKey() + POST_KEY_SUFFIX)) {
            return;
        }
        String status = reservation.getStatus();
        if (WmsReservationStatusEnum.RELEASED.name().equals(status)
                || WmsReservationStatusEnum.CONSUMED.name().equals(status)
                || WmsReservationStatusEnum.CANCELLED.name().equals(status)) {
            throw exception(RESERVATION_NOT_ACTIVE);
        }
        BigDecimal remaining = reservation.getQty().subtract(nvl(reservation.getReleasedQty()));
        BigDecimal qty = bo.getQty() == null ? remaining : bo.getQty();
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        if (qty.compareTo(remaining) > 0) {
            throw exception(RESERVATION_REMAIN_NOT_ENOUGH);
        }

        reservation.setReleasedQty(nvl(reservation.getReleasedQty()).add(qty));
        reservation.setStatus(reservation.getReleasedQty().compareTo(reservation.getQty()) >= 0
                ? targetStatus.name() : WmsReservationStatusEnum.PARTIAL.name());
        reservation.setRemark(bo.getRemark());
        wmsInvReservationMapper.updateById(reservation);

        inventoryService.post(buildPostBO(reservation, null, txnType, qty,
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
    }

    private WmsInvReservationDO requireReservation(WmsReservationHandleBO bo) {
        WmsInvReservationDO reservation = bo.getReservationId() != null
                ? wmsInvReservationMapper.selectById(bo.getReservationId()) : null;
        if (reservation == null && bo.getReservationNo() != null && !bo.getReservationNo().isEmpty()) {
            reservation = wmsInvReservationMapper.selectByReservationNo(bo.getReservationNo());
        }
        if (reservation == null) {
            throw exception(RESERVATION_NOT_EXISTS);
        }
        return reservation;
    }

    private WmsInventoryQueryBO buildQuery(WmsReserveBO bo, Long locId, String qualityStatus) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(bo.getInvOrgId());
        query.setWhId(bo.getWhId());
        query.setLocId(locId);
        query.setMatId(bo.getMatId());
        query.setBatchId(bo.getBatchId());
        query.setContainerId(bo.getContainerId());
        query.setProjectId(bo.getProjectId());
        query.setProjectTaskId(bo.getProjectTaskId());
        query.setQualityStatus(qualityStatus);
        return query;
    }

    /**
     * 维度解析：调用方没指定库位、批次时，按可用量最大的余额记录把两者补齐。
     *
     * <p>为什么在 WMS 侧解析而不是让调用方传：同一物料在一个仓库里通常分布在多个库位、多个批次，
     * "从哪条记录出"取决于账上各记录的可用量，这个信息只有 WMS 有。调用方（MES 领料申请）
     * 的业务语义是"物料 + 数量"，硬填库位或批次等于把 WMS 的库存管理职责搬到上游，
     * 填错还会让预留占到一条没有可用量的余额上。
     *
     * <p><b>库位与批次必须取自同一条余额记录</b>，不能分别挑"可用量最大的库位"和"可用量最大的批次"：
     * 预留之后 {@code RESERVE} 状态过账要按 {@code (库位, 批次)} 定位余额行累加 {@code qty_reserved}，
     * 两个维度拼错组合会命中不存在的余额行直接抛 {@code INV_BALANCE_NOT_EXISTS}；即便碰巧存在，
     * 占住的也不是真正要出的那条记录，后续出库核销会对不上账。所以先选中记录，再整条取维度。
     *
     * <p>为什么按可用量降序取第一条：预留是一次占用，只会记在一条余额记录上，后续出库要从同一条
     * 记录扣现存，所以必须挑一条这数量真的放得下的记录——挑小的会让 {@code qty_reserved} 超过现存，
     * 可用量算成负数。排除零或负可用量（已被别的预留或冻结占空）的记录。
     *
     * <p>已显式指定维度的调用方行为完全不变：{@code locId} 与 {@code batchId} 各自独立判定，
     * 只补没给的那个；解析不到可用的余额记录时抛业务错误码，不让 NOT NULL 约束异常穿出去。
     *
     * @return 选中的余额记录；调用方已把两个维度都给全时为 {@code null}（无需反查快照）
     */
    private WmsInvBalanceDO resolveDim(WmsReserveBO bo, String qualityStatus) {
        boolean locAbsent = bo.getLocId() == null;
        boolean batchAbsent = bo.getBatchId() == null;
        if (!locAbsent && !batchAbsent) {
            return null;
        }
        WmsInventoryQueryBO query = buildQuery(bo, locAbsent ? null : bo.getLocId(),
                batchAbsent ? null : bo.getBatchId(), qualityStatus);
        WmsInvBalanceDO picked = inventoryService.getBalanceList(query).stream()
                .filter(balance -> WmsInventoryService.availableQtyOf(balance).compareTo(bo.getQty()) >= 0)
                .max(Comparator.comparing(WmsInventoryService::availableQtyOf))
                .orElseThrow(() -> exception(RESERVATION_LOC_NOT_FOUND, bo.getMatId(), bo.getWhId()));
        if (locAbsent) {
            Long locId = picked.getLocId();
            if (locId == null || WmsConstants.NONE_ID.equals(locId)) {
                throw exception(RESERVATION_LOC_NOT_FOUND, bo.getMatId(), bo.getWhId());
            }
            bo.setLocId(locId);
        }
        if (batchAbsent) {
            // 批次维度落 0 而不是 null：余额行的可选维度一律用 0 表示"无"，列又是 NOT NULL
            bo.setBatchId(picked.getBatchId() == null ? WmsConstants.NONE_ID : picked.getBatchId());
        }
        return picked;
    }

    private WmsInventoryQueryBO buildQuery(WmsReserveBO bo, Long locId, Long batchId, String qualityStatus) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(bo.getInvOrgId());
        query.setWhId(bo.getWhId());
        query.setLocId(locId);
        query.setMatId(bo.getMatId());
        query.setBatchId(batchId);
        query.setContainerId(bo.getContainerId());
        query.setProjectId(bo.getProjectId());
        query.setProjectTaskId(bo.getProjectTaskId());
        query.setQualityStatus(qualityStatus);
        return query;
    }

    /**
     * 预留流水参数。
     *
     * <p>{@code wms_inv_txn} 上有六个 NOT NULL 且无默认值的列（{@code plant_id}、{@code uom_id}
     * 与四个快照 {@code mat_code} {@code mat_name} {@code uom_code} {@code lot_no}），预留记录本身
     * 一个都存不了——{@code wms_inv_reservation} 连 {@code plant_id} / {@code uom_id} 列都没有，
     * 也没有明细行表可以像入库单、出库单那样提供"按什么单位记的"语义。但 {@code RESERVE} 流水
     * 本来就要按 {@code (库位, 批次)} 定位余额行累加 {@code qty_reserved}，那条余额记录上恰好带着
     * 全部这些信息（含 {@code uom_code}，语义正是"基准单位编码快照"），所以整条取即可，不需要额外查询。
     *
     * <p>{@code uom_id} 由 {@code uom_code} 反查：{@code uom_code} 是余额行上的唯一可用线索，
     * 而 {@code wms_inv_txn.uom_id} 要求编号。反查走 {@code UomApi} 门面，不在 WMS 本地建单位表映射。
     */
    private WmsInvPostBO buildPostBO(WmsInvReservationDO reservation, WmsInvBalanceDO picked, WmsTxnTypeEnum txnType,
                                     BigDecimal qty, String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(txnType.name());
        postBO.setBizType(reservation.getBizType());
        postBO.setBizNo(reservation.getBizNo());
        postBO.setBizLineNo(reservation.getBizLineNo());
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromLocId(reservation.getLocId());
        postBO.setQualityStatusFrom(reservation.getQualityStatus());
        postBO.setQualityStatusTo(reservation.getQualityStatus());
        postBO.setProjectIdFrom(reservation.getProjectId());
        postBO.setProjectTaskIdFrom(reservation.getProjectTaskId());
        postBO.setInvOrgId(reservation.getInvOrgId());
        postBO.setPlantId(picked != null ? picked.getPlantId() : plantIdOf(reservation.getWhId()));
        postBO.setWhId(reservation.getWhId());
        postBO.setMatId(reservation.getMatId());
        postBO.setBatchId(reservation.getBatchId());
        postBO.setContainerId(reservation.getContainerId());
        postBO.setRemark(remark);
        // 快照与单位取余额记录（已显式指定维度的调用方没有 picked，回落到主数据反查）
        if (picked != null) {
            postBO.setMatCode(picked.getMatCode());
            postBO.setMatName(picked.getMatName());
            postBO.setUomCode(picked.getUomCode());
            postBO.setLotNo(picked.getLotNo());
            postBO.setFromLocCode(picked.getLocCode());
        }
        postBO.setUomId(uomIdOf(postBO.getUomCode(), picked));
        return postBO;
    }

    /** 工厂取仓库上的归属工厂，仓库主数据是权威来源。 */
    private Long plantIdOf(Long whId) {
        if (whId == null || WmsConstants.NONE_ID.equals(whId)) {
            return WmsConstants.NONE_ID;
        }
        WmsWhDO wh = warehouseService.getWh(whId);
        return wh == null || wh.getPlantId() == null ? WmsConstants.NONE_ID : wh.getPlantId();
    }

    /**
     * 由单位编码反查单位编号。余额行只留编码，流水列要编号。
     *
     * <p>一次 {@code IN} 查询而不是逐行查，避免 N+1；查不到不阻断预留——{@code uom_id} 只是流水上的
     * 可读性字段，为它放弃整笔预留会让领料链断在这里，得不偿失。
     */
    private Long uomIdOf(String uomCode, WmsInvBalanceDO picked) {
        if (StrUtil.isBlank(uomCode) || picked == null) {
            return WmsConstants.NONE_ID;
        }
        UomRespDTO uom = uomApi.getUomMapByCodes(Set.of(uomCode)).get(uomCode);
        return uom == null || uom.getId() == null ? WmsConstants.NONE_ID : uom.getId();
    }

    @Override
    public WmsInvReservationDO getReservation(Long id) {
        return wmsInvReservationMapper.selectById(id);
    }

    @Override
    public List<WmsInvReservationDO> getReservationList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsInvReservationMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsInvReservationDO> getReservationMap(Collection<Long> ids) {
        return convertMap(getReservationList(ids), WmsInvReservationDO::getId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
