package com.iboot.pf.module.wms.dal.mysql.container;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerTypeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 容器类型 Mapper。 */
@Mapper
public interface WmsContainerTypeMapper extends BaseMapperX<WmsContainerTypeDO> {

    default PageResult<ContainerTypeRespVO> selectPage(ContainerTypePageReqVO reqVO) {
        IPage<ContainerTypeRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ContainerTypeRespVO selectJoinById(@Param("id") Long id);

    IPage<ContainerTypeRespVO> selectJoinPage(IPage<ContainerTypeRespVO> page, @Param("req") ContainerTypePageReqVO reqVO);

    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsContainerTypeDO selectByCode(String code) {
        return selectOne(WmsContainerTypeDO::getCode, code);
    }
}
