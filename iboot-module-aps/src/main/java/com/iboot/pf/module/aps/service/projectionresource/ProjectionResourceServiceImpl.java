package com.iboot.pf.module.aps.service.projectionresource;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionresource.ProjectionResourceDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.projectionresource.ProjectionResourceMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 资源投影 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectionResourceServiceImpl implements ProjectionResourceService {

    @Resource
    private ProjectionResourceMapper projectionResourceMapper;

    @Override
    public Long createProjectionResource(ProjectionResourceSaveReqVO createReqVO) {
        // 插入
        ProjectionResourceDO projectionResource = BeanUtils.toBean(createReqVO, ProjectionResourceDO.class);
        projectionResourceMapper.insert(projectionResource);

        // 返回
        return projectionResource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProjectionResourceImportRespVO importProjectionResourceList(List<ProjectionResourceImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProjectionResourceImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProjectionResourceImportRespVO respVO = ProjectionResourceImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProjectionResource(BeanUtils.toBean(importItem, ProjectionResourceSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProjectionResource(ProjectionResourceSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectionResourceExists(updateReqVO.getId());
        // 更新
        ProjectionResourceDO updateObj = BeanUtils.toBean(updateReqVO, ProjectionResourceDO.class);
        projectionResourceMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectionResource(Long id) {
        // 校验存在
        validateProjectionResourceExists(id);
        // 删除
        projectionResourceMapper.deleteById(id);
    }

    @Override
        public void deleteProjectionResourceListByIds(List<Long> ids) {
        // 删除
        projectionResourceMapper.deleteByIds(ids);
        }


    private void validateProjectionResourceExists(Long id) {
        if (projectionResourceMapper.selectById(id) == null) {
            throw exception(PROJECTION_RESOURCE_NOT_EXISTS);
        }
    }

    @Override
    public ProjectionResourceRespVO getProjectionResource(Long id) {
        return projectionResourceMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProjectionResourceRespVO> getProjectionResourcePage(ProjectionResourcePageReqVO pageReqVO) {
        return projectionResourceMapper.selectPage(pageReqVO);
    }

}