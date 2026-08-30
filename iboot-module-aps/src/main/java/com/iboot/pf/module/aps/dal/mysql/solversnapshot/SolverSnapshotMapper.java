package com.iboot.pf.module.aps.dal.mysql.solversnapshot;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.SolverSnapshotDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.*;

/**
 * 求解快照 Mapper
 *
 * @author iboot
 */
@Mapper
public interface SolverSnapshotMapper extends BaseMapperX<SolverSnapshotDO> {

    default PageResult<SolverSnapshotRespVO> selectPage(SolverSnapshotPageReqVO reqVO) {
        IPage<SolverSnapshotRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    SolverSnapshotRespVO selectJoinById(@Param("id") Long id);

    IPage<SolverSnapshotRespVO> selectJoinPage(IPage<SolverSnapshotRespVO> page, @Param("req") SolverSnapshotPageReqVO reqVO);

}