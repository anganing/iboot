package com.iboot.pf.module.aps.dal.mysql.solversnapshot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotPageReqVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.ApsSolverSnapshotDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** APS-求解快照 Mapper。 */
@Mapper
public interface ApsSolverSnapshotMapper extends BaseMapperX<ApsSolverSnapshotDO> {

    default PageResult<SolverSnapshotRespVO> selectPage(SolverSnapshotPageReqVO reqVO) {
        IPage<SolverSnapshotRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    SolverSnapshotRespVO selectJoinById(@Param("id") Long id);

    /** 某版本某类型的最新一条快照；同一版本可能写过多条，最新那条才是最终结论。 */
    SolverSnapshotRespVO selectJoinLatestByVersionIdAndType(@Param("versionId") Long versionId,
                                                            @Param("snapshotType") String snapshotType);

    IPage<SolverSnapshotRespVO> selectJoinPage(IPage<SolverSnapshotRespVO> page,
                                               @Param("req") SolverSnapshotPageReqVO reqVO);

    /** 唯一索引 uk_aps_solver_snapshot_deleted 的应用层前置校验。 */
    default ApsSolverSnapshotDO selectByVersionIdAndTypeAndChecksum(Long versionId, String snapshotType,
                                                                    String checksum) {
        return selectOne(new LambdaQueryWrapper<ApsSolverSnapshotDO>()
                .eq(ApsSolverSnapshotDO::getVersionId, versionId)
                .eq(ApsSolverSnapshotDO::getSnapshotType, snapshotType)
                .eq(ApsSolverSnapshotDO::getChecksum, checksum));
    }

}
