package com.iboot.pf.module.aps.service.projectioninv;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectioninv.ProjectionInvDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.projectioninv.ProjectionInvMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 库存投影 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectionInvServiceImpl implements ProjectionInvService {

    @Resource
    private ProjectionInvMapper projectionInvMapper;

    @Override
    public Long createProjectionInv(ProjectionInvSaveReqVO createReqVO) {
        // 插入
        ProjectionInvDO projectionInv = BeanUtils.toBean(createReqVO, ProjectionInvDO.class);
        projectionInvMapper.insert(projectionInv);

        // 返回
        return projectionInv.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProjectionInvImportRespVO importProjectionInvList(List<ProjectionInvImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProjectionInvImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProjectionInvImportRespVO respVO = ProjectionInvImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProjectionInv(BeanUtils.toBean(importItem, ProjectionInvSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProjectionInv(ProjectionInvSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectionInvExists(updateReqVO.getId());
        // 更新
        ProjectionInvDO updateObj = BeanUtils.toBean(updateReqVO, ProjectionInvDO.class);
        projectionInvMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectionInv(Long id) {
        // 校验存在
        validateProjectionInvExists(id);
        // 删除
        projectionInvMapper.deleteById(id);
    }

    @Override
        public void deleteProjectionInvListByIds(List<Long> ids) {
        // 删除
        projectionInvMapper.deleteByIds(ids);
        }


    private void validateProjectionInvExists(Long id) {
        if (projectionInvMapper.selectById(id) == null) {
            throw exception(PROJECTION_INV_NOT_EXISTS);
        }
    }

    @Override
    public ProjectionInvRespVO getProjectionInv(Long id) {
        return projectionInvMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProjectionInvRespVO> getProjectionInvPage(ProjectionInvPageReqVO pageReqVO) {
        return projectionInvMapper.selectPage(pageReqVO);
    }

}