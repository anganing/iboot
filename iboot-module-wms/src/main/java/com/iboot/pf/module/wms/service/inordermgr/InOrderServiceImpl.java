package com.iboot.pf.module.wms.service.inordermgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.inordermgr.InOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.inordermgr.InOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.inorderline.InOrderLineMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-入库单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class InOrderServiceImpl implements InOrderService {

    @Resource
    private InOrderMapper inOrderMapper;
    @Resource
    private InOrderLineMapper inOrderLineMapper;

    @Override
    public Long createInOrder(InOrderSaveReqVO createReqVO) {
        // 插入
        InOrderDO inOrder = BeanUtils.toBean(createReqVO, InOrderDO.class);
        inOrderMapper.insert(inOrder);

        // 返回
        return inOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public InOrderImportRespVO importInOrderList(List<InOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return InOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        InOrderImportRespVO respVO = InOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createInOrder(BeanUtils.toBean(importItem, InOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateInOrder(InOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateInOrderExists(updateReqVO.getId());
        // 更新
        InOrderDO updateObj = BeanUtils.toBean(updateReqVO, InOrderDO.class);
        inOrderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInOrder(Long id) {
        // 校验存在
        validateInOrderExists(id);
        // 删除
        inOrderMapper.deleteById(id);

        // 删除子表
        deleteInOrderLineByOrderId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteInOrderListByIds(List<Long> ids) {
        // 删除
        inOrderMapper.deleteByIds(ids);
    
    // 删除子表
            deleteInOrderLineByOrderIds(ids);
    }


    private void validateInOrderExists(Long id) {
        if (inOrderMapper.selectById(id) == null) {
            throw exception(IN_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public InOrderRespVO getInOrder(Long id) {
        return inOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<InOrderRespVO> getInOrderPage(InOrderPageReqVO pageReqVO) {
        return inOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-入库单明细） ====================

    @Override
    public PageResult<InOrderLineDO> getInOrderLinePage(PageParam pageReqVO, Long orderId) {
        return inOrderLineMapper.selectPage(pageReqVO, orderId);
    }

    @Override
    public Long createInOrderLine(InOrderLineDO inOrderLine) {
        inOrderLine.clean(); // 清理掉创建、更新时间等相关属性值
        inOrderLineMapper.insert(inOrderLine);
        return inOrderLine.getId();
    }

    @Override
    public void updateInOrderLine(InOrderLineDO inOrderLine) {
        // 校验存在
        validateInOrderLineExists(inOrderLine.getId());
        // 更新
        inOrderLine.clean(); // 解决更新情况下：updateTime 不更新
        inOrderLineMapper.updateById(inOrderLine);
    }

    @Override
    public void deleteInOrderLine(Long id) {
        // 删除
        inOrderLineMapper.deleteById(id);
    }

	@Override
	public void deleteInOrderLineListByIds(List<Long> ids) {
        // 删除
        inOrderLineMapper.deleteByIds(ids);
	}

    @Override
    public InOrderLineDO getInOrderLine(Long id) {
        return inOrderLineMapper.selectById(id);
    }

    private void validateInOrderLineExists(Long id) {
        if (inOrderLineMapper.selectById(id) == null) {
            throw exception(IN_ORDER_LINE_NOT_EXISTS);
        }
    }

    private void deleteInOrderLineByOrderId(Long orderId) {
        inOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteInOrderLineByOrderIds(List<Long> orderIds) {
        inOrderLineMapper.deleteByOrderIds(orderIds);
	}

}