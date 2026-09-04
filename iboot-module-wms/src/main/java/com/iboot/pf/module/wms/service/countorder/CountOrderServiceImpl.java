package com.iboot.pf.module.wms.service.countorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;

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
    private WmsCodeService codeService;
    @Resource
    private WmsCountOrderMapper countOrderMapper;
    @Resource
    private WmsCountOrderLineMapper countOrderLineMapper;
    @Resource
    private WmsInvBalanceMapper invBalanceMapper;

    @Override
    public Long createCountOrder(CountOrderSaveReqVO createReqVO) {
        // 插入
        WmsCountOrderDO countOrder = BeanUtils.toBean(createReqVO, WmsCountOrderDO.class);
        countOrder.setId(null);
        countOrder.setCountNo(codeService.generate(WmsCodeRuleConstants.COUNT_ORDER, countOrder.getCountNo()));
        validateCountOrderUnique(countOrder, null);
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
        WmsCountOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsCountOrderDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateCountOrderUnique(updateObj, updateReqVO.getId());
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
    public PageResult<CountOrderLineRespVO> getCountOrderLinePage(CountOrderLinePageReqVO pageReqVO) {
        return countOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createCountOrderLine(CountOrderLineSaveReqVO createReqVO) {
        validateCountOrderExists(createReqVO.getOrderId());
        validateLineNoUnique(createReqVO.getOrderId(), createReqVO.getLineNo(), null);
        validateBalanceUnique(createReqVO.getOrderId(), createReqVO.getBalanceId(), null);
        WmsCountOrderLineDO countOrderLine = BeanUtils.toBean(createReqVO, WmsCountOrderLineDO.class);
        countOrderLine.setId(null);
        // 盘点明细的物料、批次、库位与账面数量都来自余额快照，不由用户填写
        fillFromBalance(countOrderLine);
        countOrderLineMapper.insert(countOrderLine);
        return countOrderLine.getId();
    }

    @Override
    public void updateCountOrderLine(CountOrderLineSaveReqVO updateReqVO) {
        // 校验存在
        validateCountOrderLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getOrderId(), updateReqVO.getLineNo(), updateReqVO.getId());
        validateBalanceUnique(updateReqVO.getOrderId(), updateReqVO.getBalanceId(), updateReqVO.getId());
        WmsCountOrderLineDO countOrderLine = BeanUtils.toBean(updateReqVO, WmsCountOrderLineDO.class);
        fillFromBalance(countOrderLine);
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
    public CountOrderLineRespVO getCountOrderLine(Long id) {
        return countOrderLineMapper.selectJoinById(id);
    }

    private void validateCountOrderLineExists(Long id) {
        if (countOrderLineMapper.selectById(id) == null) {
            throw exception(COUNT_ORDER_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (order_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long orderId, Integer lineNo, Long excludeId) {
        WmsCountOrderLineDO existed = countOrderLineMapper.selectByOrderIdAndLineNo(orderId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(COUNT_ORDER_LINE_NO_DUPLICATE);
    }

    /** 同一盘点单里一个库存余额只能盘一次：唯一索引是 (order_id, balance_id, deleted)。 */
    private void validateBalanceUnique(Long orderId, Long balanceId, Long excludeId) {
        WmsCountOrderLineDO existed = countOrderLineMapper.selectByOrderIdAndBalanceId(orderId, balanceId);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(COUNT_ORDER_LINE_BALANCE_DUPLICATE);
    }

    /**
     * 从库存余额快照回填盘点维度与账面数量，并按实盘数量算差异。
     * 账面数量必须来自余额而不是前端，否则差异算出来没有意义。
     */
    private void fillFromBalance(WmsCountOrderLineDO line) {
        WmsInvBalanceDO balance = invBalanceMapper.selectById(line.getBalanceId());
        if (balance == null) {
            throw exception(INV_BALANCE_NOT_EXISTS);
        }
        line.setMatId(balance.getMatId());
        line.setMatCode(balance.getMatCode());
        line.setMatName(balance.getMatName());
        line.setBatchId(balance.getBatchId());
        line.setLotNo(balance.getLotNo());
        line.setLocId(balance.getLocId());
        line.setLocCode(balance.getLocCode());
        line.setContainerId(balance.getContainerId());
        line.setProjectId(balance.getProjectId());
        line.setProjectTaskId(balance.getProjectTaskId());
        line.setBookQty(balance.getQtyOnhand());
        if (line.getActualQty() != null) {
            line.setDiffQty(line.getActualQty().subtract(balance.getQtyOnhand()));
        }
    }

    private void deleteCountOrderLineByOrderId(Long orderId) {
        countOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteCountOrderLineByOrderIds(List<Long> orderIds) {
        countOrderLineMapper.deleteByOrderIds(orderIds);
	}


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateCountOrderUnique(WmsCountOrderDO countOrder, Long excludeId) {
        WmsCountOrderDO existed = countOrderMapper.selectByCountNo(countOrder.getCountNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(COUNT_ORDER_NO_DUPLICATE);
    }

}
