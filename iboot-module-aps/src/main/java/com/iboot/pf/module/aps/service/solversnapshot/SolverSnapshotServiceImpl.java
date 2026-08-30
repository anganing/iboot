package com.iboot.pf.module.aps.service.solversnapshot;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.SolverSnapshotDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.solversnapshot.SolverSnapshotMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 求解快照 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class SolverSnapshotServiceImpl implements SolverSnapshotService {

    @Resource
    private SolverSnapshotMapper solverSnapshotMapper;

    @Override
    public Long createSolverSnapshot(SolverSnapshotSaveReqVO createReqVO) {
        // 插入
        SolverSnapshotDO solverSnapshot = BeanUtils.toBean(createReqVO, SolverSnapshotDO.class);
        solverSnapshotMapper.insert(solverSnapshot);

        // 返回
        return solverSnapshot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public SolverSnapshotImportRespVO importSolverSnapshotList(List<SolverSnapshotImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return SolverSnapshotImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        SolverSnapshotImportRespVO respVO = SolverSnapshotImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createSolverSnapshot(BeanUtils.toBean(importItem, SolverSnapshotSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateSolverSnapshot(SolverSnapshotSaveReqVO updateReqVO) {
        // 校验存在
        validateSolverSnapshotExists(updateReqVO.getId());
        // 更新
        SolverSnapshotDO updateObj = BeanUtils.toBean(updateReqVO, SolverSnapshotDO.class);
        solverSnapshotMapper.updateById(updateObj);
    }

    @Override
    public void deleteSolverSnapshot(Long id) {
        // 校验存在
        validateSolverSnapshotExists(id);
        // 删除
        solverSnapshotMapper.deleteById(id);
    }

    @Override
        public void deleteSolverSnapshotListByIds(List<Long> ids) {
        // 删除
        solverSnapshotMapper.deleteByIds(ids);
        }


    private void validateSolverSnapshotExists(Long id) {
        if (solverSnapshotMapper.selectById(id) == null) {
            throw exception(SOLVER_SNAPSHOT_NOT_EXISTS);
        }
    }

    @Override
    public SolverSnapshotRespVO getSolverSnapshot(Long id) {
        return solverSnapshotMapper.selectJoinById(id);
    }

    @Override
    public PageResult<SolverSnapshotRespVO> getSolverSnapshotPage(SolverSnapshotPageReqVO pageReqVO) {
        return solverSnapshotMapper.selectPage(pageReqVO);
    }

}