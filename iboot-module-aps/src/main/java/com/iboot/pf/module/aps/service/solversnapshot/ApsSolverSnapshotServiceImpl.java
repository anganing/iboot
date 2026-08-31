package com.iboot.pf.module.aps.service.solversnapshot;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotPageReqVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;
import com.iboot.pf.module.aps.dal.mysql.solversnapshot.ApsSolverSnapshotMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVER_SNAPSHOT_NOT_EXISTS;

/**
 * APS-求解快照 Service 实现类。只读。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsSolverSnapshotServiceImpl implements ApsSolverSnapshotService {

    @Resource
    private ApsSolverSnapshotMapper solverSnapshotMapper;

    @Override
    public SolverSnapshotRespVO getSolverSnapshot(Long id) {
        SolverSnapshotRespVO respVO = id == null ? null : solverSnapshotMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(SOLVER_SNAPSHOT_NOT_EXISTS);
        }
        return respVO;
    }

    @Override
    public PageResult<SolverSnapshotRespVO> getSolverSnapshotPage(SolverSnapshotPageReqVO pageReqVO) {
        return solverSnapshotMapper.selectPage(pageReqVO);
    }

}
