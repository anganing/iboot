package com.iboot.pf.module.wms.service.countorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.countorder.CountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.countorderline.CountOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.countorder.CountOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.countorderline.CountOrderLineMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-盘点单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class CountOrderServiceImpl implements CountOrderService {

    @Resource
    private CountOrderMapper countOrderMapper;
    @Resource
    private CountOrderLineMapper countOrderLineMapper;

    @Override
    public Long createCountOrder(CountOrderSaveReqVO createReqVO) {
        // 插入
        CountOrderDO countOrder = BeanUtils.toBean(createReqVO, CountOrderDO.class);
        countOrderMapper.insert(countOrder);

        // 返回
        return countOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public CountOrderImportRespVO importCountOrderList(List<CountOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return CountOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        CountOrderImportRespVO respVO = CountOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createCountOrder(BeanUtils.toBean(importItem, CountOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateCountOrder(CountOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateCountOrderExists(updateReqVO.getId());
        // 更新
        CountOrderDO updateObj = BeanUtils.toBean(updateReqVO, CountOrderDO.class);
        countOrderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCountOrder(Long id) {
        // 校验存在
        validateCountOrderExists(id);
        // 删除
        countOrderMapper.deleteById(id);

        // 删除子表
        deleteCountOrderLineByOrderId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteCountOrderListByIds(List<Long> ids) {
        // 删除
        countOrderMapper.deleteByIds(ids);
    
    // 删除子表
            deleteCountOrderLineByOrderIds(ids);
    }


    private void validateCountOrderExists(Long id) {
        if (countOrderMapper.selectById(id) == null) {
            throw exception(COUNT_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public CountOrderRespVO getCountOrder(Long id) {
        return countOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<CountOrderRespVO> getCountOrderPage(CountOrderPageReqVO pageReqVO) {
        return countOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-盘点明细） ====================

    @Override
    public PageResult<CountOrderLineDO> getCountOrderLinePage(PageParam pageReqVO, Long orderId) {
        return countOrderLineMapper.selectPage(pageReqVO, orderId);
    }

    @Override
    public Long createCountOrderLine(CountOrderLineDO countOrderLine) {
        countOrderLine.clean(); // 清理掉创建、更新时间等相关属性值
        countOrderLineMapper.insert(countOrderLine);
        return countOrderLine.getId();
    }

    @Override
    public void updateCountOrderLine(CountOrderLineDO countOrderLine) {
        // 校验存在
        validateCountOrderLineExists(countOrderLine.getId());
        // 更新
        countOrderLine.clean(); // 解决更新情况下：updateTime 不更新
        countOrderLineMapper.updateById(countOrderLine);
    }

    @Override
    public void deleteCountOrderLine(Long id) {
        // 删除
        countOrderLineMapper.deleteById(id);
    }

	@Override
	public void deleteCountOrderLineListByIds(List<Long> ids) {
        // 删除
        countOrderLineMapper.deleteByIds(ids);
	}

    @Override
    public CountOrderLineDO getCountOrderLine(Long id) {
        return countOrderLineMapper.selectById(id);
    }

    private void validateCountOrderLineExists(Long id) {
        if (countOrderLineMapper.selectById(id) == null) {
            throw exception(COUNT_ORDER_LINE_NOT_EXISTS);
        }
    }

    private void deleteCountOrderLineByOrderId(Long orderId) {
        countOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteCountOrderLineByOrderIds(List<Long> orderIds) {
        countOrderLineMapper.deleteByOrderIds(orderIds);
	}

}