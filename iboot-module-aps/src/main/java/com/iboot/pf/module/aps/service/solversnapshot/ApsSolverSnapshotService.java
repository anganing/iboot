package com.iboot.pf.module.aps.service.solversnapshot;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotConstraintMatchRespVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotPageReqVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;

/**
 * APS-求解快照 Service 接口。
 *
 * <p>快照是求解过程的证据链，只提供查询与导出。写入由求解流程负责，人工改写会破坏可追溯性。
 *
 * @author iboot
 */
public interface ApsSolverSnapshotService {

    /**
     * 获得求解快照
     *
     * @param id 编号
     * @return 求解快照
     */
    SolverSnapshotRespVO getSolverSnapshot(Long id);

    /**
     * 获得求解快照分页
     *
     * @param pageReqVO 分页查询
     * @return 求解快照分页
     */
    PageResult<SolverSnapshotRespVO> getSolverSnapshotPage(SolverSnapshotPageReqVO pageReqVO);

    /**
     * 获得某个排程版本的约束命中分析
     *
     * <p>取该版本最新一条 {@code CONSTRAINT_MATCH} 快照。同一版本可能有多条（求解与失败兜底
     * 各写一次），最新那条才是最终结论。版本还没有命中快照时返回 {@code null}，
     * 由调用方决定提示语——求解中、旧版本都属于正常情况，不该报错。
     *
     * @param versionId 排程版本编号
     * @return 约束命中分析，没有时返回 null
     */
    SolverSnapshotConstraintMatchRespVO getConstraintMatchByVersionId(Long versionId);

}
