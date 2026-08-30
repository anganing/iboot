package com.iboot.pf.module.aps.service.projectionmat;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionmat.ProjectionMatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.projectionmat.ProjectionMatMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * APS-MDM物料投影 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectionMatServiceImpl implements ProjectionMatService {

    @Resource
    private ProjectionMatMapper projectionMatMapper;

    @Override
    public Long createProjectionMat(ProjectionMatSaveReqVO createReqVO) {
        // 插入
        ProjectionMatDO projectionMat = BeanUtils.toBean(createReqVO, ProjectionMatDO.class);
        projectionMatMapper.insert(projectionMat);

        // 返回
        return projectionMat.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProjectionMatImportRespVO importProjectionMatList(List<ProjectionMatImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProjectionMatImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProjectionMatImportRespVO respVO = ProjectionMatImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProjectionMat(BeanUtils.toBean(importItem, ProjectionMatSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProjectionMat(ProjectionMatSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectionMatExists(updateReqVO.getId());
        // 更新
        ProjectionMatDO updateObj = BeanUtils.toBean(updateReqVO, ProjectionMatDO.class);
        projectionMatMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectionMat(Long id) {
        // 校验存在
        validateProjectionMatExists(id);
        // 删除
        projectionMatMapper.deleteById(id);
    }

    @Override
        public void deleteProjectionMatListByIds(List<Long> ids) {
        // 删除
        projectionMatMapper.deleteByIds(ids);
        }


    private void validateProjectionMatExists(Long id) {
        if (projectionMatMapper.selectById(id) == null) {
            throw exception(PROJECTION_MAT_NOT_EXISTS);
        }
    }

    @Override
    public ProjectionMatRespVO getProjectionMat(Long id) {
        return projectionMatMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProjectionMatRespVO> getProjectionMatPage(ProjectionMatPageReqVO pageReqVO) {
        return projectionMatMapper.selectPage(pageReqVO);
    }

}