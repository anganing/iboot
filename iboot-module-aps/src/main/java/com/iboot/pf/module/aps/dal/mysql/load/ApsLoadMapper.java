package com.iboot.pf.module.aps.dal.mysql.load;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.load.vo.*;

@Mapper
public interface ApsLoadMapper extends BaseMapperX<ApsLoadDO> {

    default PageResult<LoadRespVO> selectPage(LoadPageReqVO reqVO) {
        IPage<LoadRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    LoadRespVO selectJoinById(@Param("id") Long id);

    IPage<LoadRespVO> selectJoinPage(IPage<LoadRespVO> page, @Param("req") LoadPageReqVO reqVO);

    List<LoadRespVO> selectListByVersionId(@Param("versionId") Long versionId);

    @Delete("DELETE FROM aps_load WHERE version_id = #{versionId}")
    int deletePhysicalByVersionId(Long versionId);
}
