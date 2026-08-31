package com.iboot.pf.module.wms.dal.mysql.containertype;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.containertype.ContainerTypeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;

/**
 * WMS-容器类型 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ContainerTypeMapper extends BaseMapperX<ContainerTypeDO> {

    default PageResult<ContainerTypeRespVO> selectPage(ContainerTypePageReqVO reqVO) {
        IPage<ContainerTypeRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ContainerTypeRespVO selectJoinById(@Param("id") Long id);

    IPage<ContainerTypeRespVO> selectJoinPage(IPage<ContainerTypeRespVO> page, @Param("req") ContainerTypePageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default ContainerTypeDO selectByCode(String code) {
        return selectOne(ContainerTypeDO::getCode, code);
    }

}