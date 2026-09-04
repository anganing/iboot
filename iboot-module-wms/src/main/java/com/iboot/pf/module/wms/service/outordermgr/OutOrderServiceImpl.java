package com.iboot.pf.module.wms.service.outordermgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.outordermgr.OutOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.outorderline.OutOrderLineMapper;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.service.snapshot.WmsSnapshotService;

import java.math.BigDecimal;

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
    private WmsCodeService codeService;
    @Resource
    private OutOrderMapper outOrderMapper;
    @Resource
    private OutOrderLineMapper outOrderLineMapper;
    @Resource
    private WmsSnapshotService snapshotService;

    @Override
    public Long createOutOrder(OutOrderSaveReqVO createReqVO) {
        // 插入
        WmsOutOrderDO outOrder = BeanUtils.toBean(createReqVO, WmsOutOrderDO.class);
        outOrder.setId(null);
        outOrder.setOutNo(codeService.generate(WmsCodeRuleConstants.OUT_ORDER, outOrder.getOutNo()));
        validateOutOrderUnique(outOrder, null);
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
        WmsOutOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsOutOrderDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateOutOrderUnique(updateObj, updateReqVO.getId());
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
    public PageResult<OutOrderLineRespVO> getOutOrderLinePage(OutOrderLinePageReqVO pageReqVO) {
        return outOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createOutOrderLine(OutOrderLineSaveReqVO createReqVO) {
        validateOutOrderExists(createReqVO.getOrderId());
        validateLineNoUnique(createReqVO.getOrderId(), createReqVO.getLineNo(), null);
        WmsOutOrderLineDO outOrderLine = BeanUtils.toBean(createReqVO, WmsOutOrderLineDO.class);
        outOrderLine.setId(null);
        fillLineSnapshot(outOrderLine);
        // 分配、拣选、过账数量都由作业推进，创建时归零
        outOrderLine.setAllocatedQty(BigDecimal.ZERO);
        outOrderLine.setPickedQty(BigDecimal.ZERO);
        outOrderLine.setPostedQty(BigDecimal.ZERO);
        outOrderLineMapper.insert(outOrderLine);
        return outOrderLine.getId();
    }

    @Override
    public void updateOutOrderLine(OutOrderLineSaveReqVO updateReqVO) {
        // 校验存在
        validateOutOrderLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getOrderId(), updateReqVO.getLineNo(), updateReqVO.getId());
        WmsOutOrderLineDO outOrderLine = BeanUtils.toBean(updateReqVO, WmsOutOrderLineDO.class);
        fillLineSnapshot(outOrderLine);
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
    public OutOrderLineRespVO getOutOrderLine(Long id) {
        return outOrderLineMapper.selectJoinById(id);
    }

    private void validateOutOrderLineExists(Long id) {
        if (outOrderLineMapper.selectById(id) == null) {
            throw exception(OUT_ORDER_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (order_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long orderId, Integer lineNo, Long excludeId) {
        WmsOutOrderLineDO existed = outOrderLineMapper.selectByOrderIdAndLineNo(orderId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(OUT_ORDER_LINE_NO_DUPLICATE);
    }

    /** 明细上的物料、单位、来源库位、批次快照统一由快照服务补全。 */
    private void fillLineSnapshot(WmsOutOrderLineDO line) {
        MaterialRespDTO material = snapshotService.requireMaterial(line.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
        line.setUomCode(snapshotService.requireUomCode(line.getUomId()));
        line.setFromLocCode(snapshotService.locCodeOrNull(line.getFromLocId()));
        line.setLotNo(snapshotService.lotNoOrNull(line.getBatchId()));
        snapshotService.validateContainer(line.getContainerId());
    }

    private void deleteOutOrderLineByOrderId(Long orderId) {
        outOrderLineMapper.deleteByOrderId(orderId);
    }

	private void deleteOutOrderLineByOrderIds(List<Long> orderIds) {
        outOrderLineMapper.deleteByOrderIds(orderIds);
	}


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateOutOrderUnique(WmsOutOrderDO outOrder, Long excludeId) {
        WmsOutOrderDO existed = outOrderMapper.selectByOutNo(outOrder.getOutNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(OUT_ORDER_NO_DUPLICATE);
    }

}
