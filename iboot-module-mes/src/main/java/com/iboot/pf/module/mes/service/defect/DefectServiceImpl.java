package com.iboot.pf.module.mes.service.defect;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.defect.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-缺陷记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DefectServiceImpl implements DefectService {

    @Resource
    private DefectMapper defectMapper;

    @Override
    public Long createDefect(DefectSaveReqVO createReqVO) {
        // 插入
        DefectDO defect = BeanUtils.toBean(createReqVO, DefectDO.class);
        defectMapper.insert(defect);

        // 返回
        return defect.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public DefectImportRespVO importDefectList(List<DefectImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return DefectImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        DefectImportRespVO respVO = DefectImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createDefect(BeanUtils.toBean(importItem, DefectSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateDefect(DefectSaveReqVO updateReqVO) {
        // 校验存在
        validateDefectExists(updateReqVO.getId());
        // 更新
        DefectDO updateObj = BeanUtils.toBean(updateReqVO, DefectDO.class);
        defectMapper.updateById(updateObj);
    }

    @Override
    public void deleteDefect(Long id) {
        // 校验存在
        validateDefectExists(id);
        // 删除
        defectMapper.deleteById(id);
    }

    @Override
        public void deleteDefectListByIds(List<Long> ids) {
        // 删除
        defectMapper.deleteByIds(ids);
        }


    private void validateDefectExists(Long id) {
        if (defectMapper.selectById(id) == null) {
            throw exception(DEFECT_NOT_EXISTS);
        }
    }

    @Override
    public DefectRespVO getDefect(Long id) {
        return defectMapper.selectJoinById(id);
    }

    @Override
    public PageResult<DefectRespVO> getDefectPage(DefectPageReqVO pageReqVO) {
        return defectMapper.selectPage(pageReqVO);
    }

}