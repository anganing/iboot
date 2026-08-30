package com.iboot.pf.module.wms.dal.mysql.containermgr;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.containermgr.ContainerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;

/**
 * WMS-容器主档 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ContainerMapper extends BaseMapperX<ContainerDO> {

    default PageResult<ContainerRespVO> selectPage(ContainerPageReqVO reqVO) {
        IPage<ContainerRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ContainerRespVO selectJoinById(@Param("id") Long id);

    IPage<ContainerRespVO> selectJoinPage(IPage<ContainerRespVO> page, @Param("req") ContainerPageReqVO reqVO);

}