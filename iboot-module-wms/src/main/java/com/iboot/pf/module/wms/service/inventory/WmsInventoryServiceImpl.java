package com.iboot.pf.module.wms.service.inventory;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsIoTypeEnum;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsStockScopeEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_VERSION_CONFLICT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_ONHAND_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_POST_QTY_INVALID;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_POST_TYPE_INVALID;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.TXN;

/** 库存 Service 实现。库存余额只能通过过账修改，不对外提供直接改余额的方法。 */
@Service
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Resource
    private WmsInvBalanceMapper wmsInvBalanceMapper;
    @Resource
    private WmsInvTxnMapper wmsInvTxnMapper;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsWhMapper whMapper;

    @Override
    public List<WmsInvBalanceDO> getBalanceList(WmsInventoryQueryBO query) {
        return wmsInvBalanceMapper.selectBalanceList(query);
    }

    @Override
    public PageResult<WmsInvBalanceDO> getBalancePage(PageParam pageParam, WmsInventoryQueryBO query) {
        return wmsInvBalanceMapper.selectBalancePage(pageParam, query);
    }

    @Override
    public BigDecimal getAvailableQty(WmsInventoryQueryBO query) {
        BigDecimal total = BigDecimal.ZERO;
        for (WmsInvBalanceDO balance : wmsInvBalanceMapper.selectBalanceList(query)) {
            total = total.add(WmsInventoryService.availableQtyOf(balance));
        }
        return total;
    }

    @Override
    public Map<Long, BigDecimal> getAvailableQtyMap(WmsInventoryQueryBO query) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (WmsInvBalanceDO balance : wmsInvBalanceMapper.selectBalanceList(query)) {
            result.merge(balance.getMatId(), WmsInventoryService.availableQtyOf(balance), BigDecimal::add);
        }
        return result;
    }

    @Override
    public List<WmsInvTxnDO> getTxnList(WmsInventoryTxnQueryBO query) {
        return wmsInvTxnMapper.selectTxnList(query);
    }

    @Override
    public boolean posted(String idempotencyKey) {
        return wmsInvTxnMapper.selectByIdempotencyKey(idempotencyKey) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInvTxnDO post(WmsInvPostBO bo) {
        // 1. 幂等：同一个幂等键只过账一次
        WmsInvTxnDO existed = wmsInvTxnMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed;
        }
        // 2. 校验并推导收发方向
        if (bo.getQty() == null || bo.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(INV_POST_QTY_INVALID);
        }
        WmsTxnTypeEnum txnType = parseTxnType(bo.getTxnType());
        String ioType = bo.getIoType() != null && !bo.getIoType().isEmpty()
                ? bo.getIoType() : txnType.getIoType().name();

        // 3. 按方向更新余额
        BigDecimal beforeQty;
        BigDecimal afterQty;
        switch (WmsIoTypeEnum.valueOf(ioType)) {
            case IN:
                WmsInvBalanceDO target = getOrCreateBalance(buildTargetDim(bo), bo);
                beforeQty = target.getQtyOnhand();
                target.setQtyOnhand(beforeQty.add(bo.getQty()));
                updateBalance(target);
                afterQty = target.getQtyOnhand();
                break;
            case OUT:
                WmsInvBalanceDO source = requireBalance(buildSourceDim(bo));
                if (source.getQtyOnhand().compareTo(bo.getQty()) < 0) {
                    throw exception(INV_ONHAND_QTY_NOT_ENOUGH);
                }
                beforeQty = source.getQtyOnhand();
                source.setQtyOnhand(beforeQty.subtract(bo.getQty()));
                updateBalance(source);
                afterQty = source.getQtyOnhand();
                break;
            case MOVE:
                WmsInvBalanceDO moveSource = requireBalance(buildSourceDim(bo));
                if (moveSource.getQtyOnhand().compareTo(bo.getQty()) < 0) {
                    throw exception(INV_ONHAND_QTY_NOT_ENOUGH);
                }
                beforeQty = moveSource.getQtyOnhand();
                moveSource.setQtyOnhand(beforeQty.subtract(bo.getQty()));
                updateBalance(moveSource);
                WmsInvBalanceDO moveTarget = getOrCreateBalance(buildTargetDim(bo), bo);
                moveTarget.setQtyOnhand(moveTarget.getQtyOnhand().add(bo.getQty()));
                updateBalance(moveTarget);
                afterQty = moveTarget.getQtyOnhand();
                break;
            case STATUS:
                WmsInvBalanceDO balance = requireBalance(buildSourceDim(bo));
                beforeQty = balance.getQtyOnhand();
                applyStatusChange(balance, txnType, bo.getQty());
                updateBalance(balance);
                afterQty = beforeQty;
                break;
            default:
                throw exception(INV_POST_TYPE_INVALID);
        }

        // 4. 写流水
        WmsInvTxnDO txn = buildTxn(bo, txnType, ioType, beforeQty, afterQty);
        wmsInvTxnMapper.insert(txn);
        return txn;
    }

    private void applyStatusChange(WmsInvBalanceDO balance, WmsTxnTypeEnum txnType, BigDecimal qty) {
        switch (txnType) {
            case RESERVE:
                balance.setQtyReserved(nvl(balance.getQtyReserved()).add(qty));
                break;
            case RELEASE:
                BigDecimal reserved = nvl(balance.getQtyReserved()).subtract(qty);
                if (reserved.compareTo(BigDecimal.ZERO) < 0) {
                    throw exception(RESERVATION_REMAIN_NOT_ENOUGH);
                }
                balance.setQtyReserved(reserved);
                break;
            case FREEZE:
                balance.setQtyFrozen(nvl(balance.getQtyFrozen()).add(qty));
                break;
            case UNFREEZE:
                BigDecimal frozen = nvl(balance.getQtyFrozen()).subtract(qty);
                if (frozen.compareTo(BigDecimal.ZERO) < 0) {
                    throw exception(LOCK_REMAIN_NOT_ENOUGH);
                }
                balance.setQtyFrozen(frozen);
                break;
            default:
                throw exception(INV_POST_TYPE_INVALID);
        }
    }

    private WmsInvTxnDO buildTxn(WmsInvPostBO bo, WmsTxnTypeEnum txnType, String ioType,
                                 BigDecimal beforeQty, BigDecimal afterQty) {
        WmsInvTxnDO txn = new WmsInvTxnDO();
        txn.setId(null);
        txn.setTxnNo(codeService.generate(TXN, null));
        txn.setTxnType(txnType.name());
        txn.setBizType(bo.getBizType());
        txn.setBizNo(bo.getBizNo());
        txn.setBizLineNo(bo.getBizLineNo());
        txn.setIoType(ioType);
        txn.setInvOrgId(bo.getInvOrgId());
        txn.setPlantId(bo.getPlantId());
        txn.setWhId(bo.getWhId());
        txn.setFromLocId(nvl(bo.getFromLocId()));
        txn.setToLocId(nvl(bo.getToLocId()));
        txn.setMatId(bo.getMatId());
        txn.setBatchId(bo.getBatchId());
        txn.setContainerId(nvl(bo.getContainerId()));
        txn.setUomId(bo.getUomId());
        txn.setQty(bo.getQty());
        txn.setBeforeQty(beforeQty);
        txn.setAfterQty(afterQty);
        txn.setQualityStatusFrom(bo.getQualityStatusFrom());
        txn.setQualityStatusTo(bo.getQualityStatusTo());
        txn.setProjectIdFrom(nvl(bo.getProjectIdFrom()));
        txn.setProjectTaskIdFrom(nvl(bo.getProjectTaskIdFrom()));
        txn.setProjectIdTo(nvl(bo.getProjectIdTo()));
        txn.setProjectTaskIdTo(nvl(bo.getProjectTaskIdTo()));
        txn.setMatCode(bo.getMatCode());
        txn.setMatName(bo.getMatName());
        txn.setUomCode(bo.getUomCode());
        txn.setLotNo(bo.getLotNo());
        txn.setFromLocCode(bo.getFromLocCode());
        txn.setToLocCode(bo.getToLocCode());
        txn.setIdempotencyKey(bo.getIdempotencyKey());
        txn.setRemark(bo.getRemark());
        return txn;
    }

    /** 来源维度：出、转移的减少方、状态变更的作用对象。 */
    private WmsInvBalanceDO buildSourceDim(WmsInvPostBO bo) {
        WmsInvBalanceDO dim = new WmsInvBalanceDO();
        dim.setInvOrgId(bo.getFromInvOrgId() != null ? bo.getFromInvOrgId() : bo.getInvOrgId());
        dim.setPlantId(bo.getFromPlantId() != null ? bo.getFromPlantId() : bo.getPlantId());
        dim.setWhId(bo.getFromWhId() != null ? bo.getFromWhId() : bo.getWhId());
        dim.setLocId(nvl(bo.getFromLocId()));
        dim.setMatId(bo.getMatId());
        dim.setBatchId(bo.getBatchId());
        dim.setContainerId(nvl(bo.getContainerId()));
        dim.setProjectId(nvl(bo.getProjectIdFrom()));
        dim.setProjectTaskId(nvl(bo.getProjectTaskIdFrom()));
        dim.setStockScope(defaultScope(bo.getFromStockScope()));
        dim.setQualityStatus(bo.getQualityStatusFrom());
        return dim;
    }

    /** 目标维度：入、转移的增加方。 */
    private WmsInvBalanceDO buildTargetDim(WmsInvPostBO bo) {
        WmsInvBalanceDO dim = new WmsInvBalanceDO();
        dim.setInvOrgId(bo.getToInvOrgId() != null ? bo.getToInvOrgId() : bo.getInvOrgId());
        dim.setPlantId(bo.getToPlantId() != null ? bo.getToPlantId() : bo.getPlantId());
        dim.setWhId(bo.getToWhId() != null ? bo.getToWhId() : bo.getWhId());
        dim.setLocId(nvl(bo.getToLocId()));
        dim.setMatId(bo.getMatId());
        dim.setBatchId(bo.getBatchId());
        dim.setContainerId(nvl(bo.getContainerId()));
        dim.setProjectId(nvl(bo.getProjectIdTo()));
        dim.setProjectTaskId(nvl(bo.getProjectTaskIdTo()));
        dim.setStockScope(defaultScope(bo.getToStockScope()));
        dim.setQualityStatus(bo.getQualityStatusTo());
        return dim;
    }

    private WmsInvBalanceDO requireBalance(WmsInvBalanceDO dim) {
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectByDim(dim);
        if (balance == null) {
            throw exception(INV_BALANCE_NOT_EXISTS);
        }
        return balance;
    }

    private WmsInvBalanceDO getOrCreateBalance(WmsInvBalanceDO dim, WmsInvPostBO bo) {
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectByDim(dim);
        if (balance != null) {
            return balance;
        }
        balance = new WmsInvBalanceDO();
        balance.setId(null);
        balance.setInvOrgId(dim.getInvOrgId());
        balance.setPlantId(dim.getPlantId());
        balance.setWhId(dim.getWhId());
        balance.setLocId(dim.getLocId());
        balance.setMatId(dim.getMatId());
        balance.setBatchId(dim.getBatchId());
        balance.setContainerId(dim.getContainerId());
        balance.setProjectId(dim.getProjectId());
        balance.setProjectTaskId(dim.getProjectTaskId());
        balance.setStockScope(dim.getStockScope());
        balance.setQualityStatus(dim.getQualityStatus());
        fillBalanceSnapshot(balance, bo);
        balance.setQtyOnhand(BigDecimal.ZERO);
        balance.setQtyReserved(BigDecimal.ZERO);
        balance.setQtyFrozen(BigDecimal.ZERO);
        balance.setVersion(0);
        wmsInvBalanceMapper.insert(balance);
        return balance;
    }

    /**
     * 补全余额行上的六个快照列。
     *
     * <p><b>这六列在 {@code wms_inv_balance} 上都是 {@code NOT NULL} 且没有默认值</b>
     * （{@code mat_code} {@code mat_name} {@code uom_code} {@code lot_no} {@code wh_code}
     * {@code loc_code}）。不填的后果不是"字段为空"而是**整条 INSERT 失败**：MyBatis-Plus 默认的
     * {@code NOT_NULL} 插入策略会把 null 字段从 INSERT 语句里剔除，MySQL 严格模式下直接报
     * {@code Field 'mat_code' doesn't have a default value}。而这个方法只在"首次向一个此前没有
     * 库存的维度组合入库"时被调到 —— 那正是收货上架的常态（新到的料第一次上架到某个库位），
     * 所以漏填等于收货上架的默认路径必挂。
     *
     * <p>快照从主数据取而不是让调用方传：余额行一生只新建一次，查询成本可忽略；而让每个过账
     * 调用方各自传快照，传错了没人发现，主数据才是权威来源。这与 {@code WmsSnapshotService}
     * 给单据明细补快照是同一套口径。
     *
     * <p>{@code lot_no} 与 {@code loc_code} 允许维度上为 0（散批、未定库位），但列是 {@code NOT NULL}，
     * 所以回落到 {@code DEFAULT} 与空串而不是留 null。
     */
    private void fillBalanceSnapshot(WmsInvBalanceDO balance, WmsInvPostBO bo) {
        balance.setMatCode(StrUtil.blankToDefault(bo.getMatCode(), StrUtil.EMPTY));
        balance.setMatName(StrUtil.blankToDefault(bo.getMatName(), StrUtil.EMPTY));
        balance.setUomCode(StrUtil.blankToDefault(bo.getUomCode(), StrUtil.EMPTY));
        balance.setLotNo(StrUtil.blankToDefault(bo.getLotNo(), WmsConstants.DEFAULT_LOT_NO));
        balance.setWhCode(whCode(balance.getWhId()));
        balance.setLocCode(StrUtil.blankToDefault(bo.getToLocCode(), StrUtil.EMPTY));
    }

    /** 余额快照取仓库编码，不反查跨模块主数据。 */
    private String whCode(Long whId) {
        if (whId == null || whId == 0L) {
            return StrUtil.EMPTY;
        }
        WmsWhDO wh = whMapper.selectById(whId);
        if (wh == null) {
            throw exception(WH_NOT_EXISTS);
        }
        return wh.getCode();
    }

    /** 按版本号更新余额，版本冲突直接失败，由调用方重试。 */
    private void updateBalance(WmsInvBalanceDO balance) {
        int currentVersion = balance.getVersion() == null ? 0 : balance.getVersion();
        balance.setVersion(currentVersion + 1);
        LambdaUpdateWrapper<WmsInvBalanceDO> wrapper = new LambdaUpdateWrapper<WmsInvBalanceDO>()
                .eq(WmsInvBalanceDO::getId, balance.getId())
                .eq(WmsInvBalanceDO::getVersion, currentVersion);
        if (wmsInvBalanceMapper.update(balance, wrapper) == 0) {
            throw exception(INV_BALANCE_VERSION_CONFLICT);
        }
    }

    private WmsTxnTypeEnum parseTxnType(String txnType) {
        for (WmsTxnTypeEnum value : WmsTxnTypeEnum.values()) {
            if (ObjUtil.equal(value.name(), txnType)) {
                return value;
            }
        }
        throw exception(INV_POST_TYPE_INVALID);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static Long nvl(Long value) {
        return value == null ? WmsConstants.NONE_ID : value;
    }

    private static String defaultScope(String scope) {
        return WmsStockScopeEnum.defaultIfEmpty(scope);
    }

}
