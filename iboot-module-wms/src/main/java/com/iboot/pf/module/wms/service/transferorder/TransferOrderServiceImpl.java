package com.iboot.pf.module.wms.service.transferorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.transferorder.TransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.transferorder.TransferOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.transferorderline.TransferOrderLineMapper;

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
    private TransferOrderMapper transferOrderMapper;
    @Resource
    private TransferOrderLineMapper transferOrderLineMapper;

    @Override
    public Long createTransferOrder(TransferOrderSaveReqVO createReqVO) {
        // 插入
        TransferOrderDO transferOrder = BeanUtils.toBean(createReqVO, TransferOrderDO.class);
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
        TransferOrderDO updateObj = BeanUtils.toBean(updateReqVO, TransferOrderDO.class);
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
    public PageResult<TransferOrderLineDO> getTransferOrderLinePage(PageParam pageReqVO, Long orderId) {
        return transferOrderLineMapper.selectPage(pageReqVO, orderId);
    }

    @Override
    public Long createTransferOrderLine(TransferOrderLineDO transferOrderLine) {
        transferOrderLine.clean(); // 清理掉创建、更新时间等相关属性值
        transferOrderLineMapper.insert(transferOrderLine);
        return transferOrderLine.getId();
    }

    @Override
    public void updateTransferOrderLine(TransferOrderLineDO transferOrderLine) {
        // 校验存在
        validateTransferOrderLineExists(transferOrderLine.getId());
        // 更新
        transferOrderLine.clean(); // 解决更新情况下：updateTime 不更新
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
    public TransferOrderLineDO getTransferOrderLine(Long id) {
        return transferOrderLineMapper.selectById(id);
    }

    private void validateTransferOrderLineExists(Long id) {
        if (transferOrderLineMapper.selectById(id) == null) {
            throw exception(TRANSFER_ORDER_LINE_NOT_EXISTS);
        }
    }

    private void deleteTransferOrderLineByOrderId(Long orderId) {
        transferOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteTransferOrderLineByOrderIds(List<Long> orderIds) {
        transferOrderLineMapper.deleteByOrderIds(orderIds);
	}

}