package com.iboot.pf.module.aps.service.solversnapshot;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.SolverSnapshotDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 求解快照 Service 接口
 *
 * @author iboot
 */
public interface SolverSnapshotService {

    /**
     * 创建求解快照
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSolverSnapshot(@Valid SolverSnapshotSaveReqVO createReqVO);

    /**
     * 导入求解快照
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    SolverSnapshotImportRespVO importSolverSnapshotList(List<SolverSnapshotImportExcelVO> importList);

    /**
     * 更新求解快照
     *
     * @param updateReqVO 更新信息
     */
    void updateSolverSnapshot(@Valid SolverSnapshotSaveReqVO updateReqVO);

    /**
     * 删除求解快照
     *
     * @param id 编号
     */
    void deleteSolverSnapshot(Long id);

    /**
    * 批量删除求解快照
    *
    * @param ids 编号
    */
    void deleteSolverSnapshotListByIds(List<Long> ids);

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