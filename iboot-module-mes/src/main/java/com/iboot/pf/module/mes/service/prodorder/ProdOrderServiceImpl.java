package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-生产工单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdOrderServiceImpl implements ProdOrderService {

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Override
    public Long createProdOrder(ProdOrderSaveReqVO createReqVO) {
        // 插入
        ProdOrderDO prodOrder = BeanUtils.toBean(createReqVO, ProdOrderDO.class);
        prodOrderMapper.insert(prodOrder);

        // 返回
        return prodOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProdOrderImportRespVO importProdOrderList(List<ProdOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProdOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProdOrderImportRespVO respVO = ProdOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProdOrder(BeanUtils.toBean(importItem, ProdOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProdOrder(ProdOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateProdOrderExists(updateReqVO.getId());
        // 更新
        ProdOrderDO updateObj = BeanUtils.toBean(updateReqVO, ProdOrderDO.class);
        prodOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteProdOrder(Long id) {
        // 校验存在
        validateProdOrderExists(id);
        // 删除
        prodOrderMapper.deleteById(id);
    }

    @Override
        public void deleteProdOrderListByIds(List<Long> ids) {
        // 删除
        prodOrderMapper.deleteByIds(ids);
        }


    private void validateProdOrderExists(Long id) {
        if (prodOrderMapper.selectById(id) == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public ProdOrderRespVO getProdOrder(Long id) {
        return prodOrderMapper.selectJoinById(id);
    }

    @Override
    public List<ProdOrderDO> getProdOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return prodOrderMapper.selectBatchIds(ids);
    }

    @Override
    public List<ProdOrderDO> getProdOrderListByPlantId(Long plantId) {
        return prodOrderMapper.selectList(new LambdaQueryWrapper<ProdOrderDO>()
                .eq(plantId != null, ProdOrderDO::getPlantId, plantId));
    }

    @Override
    public PageResult<ProdOrderRespVO> getProdOrderPage(ProdOrderPageReqVO pageReqVO) {
        return prodOrderMapper.selectPage(pageReqVO);
    }

}
