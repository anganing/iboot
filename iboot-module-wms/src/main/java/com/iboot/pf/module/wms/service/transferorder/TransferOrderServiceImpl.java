package com.iboot.pf.module.wms.service.transferorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.transferorder.TransferOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.transferorderline.TransferOrderLineMapper;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.enums.WmsStockScopeEnum;
import com.iboot.pf.module.wms.service.snapshot.WmsSnapshotService;

import java.math.BigDecimal;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库存转移调拨单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class TransferOrderServiceImpl implements TransferOrderService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private TransferOrderMapper transferOrderMapper;
    @Resource
    private TransferOrderLineMapper transferOrderLineMapper;
    @Resource
    private WmsSnapshotService snapshotService;

    @Override
    public Long createTransferOrder(TransferOrderSaveReqVO createReqVO) {
        // 插入
        WmsTransferOrderDO transferOrder = BeanUtils.toBean(createReqVO, WmsTransferOrderDO.class);
        transferOrder.setId(null);
        transferOrder.setTransferNo(codeService.generate(WmsCodeRuleConstants.TRANSFER_ORDER, transferOrder.getTransferNo()));
        validateTransferOrderUnique(transferOrder, null);
        transferOrderMapper.insert(transferOrder);

        // 返回
        return transferOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public TransferOrderImportRespVO importTransferOrderList(List<TransferOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return TransferOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        TransferOrderImportRespVO respVO = TransferOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createTransferOrder(BeanUtils.toBean(importItem, TransferOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateTransferOrder(TransferOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateTransferOrderExists(updateReqVO.getId());
        // 更新
        WmsTransferOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsTransferOrderDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateTransferOrderUnique(updateObj, updateReqVO.getId());
        transferOrderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferOrder(Long id) {
        // 校验存在
        validateTransferOrderExists(id);
        // 删除
        transferOrderMapper.deleteById(id);

        // 删除子表
        deleteTransferOrderLineByOrderId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteTransferOrderListByIds(List<Long> ids) {
        // 删除
        transferOrderMapper.deleteByIds(ids);
    
    // 删除子表
            deleteTransferOrderLineByOrderIds(ids);
    }


    private void validateTransferOrderExists(Long id) {
        if (transferOrderMapper.selectById(id) == null) {
            throw exception(TRANSFER_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public TransferOrderRespVO getTransferOrder(Long id) {
        return transferOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TransferOrderRespVO> getTransferOrderPage(TransferOrderPageReqVO pageReqVO) {
        return transferOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-库存转移调拨明细） ====================

    @Override
    public PageResult<TransferOrderLineRespVO> getTransferOrderLinePage(TransferOrderLinePageReqVO pageReqVO) {
        return transferOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createTransferOrderLine(TransferOrderLineSaveReqVO createReqVO) {
        validateTransferOrderExists(createReqVO.getOrderId());
        validateLineNoUnique(createReqVO.getOrderId(), createReqVO.getLineNo(), null);
        WmsTransferOrderLineDO transferOrderLine = BeanUtils.toBean(createReqVO, WmsTransferOrderLineDO.class);
        transferOrderLine.setId(null);
        fillLineSnapshot(transferOrderLine);
        // 搬运与过账数量由作业推进，创建时归零
        transferOrderLine.setMovedQty(BigDecimal.ZERO);
        transferOrderLine.setPostedQty(BigDecimal.ZERO);
        transferOrderLineMapper.insert(transferOrderLine);
        return transferOrderLine.getId();
    }

    @Override
    public void updateTransferOrderLine(TransferOrderLineSaveReqVO updateReqVO) {
        // 校验存在
        validateTransferOrderLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getOrderId(), updateReqVO.getLineNo(), updateReqVO.getId());
        WmsTransferOrderLineDO transferOrderLine = BeanUtils.toBean(updateReqVO, WmsTransferOrderLineDO.class);
        fillLineSnapshot(transferOrderLine);
        transferOrderLineMapper.updateById(transferOrderLine);
    }

    @Override
    public void deleteTransferOrderLine(Long id) {
        // 删除
        transferOrderLineMapper.deleteById(id);
    }

	@Override
	public void deleteTransferOrderLineListByIds(List<Long> ids) {
        // 删除
        transferOrderLineMapper.deleteByIds(ids);
	}

    @Override
    public TransferOrderLineRespVO getTransferOrderLine(Long id) {
        return transferOrderLineMapper.selectJoinById(id);
    }

    private void validateTransferOrderLineExists(Long id) {
        if (transferOrderLineMapper.selectById(id) == null) {
            throw exception(TRANSFER_ORDER_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (order_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long orderId, Integer lineNo, Long excludeId) {
        WmsTransferOrderLineDO existed = transferOrderLineMapper.selectByOrderIdAndLineNo(orderId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(TRANSFER_ORDER_LINE_NO_DUPLICATE);
    }

    /** 明细上的物料、单位、批次快照补全，库存范围为空时按公共库存兜底。 */
    private void fillLineSnapshot(WmsTransferOrderLineDO line) {
        MaterialRespDTO material = snapshotService.requireMaterial(line.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setUomCode(snapshotService.requireUomCode(line.getUomId()));
        line.setLotNo(snapshotService.lotNoOrNull(line.getBatchId()));
        snapshotService.validateContainer(line.getContainerId());
        line.setFromStockScope(WmsStockScopeEnum.defaultIfEmpty(line.getFromStockScope()));
        line.setToStockScope(WmsStockScopeEnum.defaultIfEmpty(line.getToStockScope()));
    }

    private void deleteTransferOrderLineByOrderId(Long orderId) {
        transferOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteTransferOrderLineByOrderIds(List<Long> orderIds) {
        transferOrderLineMapper.deleteByOrderIds(orderIds);
	}


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateTransferOrderUnique(WmsTransferOrderDO transferOrder, Long excludeId) {
        WmsTransferOrderDO existed = transferOrderMapper.selectByTransferNo(transferOrder.getTransferNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(TRANSFER_ORDER_NO_DUPLICATE);
    }

}
