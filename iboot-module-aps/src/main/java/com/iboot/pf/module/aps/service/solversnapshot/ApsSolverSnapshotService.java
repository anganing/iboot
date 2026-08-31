package com.iboot.pf.module.aps.service.solversnapshot;

import com.iboot.pf.framework.common.pojo.PageResult;
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

}
