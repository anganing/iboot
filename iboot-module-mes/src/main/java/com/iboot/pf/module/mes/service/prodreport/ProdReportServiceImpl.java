package com.iboot.pf.module.mes.service.prodreport;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-生产报工记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdReportServiceImpl implements ProdReportService {

    @Resource
    private ProdReportMapper prodReportMapper;

    @Override
    public Long createProdReport(ProdReportSaveReqVO createReqVO) {
        // 插入
        ProdReportDO prodReport = BeanUtils.toBean(createReqVO, ProdReportDO.class);
        prodReportMapper.insert(prodReport);

        // 返回
        return prodReport.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProdReportImportRespVO importProdReportList(List<ProdReportImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProdReportImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProdReportImportRespVO respVO = ProdReportImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProdReport(BeanUtils.toBean(importItem, ProdReportSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProdReport(ProdReportSaveReqVO updateReqVO) {
        // 校验存在
        validateProdReportExists(updateReqVO.getId());
        // 更新
        ProdReportDO updateObj = BeanUtils.toBean(updateReqVO, ProdReportDO.class);
        prodReportMapper.updateById(updateObj);
    }

    @Override
    public void deleteProdReport(Long id) {
        // 校验存在
        validateProdReportExists(id);
        // 删除
        prodReportMapper.deleteById(id);
    }

    @Override
        public void deleteProdReportListByIds(List<Long> ids) {
        // 删除
        prodReportMapper.deleteByIds(ids);
        }


    private void validateProdReportExists(Long id) {
        if (prodReportMapper.selectById(id) == null) {
            throw exception(PROD_REPORT_NOT_EXISTS);
        }
    }

    @Override
    public ProdReportRespVO getProdReport(Long id) {
        return prodReportMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProdReportRespVO> getProdReportPage(ProdReportPageReqVO pageReqVO) {
        return prodReportMapper.selectPage(pageReqVO);
    }

}