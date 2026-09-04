package com.iboot.pf.module.wms.service.inordermgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.inordermgr.InOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.inorderline.InOrderLineMapper;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.service.snapshot.WmsSnapshotService;

import java.math.BigDecimal;

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
    private WmsCodeService codeService;
    @Resource
    private InOrderMapper inOrderMapper;
    @Resource
    private InOrderLineMapper inOrderLineMapper;
    @Resource
    private WmsSnapshotService snapshotService;

    @Override
    public Long createInOrder(InOrderSaveReqVO createReqVO) {
        // 插入
        WmsInOrderDO inOrder = BeanUtils.toBean(createReqVO, WmsInOrderDO.class);
        inOrder.setId(null);
        inOrder.setInNo(codeService.generate(WmsCodeRuleConstants.IN_ORDER, inOrder.getInNo()));
        validateInOrderUnique(inOrder, null);
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
        WmsInOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsInOrderDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateInOrderUnique(updateObj, updateReqVO.getId());
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
    public PageResult<InOrderLineRespVO> getInOrderLinePage(InOrderLinePageReqVO pageReqVO) {
        return inOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createInOrderLine(InOrderLineSaveReqVO createReqVO) {
        validateInOrderExists(createReqVO.getOrderId());
        validateLineNoUnique(createReqVO.getOrderId(), createReqVO.getLineNo(), null);
        WmsInOrderLineDO inOrderLine = BeanUtils.toBean(createReqVO, WmsInOrderLineDO.class);
        inOrderLine.setId(null);
        fillLineSnapshot(inOrderLine);
        // 数量进度由过账推进，创建时归零
        inOrderLine.setCompletedQty(BigDecimal.ZERO);
        inOrderLine.setPostedQty(BigDecimal.ZERO);
        inOrderLineMapper.insert(inOrderLine);
        return inOrderLine.getId();
    }

    @Override
    public void updateInOrderLine(InOrderLineSaveReqVO updateReqVO) {
        // 校验存在
        validateInOrderLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getOrderId(), updateReqVO.getLineNo(), updateReqVO.getId());
        WmsInOrderLineDO inOrderLine = BeanUtils.toBean(updateReqVO, WmsInOrderLineDO.class);
        fillLineSnapshot(inOrderLine);
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
    public InOrderLineRespVO getInOrderLine(Long id) {
        return inOrderLineMapper.selectJoinById(id);
    }

    private void validateInOrderLineExists(Long id) {
        if (inOrderLineMapper.selectById(id) == null) {
            throw exception(IN_ORDER_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (order_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long orderId, Integer lineNo, Long excludeId) {
        WmsInOrderLineDO existed = inOrderLineMapper.selectByOrderIdAndLineNo(orderId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IN_ORDER_LINE_NO_DUPLICATE);
    }

    /** 明细上的物料、单位、库位、批次快照统一由快照服务补全。 */
    private void fillLineSnapshot(WmsInOrderLineDO line) {
        MaterialRespDTO material = snapshotService.requireMaterial(line.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
        line.setUomCode(snapshotService.requireUomCode(line.getUomId()));
        line.setLocCode(snapshotService.locCodeOrNull(line.getLocId()));
        line.setLotNo(snapshotService.lotNoOrNull(line.getBatchId()));
        snapshotService.validateContainer(line.getContainerId());
    }

    private void deleteInOrderLineByOrderId(Long orderId) {
        inOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteInOrderLineByOrderIds(List<Long> orderIds) {
        inOrderLineMapper.deleteByOrderIds(orderIds);
	}


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateInOrderUnique(WmsInOrderDO inOrder, Long excludeId) {
        WmsInOrderDO existed = inOrderMapper.selectByInNo(inOrder.getInNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IN_ORDER_NO_DUPLICATE);
    }

}
