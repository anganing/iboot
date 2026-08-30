package com.iboot.pf.module.mes.service.proddemand;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-生产需求 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdDemandServiceImpl implements ProdDemandService {

    @Resource
    private ProdDemandMapper prodDemandMapper;

    @Override
    public Long createProdDemand(ProdDemandSaveReqVO createReqVO) {
        // 插入
        ProdDemandDO prodDemand = BeanUtils.toBean(createReqVO, ProdDemandDO.class);
        prodDemandMapper.insert(prodDemand);

        // 返回
        return prodDemand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProdDemandImportRespVO importProdDemandList(List<ProdDemandImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProdDemandImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProdDemandImportRespVO respVO = ProdDemandImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProdDemand(BeanUtils.toBean(importItem, ProdDemandSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProdDemand(ProdDemandSaveReqVO updateReqVO) {
        // 校验存在
        validateProdDemandExists(updateReqVO.getId());
        // 更新
        ProdDemandDO updateObj = BeanUtils.toBean(updateReqVO, ProdDemandDO.class);
        prodDemandMapper.updateById(updateObj);
    }

    @Override
    public void deleteProdDemand(Long id) {
        // 校验存在
        validateProdDemandExists(id);
        // 删除
        prodDemandMapper.deleteById(id);
    }

    @Override
        public void deleteProdDemandListByIds(List<Long> ids) {
        // 删除
        prodDemandMapper.deleteByIds(ids);
        }


    private void validateProdDemandExists(Long id) {
        if (prodDemandMapper.selectById(id) == null) {
            throw exception(PROD_DEMAND_NOT_EXISTS);
        }
    }

    @Override
    public ProdDemandRespVO getProdDemand(Long id) {
        return prodDemandMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProdDemandRespVO> getProdDemandPage(ProdDemandPageReqVO pageReqVO) {
        return prodDemandMapper.selectPage(pageReqVO);
    }

}