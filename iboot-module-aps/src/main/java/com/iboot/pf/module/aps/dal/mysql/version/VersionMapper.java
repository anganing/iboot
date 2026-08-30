package com.iboot.pf.module.aps.dal.mysql.version;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.version.VersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.version.vo.*;

/**
 * APS-排程版本 Mapper
 *
 * @author iboot
 */
@Mapper
public interface VersionMapper extends BaseMapperX<VersionDO> {

    default PageResult<VersionRespVO> selectPage(VersionPageReqVO reqVO) {
        IPage<VersionRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    VersionRespVO selectJoinById(@Param("id") Long id);

    IPage<VersionRespVO> selectJoinPage(IPage<VersionRespVO> page, @Param("req") VersionPageReqVO reqVO);

}