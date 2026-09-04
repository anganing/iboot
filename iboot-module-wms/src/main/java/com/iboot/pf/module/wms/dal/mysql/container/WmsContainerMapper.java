package com.iboot.pf.module.wms.dal.mysql.container;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 容器主档 Mapper。 */
@Mapper
public interface WmsContainerMapper extends BaseMapperX<WmsContainerDO> {

    default PageResult<ContainerRespVO> selectPage(ContainerPageReqVO reqVO) {
        IPage<ContainerRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ContainerRespVO selectJoinById(@Param("id") Long id);

    IPage<ContainerRespVO> selectJoinPage(IPage<ContainerRespVO> page, @Param("req") ContainerPageReqVO reqVO);

    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsContainerDO selectByContainerNo(String containerNo) {
        return selectOne(WmsContainerDO::getContainerNo, containerNo);
    }
}
