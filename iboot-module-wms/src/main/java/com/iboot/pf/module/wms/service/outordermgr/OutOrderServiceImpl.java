package com.iboot.pf.module.wms.service.outordermgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.outordermgr.OutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorderline.OutOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.outordermgr.OutOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.outorderline.OutOrderLineMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-出库单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OutOrderServiceImpl implements OutOrderService {

    @Resource
    private OutOrderMapper outOrderMapper;
    @Resource
    private OutOrderLineMapper outOrderLineMapper;

    @Override
    public Long createOutOrder(OutOrderSaveReqVO createReqVO) {
        // 插入
        OutOrderDO outOrder = BeanUtils.toBean(createReqVO, OutOrderDO.class);
        outOrderMapper.insert(outOrder);

        // 返回
        return outOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public OutOrderImportRespVO importOutOrderList(List<OutOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return OutOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        OutOrderImportRespVO respVO = OutOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createOutOrder(BeanUtils.toBean(importItem, OutOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateOutOrder(OutOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateOutOrderExists(updateReqVO.getId());
        // 更新
        OutOrderDO updateObj = BeanUtils.toBean(updateReqVO, OutOrderDO.class);
        outOrderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutOrder(Long id) {
        // 校验存在
        validateOutOrderExists(id);
        // 删除
        outOrderMapper.deleteById(id);

        // 删除子表
        deleteOutOrderLineByOrderId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteOutOrderListByIds(List<Long> ids) {
        // 删除
        outOrderMapper.deleteByIds(ids);
    
    // 删除子表
            deleteOutOrderLineByOrderIds(ids);
    }


    private void validateOutOrderExists(Long id) {
        if (outOrderMapper.selectById(id) == null) {
            throw exception(OUT_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public OutOrderRespVO getOutOrder(Long id) {
        return outOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<OutOrderRespVO> getOutOrderPage(OutOrderPageReqVO pageReqVO) {
        return outOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-出库单明细） ====================

    @Override
    public PageResult<OutOrderLineDO> getOutOrderLinePage(PageParam pageReqVO, Long orderId) {
        return outOrderLineMapper.selectPage(pageReqVO, orderId);
    }

    @Override
    public Long createOutOrderLine(OutOrderLineDO outOrderLine) {
        outOrderLine.clean(); // 清理掉创建、更新时间等相关属性值
        outOrderLineMapper.insert(outOrderLine);
        return outOrderLine.getId();
    }

    @Override
    public void updateOutOrderLine(OutOrderLineDO outOrderLine) {
        // 校验存在
        validateOutOrderLineExists(outOrderLine.getId());
        // 更新
        outOrderLine.clean(); // 解决更新情况下：updateTime 不更新
        outOrderLineMapper.updateById(outOrderLine);
    }

    @Override
    public void deleteOutOrderLine(Long id) {
        // 删除
        outOrderLineMapper.deleteById(id);
    }

	@Override
	public void deleteOutOrderLineListByIds(List<Long> ids) {
        // 删除
        outOrderLineMapper.deleteByIds(ids);
	}

    @Override
    public OutOrderLineDO getOutOrderLine(Long id) {
        return outOrderLineMapper.selectById(id);
    }

    private void validateOutOrderLineExists(Long id) {
        if (outOrderLineMapper.selectById(id) == null) {
            throw exception(OUT_ORDER_LINE_NOT_EXISTS);
        }
    }

    private void deleteOutOrderLineByOrderId(Long orderId) {
        outOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteOutOrderLineByOrderIds(List<Long> orderIds) {
        outOrderLineMapper.deleteByOrderIds(orderIds);
	}

}