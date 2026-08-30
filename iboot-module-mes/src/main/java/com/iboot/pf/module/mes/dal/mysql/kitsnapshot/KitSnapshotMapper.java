package com.iboot.pf.module.mes.dal.mysql.kitsnapshot;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.*;

/**
 * MES-齐套检查快照 Mapper
 *
 * @author iboot
 */
@Mapper
public interface KitSnapshotMapper extends BaseMapperX<KitSnapshotDO> {

    default PageResult<KitSnapshotRespVO> selectPage(KitSnapshotPageReqVO reqVO) {
        IPage<KitSnapshotRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    KitSnapshotRespVO selectJoinById(@Param("id") Long id);

    IPage<KitSnapshotRespVO> selectJoinPage(IPage<KitSnapshotRespVO> page, @Param("req") KitSnapshotPageReqVO reqVO);

}