package com.iboot.pf.module.mes.service.materialreq;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-领退补料申请 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class MaterialReqServiceImpl implements MaterialReqService {

    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @Override
    public Long createMaterialReq(MaterialReqSaveReqVO createReqVO) {
        // 插入
        MaterialReqDO materialReq = BeanUtils.toBean(createReqVO, MaterialReqDO.class);
        materialReqMapper.insert(materialReq);

        // 返回
        return materialReq.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public MaterialReqImportRespVO importMaterialReqList(List<MaterialReqImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return MaterialReqImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        MaterialReqImportRespVO respVO = MaterialReqImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createMaterialReq(BeanUtils.toBean(importItem, MaterialReqSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateMaterialReq(MaterialReqSaveReqVO updateReqVO) {
        // 校验存在
        validateMaterialReqExists(updateReqVO.getId());
        // 更新
        MaterialReqDO updateObj = BeanUtils.toBean(updateReqVO, MaterialReqDO.class);
        materialReqMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterialReq(Long id) {
        // 校验存在
        validateMaterialReqExists(id);
        // 删除
        materialReqMapper.deleteById(id);

        // 删除子表
        deleteMaterialReqLineByReqId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteMaterialReqListByIds(List<Long> ids) {
        // 删除
        materialReqMapper.deleteByIds(ids);
    
    // 删除子表
            deleteMaterialReqLineByReqIds(ids);
    }


    private void validateMaterialReqExists(Long id) {
        if (materialReqMapper.selectById(id) == null) {
            throw exception(MATERIAL_REQ_NOT_EXISTS);
        }
    }

    @Override
    public MaterialReqRespVO getMaterialReq(Long id) {
        return materialReqMapper.selectJoinById(id);
    }

    @Override
    public PageResult<MaterialReqRespVO> getMaterialReqPage(MaterialReqPageReqVO pageReqVO) {
        return materialReqMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（MES-领退补料申请明细） ====================

    @Override
    public PageResult<MaterialReqLineDO> getMaterialReqLinePage(PageParam pageReqVO, Long reqId) {
        return materialReqLineMapper.selectPage(pageReqVO, reqId);
    }

    @Override
    public Long createMaterialReqLine(MaterialReqLineDO materialReqLine) {
        materialReqLine.clean(); // 清理掉创建、更新时间等相关属性值
        materialReqLineMapper.insert(materialReqLine);
        return materialReqLine.getId();
    }

    @Override
    public void updateMaterialReqLine(MaterialReqLineDO materialReqLine) {
        // 校验存在
        validateMaterialReqLineExists(materialReqLine.getId());
        // 更新
        materialReqLine.clean(); // 解决更新情况下：updateTime 不更新
        materialReqLineMapper.updateById(materialReqLine);
    }

    @Override
    public void deleteMaterialReqLine(Long id) {
        // 删除
        materialReqLineMapper.deleteById(id);
    }

	@Override
	public void deleteMaterialReqLineListByIds(List<Long> ids) {
        // 删除
        materialReqLineMapper.deleteByIds(ids);
	}

    @Override
    public MaterialReqLineDO getMaterialReqLine(Long id) {
        return materialReqLineMapper.selectById(id);
    }

    private void validateMaterialReqLineExists(Long id) {
        if (materialReqLineMapper.selectById(id) == null) {
            throw exception(MATERIAL_REQ_LINE_NOT_EXISTS);
        }
    }

    private void deleteMaterialReqLineByReqId(Long reqId) {
        materialReqLineMapper.deleteByReqId(reqId);
    }

	private void deleteMaterialReqLineByReqIds(List<Long> reqIds) {
        materialReqLineMapper.deleteByReqIds(reqIds);
	}

}